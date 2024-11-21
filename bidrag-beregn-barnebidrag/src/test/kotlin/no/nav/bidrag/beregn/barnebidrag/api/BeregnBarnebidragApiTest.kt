package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
internal class BeregnBarnebidragApiTest {
    private lateinit var filnavn: String

    // Beregningsperiode
    private lateinit var forventetBeregningsperiode: ÅrMånedsperiode

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

    // Underholdskostnad
    private lateinit var forventetUnderholdskostnad: BigDecimal

    // BP andel underholdskostnad
    private lateinit var forventetEndeligAndelFaktor: BigDecimal
    private lateinit var forventetAndelBeløp: BigDecimal
    private lateinit var forventetBeregnetAndelFaktor: BigDecimal
    private lateinit var forventetBarnEndeligInntekt: BigDecimal
    private var forventetBarnetErSelvforsørgetBp: Boolean = false

    // Samværsfradrag
    private lateinit var forventetSamværsfradrag: BigDecimal

    // Endelig bidrag
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
    private var forventetBarnetErSelvforsørgetEb: Boolean = false
    private var forventetBidragJustertForDeltBosted: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBP: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBM: Boolean = false
    private var forventetBidragJustertNedTilEvne: Boolean = false
    private var forventetBidragJustertNedTil25ProsentAvInntekt: Boolean = false
    private var forventetAntallBarnetilleggBP: Int = 1
    private var forventetAntallBarnetilleggBM: Int = 1

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @Disabled
    @DisplayName("Barnebidrag - eksempel X")
    fun testBarnebidrag_EksempelX() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempelX.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2024-08"), null)

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

        // Underholdskostnad
        forventetUnderholdskostnad = BigDecimal.valueOf(9000.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(7021.38).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900.00).setScale(2)
        forventetBarnetErSelvforsørgetBp = false

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(547.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3496.53).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3500).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(6500).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(7047).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6500).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(4043.53).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(4043.53).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3496.53).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørgetEb = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true
        forventetBidragJustertNedTil25ProsentAvInntekt = false

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 1 - kostnadsberegnet bidrag")
    fun testBarnebidrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel1.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Bidragsevne
        forventetBidragsevne = BigDecimal.valueOf(16357.14).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(87450.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(79475.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(11711.30).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(41000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(132186.30).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2643726000).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(10416.67).setScale(2)

        // Underholdskostnad
        forventetUnderholdskostnad = BigDecimal.valueOf(5999.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørgetBp = false

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørgetEb = false
        forventetBidragJustertForDeltBosted = false
        forventetBidragJustertForNettoBarnetilleggBP = false
        forventetBidragJustertForNettoBarnetilleggBM = false
        forventetBidragJustertNedTilEvne = false
        forventetBidragJustertNedTil25ProsentAvInntekt = false

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

        val underholdskostnadResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
            .map {
                DelberegningUnderholdskostnad(
                    periode = it.innhold.periode,
                    forbruksutgift = it.innhold.forbruksutgift,
                    boutgift = it.innhold.boutgift,
                    barnetilsynMedStønad = it.innhold.barnetilsynMedStønad,
                    nettoTilsynsutgift = it.innhold.nettoTilsynsutgift,
                    barnetrygd = it.innhold.barnetrygd,
                    underholdskostnad = it.innhold.underholdskostnad,
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
                    resultatBeløp = it.innhold.resultatBeløp,
                    uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                    bruttoBidragEtterBarnetilleggBM = it.innhold.bruttoBidragEtterBarnetilleggBM,
                    nettoBidragEtterBarnetilleggBM = it.innhold.nettoBidragEtterBarnetilleggBM,
                    bruttoBidragJustertForEvneOg25Prosent = it.innhold.bruttoBidragJustertForEvneOg25Prosent,
                    bruttoBidragEtterBarnetilleggBP = it.innhold.bruttoBidragEtterBarnetilleggBP,
                    nettoBidragEtterSamværsfradrag = it.innhold.nettoBidragEtterSamværsfradrag,
                    bpAndelAvUVedDeltBostedFaktor = it.innhold.bpAndelAvUVedDeltBostedFaktor,
                    bpAndelAvUVedDeltBostedBeløp = it.innhold.bpAndelAvUVedDeltBostedBeløp,
                    ingenEndringUnderGrense = false,
                    barnetErSelvforsørget = false,
                    bidragJustertForDeltBosted = false,
                    bidragJustertForNettoBarnetilleggBP = false,
                    bidragJustertForNettoBarnetilleggBM = false,
                    bidragJustertNedTilEvne = false,
                    bidragJustertNedTil25ProsentAvInntekt = false,
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

        val antallBarnetilleggBM = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallBarnetilleggBP = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
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
            { assertThat(underholdskostnadResultatListe).isNotNull },
            { assertThat(underholdskostnadResultatListe).hasSize(1) },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(1) },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(1) },
            { assertThat(endeligBidragResultatListe).isNotNull },
            { assertThat(endeligBidragResultatListe).hasSize(1) },

            // Resultat
            // Bidragsevne
            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(forventetBeregningsperiode) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(forventetBidragsevne) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(forventetMinstefradrag) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(forventetSkattAlminneligInntekt) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(forventetTrygdeavgift) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(forventetTrinnskatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(forventetSumSkatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkattFaktor).isEqualTo(forventetSumSkattFaktor) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(forventetUnderholdBarnEgenHusstand) },
            { assertThat(bidragsevneResultatListe[0].sumInntekt25Prosent).isEqualTo(forventetSumInntekt25Prosent) },

            // Underholdskostnad
            { assertThat(underholdskostnadResultatListe[0].periode).isEqualTo(forventetBeregningsperiode) },
            { assertThat(underholdskostnadResultatListe[0].underholdskostnad).isEqualTo(forventetUnderholdskostnad) },

            // BP andel underholdskostnad
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(forventetBeregningsperiode) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor).isEqualTo(forventetEndeligAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp).isEqualTo(forventetAndelBeløp) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor).isEqualTo(forventetBeregnetAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt).isEqualTo(forventetBarnEndeligInntekt) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørgetBp) },

            // Samværsfradrag
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(forventetBeregningsperiode) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(forventetSamværsfradrag) },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(forventetBeregningsperiode) },
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
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørgetBp) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isEqualTo(forventetBidragJustertForDeltBosted) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(forventetBidragJustertForNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isEqualTo(forventetBidragJustertForNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isEqualTo(forventetBidragJustertNedTilEvne) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isEqualTo(forventetBidragJustertNedTil25ProsentAvInntekt) },

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
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },
            { assertThat(antallSjablonSjablontall).isEqualTo(15) },
            { assertThat(antallSjablonBidragsevne).isEqualTo(1) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(1) },
            { assertThat(antallSjablonSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Beregn månedsbeløp faktisk utgift og tilleggsstønad")
    fun testBeregnMånedsbeløpFaktiskUtgiftTilleggsstønad() {
        val faktiskUtgift = BigDecimal.valueOf(1000)
        val kostpenger = BigDecimal.valueOf(400)
        val responseFaktiskUtgift = beregnBarnebidragService.beregnMånedsbeløpFaktiskUtgift(faktiskUtgift, kostpenger)

        val tilleggsstønad = BigDecimal.valueOf(17)
        val responseTilleggsstønad = beregnBarnebidragService.beregnMånedsbeløpTilleggsstønad(tilleggsstønad)

        assertThat(responseFaktiskUtgift).isEqualByComparingTo(BigDecimal.valueOf(550))
        assertThat(responseTilleggsstønad).isEqualByComparingTo(BigDecimal.valueOf(368.33))

        // Test uten angitt kostpenger, default er BigDecimal.ZERO
        val faktiskUtgift2 = BigDecimal.valueOf(500)
        val responseFaktiskUtgift2 = beregnBarnebidragService.beregnMånedsbeløpFaktiskUtgift(faktiskUtgift2)
        assertThat(responseFaktiskUtgift2).isEqualByComparingTo(BigDecimal.valueOf(458.33))
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
