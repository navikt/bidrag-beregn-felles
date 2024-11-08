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
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
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
internal class BeregnBarnebidragApiTest {
    private lateinit var filnavn: String

    // Bidragsevne
    private lateinit var forventetBidragsevne: BigDecimal
    private lateinit var forventetMinstefradrag: BigDecimal
    private lateinit var forventetSkattAlminneligInntekt: BigDecimal
    private lateinit var forventetTrinnskatt: BigDecimal
    private lateinit var forventetTrygdeavgift: BigDecimal
    private lateinit var forventetSumSkatt: BigDecimal
    private lateinit var forventetSumSkattFaktor: BigDecimal
    private lateinit var forventetUnderholdBarnEgenHusstand: BigDecimal
    private lateinit var forventetSumInntekt25Prosent: BigDecimal

    // BP andel underholdskostnad
    private lateinit var forventetEndeligAndelFaktor: BigDecimal
    private lateinit var forventetAndelBeløp: BigDecimal
    private lateinit var forventetBeregnetAndelFaktor: BigDecimal
    private lateinit var forventetBarnEndeligInntekt: BigDecimal
    private var forventetBarnetErSelvforsørget: Boolean = false

    // Samværsfradrag
    private lateinit var forventetSamværsfradrag: BigDecimal

    // Endelig bidrag
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

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 1")
    fun testBarnebidrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel1.json"

        // Bidragsevne
        forventetBidragsevne = BigDecimal.valueOf(4043.53).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(71610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(9727.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(39000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(120337.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2406752000).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(10416.67).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(7021.38).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900.00).setScale(2)
        forventetBarnetErSelvforsørget = false

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(547.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3496.53).setScale(2)
        forventetResultatkode = Resultatkode.BIDRAG_REDUSERT_AV_EVNE
        forventetResultatbeløp = BigDecimal.valueOf(3500).setScale(0)
        forventetKostnadsberegnetBidrag = BigDecimal.valueOf(6474.38).setScale(2)
        forventetNettoBarnetilleggBP = BigDecimal.valueOf(1500).setScale(2)
        forventetNettoBarnetilleggBM = BigDecimal.valueOf(2500).setScale(2)
        forventetJustertNedTilEvne = true
        forventetJustertNedTil25ProsentAvInntekt = false
        forventetJustertForNettoBarnetilleggBP = false
        forventetJustertForNettoBarnetilleggBM = false

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    private fun utførBeregningerOgEvaluerResultatBarnebidrag() {
        val request = lesFilOgByggRequest(filnavn)
        val barnebidragResultat = beregnBarnebidragService.beregnBarnebidrag(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(barnebidragResultatGrunnlagListe)

        val bidragsevneResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragsevne>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE)
            .map {
                DelberegningBidragsevne(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                    skatt = it.innhold.skatt,
                    underholdBarnEgenHusstand = it.innhold.underholdBarnEgenHusstand,
                    sumInntekt25Prosent = it.innhold.sumInntekt25Prosent,
                )
            }

        val bpAndelUnderholdskostnadResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                DelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                    andelBeløp = it.innhold.andelBeløp,
                    beregnetAndelFaktor = it.innhold.beregnetAndelFaktor,
                    barnEndeligInntekt = it.innhold.barnEndeligInntekt,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                )
            }

        val samværsfradragResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
            .map {
                DelberegningSamværsfradrag(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }

        val endeligBidragResultatListe = barnebidragResultatGrunnlagListe
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

        val referanseSB = request.søknadsbarnReferanse

        val antallInntektRapporteringPeriodeBP = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallInntektRapporteringPeriodeBM = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallInntektRapporteringPeriodeSB = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallDelberegningSumInntektPeriodeBP = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningSumInntektPeriodeBM = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallDelberegningSumInntektPeriodeSB = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallDelberegningBoforholdPeriode = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BOFORHOLD }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBarnIHusstandPeriode = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningVoksneIHusstandPeriode = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBidragsevne = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        val antallDelberegningUnderholdskostnad = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallBostatusPeriodeBP = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallBostatusPeriodeSB = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallUnderholdskostnad = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallSamværsklasse = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallDeltBosted = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELT_BOSTED }
            .size

        val antallBarnetilleggBM = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.BARNETILLEGG_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallBarnetilleggBP = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.BARNETILLEGG_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallSjablonSjablontall = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        val antallSjablonBidragsevne = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SJABLON_BIDRAGSEVNE }
            .size

        val antallSjablonTrinnvisSkattesats = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
            .size

        val antallSjablonSamværsfradrag = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SJABLON_SAMVARSFRADRAG }
            .size

        assertAll(
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
            { assertThat(bidragsevneResultatListe).hasSize(1) },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(1) },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(1) },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(1) },

            // Resultat
            // Bidragsevne
            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(forventetBidragsevne) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(forventetMinstefradrag) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(forventetSkattAlminneligInntekt) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(forventetTrygdeavgift) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(forventetTrinnskatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(forventetSumSkatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkattFaktor).isEqualTo(forventetSumSkattFaktor) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(forventetUnderholdBarnEgenHusstand) },
            { assertThat(bidragsevneResultatListe[0].sumInntekt25Prosent).isEqualTo(forventetSumInntekt25Prosent) },

            // BP andel underholdskostnad
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor).isEqualTo(forventetEndeligAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp).isEqualTo(forventetAndelBeløp) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor).isEqualTo(forventetBeregnetAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt).isEqualTo(forventetBarnEndeligInntekt) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørget) },

            // Samværsfradrag
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(forventetSamværsfradrag) },

            // Endelig bidrag
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
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(1) },
            { assertThat(antallInntektRapporteringPeriodeBM).isEqualTo(1) },
            { assertThat(antallInntektRapporteringPeriodeSB).isEqualTo(1) },
            { assertThat(antallDelberegningSumInntektPeriodeBP).isEqualTo(1) },
            { assertThat(antallDelberegningSumInntektPeriodeBM).isEqualTo(1) },
            { assertThat(antallDelberegningSumInntektPeriodeSB).isEqualTo(1) },
            { assertThat(antallDelberegningBoforholdPeriode).isEqualTo(1) },
            { assertThat(antallDelberegningBarnIHusstandPeriode).isEqualTo(1) },
            { assertThat(antallDelberegningVoksneIHusstandPeriode).isEqualTo(1) },
            { assertThat(antallDelberegningBidragsevne).isEqualTo(1) },
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(1) },
            { assertThat(antallBostatusPeriodeBP).isEqualTo(1) },
            { assertThat(antallBostatusPeriodeSB).isEqualTo(1) },
            { assertThat(antallUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallSamværsklasse).isEqualTo(1) },
            { assertThat(antallDeltBosted).isEqualTo(1) },
            { assertThat(antallBarnetilleggBP).isEqualTo(1) },
            { assertThat(antallBarnetilleggBM).isEqualTo(1) },
            { assertThat(antallSjablonSjablontall).isEqualTo(13) },
            { assertThat(antallSjablonBidragsevne).isEqualTo(1) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(1) },
            { assertThat(antallSjablonSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

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
