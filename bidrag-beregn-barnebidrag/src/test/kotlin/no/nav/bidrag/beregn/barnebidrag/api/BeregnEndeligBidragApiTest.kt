package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnEndeligBidragApiTest : FellesApiTest() {
    private lateinit var filnavn: String
    private var forventetBeregnetBeløp: BigDecimal? = null
    private var forventetResultatbeløp: BigDecimal? = null
    private lateinit var forventetUMinusNettoBarnetilleggBM: BigDecimal
    private lateinit var forventetBruttoBidragEtterBarnetilleggBM: BigDecimal
    private lateinit var forventetNettoBidragEtterBarnetilleggBM: BigDecimal
    private lateinit var forventetBruttoBidragJustertForEvneOg25Prosent: BigDecimal
    private lateinit var forventetBruttoBidragEtterBegrensetRevurdering: BigDecimal
    private lateinit var forventetBruttoBidragEtterBarnetilleggBP: BigDecimal
    private lateinit var forventetNettoBidragEtterSamværsfradrag: BigDecimal
    private lateinit var forventetBpAndelAvUVedDeltBostedFaktor: BigDecimal
    private lateinit var forventetBpAndelAvUVedDeltBostedBeløp: BigDecimal
    private var forventetLøpendeForskudd: BigDecimal? = null
    private var forventetLøpendeBidrag: BigDecimal? = null
    private var forventetBarnetErSelvforsørget: Boolean = false
    private var forventetBidragJustertForDeltBosted: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBP: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBM: Boolean = false
    private var forventetBidragJustertNedTilEvne: Boolean = false
    private var forventetBidragJustertNedTil25ProsentAvInntekt: Boolean = false
    private var forventetBidragJustertTilForskuddssats: Boolean = false
    private var forventetBegrensetRevurderingUtført: Boolean = false
    private var forventetIkkeOmsorgForBarnet: Boolean = false
    private var forventetFeilmelding = ""
    private var forventetPerioderMedFeilListe = emptyList<ÅrMånedsperiode>()
    private var forventetExceptionBegrensetRevurdering = false
    private var forventetAntallDelberegningBidragsevne: Int = 1
    private var forventetAntallDelberegningUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningBPAndelUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningSamværsfradrag: Int = 1
    private var forventetAntallSamværsklasse: Int = 1
    private var forventetAntallBostatus: Int = 1
    private var forventetAntallBarnetilleggBM: Int = 1
    private var forventetAntallBarnetilleggBP: Int = 1
    private var forventetAntallPerioder: Int = 1
    private var forventetTilPeriode: YearMonth? = null

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 1 - Barnet er selvforsørget")
    fun testEndeligBidrag_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel1.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        forventetAntallBostatus = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag(0)
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 2 - Bidrag redusert til evne ved delt bosted")
    fun testEndeligBidrag_Eksempel2() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel2.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(999).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(1000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(999).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(999).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertNedTilEvne = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 3 - Bidrag redusert til 25 prosent av inntekt ved delt bosted")
    fun testEndeligBidrag_Eksempel3() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel3.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(999).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(1000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(999).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(999).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 4A - Bidrag justert for delt bosted")
    fun testEndeligBidrag_Eksempel4A() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel4A.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(2000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(2000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2000).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBidragJustertForDeltBosted = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 4B - Bidrag justert for delt bosted")
    fun testEndeligBidrag_Eksempel4B() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel4B.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForDeltBosted = true
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 5 - Bidrag satt til barnetillegg BP")
    fun testEndeligBidrag_Eksempel5() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel5.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2828.41).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(2830).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3990.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(2500).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(2500).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(5828.41).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2828.41).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBP = true
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 6 - Bidrag redusert til evne")
    fun testEndeligBidrag_Eksempel6() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel6.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(801).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(800).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(1989.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(1800).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(1800).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1800).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(801).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 7 - Bidrag redusert til 25 prosent av inntekt")
    fun testEndeligBidrag_Eksempel7() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel7.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(801).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(800).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(1989.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(1800).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(1800).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1800).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(801).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 8A - Bidrag satt til underholdskostnad minus barnetillegg BM")
    fun testEndeligBidrag_Eksempel8A() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel8A.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(990.37).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(990).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(1889.37).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(1889.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(990.37).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 8B - Bidrag satt til underholdskostnad minus barnetillegg BM - flere typer barnetillegg")
    fun testEndeligBidrag_Eksempel8B() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel8B.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(990.37).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(990).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(990.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(1889.37).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(1889.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(990.37).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetAntallBarnetilleggBM = 2
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 9 - Kostnadsberegnet bidrag")
    fun testEndeligBidrag_Eksempel9() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel9.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10A - Begrenset revurdering - beregnet bidrag er høyere enn løpende forskudd")
    fun testEndeligBidrag_Eksempel10A() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10A.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(4500).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(4500).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(5499).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(5499).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(4500).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(4500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(4000).setScale(0)
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10B - Begrenset revurdering - beregnet bidrag er lavere enn løpende forskudd og høyere enn løpende bidrag")
    fun testEndeligBidrag_Eksempel10B() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10B.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(5500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(4000).setScale(0)
        forventetBegrensetRevurderingUtført = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10C - Begrenset revurdering - beregnet bidrag er lavere enn løpende bidrag - skal kaste exception")
    fun testEndeligBidrag_Eksempel10C() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10C.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(5500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(5200).setScale(0)
        forventetBegrensetRevurderingUtført = true
        forventetFeilmelding = "Kan ikke fatte vedtak fordi beregnet bidrag for følgende perioder er lavere enn løpende bidrag: 2024-08 - "
        forventetPerioderMedFeilListe = listOf(ÅrMånedsperiode(YearMonth.parse("2024-08"), null))
        forventetExceptionBegrensetRevurdering = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10D - Begrenset revurdering - beregnet bidrag er høyere enn løpende forskudd, men overstyres av barnetillegg BP")
    fun testEndeligBidrag_Eksempel10D() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10D.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5106.21).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5110).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(5499).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6105.21).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5106.21).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(4500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(4000).setScale(0)
        forventetBidragJustertForNettoBarnetilleggBP = true
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10E - Begrenset revurdering - beløpshistorikk forskudd mangler")
    fun testEndeligBidrag_Eksempel10E() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10E.json"
        val exception = assertThrows(IllegalArgumentException::class.java) {
            utførBeregningerOgEvaluerResultatEndeligBidrag()
        }
        assertThat(exception.message).contains("Beløpshistorikk grunnlag mangler for begrenset revurdering")
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10F - Begrenset revurdering - beløpshistorikk bidrag er tom for periode")
    fun testEndeligBidrag_Eksempel10F() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10F.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(4500).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(4500).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(5499).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(5499).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(4500).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(4500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.ZERO.setScale(0)
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10G - Begrenset revurdering - delt bosted og beregnet bidrag er høyere enn løpende forskudd")
    fun testEndeligBidrag_Eksempel10G() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10G.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(1500).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(1500).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(2000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2000).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(1500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(1000).setScale(0)
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10H - Begrenset revurdering - indikator for begrenset revurdering er false")
    fun testEndeligBidrag_Eksempel10H() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10H.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = null
        forventetLøpendeBidrag = null
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10I - Begrenset revurdering - beregnet bidrag er 0 - skal ikke kaste exception")
    fun testEndeligBidrag_Eksempel10I() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10I.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(5500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(5200).setScale(0)
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetBegrensetRevurderingUtført = true
        forventetExceptionBegrensetRevurdering = false
        forventetAntallBarnetilleggBP = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10J - Begrenset revurdering - flere perioder")
    fun testEndeligBidrag_Eksempel10J() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10J.json"
        utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioderBegrensetRevurdering()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10K - Begrenset revurdering - løpende forskudd mangler i starten av beregningsperioden - skal kaste exception")
    fun testEndeligBidrag_Eksempel10K() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10K.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(745.35).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(750).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1744.35).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(745.35).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.ZERO.setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(4000).setScale(0)
        forventetBidragJustertForNettoBarnetilleggBP = true
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        forventetFeilmelding = "Kan ikke fatte vedtak fordi løpende forskudd mangler i første beregningsperiode: 2024-08 - 2024-09"
        forventetPerioderMedFeilListe = listOf(ÅrMånedsperiode(YearMonth.parse("2024-08"), YearMonth.parse("2024-09")))
        forventetExceptionBegrensetRevurdering = true
        forventetAntallPerioder = 2
        forventetTilPeriode = YearMonth.parse("2024-09")
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 10L - Begrenset revurdering - løpende forskudd mangler i slutten av beregningsperioden - skal ikke kaste exception")
    fun testEndeligBidrag_Eksempel10L() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel10L.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8514.87).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5001).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6000).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(5500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(700).setScale(0)
        forventetBegrensetRevurderingUtført = true
        forventetExceptionBegrensetRevurdering = false
        forventetAntallPerioder = 2
        forventetTilPeriode = YearMonth.parse("2024-09")
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 11 - Søknadsbarnet bor hos BP - bidrag skal ikke beregnes")
    fun testEndeligBidrag_Eksempel11() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel11.json"
        forventetBeregnetBeløp = null
        forventetResultatbeløp = null
        forventetUMinusNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.ZERO.setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetIkkeOmsorgForBarnet = true
        forventetAntallDelberegningBPAndelUnderholdskostnad = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag(0)
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel med flere perioder")
    fun testEndeligBidrag_Eksempel_Flere_perioder() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel_flere_perioder.json"
        forventetAntallDelberegningBidragsevne = 4
        forventetAntallDelberegningUnderholdskostnad = 2
        forventetAntallDelberegningBPAndelUnderholdskostnad = 5
        forventetAntallDelberegningSamværsfradrag = 5
        forventetAntallSamværsklasse = 4
        forventetAntallBarnetilleggBP = 3
        forventetAntallBarnetilleggBM = 3
        forventetAntallBostatus = 3
        utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidrag(antallGrunnlag: Int = 1) {
        val request = lesFilOgByggRequest(filnavn)
        val endeligBidragResultat = api.beregnEndeligBidrag(request)
        printJson(endeligBidragResultat)

        val alleReferanser = hentAlleReferanser(endeligBidragResultat.grunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat.grunnlagListe).filter { it != "Person_Søknadsbarn" }

        val endeligBidragResultatListe = hentSluttberegning(endeligBidragResultat.grunnlagListe)
        val feilmelding = endeligBidragResultat.feilmelding
        val perioderMedFeilListe = endeligBidragResultat.perioderMedFeilListe
        val skalKasteBegrensetRevurderingException = endeligBidragResultat.skalKasteBegrensetRevurderingException

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val antallDelberegningBidragsevne = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        val antallDelberegningUnderholdskostnad = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallSamværsklasse = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallBostatus = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .size

        val antallBarnetilleggBM = endeligBidragResultat.grunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBM,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        val antallBarnetilleggBP = endeligBidragResultat.grunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBP,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        assertAll(
            { assertThat(endeligBidragResultat).isNotNull },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(forventetAntallPerioder) },

            // Resultat
            {
                assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), forventetTilPeriode))
            },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(forventetBeregnetBeløp) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(forventetResultatbeløp) },
            { assertThat(endeligBidragResultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(forventetUMinusNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBM).isEqualTo(forventetBruttoBidragEtterBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterBarnetilleggBM).isEqualTo(forventetNettoBidragEtterBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(forventetBruttoBidragJustertForEvneOg25Prosent) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBegrensetRevurdering).isEqualTo(forventetBruttoBidragEtterBegrensetRevurdering) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(forventetBruttoBidragEtterBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(forventetNettoBidragEtterSamværsfradrag) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(forventetBpAndelAvUVedDeltBostedFaktor) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(forventetBpAndelAvUVedDeltBostedBeløp) },
            { assertThat(endeligBidragResultatListe[0].løpendeForskudd).isEqualTo(forventetLøpendeForskudd) },
            { assertThat(endeligBidragResultatListe[0].løpendeBidrag).isEqualTo(forventetLøpendeBidrag) },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørget) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isEqualTo(forventetBidragJustertForDeltBosted) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(forventetBidragJustertForNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isEqualTo(forventetBidragJustertForNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isEqualTo(forventetBidragJustertNedTilEvne) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isEqualTo(forventetBidragJustertNedTil25ProsentAvInntekt) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertTilForskuddssats).isEqualTo(forventetBidragJustertTilForskuddssats) },
            { assertThat(endeligBidragResultatListe[0].begrensetRevurderingUtført).isEqualTo(forventetBegrensetRevurderingUtført) },
            { assertThat(endeligBidragResultatListe[0].ikkeOmsorgForBarnet).isEqualTo(forventetIkkeOmsorgForBarnet) },

            { assertThat(feilmelding).isEqualTo(forventetFeilmelding) },
            { assertThat(perioderMedFeilListe).isEqualTo(forventetPerioderMedFeilListe) },
            { assertThat(skalKasteBegrensetRevurderingException).isEqualTo(forventetExceptionBegrensetRevurdering) },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(forventetAntallDelberegningBPAndelUnderholdskostnad) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(antallGrunnlag) },
            { assertThat(antallSamværsklasse).isEqualTo(antallGrunnlag) },
            { assertThat(antallBostatus).isEqualTo(forventetAntallBostatus) },
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },

            // Referanser
            {
                assertThat(alleReferanser).containsAll(alleRefererteReferanser)
            },
        )
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val endeligBidragResultat = api.beregnEndeligBidrag(request)
        printJson(endeligBidragResultat)

        val alleReferanser = hentAlleReferanser(endeligBidragResultat.grunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat.grunnlagListe).filter { it != "Person_Søknadsbarn" }

        val endeligBidragResultatListe = hentSluttberegning(endeligBidragResultat.grunnlagListe)

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val antallDelberegningBidragsevne = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        val antallDelberegningUnderholdskostnad = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallSamværsklasse = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallBostatus = endeligBidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .size

        val antallBarnetilleggBM = endeligBidragResultat.grunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBM,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        val antallBarnetilleggBP = endeligBidragResultat.grunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBP,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        assertAll(
            { assertThat(endeligBidragResultat).isNotNull },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(9) },

            // Resultat
            // Barnet er selvforsørget
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-04")) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[0].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isTrue },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[0].ikkeOmsorgForBarnet).isFalse },

            // Bidrag redusert til evne
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-06")) },
            { assertThat(endeligBidragResultatListe[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4760).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[1].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[1].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[1].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[1].ikkeOmsorgForBarnet).isFalse },

            // Bidrag redusert til evne
            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-10")) },
            { assertThat(endeligBidragResultatListe[2].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5657.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[2].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[2].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTilEvne).isTrue },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[2].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[2].ikkeOmsorgForBarnet).isFalse },

            // Bidrag redusert til 25% av inntekt
            { assertThat(endeligBidragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-10", "2024-01")) },
            { assertThat(endeligBidragResultatListe[3].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[3].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5657.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[3].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[3].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[3].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[3].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertNedTil25ProsentAvInntekt).isTrue },
            { assertThat(endeligBidragResultatListe[3].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[3].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[3].ikkeOmsorgForBarnet).isFalse },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertThat(endeligBidragResultatListe[4].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[4].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[4].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[4].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[4].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[4].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[4].ikkeOmsorgForBarnet).isFalse },

            // Bidrag redusert til 25% av inntekt
            { assertThat(endeligBidragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-05")) },
            { assertThat(endeligBidragResultatListe[5].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[5].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(15766.23).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(12000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(11101).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[5].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[5].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[5].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertNedTil25ProsentAvInntekt).isTrue },
            { assertThat(endeligBidragResultatListe[5].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[5].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[5].ikkeOmsorgForBarnet).isFalse },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[6].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(endeligBidragResultatListe[6].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].resultatBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[6].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.valueOf(0.1).setScale(10)) },
            { assertThat(endeligBidragResultatListe[6].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[6].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[6].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[6].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[6].ikkeOmsorgForBarnet).isFalse },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[7].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-08")) },
            { assertThat(endeligBidragResultatListe[7].beregnetBeløp).isNull() },
            { assertThat(endeligBidragResultatListe[7].resultatBeløp).isNull() },
            { assertThat(endeligBidragResultatListe[7].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[7].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[7].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[7].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[7].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[7].ikkeOmsorgForBarnet).isTrue },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[8].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(endeligBidragResultatListe[8].beregnetBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].resultatBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[8].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.valueOf(0.1).setScale(10)) },
            { assertThat(endeligBidragResultatListe[8].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[8].løpendeForskudd).isNull() },
            { assertThat(endeligBidragResultatListe[8].løpendeBidrag).isNull() },
            { assertThat(endeligBidragResultatListe[8].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[8].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[8].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[8].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[8].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[8].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[8].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[8].begrensetRevurderingUtført).isFalse },
            { assertThat(endeligBidragResultatListe[8].ikkeOmsorgForBarnet).isFalse },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(forventetAntallDelberegningBidragsevne) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(forventetAntallDelberegningUnderholdskostnad) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(forventetAntallDelberegningBPAndelUnderholdskostnad) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(forventetAntallDelberegningSamværsfradrag) },
            { assertThat(antallSamværsklasse).isEqualTo(forventetAntallSamværsklasse) },
            { assertThat(antallBostatus).isEqualTo(forventetAntallBostatus) },
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioderBegrensetRevurdering() {
        val request = lesFilOgByggRequest(filnavn)
        val endeligBidragResultat = api.beregnEndeligBidrag(request)
        printJson(endeligBidragResultat)

        val alleReferanser = hentAlleReferanser(endeligBidragResultat.grunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat.grunnlagListe).filter { it != "Person_Søknadsbarn" }

        val endeligBidragResultatListe = hentSluttberegning(endeligBidragResultat.grunnlagListe)
        val feilmelding = endeligBidragResultat.feilmelding
        val perioderMedFeilListe = endeligBidragResultat.perioderMedFeilListe
        val skalKasteBegrensetRevurderingException = endeligBidragResultat.skalKasteBegrensetRevurderingException

        assertAll(
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(3) },

            // Resultat
            // Begrenset revurdering - beregnet bidrag er høyere enn løpende forskudd
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(BigDecimal.valueOf(4500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(4500).setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(8514.87).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(5499).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(5499).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(4500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].løpendeForskudd).isEqualTo(BigDecimal.valueOf(4500).setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].løpendeBidrag).isEqualTo(BigDecimal.valueOf(4000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertTilForskuddssats).isTrue },
            { assertThat(endeligBidragResultatListe[0].begrensetRevurderingUtført).isTrue },

            // Begrenset revurdering - beregnet bidrag er lavere enn løpende forskudd og høyere enn løpende bidrag
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-10")) },
            { assertThat(endeligBidragResultatListe[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(5000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(8514.87).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].løpendeForskudd).isEqualTo(BigDecimal.valueOf(5500).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].løpendeBidrag).isEqualTo(BigDecimal.valueOf(4000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[1].begrensetRevurderingUtført).isTrue },

            // Begrenset revurdering - beregnet bidrag er lavere enn løpende bidrag
            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-10"), null)) },
            { assertThat(endeligBidragResultatListe[2].beregnetBeløp).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(5000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(8514.87).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBegrensetRevurdering).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(6000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(5001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].løpendeForskudd).isEqualTo(BigDecimal.valueOf(5500).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].løpendeBidrag).isEqualTo(BigDecimal.valueOf(5200).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertTilForskuddssats).isFalse },
            { assertThat(endeligBidragResultatListe[2].begrensetRevurderingUtført).isTrue },

            { assertThat(feilmelding).isEqualTo("Kan ikke fatte vedtak fordi beregnet bidrag for følgende perioder er lavere enn løpende bidrag: 2024-10 - ") },
            { assertThat(perioderMedFeilListe).isEqualTo(listOf(ÅrMånedsperiode(YearMonth.parse("2024-10"), null))) },
            { assertThat(skalKasteBegrensetRevurderingException).isTrue },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
