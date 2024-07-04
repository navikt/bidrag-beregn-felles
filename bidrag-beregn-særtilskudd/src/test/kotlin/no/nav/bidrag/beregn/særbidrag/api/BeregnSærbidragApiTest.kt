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

    private var forventetBidragsevneBelop = BigDecimal.ZERO
    private var forventetBPAndelSaertilskuddProsentBarn = BigDecimal.ZERO
    private var forventetBPAndelSaertilskuddBelopBarn = BigDecimal.ZERO
    private var forventetSamvaersfradragBelopBarn1 = BigDecimal.ZERO
    private var forventetSamvaersfradragBelopBarn2 = BigDecimal.ZERO
    private var forventetSaertilskuddBelopBarn = BigDecimal.ZERO
    private lateinit var forventetSaertilskuddResultatkodeBarn: Resultatkode

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
        forventetBidragsevneBelop = BigDecimal.valueOf(11069)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(60.6)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(4242)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(457)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(4242)
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_INNVILGET
        utfoerBeregningerOgEvaluerResultat_EttSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel2.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6696)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(49.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(2982)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(2982)
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_INNVILGET
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel3.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_INNVILGET
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel4.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel5.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(9962)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(62.8)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(7536)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel6.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(10891)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.1)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6612)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(6612)
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_INNVILGET
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel7.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel8.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = Resultatkode.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    private fun utfoerBeregningerOgEvaluerResultat_EttSoknadsbarn() {
        val request = lesFilOgByggRequest(filnavn)

        val totalSærtilskuddResultat = beregnSærbidragService.beregn(request)

        TestUtil.printJson(totalSærtilskuddResultat)

// TODO

//        val saertilskuddDelberegningResultat = totalSærtilskuddResultat?.let { SærtilskuddDelberegningResultat(it) }
//        val alleReferanser = totalSærtilskuddResultat.let { TestUtil.hentAlleReferanser(it) }
//        val referanserIGrunnlagListe = totalSærtilskuddResultat!!.grunnlagListe.map(Grunnlag::referanse).toList()

        assertAll(
            { assertThat(totalSærtilskuddResultat).isNotNull },
            { assertThat(totalSærtilskuddResultat.beregnetSærtilskuddPeriodeListe).hasSize(1) },
            { assertThat(totalSærtilskuddResultat.beregnetSærtilskuddPeriodeListe).isNotNull },
            { assertThat(totalSærtilskuddResultat.beregnetSærtilskuddPeriodeListe[0].resultat.beløp).isEqualTo(forventetSaertilskuddBelopBarn) },
            {
                assertThat(totalSærtilskuddResultat.beregnetSærtilskuddPeriodeListe[0].resultat.resultatkode).isEqualTo(
                    forventetSaertilskuddResultatkodeBarn,
                )
            },
//            { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe).size().isEqualTo(1) },
//            { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].belop).isEqualTo(forventetBidragsevneBelop) },
//            { assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe).size().isEqualTo(1) },
//            {
//                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].belop).isEqualTo(
//                    forventetBPAndelSaertilskuddBelopBarn,
//                )
//            },
//            {
//                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].prosent).isEqualTo(
//                    forventetBPAndelSaertilskuddProsentBarn,
//                )
//            },
//            { assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe).size().isEqualTo(1) },
//            {
//                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].belop).isEqualTo(forventetSamvaersfradragBelopBarn1)
//            },
//            { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

    private fun utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn() {
        val request = lesFilOgByggRequest(filnavn)

        val totalSærtilskuddResultat = beregnSærbidragService.beregn(request)

        TestUtil.printJson(totalSærtilskuddResultat)

// TODO

//        val saertilskuddDelberegningResultat = totalSærtilskuddResultat?.let { SaertilskuddDelberegningResultat(it) }
//        val alleReferanser = totalSærtilskuddResultat?.let { TestUtil.hentAlleReferanser(it) }
//        val referanserIGrunnlagListe = totalSærtilskuddResultat!!.grunnlagListe.stream().map(Grunnlag::referanse).toList()

        assertAll(
            Executable { assertThat(totalSærtilskuddResultat).isNotNull },
//            Executable { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe).hasSize(1) },
//            Executable {
//                assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].belop).isEqualTo(forventetBidragsevneBelop)
//            },
//            Executable { assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe).hasSize(1) },
//            Executable {
//                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].belop).isEqualTo(
//                    forventetBPAndelSaertilskuddBelopBarn,
//                )
//            },
//            Executable {
//                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].prosent).isEqualTo(
//                    forventetBPAndelSaertilskuddProsentBarn,
//                )
//            },
//            Executable { assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe).hasSize(2) },
//            Executable {
//                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].belop).isEqualTo(forventetSamvaersfradragBelopBarn1)
//            },
//            Executable {
//                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[1].belop).isEqualTo(forventetSamvaersfradragBelopBarn2)
//            },
//            Executable { assertThat(totalSærtilskuddResultat.beregnetSaertilskuddPeriodeListe).hasSize(1) },
//            Executable {
//                assertThat(totalSærtilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.belop).isEqualTo(
//                    forventetSaertilskuddBelopBarn,
//                )
//            },
//            Executable {
//                assertThat(totalSærtilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.kode.toString()).isEqualTo(
//                    forventetSaertilskuddResultatkodeBarn,
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
