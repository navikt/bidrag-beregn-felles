package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnEndeligBidragApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetBeregnetBeløp: BigDecimal
    private lateinit var forventetResultatbeløp: BigDecimal
    private lateinit var forventetUMinusNettoBarnetilleggBM: BigDecimal
    private lateinit var forventetBruttoBidragEtterBarnetilleggBM: BigDecimal
    private lateinit var forventetNettoBidragEtterBarnetilleggBM: BigDecimal
    private lateinit var forventetBruttoBidragJustertForEvneOg25Prosent: BigDecimal
    private lateinit var forventetBruttoBidragEtterBarnetilleggBP: BigDecimal
    private lateinit var forventetNettoBidragEtterSamværsfradrag: BigDecimal
    private lateinit var forventetBpAndelAvUVedDeltBostedFaktor: BigDecimal
    private lateinit var forventetBpAndelAvUVedDeltBostedBeløp: BigDecimal
    private var forventetBarnetErSelvforsørget: Boolean = false
    private var forventetBidragJustertForDeltBosted: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBP: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBM: Boolean = false
    private var forventetBidragJustertNedTilEvne: Boolean = false
    private var forventetBidragJustertNedTil25ProsentAvInntekt: Boolean = false
    private var forventetAntallDelberegningBidragsevne: Int = 1
    private var forventetAntallDelberegningUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningBPAndelUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningSamværsfradrag: Int = 1
    private var forventetAntallDelberegningSumInntekt: Int = 1
    private var forventetAntallDelberegningBarnetilleggSkattesats: Int = 1
    private var forventetAntallDelberegningNettoBarnetillegg: Int = 1
    private var forventetAntallSamværsklasse: Int = 1
    private var forventetAntallBarnetilleggBM: Int = 1
    private var forventetAntallBarnetilleggBP: Int = 1

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = true
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 0
        forventetAntallDelberegningBarnetilleggSkattesats = 0
        forventetAntallDelberegningNettoBarnetillegg = 0
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(999).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 0
        forventetAntallDelberegningBarnetilleggSkattesats = 0
        forventetAntallDelberegningNettoBarnetillegg = 0
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(999).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetAntallDelberegningSumInntekt = 0
        forventetAntallDelberegningBarnetilleggSkattesats = 0
        forventetAntallDelberegningNettoBarnetillegg = 0
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2000).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.valueOf(0.1).setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 0
        forventetAntallDelberegningBarnetilleggSkattesats = 0
        forventetAntallDelberegningNettoBarnetillegg = 0
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = true
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 1
        forventetAntallDelberegningBarnetilleggSkattesats = 1
        forventetAntallDelberegningNettoBarnetillegg = 1
        forventetAntallBarnetilleggBP = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(5828.41).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2828.41).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = true
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1800).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(801).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1800).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(801).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = true
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(990.37).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(1889.37).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(990.37).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
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
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6000).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5001).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 2
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel med flere perioder")
    fun testEndeligBidrag_Eksempel_Flere_perioder() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel_flere_perioder.json"
        forventetAntallDelberegningBidragsevne = 4
        forventetAntallDelberegningUnderholdskostnad = 2
        forventetAntallDelberegningBPAndelUnderholdskostnad = 5
        forventetAntallDelberegningSamværsfradrag = 5
        forventetAntallDelberegningSumInntekt = 2
        forventetAntallDelberegningBarnetilleggSkattesats = 2
        forventetAntallDelberegningNettoBarnetillegg = 6
        forventetAntallSamværsklasse = 4
        forventetAntallBarnetilleggBP = 3
        forventetAntallBarnetilleggBM = 3
        utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidrag(antallGrunnlag: Int = 1) {
        val request = lesFilOgByggRequest(filnavn)
        val endeligBidragResultat = beregnBarnebidragService.beregnEndeligBidrag(request)
        printJson(endeligBidragResultat)

        val alleReferanser = hentAlleReferanser(endeligBidragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat).filter { it != "Person_Søknadsbarn" }

        val endeligBidragResultatListe = endeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningBarnebidrag(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatBeløp = it.innhold.resultatBeløp,
                    uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                    bruttoBidragEtterBarnetilleggBM = it.innhold.bruttoBidragEtterBarnetilleggBM,
                    nettoBidragEtterBarnetilleggBM = it.innhold.nettoBidragEtterBarnetilleggBM,
                    bruttoBidragJustertForEvneOg25Prosent = it.innhold.bruttoBidragJustertForEvneOg25Prosent,
                    bruttoBidragEtterBarnetilleggBP = it.innhold.bruttoBidragEtterBarnetilleggBP,
                    nettoBidragEtterSamværsfradrag = it.innhold.nettoBidragEtterSamværsfradrag,
                    bpAndelAvUVedDeltBostedFaktor = it.innhold.bpAndelAvUVedDeltBostedFaktor,
                    bpAndelAvUVedDeltBostedBeløp = it.innhold.bpAndelAvUVedDeltBostedBeløp,
                    ingenEndringUnderGrense = it.innhold.ingenEndringUnderGrense,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                    bidragJustertForDeltBosted = it.innhold.bidragJustertForDeltBosted,
                    bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                    bidragJustertForNettoBarnetilleggBM = it.innhold.bidragJustertForNettoBarnetilleggBM,
                    bidragJustertNedTilEvne = it.innhold.bidragJustertNedTilEvne,
                    bidragJustertNedTil25ProsentAvInntekt = it.innhold.bidragJustertNedTil25ProsentAvInntekt,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val antallDelberegningBidragsevne = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        val antallDelberegningUnderholdskostnad = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallDelberegningSumInntekt = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .size

        val antallDelberegningBarnetilleggSkattesats = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS }
            .size

        val antallDelberegningNettoBarnetillegg = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG }
            .size

        val antallSamværsklasse = endeligBidragResultat
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallBarnetilleggBM = endeligBidragResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallBarnetilleggBP = endeligBidragResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        assertAll(
            { assertThat(endeligBidragResultat).isNotNull },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(1) },

            // Resultat
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(forventetBeregnetBeløp) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(forventetResultatbeløp) },
            { assertThat(endeligBidragResultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(forventetUMinusNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBM).isEqualTo(forventetBruttoBidragEtterBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterBarnetilleggBM).isEqualTo(forventetNettoBidragEtterBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(forventetBruttoBidragJustertForEvneOg25Prosent) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(forventetBruttoBidragEtterBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(forventetNettoBidragEtterSamværsfradrag) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(forventetBpAndelAvUVedDeltBostedFaktor) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(forventetBpAndelAvUVedDeltBostedBeløp) },
            { assertThat(endeligBidragResultatListe[0].ingenEndringUnderGrense).isFalse() },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørget) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isEqualTo(forventetBidragJustertForDeltBosted) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(forventetBidragJustertForNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isEqualTo(forventetBidragJustertForNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isEqualTo(forventetBidragJustertNedTilEvne) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isEqualTo(forventetBidragJustertNedTil25ProsentAvInntekt) },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningSumInntekt).isEqualTo(forventetAntallDelberegningSumInntekt) },
            { assertThat(antallDelberegningBarnetilleggSkattesats).isEqualTo(forventetAntallDelberegningBarnetilleggSkattesats) },
            { assertThat(antallDelberegningNettoBarnetillegg).isEqualTo(forventetAntallDelberegningNettoBarnetillegg) },
            { assertThat(antallSamværsklasse).isEqualTo(antallGrunnlag) },
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidragFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val endeligBidragResultat = beregnBarnebidragService.beregnEndeligBidrag(request)
        printJson(endeligBidragResultat)

        val alleReferanser = hentAlleReferanser(endeligBidragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat).filter { it != "Person_Søknadsbarn" }

        val endeligBidragResultatListe = endeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningBarnebidrag(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatBeløp = it.innhold.resultatBeløp,
                    uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                    bruttoBidragEtterBarnetilleggBM = it.innhold.bruttoBidragEtterBarnetilleggBM,
                    nettoBidragEtterBarnetilleggBM = it.innhold.nettoBidragEtterBarnetilleggBM,
                    bruttoBidragJustertForEvneOg25Prosent = it.innhold.bruttoBidragJustertForEvneOg25Prosent,
                    bruttoBidragEtterBarnetilleggBP = it.innhold.bruttoBidragEtterBarnetilleggBP,
                    nettoBidragEtterSamværsfradrag = it.innhold.nettoBidragEtterSamværsfradrag,
                    bpAndelAvUVedDeltBostedFaktor = it.innhold.bpAndelAvUVedDeltBostedFaktor,
                    bpAndelAvUVedDeltBostedBeløp = it.innhold.bpAndelAvUVedDeltBostedBeløp,
                    ingenEndringUnderGrense = it.innhold.ingenEndringUnderGrense,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                    bidragJustertForDeltBosted = it.innhold.bidragJustertForDeltBosted,
                    bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                    bidragJustertForNettoBarnetilleggBM = it.innhold.bidragJustertForNettoBarnetilleggBM,
                    bidragJustertNedTilEvne = it.innhold.bidragJustertNedTilEvne,
                    bidragJustertNedTil25ProsentAvInntekt = it.innhold.bidragJustertNedTil25ProsentAvInntekt,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val antallDelberegningBidragsevne = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        val antallDelberegningUnderholdskostnad = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallDelberegningSumInntekt = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .size

        val antallDelberegningBarnetilleggSkattesats = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS }
            .size

        val antallDelberegningNettoBarnetillegg = endeligBidragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG }
            .size

        val antallSamværsklasse = endeligBidragResultat
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallBarnetilleggBM = endeligBidragResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallBarnetilleggBP = endeligBidragResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        assertAll(
            { assertThat(endeligBidragResultat).isNotNull },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(8) },

            // Resultat
            // Barnet er selvforsørget
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-04")) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isTrue },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Bidrag satt til underholdskostnad minus barnetillegg BM
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-06")) },
            { assertThat(endeligBidragResultatListe[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4760).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(5757.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[1].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[1].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[1].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Bidrag redusert til evne
            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-10")) },
            { assertThat(endeligBidragResultatListe[2].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5657.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[2].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[2].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[2].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTilEvne).isTrue },
            { assertThat(endeligBidragResultatListe[2].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Bidrag redusert til 25% av inntekt
            { assertThat(endeligBidragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-10", "2024-01")) },
            { assertThat(endeligBidragResultatListe[3].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[3].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(5657.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4758.41).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[3].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[3].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertForNettoBarnetilleggBM).isTrue },
            { assertThat(endeligBidragResultatListe[3].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[3].bidragJustertNedTil25ProsentAvInntekt).isTrue },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertThat(endeligBidragResultatListe[4].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[4].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[4].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[4].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[4].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Bidrag redusert til 25% av inntekt
            { assertThat(endeligBidragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-05")) },
            { assertThat(endeligBidragResultatListe[5].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[5].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(15766.23).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(12000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.valueOf(11101).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.ZERO.setScale(10)) },
            { assertThat(endeligBidragResultatListe[5].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[5].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForDeltBosted).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[5].bidragJustertNedTil25ProsentAvInntekt).isTrue },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[6].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-08")) },
            { assertThat(endeligBidragResultatListe[6].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].resultatBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[6].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.valueOf(0.1).setScale(10)) },
            { assertThat(endeligBidragResultatListe[6].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[6].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[6].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Delt bosted
            { assertThat(endeligBidragResultatListe[7].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(endeligBidragResultatListe[7].beregnetBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].resultatBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[7].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBidragEtterBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragJustertForEvneOg25Prosent).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bruttoBidragEtterBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBidragEtterSamværsfradrag).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].bpAndelAvUVedDeltBostedFaktor).isEqualTo(BigDecimal.valueOf(0.1).setScale(10)) },
            { assertThat(endeligBidragResultatListe[7].bpAndelAvUVedDeltBostedBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].ingenEndringUnderGrense).isFalse },
            { assertThat(endeligBidragResultatListe[7].barnetErSelvforsørget).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForDeltBosted).isTrue },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertForNettoBarnetilleggBM).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[7].bidragJustertNedTil25ProsentAvInntekt).isFalse },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(forventetAntallDelberegningBidragsevne) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(forventetAntallDelberegningUnderholdskostnad) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(forventetAntallDelberegningBPAndelUnderholdskostnad) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(forventetAntallDelberegningSamværsfradrag) },
            { assertThat(antallDelberegningSumInntekt).isEqualTo(forventetAntallDelberegningSumInntekt) },
            { assertThat(antallDelberegningBarnetilleggSkattesats).isEqualTo(forventetAntallDelberegningBarnetilleggSkattesats) },
            { assertThat(antallDelberegningNettoBarnetillegg).isEqualTo(forventetAntallDelberegningNettoBarnetillegg) },
            { assertThat(antallSamværsklasse).isEqualTo(forventetAntallSamværsklasse) },
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    // TODO Flytte til felles
    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    // TODO Flytte til felles
    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

    // TODO Flytte til felles
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

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
