package no.nav.bidrag.beregn.forskudd.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.forskudd.TestUtil
import no.nav.bidrag.beregn.forskudd.service.BeregnForskuddService
import no.nav.bidrag.beregn.forskudd.testdata.SjablonApiStub
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
internal class BeregnForskuddApiTest {
    private lateinit var filnavn: String

    private var forventetForskuddBeløp = 0
    private lateinit var forventetForskuddResultatkode: Resultatkode
    private lateinit var forventetForskuddRegel: String
    private var forventetAntallDelberegningReferanser: Int = 2

    @Mock
    private lateinit var beregnForskuddService: BeregnForskuddService

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
      Resultatkode	FORHØYET_FORSKUDD_11_ÅR_125_PROSENT

      REGEL 3
      Betingelse 1	Søknadsbarn alder er lavere enn 11 år
      Betingelse 2	Søknadsbarn bostedsstatus er BOR_IKKE_MED_FORELDRE
      Resultatkode	FORHØYET_FORSKUDD_100_PROSENT

      REGEL 4
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0005 x 0013
      Resultatkode	AVSLAG

      REGEL 5
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0033
      Betingelse 2	Søknadsbarn alder er høyere enn eller lik 11 år
      Resultatkode	FORHØYET_FORSKUDD_11_ÅR_125_PROSENT

      REGEL 6
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0033
      Betingelse 2	Søknadsbarn alder er lavere enn 11 år
      Resultatkode	FORHØYET_FORSKUDD_100_PROSENT

      REGEL 7
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0034
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	ORDINÆRT_FORSKUDD_75_PROSENT

      REGEL 8
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0034
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 9
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0034 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	ORDINÆRT_FORSKUDD_75_PROSENT

      REGEL 10
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0034 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er ENSLIG
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 11
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0035
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	ORDINÆRT_FORSKUDD_75_PROSENT

      REGEL 12
      Betingelse 1	Bidragsmottakers inntekt er høyere enn 0035
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er 1
      Resultatkode	REDUSERT_FORSKUDD_50_PROSENT

      REGEL 13
      Betingelse 1	Bidragsmottakers inntekt er lavere enn eller lik 0035 + (0036 x antall barn utover ett)
      Betingelse 2	Bidragsmottakers sivilstand er GIFT
      Betingelse 3	Antall barn i husstand er mer enn 1
      Resultatkode	ORDINÆRT_FORSKUDD_75_PROSENT

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
        forventetForskuddBeløp = 2080
        forventetForskuddResultatkode = Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
        forventetForskuddRegel = "REGEL 5"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 300000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel2.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 11"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 370000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel3.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 370000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel4.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel5.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel6.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 3; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel7.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        forventetAntallDelberegningReferanser = 4
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Ordinært forskudd: SB alder > 11 år; BM inntekt 460000; BM antall barn egen husstand 3; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel8.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 9"
        forventetAntallDelberegningReferanser = 4
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 9")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel09() {
        // Redusert forskudd: SB alder > 11 år; BM inntekt 530000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel9.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 10")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel10() {
        // Avslag: SB alder > 11 år; BM inntekt 540000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel10.json"
        forventetForskuddBeløp = 0
        forventetForskuddResultatkode = Resultatkode.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 11")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel11() {
        // Avslag: SB alder > 11 år; BM inntekt 540000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel11.json"
        forventetForskuddBeløp = 0
        forventetForskuddResultatkode = Resultatkode.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 12")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel12() {
        // Forhøyet forskudd: SB alder < 11 år; BM inntekt 290000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel12.json"
        forventetForskuddBeløp = 1670
        forventetForskuddResultatkode = Resultatkode.FORHØYET_FORSKUDD_100_PROSENT
        forventetForskuddRegel = "REGEL 6"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 13")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel13() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 290000+13000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel13.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 14")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel14() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 361000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel14.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 15")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel15() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 361000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel15.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 16")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel16() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 468000; BM antall barn egen husstand 1; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel16.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 7"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 17")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel17() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 468000; BM antall barn egen husstand 1; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel17.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 12"
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 18")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel18() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 429000; BM antall barn egen husstand 2; BM sivilstatus enslig
        filnavn = "src/test/resources/testfiler/forskudd_eksempel18.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 9"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 19")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel19() {
        // Ordinært forskudd: SB alder < 11 år; BM inntekt 429000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel19.json"
        forventetForskuddBeløp = 1250
        forventetForskuddResultatkode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
        forventetForskuddRegel = "REGEL 13"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 20")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel20() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 430000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel20.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 14"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 22")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel22() {
        // Avslag: SB alder < 11 år; BM inntekt 489000+60000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel22.json"
        forventetForskuddBeløp = 0
        forventetForskuddResultatkode = Resultatkode.AVSLAG
        forventetForskuddRegel = "REGEL 4"
        forventetAntallDelberegningReferanser = 5
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 23")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel23() {
        // Redusert forskudd: SB alder < 11 år; BM inntekt 489000; BM antall barn egen husstand 2; BM sivilstatus gift
        filnavn = "src/test/resources/testfiler/forskudd_eksempel23.json"
        forventetForskuddBeløp = 830
        forventetForskuddResultatkode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
        forventetForskuddRegel = "REGEL 14"
        forventetAntallDelberegningReferanser = 3
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel med flere perioder")
    fun skalKalleCoreOgReturnereEtResultat_EksempelMedFlerePerioder() {
        filnavn = "src/test/resources/testfiler/forskudd_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatFlerePerioder()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel med mange inntektstyper")
    fun skalKalleCoreOgReturnereEtResultat_EksempelMedMangeInntektstyper() {
        filnavn = "src/test/resources/testfiler/forskudd_eksempel_mange_inntektstyper.json"
        utførBeregningerOgEvaluerResultatMangeInntektstyper()
    }

    private fun utførBeregningerOgEvaluerResultat() {
        val request = lesFilOgByggRequest(filnavn)

        // Kall rest-API for forskudd
        val forskuddResultat = beregnForskuddService.beregn(request)

        TestUtil.printJson(forskuddResultat)

        assertAll(
            { assertThat(forskuddResultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).hasSize(1) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.belop.intValueExact()).isEqualTo(forventetForskuddBeløp) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.kode).isEqualTo(forventetForskuddResultatkode) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.regel).isEqualTo(forventetForskuddRegel) },
            {
                assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe).hasSize(1)
            },
            {
                assertThat(
                    forskuddResultat.grunnlagListe
                        .filter { it.type == Grunnlagstype.SLUTTBEREGNING_FORSKUDD }
                        .flatMap { it.grunnlagsreferanseListe },
                ).hasSize(forskuddResultat.grunnlagListe.size.minus(forventetAntallDelberegningReferanser))
            },
            {
                assertThat(
                    forskuddResultat.grunnlagListe
                        .filter { it.type == Grunnlagstype.SLUTTBEREGNING_FORSKUDD }
                        .flatMap { it.grunnlagsreferanseListe }
                        .count { it.startsWith("sjablon") },
                ).isEqualTo(7)
            },
        )
    }

    private fun utførBeregningerOgEvaluerResultatFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)

        // Kall rest-API for forskudd
        val forskuddResultat = beregnForskuddService.beregn(request)

        TestUtil.printJson(forskuddResultat)

        assertAll(
            { assertThat(forskuddResultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).hasSize(8) },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.belop.intValueExact()).isEqualTo(1250) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.kode).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[0].resultat.regel).isEqualTo("REGEL 13") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[1].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[1].resultat.belop.intValueExact()).isEqualTo(0) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[1].resultat.kode).isEqualTo(Resultatkode.AVSLAG) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[1].resultat.regel).isEqualTo("REGEL 4") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[2].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[2].resultat.belop.intValueExact()).isEqualTo(0) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[2].resultat.kode).isEqualTo(Resultatkode.AVSLAG) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[2].resultat.regel).isEqualTo("REGEL 4") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[3].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[3].resultat.belop.intValueExact()).isEqualTo(0) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[3].resultat.kode).isEqualTo(Resultatkode.AVSLAG) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[3].resultat.regel).isEqualTo("REGEL 4") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[4].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[4].resultat.belop.intValueExact()).isEqualTo(940) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[4].resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[4].resultat.regel).isEqualTo("REGEL 14") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[5].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[5].resultat.belop.intValueExact()).isEqualTo(1880) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[5].resultat.kode).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_100_PROSENT) },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[5].resultat.regel).isEqualTo("REGEL 3") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[6].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[6].resultat.belop.intValueExact()).isEqualTo(2350) },
            {
                assertThat(
                    forskuddResultat.beregnetForskuddPeriodeListe[6].resultat.kode,
                ).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT)
            },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[6].resultat.regel).isEqualTo("REGEL 2") },

            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[7].resultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[7].resultat.belop.intValueExact()).isEqualTo(2350) },
            {
                assertThat(
                    forskuddResultat.beregnetForskuddPeriodeListe[7].resultat.kode,
                ).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT)
            },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe[7].resultat.regel).isEqualTo("REGEL 2") },
        )
    }

    private fun utførBeregningerOgEvaluerResultatMangeInntektstyper() {
        val request = lesFilOgByggRequest(filnavn)

        // Kall rest-API for forskudd
        val forskuddResultat = beregnForskuddService.beregn(request)

        TestUtil.printJson(forskuddResultat)

        assertAll(
            { assertThat(forskuddResultat).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).isNotNull },
            { assertThat(forskuddResultat.beregnetForskuddPeriodeListe).hasSize(1) },

            { assertThat(forskuddResultat.grunnlagListe).hasSize(24) },
            {
                assertThat(
                    forskuddResultat.grunnlagListe
                        .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
                        .first()
                        .grunnlagsreferanseListe,
                ).hasSize(11)
            },
            {
                assertThat(
                    forskuddResultat.grunnlagListe
                        .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
                        .first()
                        .grunnlagsreferanseListe,
                ).anySatisfy { it.startsWith("sjablon_InnslagKapitalInntektBeløp") }
            },
            {
                assertThat(
                    forskuddResultat.grunnlagListe
                        .filter { it.referanse.startsWith("sjablon_InnslagKapitalInntektBeløp") },
                ).hasSizeGreaterThan(0)
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
}
