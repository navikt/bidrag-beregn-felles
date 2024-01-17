package no.nav.bidrag.beregn.forskudd.service.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.service.BeregnForskuddService
import no.nav.bidrag.beregn.forskudd.service.TestUtil
import no.nav.bidrag.beregn.forskudd.service.testdata.SjablonApiStub
import no.nav.bidrag.domene.enums.beregning.ResultatkodeForskudd
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
internal class BeregnForskuddApiTest {
    private lateinit var url: String
    private lateinit var filnavn: String

    private var forventetForskuddBelop = 0
    private lateinit var forventetForskuddResultatkode: ResultatkodeForskudd
    private lateinit var forventetForskuddRegel: String

    @Mock
    private lateinit var forskuddCoreMock: ForskuddCore
    private lateinit var beregnForskuddService: BeregnForskuddService

    @Captor
    private lateinit var grunnlagTilCoreCaptor: ArgumentCaptor<BeregnForskuddGrunnlagCore>

    @BeforeEach
    fun initMock() {
        SjablonApiStub().settOppSjablonStub()
        beregnForskuddService = BeregnForskuddService()
    }
    /*
      Beskrivelse av regler

      REGEL 1
      Betingelse 1	Søknadsbarn alder er høyere enn eller lik 18 år
      Resultatkode	AVSLAG

      REGEL 2
      Betingelse 1	Søknadsbarn alder er høyere enn eller lik 11 år
      Betingelse 2	Søknadsbarn bostedsstatus er BOR_IKKE_MED_FORELDRE
      Resultatkode	FORHOYET_FORSKUDD_11_AAR_125_PROSENT

      REGEL 3
      Betingelse 1	Søknadsbarn alder er lavere enn 11 år
      Betingelse 2	Søknadsbarn bostedsstatus er BOR_IKKE_MED_FORELDRE
      Resultatkode	FORHOYET_FORSKUDD_100_PROSENT

      REGEL 4
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0005 x 0013
      Resultatkode	AVSLAG

      REGEL 5
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0033
      Betingelse 2	Søknadsbarn alder er høyere enn eller lik 11 år
      Resultatkode	FORHOYET_FORSKUDD_11_AAR_125_PROSENT

      REGEL 6
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0033
      Betingelse 2	Søknadsbarn alder er lavere enn 11 år
      Resultatkode	FORHOYET_FORSKUDD_100_PROSENT

      REGEL 7
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0034
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	ORDINAERT_FORSKUDD_75_PROSENT

      REGEL 8
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0034
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 9
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0034 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	ORDINAERT_FORSKUDD_75_PROSENT

      REGEL 10
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0034 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 11
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0035
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	ORDINAERT_FORSKUDD_75_PROSENT

      REGEL 12
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0035
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 13
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0035 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	ORDINAERT_FORSKUDD_75_PROSENT

      REGEL 14
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0035 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT
     */

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
        // Forhøyet forskudd ved 11 år: SB alder > 11 år; BM inntekt 290000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel1.json"
        forventetForskuddBelop = 2080
        forventetForskuddResultatkode = ResultatkodeForskudd.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
        forventetForskuddRegel = "REGEL 5"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 300000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel2.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 11"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 370000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel3.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 370000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel4.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel5.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel6.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 3; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel7.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 3; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel8.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 9"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 9")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel09() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 530000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel9.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 10")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel10() {
        // Avslag: SB alder > 11 år; BM inntekt 540000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel10.json"
        forventetForskuddBelop = 0
        forventetForskuddResultatkode = ResultatkodeForskudd.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 11")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel11() {
        // Avslag: SB alder > 11 år; BM inntekt 540000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel11.json"
        forventetForskuddBelop = 0
        forventetForskuddResultatkode = ResultatkodeForskudd.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 12")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel12() {
        // Forhøyet forskudd: SB alder < 11 år; BM inntekt 290000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel12.json"
        forventetForskuddBelop = 1670
        forventetForskuddResultatkode = ResultatkodeForskudd.FORHØYET_FORSKUDD_100_PROSENT
        forventetForskuddRegel = "REGEL 6"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 13")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel13() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 290000+13000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel13.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 14")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel14() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 361000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel14.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 15")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel15() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 361000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel15.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 16")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel16() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 468000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel16.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 17")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel17() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 468000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel17.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 18")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel18() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 429000; BM antall barn egen husstand 2; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel18.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 9"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 19")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel19() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 429000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel19.json"
        forventetForskuddBelop = 1250
        forventetForskuddResultatkode = ResultatkodeForskudd.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 20")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel20() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 430000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel20.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 14"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 22")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel22() {
        // Avslag: SB alder < 11 år; BM inntekt 489000+60000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel22.json"
        forventetForskuddBelop = 0
        forventetForskuddResultatkode = ResultatkodeForskudd.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        utfoerBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 23")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel23() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 489000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel23.json"
        forventetForskuddBelop = 830
        forventetForskuddResultatkode = ResultatkodeForskudd.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 14"
        utfoerBeregningerOgEvaluerResultat()
    }

    private fun utfoerBeregningerOgEvaluerResultat() {
        val request: BeregnGrunnlag = lesFilOgByggRequest(filnavn)

        // Kall rest-API for forskudd
        val forskuddResultat = beregnForskuddService.beregn(request)

        TestUtil.printJson(forskuddResultat)

        assertAll(
            Executable { assertThat(forskuddResultat).isNotNull() },
            Executable { assertThat(forskuddResultat?.beregnetForskuddPeriodeListe).isNotNull() },
            Executable { assertThat(forskuddResultat?.beregnetForskuddPeriodeListe).hasSize(1) },
            Executable { assertThat(forskuddResultat?.beregnetForskuddPeriodeListe?.get(0)?.resultat).isNotNull() },
            Executable {
                assertThat(forskuddResultat?.beregnetForskuddPeriodeListe?.get(0)?.resultat?.belop?.intValueExact())
                    .isEqualTo(forventetForskuddBelop)
            },
            Executable {
                assertThat(
                    forskuddResultat?.beregnetForskuddPeriodeListe?.get(0)?.resultat?.kode,
                ).isEqualTo(forventetForskuddResultatkode)
            },
            Executable {
                assertThat(
                    forskuddResultat?.beregnetForskuddPeriodeListe?.get(0)?.resultat?.regel,
                ).isEqualTo(forventetForskuddRegel)
            },
            Executable {
                assertThat(forskuddResultat?.beregnetForskuddPeriodeListe?.get(0)?.grunnlagsreferanseListe?.distinct()).size().isEqualTo(
                    forskuddResultat?.grunnlagListe?.size,
                )
            },
            Executable {
                assertThat(
                    forskuddResultat?.beregnetForskuddPeriodeListe?.get(
                        0,
                    )?.grunnlagsreferanseListe?.count { it.startsWith("Sjablon") }?.toLong(),
                ).isEqualTo(7)
            },
        )
    }

    private fun lesFilOgByggRequest(filnavn: String?): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn!!))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
