package no.nav.bidrag.beregn.særbidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.beregn.særbidrag.testdata.SjablonApiStub
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
internal class BeregnSærbidragApiTest {
    private lateinit var filnavn: String

    private var forventetBidragsevneBeløp = BigDecimal.ZERO
    private var forventetBPAndelSærbidragProsentBarn = BigDecimal.ZERO
    private var forventetBPAndelSærbidragBeløpBarn = BigDecimal.ZERO
    private var forventetSærbidragBeløpBarn = BigDecimal.ZERO
    private lateinit var forventetSærbidragResultatkodeBarn: Resultatkode

    @Mock
    private lateinit var beregnSærbidragService: BeregnSærbidragService

    @BeforeEach
    fun initMock() {
        SjablonApiStub().settOppSjablonStub()
        beregnSærbidragService = BeregnSærbidragService()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
        // Enkel beregning med full evne, ett barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel1.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(11069)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(60.6)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(4242)
        forventetSærbidragBeløpBarn = BigDecimal.valueOf(4242)
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_INNVILGET
        utførBeregningerOgEvaluerResultat_EttSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel2.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6696)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(49.7)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(2982)
        forventetSærbidragBeløpBarn = BigDecimal.valueOf(2982)
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_INNVILGET
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel3.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(6684)
        forventetSærbidragBeløpBarn = BigDecimal.valueOf(6684)
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_INNVILGET
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel4.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(6684)
        forventetSærbidragBeløpBarn = BigDecimal.ZERO
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel5.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(9962)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(62.8)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(7536)
        forventetSærbidragBeløpBarn = BigDecimal.ZERO
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel6.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(10891)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(55.1)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(6612)
        forventetSærbidragBeløpBarn = BigDecimal.valueOf(6612)
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_INNVILGET
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel7.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(6684)
        forventetSærbidragBeløpBarn = BigDecimal.ZERO
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel8.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSærbidragBeløpBarn = BigDecimal.valueOf(6684)
        forventetSærbidragBeløpBarn = BigDecimal.ZERO
        forventetSærbidragResultatkodeBarn = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        utførBeregningerOgEvaluerResultat_ToSøknadsbarn()
    }

    private fun utførBeregningerOgEvaluerResultat_EttSøknadsbarn() {
        val request = lesFilOgByggRequest(filnavn)

         val totalSærbidragResultat = beregnSærbidragService.beregn(request)

        TestUtil.printJson(totalSærbidragResultat)

// TODO

//        val særbidragDelberegningResultat = totalSærbidragResultat?.let { SærbidragDelberegningResultat(it) }
//        val alleReferanser = totalSærbidragResultat.let { TestUtil.hentAlleReferanser(it) }
//        val referanserIGrunnlagListe = totalSærbidragResultat!!.grunnlagListe.map(Grunnlag::referanse).toList()

        assertAll(
            { assertThat(totalSærbidragResultat).isNotNull },
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe).hasSize(1) },
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe).isNotNull },
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.beløp).isEqualTo(forventetSærbidragBeløpBarn) },
            {
                assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.resultatkode).isEqualTo(
                    forventetSærbidragResultatkodeBarn,
                )
            },
//            { assertThat(særbidragDelberegningResultat!!.bidragsevneListe).size().isEqualTo(1) },
//            { assertThat(særbidragDelberegningResultat!!.bidragsevneListe[0].beløp).isEqualTo(forventetBidragsevneBeløp) },
//            { assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe).size().isEqualTo(1) },
//            {
//                assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe[0].beløp).isEqualTo(
//                    forventetBPAndelSærbidragBeløpBarn,
//                )
//            },
//            {
//                assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe[0].prosent).isEqualTo(
//                    forventetBPAndelSærbidragProsentBarn,
//                )
//            },
//            { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultat_ToSøknadsbarn() {
        val request = lesFilOgByggRequest(filnavn)

        val totalSærbidragResultat = beregnSærbidragService.beregn(request)

        TestUtil.printJson(totalSærbidragResultat)

// TODO

//        val særbidragDelberegningResultat = totalSærbidragResultat?.let { SærbidragDelberegningResultat(it) }
//        val alleReferanser = totalSærbidragResultat?.let { TestUtil.hentAlleReferanser(it) }
//        val referanserIGrunnlagListe = totalSærbidragResultat!!.grunnlagListe.stream().map(Grunnlag::referanse).toList()

        assertAll(
            Executable { assertThat(totalSærbidragResultat).isNotNull },
//            Executable { assertThat(særbidragDelberegningResultat!!.bidragsevneListe).hasSize(1) },
//            Executable {
//                assertThat(særbidragDelberegningResultat!!.bidragsevneListe[0].beløp).isEqualTo(forventetBidragsevneBeløp)
//            },
//            Executable { assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe).hasSize(1) },
//            Executable {
//                assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe[0].beløp).isEqualTo(
//                    forventetBPAndelSærbidragBeløpBarn,
//                )
//            },
//            Executable {
//                assertThat(særbidragDelberegningResultat!!.bpsAndelSærbidragListe[0].prosent).isEqualTo(
//                    forventetBPAndelSærbidragProsentBarn,
//                )
//            },
//            Executable { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe).hasSize(1) },
//            Executable {
//                assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.beløp).isEqualTo(
//                    forventetSærbidragBeløpBarn,
//                )
//            },
//            Executable {
//                assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.kode.toString()).isEqualTo(
//                    forventetSærbidragResultatkodeBarn,
//                )
//            },
//            Executable { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

    private fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }
}
