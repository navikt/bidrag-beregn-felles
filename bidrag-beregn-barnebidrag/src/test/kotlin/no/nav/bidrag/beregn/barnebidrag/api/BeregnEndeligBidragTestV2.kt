package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.beregn.barnebidrag.service.beregning.BeregnEndeligBidragServiceV2
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningAndelAvBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragJustertForBPBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordelingLøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndeligBidragBeregnet
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEvne25ProsentAvInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnEndeligBidragTestV2 : FellesTest() {
    private lateinit var filnavn: String
    private lateinit var filnavnLøpendeBidrag: String

    // BidragTilFordeling
    private lateinit var forventetUMinusNettoBarnetilleggBM: BigDecimal
    private lateinit var forventetBPAndelAvUMinusSamværsfradrag: BigDecimal
    private lateinit var forventetBidragTilFordeling: BigDecimal

    // SumBidragTilFordeling
    private lateinit var forventetSumBidragTilFordeling: BigDecimal
    private var forventetSumPrioriterteBidragTilFordeling: BigDecimal = BigDecimal.ZERO
    private var forventetErKompletteGrunnlagForAlleLøpendeBidrag: Boolean = true
    private var forventetAntallDelberegningBidragTilFordeling: Int = 1
    private var forventetAntallDelberegningBidragTilFordelingLøpendeBidrag: Int = 0

    // Evne25ProsentAvInntekt
    private lateinit var forventetEvneJustertFor25ProsentAvInntekt: BigDecimal
    private var forventetErEvneJustertNedTil25ProsentAvInntekt: Boolean = false

    // AndelAvBidragsevne
    private lateinit var forventetAndelAvSumBidragTilFordelingFaktor: BigDecimal
    private lateinit var forventetAndelAvEvneBeløp: BigDecimal
    private lateinit var forventetBidragEtterFordeling: BigDecimal
    private var forventetHarBPFullEvne: Boolean = true
    private var forventetAntallDelberegningEvne25ProsentAvInntekt: Int = 1

    // BidragTilFordeling
    private lateinit var forventetReduksjonUnderholdskostnad: BigDecimal
    private lateinit var forventetBidragTilFordelingLøpendeBidrag: BigDecimal

    // BidragJustertForNettoBarnetilleggBP
    private lateinit var forventetBidragJustertForNettoBarnetilleggBP: BigDecimal
    private var forventetErBidragJustertTilNettoBarnetilleggBP: Boolean = false
    private var forventetAntallDelberegningNettoBarnetilleggBP: Int = 1

    // EndeligBidragBeregnet
    private lateinit var forventetBeregnetBeløp: BigDecimal
    private lateinit var forventetResultatBeløp: BigDecimal

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
    }

    @Test
    @DisplayName("Bidrag til fordeling - eksempel 1")
    fun testBidragTilFordeling_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/bidragtilfordeling_eksempel1.json"
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(17000).setScale(2)
        forventetBPAndelAvUMinusSamværsfradrag = BigDecimal.valueOf(11000).setScale(2)
        forventetBidragTilFordeling = BigDecimal.valueOf(12000).setScale(2)
        utførBeregningerOgEvaluerResultatBidragTilFordeling()
    }

    private fun utførBeregningerOgEvaluerResultatBidragTilFordeling() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningBidragTilFordeling(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragTilFordeling>(Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING)
            .map {
                DelberegningBidragTilFordeling(
                    periode = it.innhold.periode,
                    bidragTilFordeling = it.innhold.bidragTilFordeling,
                    uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                    bpAndelAvUMinusSamværsfradrag = it.innhold.bpAndelAvUMinusSamværsfradrag,
                )
            }

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val antallDelberegningUnderholdskostnad = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallDelberegningBPAndelUnderholdskostnad = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL }
            .size

        val antallDelberegningSamværsfradrag = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        val antallDelberegningNettoBarnetilleggBM = resultat
            .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningNettoBarnetillegg>(
                grunnlagType = Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG,
                referanse = referanseBM,
            )
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].bidragTilFordeling).isEqualTo(forventetBidragTilFordeling) },
            { assertThat(resultatListe[0].uMinusNettoBarnetilleggBM).isEqualTo(forventetUMinusNettoBarnetilleggBM) },
            { assertThat(resultatListe[0].bpAndelAvUMinusSamværsfradrag).isEqualTo(forventetBPAndelAvUMinusSamværsfradrag) },

            // Grunnlag
            { assertThat(antallDelberegningUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningBPAndelUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallDelberegningNettoBarnetilleggBM).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Sum bidrag til fordeling - eksempel 1 - 2 søknadsbarn")
    fun testSumBidragTilFordeling_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/sumbidragtilfordeling_eksempel1.json"
        forventetSumBidragTilFordeling = BigDecimal.valueOf(20000).setScale(2)
        forventetSumPrioriterteBidragTilFordeling = BigDecimal.ZERO.setScale(2)
        forventetErKompletteGrunnlagForAlleLøpendeBidrag = true
        forventetAntallDelberegningBidragTilFordeling = 2
        forventetAntallDelberegningBidragTilFordelingLøpendeBidrag = 0
        utførBeregningerOgEvaluerResultatSumBidragTilFordeling()
    }

    @Test
    @DisplayName("Sum bidrag til fordeling - eksempel 2 - 1 søknadsbarn og 1 løpende bidrag")
    fun testSumBidragTilFordeling_Eksempel2() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/sumbidragtilfordeling_eksempel2.json"
        forventetSumBidragTilFordeling = BigDecimal.valueOf(22000).setScale(2)
        forventetSumPrioriterteBidragTilFordeling = BigDecimal.ZERO.setScale(2)
        forventetErKompletteGrunnlagForAlleLøpendeBidrag = false
        forventetAntallDelberegningBidragTilFordeling = 1
        forventetAntallDelberegningBidragTilFordelingLøpendeBidrag = 1
        utførBeregningerOgEvaluerResultatSumBidragTilFordeling()
    }

    private fun utførBeregningerOgEvaluerResultatSumBidragTilFordeling() {
        val request: List<BeregnGrunnlag> = lesFilOgByggRequestGenerisk(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningSumBidragTilFordeling(
            mottattGrunnlagListe = request,
        )
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSumBidragTilFordeling>(Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING)
            .map {
                DelberegningSumBidragTilFordeling(
                    periode = it.innhold.periode,
                    sumBidragTilFordeling = it.innhold.sumBidragTilFordeling,
                    sumPrioriterteBidragTilFordeling = it.innhold.sumPrioriterteBidragTilFordeling,
                    erKompletteGrunnlagForAlleLøpendeBidrag = it.innhold.erKompletteGrunnlagForAlleLøpendeBidrag,
                )
            }

        val antallDelberegningBidragTilFordeling = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING }
            .size

        val antallDelberegningBidragTilFordelingLøpendeBidrag = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG }
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].sumBidragTilFordeling).isEqualTo(forventetSumBidragTilFordeling) },
            { assertThat(resultatListe[0].sumPrioriterteBidragTilFordeling).isEqualTo(forventetSumPrioriterteBidragTilFordeling) },
            {
                assertThat(resultatListe[0].erKompletteGrunnlagForAlleLøpendeBidrag).isEqualTo(
                    forventetErKompletteGrunnlagForAlleLøpendeBidrag,
                )
            },

            // Grunnlag
            { assertThat(antallDelberegningBidragTilFordeling).isEqualTo(forventetAntallDelberegningBidragTilFordeling) },
            {
                assertThat(antallDelberegningBidragTilFordelingLøpendeBidrag).isEqualTo(
                    forventetAntallDelberegningBidragTilFordelingLøpendeBidrag,
                )
            },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Evne 25 prosent av inntekt - eksempel 1 - evne er lavere enn 25 prosent av inntekt")
    fun testEvne25ProsentAvInntekt_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/evne25ProsentAvInntekt_eksempel1.json"
        forventetEvneJustertFor25ProsentAvInntekt = BigDecimal.valueOf(2500).setScale(2)
        forventetErEvneJustertNedTil25ProsentAvInntekt = false
        utførBeregningerOgEvaluerResultatEvne25ProsentAvInntekt()
    }

    @Test
    @DisplayName("Sum bidrag til fordeling - eksempel 2 - evne er høyere enn 25 prosent av inntekt")
    fun testEvne25ProsentAvInntekt_Eksempel2() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/evne25ProsentAvInntekt_eksempel2.json"
        forventetEvneJustertFor25ProsentAvInntekt = BigDecimal.valueOf(2000).setScale(2)
        forventetErEvneJustertNedTil25ProsentAvInntekt = true
        utførBeregningerOgEvaluerResultatEvne25ProsentAvInntekt()
    }

    private fun utførBeregningerOgEvaluerResultatEvne25ProsentAvInntekt() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningEvne25ProsentAvInntekt(
            mottattGrunnlag = request,
        )
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEvne25ProsentAvInntekt>(Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT)
            .map {
                DelberegningEvne25ProsentAvInntekt(
                    periode = it.innhold.periode,
                    evneJustertFor25ProsentAvInntekt = it.innhold.evneJustertFor25ProsentAvInntekt,
                    erEvneJustertNedTil25ProsentAvInntekt = it.innhold.erEvneJustertNedTil25ProsentAvInntekt,
                )
            }

        val antallDelberegningBidragsevne = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].evneJustertFor25ProsentAvInntekt).isEqualTo(forventetEvneJustertFor25ProsentAvInntekt) },
            { assertThat(resultatListe[0].erEvneJustertNedTil25ProsentAvInntekt).isEqualTo(forventetErEvneJustertNedTil25ProsentAvInntekt) },

            // Grunnlag
            { assertThat(antallDelberegningBidragsevne).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Andel av bidragsevne - eksempel 1 - søknadsbarn")
    fun testAndelAvBidragsevne_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/andelavbidragsevne_eksempel1.json"
        forventetAndelAvSumBidragTilFordelingFaktor = BigDecimal.valueOf(0.5333333333).setScale(10)
        forventetAndelAvEvneBeløp = BigDecimal.valueOf(3200).setScale(2)
        forventetBidragEtterFordeling = BigDecimal.valueOf(3200).setScale(2)
        forventetHarBPFullEvne = false
        forventetAntallDelberegningEvne25ProsentAvInntekt = 1
        utførBeregningerOgEvaluerResultatAndelAvBidragsevne()
    }

    private fun utførBeregningerOgEvaluerResultatAndelAvBidragsevne() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningAndelAvBidragsevne(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val andelAvBidragsevneResultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningAndelAvBidragsevne>(Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE)
            .map {
                DelberegningAndelAvBidragsevne(
                    periode = it.innhold.periode,
                    andelAvSumBidragTilFordelingFaktor = it.innhold.andelAvSumBidragTilFordelingFaktor,
                    andelAvEvneBeløp = it.innhold.andelAvEvneBeløp,
                    bidragEtterFordeling = it.innhold.bidragEtterFordeling,
                    harBPFullEvne = it.innhold.harBPFullEvne,
                )
            }

        val antallDelberegningEvne25ProsentAvInntekt = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT }
            .size

        val antallDelberegningSumBidragTilFordeling = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING }
            .size

        val antallDelberegningBidragTilFordeling = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING }
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(andelAvBidragsevneResultatListe).isNotNull },
            { assertThat(andelAvBidragsevneResultatListe).hasSize(1) },

            // Resultat
            { assertThat(andelAvBidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            {
                assertThat(andelAvBidragsevneResultatListe[0].andelAvSumBidragTilFordelingFaktor).isEqualTo(
                    forventetAndelAvSumBidragTilFordelingFaktor,
                )
            },
            { assertThat(andelAvBidragsevneResultatListe[0].andelAvEvneBeløp).isEqualTo(forventetAndelAvEvneBeløp) },
            { assertThat(andelAvBidragsevneResultatListe[0].bidragEtterFordeling).isEqualTo(forventetBidragEtterFordeling) },
            { assertThat(andelAvBidragsevneResultatListe[0].harBPFullEvne).isEqualTo(forventetHarBPFullEvne) },

            // Grunnlag
            { assertThat(antallDelberegningEvne25ProsentAvInntekt).isEqualTo(forventetAntallDelberegningEvne25ProsentAvInntekt) },
            { assertThat(antallDelberegningSumBidragTilFordeling).isEqualTo(1) },
            { assertThat(antallDelberegningBidragTilFordeling).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Bidrag til fordeling løpende bidrag - eksempel 1")
    fun testBidragTilFordelingLøpendeBidrag_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/bidragtilfordelingløpendebidrag_eksempel1.json"
        forventetReduksjonUnderholdskostnad = BigDecimal.valueOf(2000).setScale(2)
        forventetBidragTilFordelingLøpendeBidrag = BigDecimal.valueOf(12000).setScale(2)
        utførBeregningerOgEvaluerResultatBidragTilFordelingLøpendeBidrag()
    }

    private fun utførBeregningerOgEvaluerResultatBidragTilFordelingLøpendeBidrag() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningBidragTilFordelingLøpendeBidrag(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragTilFordelingLøpendeBidrag>(Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG)
            .map {
                DelberegningBidragTilFordelingLøpendeBidrag(
                    periode = it.innhold.periode,
                    reduksjonUnderholdskostnad = it.innhold.reduksjonUnderholdskostnad,
                    bidragTilFordeling = it.innhold.bidragTilFordeling,
                )
            }

        val antallLøpendeBidrag = resultat
            .filter { it.type == Grunnlagstype.LØPENDE_BIDRAG_PERIODE }
            .size

        val antallDelberegningSamværsfradrag = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].bidragTilFordeling).isEqualTo(forventetBidragTilFordelingLøpendeBidrag) },
            { assertThat(resultatListe[0].reduksjonUnderholdskostnad).isEqualTo(forventetReduksjonUnderholdskostnad) },

            // Grunnlag
            { assertThat(antallLøpendeBidrag).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Bidrag justert for BP barnetillegg - eksempel 1 - bidrag etter fordeling av evne > BP netto barnetillegg")
    fun testBidragJustertForBPBarnetillegg_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/bidragjustertforbpbarnetillegg_eksempel1.json"
        forventetBidragJustertForNettoBarnetilleggBP = BigDecimal.valueOf(3200).setScale(2)
        forventetErBidragJustertTilNettoBarnetilleggBP = false
        forventetAntallDelberegningNettoBarnetilleggBP = 1
        utførBeregningerOgEvaluerResultatBidragJustertForBPBarnetillegg()
    }

    @Test
    @DisplayName("Bidrag justert for BP barnetillegg - eksempel 2 - bidrag etter fordeling av evne < BP netto barnetillegg")
    fun testBidragJustertForBPBarnetillegg_Eksempel2() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/bidragjustertforbpbarnetillegg_eksempel2.json"
        forventetBidragJustertForNettoBarnetilleggBP = BigDecimal.valueOf(4000).setScale(2)
        forventetErBidragJustertTilNettoBarnetilleggBP = true
        forventetAntallDelberegningNettoBarnetilleggBP = 1
        utførBeregningerOgEvaluerResultatBidragJustertForBPBarnetillegg()
    }

    @Test
    @DisplayName("Bidrag justert for BP barnetillegg - eksempel 3 - BP barnetillegg eksisterer ikke")
    fun testBidragJustertForBPBarnetillegg_Eksempel3() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/bidragjustertforbpbarnetillegg_eksempel3.json"
        forventetBidragJustertForNettoBarnetilleggBP = BigDecimal.valueOf(3200).setScale(2)
        forventetErBidragJustertTilNettoBarnetilleggBP = false
        forventetAntallDelberegningNettoBarnetilleggBP = 0
        utførBeregningerOgEvaluerResultatBidragJustertForBPBarnetillegg()
    }

    private fun utførBeregningerOgEvaluerResultatBidragJustertForBPBarnetillegg() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningBidragJustertForBPBarnetillegg(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragJustertForBPBarnetillegg>(Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG)
            .map {
                DelberegningBidragJustertForBPBarnetillegg(
                    periode = it.innhold.periode,
                    bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                    erBidragJustertTilNettoBarnetilleggBP = it.innhold.erBidragJustertTilNettoBarnetilleggBP,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val antallDelberegningAndelAvBidragsevne = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE }
            .size

        val antallDelberegningNettoBarnetilleggBP = resultat
            .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningNettoBarnetillegg>(
                grunnlagType = Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG,
                referanse = referanseBP,
            )
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(forventetBidragJustertForNettoBarnetilleggBP) },
            { assertThat(resultatListe[0].erBidragJustertTilNettoBarnetilleggBP).isEqualTo(forventetErBidragJustertTilNettoBarnetilleggBP) },

            // Grunnlag
            { assertThat(antallDelberegningAndelAvBidragsevne).isEqualTo(1) },
            { assertThat(antallDelberegningNettoBarnetilleggBP).isEqualTo(forventetAntallDelberegningNettoBarnetilleggBP) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Endelig bidrag beregnet - eksempel 1 - beregnet bidrag > 0")
    fun testEndeligBidragBeregnet_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidragberegnet_eksempel1.json"
        forventetBeregnetBeløp = BigDecimal.valueOf(2200).setScale(2)
        forventetResultatBeløp = BigDecimal.valueOf(2200).setScale(0)
        utførBeregningerOgEvaluerResultatEndeligBidragBeregnet()
    }

    @Test
    @DisplayName("Endelig bidrag beregnet - eksempel 2 - beregnet bidrag < 0")
    fun testEndeligBidragBeregnet_Eksempel2() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidragberegnet_eksempel2.json"
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatBeløp = BigDecimal.ZERO.setScale(0)
        utførBeregningerOgEvaluerResultatEndeligBidragBeregnet()
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidragBeregnet() {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = BeregnEndeligBidragServiceV2.delberegningEndeligBidragBeregnet(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndeligBidragBeregnet>(Grunnlagstype.DELBEREGNING_ENDELIG_BIDRAG_BEREGNET)
            .map {
                DelberegningEndeligBidragBeregnet(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatBeløp = it.innhold.resultatBeløp,
                )
            }

        val antallDelberegningBidragJustertForBPBarnetillegg = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG }
            .size

        val antallDelberegningSamværsfradrag = resultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
            .size

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(resultatListe).isNotNull },
            { assertThat(resultatListe).hasSize(1) },

            // Resultat
            { assertThat(resultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(resultatListe[0].beregnetBeløp).isEqualTo(forventetBeregnetBeløp) },
            { assertThat(resultatListe[0].resultatBeløp).isEqualTo(forventetResultatBeløp) },

            // Grunnlag
            { assertThat(antallDelberegningBidragJustertForBPBarnetillegg).isEqualTo(1) },
            { assertThat(antallDelberegningSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    @Test
    @DisplayName("Endelig bidrag - eksempel 1 - kaller alle delberegninger som inngår i endelig bidrag")
    fun testEndeligBidrag_Eksempel1() {
        filnavn = "src/test/resources/testfiler/endeligbidrag/endeligbidragv2_søknadsbarn_eksempel1.json"
        filnavnLøpendeBidrag = "src/test/resources/testfiler/endeligbidrag/endeligbidragv2_løpendebidrag_eksempel1.json"
        utførBeregningerOgEvaluerResultatEndeligBidrag()
    }

    private fun utførBeregningerOgEvaluerResultatEndeligBidrag() {
        val requestSøknadsbarnGrunnlag: List<BeregnGrunnlag> = lesFilOgByggRequestGenerisk(filnavn)
        val requestLøpendeBidragGrunnlag: List<BeregnGrunnlag> = lesFilOgByggRequestGenerisk(filnavnLøpendeBidrag)
        val resultat = BeregnEndeligBidragServiceV2.delberegningEndeligBidragV2(
            grunnlagSøknadsbarnListe = requestSøknadsbarnGrunnlag,
            grunnlagLøpendeBidragListe = requestLøpendeBidragGrunnlag,
        )
        printJson(resultat)

        var teller = 0

        resultat.map { beregnGrunnlag ->
            teller++
            val alleReferanser = hentAlleReferanser(beregnGrunnlag.grunnlagListe)

            // TODO Denne er litt dirty
            val alleRefererteReferanser = hentAlleRefererteReferanser(beregnGrunnlag.grunnlagListe)
                .filterNot { it.contains("LØPENDE_BIDRAG") }
                .filterNot { it.contains("Søknadsbarn_0${teller + 1}") }
                .filterNot { it.contains("Søknadsbarn_0${teller - 1}") }

            val delberegningBidragTilFordelingListe = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragTilFordeling>(Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING)
                .map {
                    DelberegningBidragTilFordeling(
                        periode = it.innhold.periode,
                        bidragTilFordeling = it.innhold.bidragTilFordeling,
                        uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                        bpAndelAvUMinusSamværsfradrag = it.innhold.bpAndelAvUMinusSamværsfradrag,
                    )
                }

            val delberegningSumBidragTilFordelingListe = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSumBidragTilFordeling>(Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING)
                .map {
                    DelberegningSumBidragTilFordeling(
                        periode = it.innhold.periode,
                        sumBidragTilFordeling = it.innhold.sumBidragTilFordeling,
                        sumPrioriterteBidragTilFordeling = it.innhold.sumPrioriterteBidragTilFordeling,
                        erKompletteGrunnlagForAlleLøpendeBidrag = it.innhold.erKompletteGrunnlagForAlleLøpendeBidrag,
                    )
                }

            val delberegningEvne25ProsentAvInntektListe = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEvne25ProsentAvInntekt>(Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT)
                .map {
                    DelberegningEvne25ProsentAvInntekt(
                        periode = it.innhold.periode,
                        evneJustertFor25ProsentAvInntekt = it.innhold.evneJustertFor25ProsentAvInntekt,
                        erEvneJustertNedTil25ProsentAvInntekt = it.innhold.erEvneJustertNedTil25ProsentAvInntekt,
                    )
                }

            val delberegningAndelAvBidragsevneListe = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningAndelAvBidragsevne>(Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE)
                .map {
                    DelberegningAndelAvBidragsevne(
                        periode = it.innhold.periode,
                        andelAvSumBidragTilFordelingFaktor = it.innhold.andelAvSumBidragTilFordelingFaktor,
                        andelAvEvneBeløp = it.innhold.andelAvEvneBeløp,
                        bidragEtterFordeling = it.innhold.bidragEtterFordeling,
                        harBPFullEvne = it.innhold.harBPFullEvne,
                    )
                }

            val delberegningBidragJustertForBPBarnetillegg = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragJustertForBPBarnetillegg>(Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG)
                .map {
                    DelberegningBidragJustertForBPBarnetillegg(
                        periode = it.innhold.periode,
                        bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                        erBidragJustertTilNettoBarnetilleggBP = it.innhold.erBidragJustertTilNettoBarnetilleggBP,
                    )
                }

            val delberegningEndeligBidragBeregnet = beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndeligBidragBeregnet>(Grunnlagstype.DELBEREGNING_ENDELIG_BIDRAG_BEREGNET)
                .map {
                    DelberegningEndeligBidragBeregnet(
                        periode = it.innhold.periode,
                        beregnetBeløp = it.innhold.beregnetBeløp,
                        resultatBeløp = it.innhold.resultatBeløp,
                    )
                }

            // Delberegning Bidrag til fordeling
            if (teller == 1) {
                assertAll(
                    { assertThat(delberegningBidragTilFordelingListe).hasSize(1) },
                    { assertThat(delberegningBidragTilFordelingListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), null)) },
                    { assertThat(delberegningBidragTilFordelingListe[0].bidragTilFordeling).isEqualTo(BigDecimal.valueOf(4000).setScale(2)) },
                    { assertThat(delberegningBidragTilFordelingListe[0].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(4000).setScale(2)) },
                    {
                        assertThat(delberegningBidragTilFordelingListe[0].bpAndelAvUMinusSamværsfradrag).isEqualTo(
                            BigDecimal.valueOf(3500).setScale(2),
                        )
                    },
                )
            } else if (teller == 2) {
                assertAll(
                    { assertThat(delberegningBidragTilFordelingListe).hasSize(1) },
                    { assertThat(delberegningBidragTilFordelingListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), null)) },
                    { assertThat(delberegningBidragTilFordelingListe[0].bidragTilFordeling).isEqualTo(BigDecimal.valueOf(2400).setScale(2)) },
                    { assertThat(delberegningBidragTilFordelingListe[0].uMinusNettoBarnetilleggBM).isEqualTo(BigDecimal.valueOf(3000).setScale(2)) },
                    {
                        assertThat(delberegningBidragTilFordelingListe[0].bpAndelAvUMinusSamværsfradrag).isEqualTo(
                            BigDecimal.valueOf(2000).setScale(2),
                        )
                    },
                )
            }

            // Delberegning Sum bidrag til fordeling (samme resultat for begge søknadsbarn)
            assertAll(
                { assertThat(delberegningSumBidragTilFordelingListe).hasSize(2) },
                {
                    assertThat(delberegningSumBidragTilFordelingListe[0].periode).isEqualTo(
                        ÅrMånedsperiode(
                            YearMonth.parse("2025-03"),
                            YearMonth.parse("2025-06"),
                        ),
                    )
                },
                { assertThat(delberegningSumBidragTilFordelingListe[0].sumBidragTilFordeling).isEqualTo(BigDecimal.valueOf(10900).setScale(2)) },
                { assertThat(delberegningSumBidragTilFordelingListe[0].sumPrioriterteBidragTilFordeling).isEqualTo(BigDecimal.ZERO.setScale(2)) },
                { assertThat(delberegningSumBidragTilFordelingListe[0].erKompletteGrunnlagForAlleLøpendeBidrag).isEqualTo(false) },
                { assertThat(delberegningSumBidragTilFordelingListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                { assertThat(delberegningSumBidragTilFordelingListe[1].sumBidragTilFordeling).isEqualTo(BigDecimal.valueOf(7400).setScale(2)) },
                { assertThat(delberegningSumBidragTilFordelingListe[1].sumPrioriterteBidragTilFordeling).isEqualTo(BigDecimal.ZERO.setScale(2)) },
                { assertThat(delberegningSumBidragTilFordelingListe[1].erKompletteGrunnlagForAlleLøpendeBidrag).isFalse },
            )

            // Delberegning Evne 25 prosent av inntekt
            if (teller == 1) {
                assertAll(
                    { assertThat(delberegningEvne25ProsentAvInntektListe).hasSize(1) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), null)) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].evneJustertFor25ProsentAvInntekt).isEqualTo(BigDecimal.valueOf(8000).setScale(2)) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].erEvneJustertNedTil25ProsentAvInntekt).isFalse },
                )
            } else if (teller == 2) {
                assertAll(
                    { assertThat(delberegningEvne25ProsentAvInntektListe).hasSize(1) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), null)) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].evneJustertFor25ProsentAvInntekt).isEqualTo(BigDecimal.valueOf(8000).setScale(2)) },
                    { assertThat(delberegningEvne25ProsentAvInntektListe[0].erEvneJustertNedTil25ProsentAvInntekt).isTrue },
                )
            }

            // Delberegning Andel av bidragsevne
            if (teller == 1) {
                assertAll(
                    { assertThat(delberegningAndelAvBidragsevneListe).hasSize(2) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].bidragEtterFordeling).isEqualTo(BigDecimal.valueOf(2935.78).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].andelAvSumBidragTilFordelingFaktor).isEqualTo(BigDecimal.valueOf(0.3669724771).setScale(10)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].andelAvEvneBeløp).isEqualTo(BigDecimal.valueOf(2935.78).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].harBPFullEvne).isFalse },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].bidragEtterFordeling).isEqualTo(BigDecimal.valueOf(4000).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].andelAvSumBidragTilFordelingFaktor).isEqualTo(BigDecimal.valueOf(0.5405405405).setScale(10)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].andelAvEvneBeløp).isEqualTo(BigDecimal.valueOf(4324.32).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].harBPFullEvne).isTrue },
                )
            } else if (teller == 2) {
                assertAll(
                    { assertThat(delberegningAndelAvBidragsevneListe).hasSize(2) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].bidragEtterFordeling).isEqualTo(BigDecimal.valueOf(1761.47).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].andelAvSumBidragTilFordelingFaktor).isEqualTo(BigDecimal.valueOf(0.2201834862).setScale(10)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].andelAvEvneBeløp).isEqualTo(BigDecimal.valueOf(1761.47).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[0].harBPFullEvne).isFalse },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].bidragEtterFordeling).isEqualTo(BigDecimal.valueOf(2400).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].andelAvSumBidragTilFordelingFaktor).isEqualTo(BigDecimal.valueOf(0.3243243243).setScale(10)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].andelAvEvneBeløp).isEqualTo(BigDecimal.valueOf(2594.59).setScale(2)) },
                    { assertThat(delberegningAndelAvBidragsevneListe[1].harBPFullEvne).isTrue },
                )
            }

            // Delberegning Bidrag justert for BP barnetillegg
            if (teller == 1) {
                assertAll(
                    { assertThat(delberegningBidragJustertForBPBarnetillegg).hasSize(2) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(2935.78).setScale(2)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].erBidragJustertTilNettoBarnetilleggBP).isFalse },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].bidragJustertForNettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(4000).setScale(2)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].erBidragJustertTilNettoBarnetilleggBP).isFalse },
                )
            } else if (teller == 2) {
                assertAll(
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[0].erBidragJustertTilNettoBarnetilleggBP).isTrue },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].bidragJustertForNettoBarnetilleggBP).isEqualTo(BigDecimal.valueOf(2400).setScale(2)) },
                    { assertThat(delberegningBidragJustertForBPBarnetillegg[1].erBidragJustertTilNettoBarnetilleggBP).isFalse },
                )
            }

            // Referanser
            assertThat(alleReferanser).containsAll(alleRefererteReferanser)

            // Delberegning Endelig bidrag beregnet
            if (teller == 1) {
                assertAll(
                    { assertThat(delberegningEndeligBidragBeregnet).hasSize(2) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2435.78).setScale(2)) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(2440).setScale(0)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(3500).setScale(2)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(3500).setScale(0)) },
                )
            } else if (teller == 2) {
                assertAll(
                    { assertThat(delberegningEndeligBidragBeregnet).hasSize(2) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-03"), YearMonth.parse("2025-06"))) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].beregnetBeløp).isEqualTo(BigDecimal.valueOf(1600).setScale(2)) },
                    { assertThat(delberegningEndeligBidragBeregnet[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(1600).setScale(0)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-06"), null)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].beregnetBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(2)) },
                    { assertThat(delberegningEndeligBidragBeregnet[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(2000).setScale(0)) },
                )
            }

            // Referanser
            assertThat(alleReferanser).containsAll(alleRefererteReferanser)
        }
    }
}
