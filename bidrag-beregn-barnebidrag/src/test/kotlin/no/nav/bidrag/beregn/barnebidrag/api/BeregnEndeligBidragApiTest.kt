package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Resultatkode
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

@ExtendWith(MockitoExtension::class)
internal class BeregnEndeligBidragApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetBeregnetBeløp: BigDecimal
    private lateinit var forventetResultatkode: Resultatkode
    private lateinit var forventetResultatbeløp: BigDecimal
    private lateinit var forventetKostnadsberegnetBidrag: BigDecimal
    private lateinit var forventetNettoBarnetilleggBP: BigDecimal
    private lateinit var forventetNettoBarnetilleggBM: BigDecimal
    private var forventetJustertNedTilEvne: Boolean = false
    private var forventetJustertNedTil25ProsentAvInntekt: Boolean = false
    private var forventetJustertForNettoBarnetilleggBP: Boolean = false
    private var forventetJustertForNettoBarnetilleggBM: Boolean = false
    private var forventetAntallDelberegningBidragsevne: Int = 1
    private var forventetAntallDelberegningUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningBPAndelUnderholdskostnad: Int = 1
    private var forventetAntallDelberegningSamværsfradrag: Int = 1
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
    fun testEndeligBidrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel1.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatkode = Resultatkode.BARNET_ER_SELVFORSØRGET
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.ZERO.setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag(0)
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 2A - Delt bosted ved kostnadsberegnet bidrag")
    fun testEndeligBidrag_Eksempel02A() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel2A.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetResultatkode = Resultatkode.DELT_BOSTED
        forventetResultatbeløp = BigDecimal.valueOf(2000).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(2000).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 2B - Delt bosted ved barnetillegg BP")
    fun testEndeligBidrag_Eksempel02B() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel2B.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2000).setScale(2)
        forventetResultatkode = Resultatkode.DELT_BOSTED
        forventetResultatbeløp = BigDecimal.valueOf(2000).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(2000).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(3000).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = true
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 3 - Bidrag ikke beregnet delt bosted")
    fun testEndeligBidrag_Eksempel03() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel3.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_IKKE_BEREGNET_DELT_BOSTED
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.ZERO.setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(3000).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.ZERO.setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = true
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 0
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 4 - Bidrag satt til barnetillegg BP")
    fun testEndeligBidrag_Eksempel04() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel4.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2001).setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_SATT_TIL_BARNETILLEGG_BP
        forventetResultatbeløp = BigDecimal.valueOf(2000).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5001).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(3000).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(9000).setScale(2)
        forventetJustertNedTilEvne = true
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = true
        forventetJustertForNettoBarnetilleggBM = true
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 5A - Bidrag satt til underholdskostnad minus barnetillegg BM")
    fun testEndeligBidrag_Eksempel05A() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel5A.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(1001).setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_SATT_TIL_UNDERHOLDSKOSTNAD_MINUS_BARNETILLEGG_BM
        forventetResultatbeløp = BigDecimal.valueOf(1000).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5101).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(1000).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(8100).setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = true
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = true
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 5B - Bidrag satt til underholdskostnad minus barnetillegg BM. Netto barnetillegg BM > U")
    fun testEndeligBidrag_Eksempel05B() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel5B.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_SATT_TIL_UNDERHOLDSKOSTNAD_MINUS_BARNETILLEGG_BM
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5101).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.ZERO.setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(10800).setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = true
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = true
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 6 - Bidrag redusert til 25 prosent av inntekt")
    fun testEndeligBidrag_Eksempel06() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel6.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2501).setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT
        forventetResultatbeløp = BigDecimal.valueOf(2500).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5001).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(1500).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(2500).setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = true
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 7 - Bidrag redusert av evne")
    fun testEndeligBidrag_Eksempel07() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel7.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2501).setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_REDUSERT_AV_EVNE
        forventetResultatbeløp = BigDecimal.valueOf(2500).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5001).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(1500).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(2500).setScale(2)
        forventetJustertNedTilEvne = true
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false
        forventetAntallBarnetilleggBP = 1
        forventetAntallBarnetilleggBM = 1
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 8 - Kostnadsberegnet bidrag")
    fun testEndeligBidrag_Eksempel8() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidrag_eksempel8.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(5001).setScale(2)
        forventetResultatkode = Resultatkode.KOSTNADSBEREGNET_BIDRAG
        forventetResultatbeløp = BigDecimal.valueOf(5000).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(5001).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(1500).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(2500).setScale(2)
        forventetJustertNedTilEvne = false
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false
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
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat)

        val endeligBidragResultatListe = endeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningBarnebidrag(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatKode = it.innhold.resultatKode,
                    resultatBeløp = it.innhold.resultatBeløp,
                    kostnadsberegnetBidrag = it.innhold.kostnadsberegnetBidrag,
                    nettoBarnetilleggBP = it.innhold.nettoBarnetilleggBP,
                    nettoBarnetilleggBM = it.innhold.nettoBarnetilleggBM,
                    justertNedTilEvne = it.innhold.justertNedTilEvne,
                    justertNedTil25ProsentAvInntekt = it.innhold.justertNedTil25ProsentAvInntekt,
                    justertForNettoBarnetilleggBP = it.innhold.justertForNettoBarnetilleggBP,
                    justertForNettoBarnetilleggBM = it.innhold.justertForNettoBarnetilleggBM,
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
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(forventetBeregnetBeløp) },
            { assertThat(endeligBidragResultatListe[0].resultatKode).isEqualTo(forventetResultatkode) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(forventetResultatbeløp) },
            { assertThat(endeligBidragResultatListe[0].kostnadsberegnetBidrag).isEqualTo(forventetKostnadsberegnetBidrag) },
            { assertThat(endeligBidragResultatListe[0].nettoBarnetilleggBP).isEqualTo(forventetNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].nettoBarnetilleggBM).isEqualTo(forventetNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].justertNedTilEvne).isEqualTo(forventetJustertNedTilEvne) },
            { assertThat(endeligBidragResultatListe[0].justertNedTil25ProsentAvInntekt).isEqualTo(forventetJustertNedTil25ProsentAvInntekt) },
            { assertThat(endeligBidragResultatListe[0].justertForNettoBarnetilleggBP).isEqualTo(forventetJustertForNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].justertForNettoBarnetilleggBM).isEqualTo(forventetJustertForNettoBarnetilleggBM) },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(antallGrunnlag) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(antallGrunnlag) },
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
        val alleRefererteReferanser = hentAlleRefererteReferanser(endeligBidragResultat)

        val endeligBidragResultatListe = endeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningBarnebidrag(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatKode = it.innhold.resultatKode,
                    resultatBeløp = it.innhold.resultatBeløp,
                    kostnadsberegnetBidrag = it.innhold.kostnadsberegnetBidrag,
                    nettoBarnetilleggBP = it.innhold.nettoBarnetilleggBP,
                    nettoBarnetilleggBM = it.innhold.nettoBarnetilleggBM,
                    justertNedTilEvne = it.innhold.justertNedTilEvne,
                    justertNedTil25ProsentAvInntekt = it.innhold.justertNedTil25ProsentAvInntekt,
                    justertForNettoBarnetilleggBP = it.innhold.justertForNettoBarnetilleggBP,
                    justertForNettoBarnetilleggBM = it.innhold.justertForNettoBarnetilleggBM,
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
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-04")) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].resultatKode).isEqualTo(Resultatkode.BARNET_ER_SELVFORSØRGET) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[0].kostnadsberegnetBidrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].nettoBarnetilleggBM).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[0].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[0].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[0].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[0].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-06")) },
            { assertThat(endeligBidragResultatListe[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(9001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].resultatKode).isEqualTo(Resultatkode.BIDRAG_REDUSERT_AV_EVNE) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(9000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[1].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(11001).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(3000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(9000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[1].justertNedTilEvne).isTrue },
            { assertThat(endeligBidragResultatListe[1].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[1].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[1].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-10")) },
            { assertThat(endeligBidragResultatListe[2].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].resultatKode).isEqualTo(Resultatkode.BIDRAG_REDUSERT_AV_EVNE) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[2].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(11101).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(9000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[2].justertNedTilEvne).isTrue },
            { assertThat(endeligBidragResultatListe[2].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[2].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[2].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-10", "2024-01")) },
            { assertThat(endeligBidragResultatListe[3].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].resultatKode).isEqualTo(Resultatkode.BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT) },
            { assertThat(endeligBidragResultatListe[3].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[3].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(7101).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(9000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[3].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[3].justertNedTil25ProsentAvInntekt).isTrue },
            { assertThat(endeligBidragResultatListe[3].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[3].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertThat(endeligBidragResultatListe[4].beregnetBeløp).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].resultatKode).isEqualTo(Resultatkode.BIDRAG_IKKE_BEREGNET_DELT_BOSTED) },
            { assertThat(endeligBidragResultatListe[4].resultatBeløp).isEqualTo(BigDecimal.ZERO.setScale(0)) },
            { assertThat(endeligBidragResultatListe[4].kostnadsberegnetBidrag).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(2500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[4].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[4].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[4].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[4].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-05")) },
            { assertThat(endeligBidragResultatListe[5].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2601).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].resultatKode).isEqualTo(Resultatkode.BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT) },
            { assertThat(endeligBidragResultatListe[5].resultatBeløp).isEqualTo(BigDecimal.valueOf(2600).setScale(0)) },
            { assertThat(endeligBidragResultatListe[5].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(11101).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBarnetilleggBP).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(2500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[5].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[5].justertNedTil25ProsentAvInntekt).isTrue },
            { assertThat(endeligBidragResultatListe[5].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[5].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[6].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-08")) },
            { assertThat(endeligBidragResultatListe[6].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].resultatKode).isEqualTo(Resultatkode.DELT_BOSTED) },
            { assertThat(endeligBidragResultatListe[6].resultatBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[6].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(1500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(2500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[6].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[6].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[6].justertForNettoBarnetilleggBP).isFalse },
            { assertThat(endeligBidragResultatListe[6].justertForNettoBarnetilleggBM).isFalse },

            { assertThat(endeligBidragResultatListe[7].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(endeligBidragResultatListe[7].beregnetBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].resultatKode).isEqualTo(Resultatkode.DELT_BOSTED) },
            { assertThat(endeligBidragResultatListe[7].resultatBeløp).isEqualTo(BigDecimal.valueOf(1000).setScale(0)) },
            { assertThat(endeligBidragResultatListe[7].kostnadsberegnetBidrag).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(1500).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].nettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(10800).setScale(2)) },
            { assertThat(endeligBidragResultatListe[7].justertNedTilEvne).isFalse },
            { assertThat(endeligBidragResultatListe[7].justertNedTil25ProsentAvInntekt).isFalse },
            { assertThat(endeligBidragResultatListe[7].justertForNettoBarnetilleggBP).isTrue },
            { assertThat(endeligBidragResultatListe[7].justertForNettoBarnetilleggBM).isTrue },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(forventetAntallDelberegningBidragsevne) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(forventetAntallDelberegningUnderholdskostnad) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(forventetAntallDelberegningBPAndelUnderholdskostnad) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(forventetAntallDelberegningSamværsfradrag) },
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
