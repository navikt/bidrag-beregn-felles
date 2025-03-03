package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.core.exception.BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException
import no.nav.bidrag.beregn.core.exception.BegrensetRevurderingLøpendeForskuddManglerException
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtalePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnBarnebidragApiTest : FellesApiTest() {
    private lateinit var filnavn: String

    // Beregningsperiode
    private lateinit var forventetBeregningsperiode: ÅrMånedsperiode

    // Resultat
    private var forventetEndeligResultatbeløp: BigDecimal? = null

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

    // Endring sjekk grense
    private var forventetEndringErOverGrense: Boolean = true

    // Endelig bidrag
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
    private var forventetBidragJustertForDeltBosted: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBP: Boolean = false
    private var forventetBidragJustertForNettoBarnetilleggBM: Boolean = false
    private var forventetBidragJustertNedTilEvne: Boolean = false
    private var forventetBidragJustertNedTil25ProsentAvInntekt: Boolean = false
    private var forventetBidragJustertTilForskuddssats: Boolean = false
    private var forventetBegrensetRevurderingUtført: Boolean = false
    private var forventetFeilmelding = ""
    private var forventetPerioderMedFeilListe = emptyList<ÅrMånedsperiode>()
    private var forventetExceptionBegrensetRevurderingBeregnetBidragErLavereEnnLøpendeBidrag = false
    private var forventetExceptionBegrensetRevurderingLøpendeForskuddMangler = false

    // Sjabloner
    private var forventetAntallSjablonSjablontall: Int = 11

    // Grunnlag
    private var forventetAntallInntektrapporteringBP: Int = 1
    private var forventetAntallInntektrapporteringBM: Int = 1
    private var forventetAntallInntektrapporteringSB: Int = 1
    private var forventetAntallBarnetilleggBP: Int = 1
    private var forventetAntallBarnetilleggBM: Int = 1
    private var forventetAntallEndringSjekkGrense: Int = 1

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @Disabled
    @DisplayName("Barnebidrag - eksempel X")
    fun testBarnebidrag_EksempelX() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempelX.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2024-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3500).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(547.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3496.53).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3500).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(6500).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(7047).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6500).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(4043.53).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(4043.53).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(4043.53).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3496.53).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true
        forventetBidragJustertNedTilEvne = true

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 1A - kostnadsberegnet bidrag")
    fun testBarnebidrag_Eksempel01A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel1A.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3490).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 1B - kostnadsberegnet bidrag (samme som eksempel 1A, men barnet fyller 18 år i perioden)")
    fun testBarnebidrag_Eksempel01B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel1B.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2020-12"))

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(5550).setScale(0)

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
        forventetUnderholdskostnad = BigDecimal.valueOf(9724.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(528.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(5549.50).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5550).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(9724).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 2 - barnetillegg BP og BM")
    fun testBarnebidrag_Eksempel02() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel2.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(2920).setScale(0)

        // Bidragsevne
        forventetBidragsevne = BigDecimal.valueOf(17669.14).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(87450.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(84755.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(12719.30).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(42968.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(140442.30).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2680196565).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(10916.67).setScale(2)

        // Underholdskostnad
        forventetUnderholdskostnad = BigDecimal.valueOf(5999.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.6009174312).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(3604.90).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.6009174312).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(2917.37).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(2920).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(2917.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3173.37).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(2917.37).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3173.37).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3173.37).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3173.37).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(2917.37).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetBidragJustertForNettoBarnetilleggBM = true

        // Grunnlag
        forventetAntallInntektrapporteringBP = 2
        forventetAntallInntektrapporteringBM = 2

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 3A - begrenset revurdering som skal kaste exception fordi beregnet bidrag er lavere enn løpende bidrag")
    fun testBarnebidrag_Eksempel03A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel3A.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3490).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.valueOf(5500).setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(5200).setScale(0)
        forventetBegrensetRevurderingUtført = true
        forventetFeilmelding = "Kan ikke fatte vedtak fordi beregnet bidrag for følgende perioder er lavere enn løpende bidrag: 2020-08 - "
        forventetPerioderMedFeilListe = listOf(ÅrMånedsperiode(YearMonth.parse("2020-08"), null))
        forventetExceptionBegrensetRevurderingBeregnetBidragErLavereEnnLøpendeBidrag = true

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 3B - begrenset revurdering som skal kaste exception fordi løpende forskudd mangler i starten av beregningsperioden")
    fun testBarnebidrag_Eksempel03B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel3B.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.ZERO.setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.ZERO.setScale(2)
        forventetResultatbeløp = BigDecimal.ZERO.setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(256).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(256).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)
        forventetLøpendeForskudd = BigDecimal.ZERO.setScale(0)
        forventetLøpendeBidrag = BigDecimal.valueOf(5200).setScale(0)
        forventetBidragJustertTilForskuddssats = true
        forventetBegrensetRevurderingUtført = true
        forventetFeilmelding = "Kan ikke fatte vedtak fordi løpende forskudd mangler i første beregningsperiode: 2020-08 - "
        forventetPerioderMedFeilListe = listOf(ÅrMånedsperiode(YearMonth.parse("2020-08"), null))
        forventetExceptionBegrensetRevurderingLøpendeForskuddMangler = true

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 4A - kostnadsberegnet bidrag - sjekk mot 12%-regel og endring er ikke over grense")
    fun testBarnebidrag_Eksempel04A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel4A.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3500).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endring sjekk grense
        forventetEndringErOverGrense = false

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 4B - kostnadsberegnet bidrag - ingen sjekk mot 12%-regel fordi egetTiltak=true")
    fun testBarnebidrag_Eksempel04B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel4B.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3490).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endring sjekk grense
        forventetAntallEndringSjekkGrense = 0

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Sjabloner
        forventetAntallSjablonSjablontall = 10

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 4C - kostnadsberegnet bidrag - sjekk mot 12%-regel og endring er over grense")
    fun testBarnebidrag_Eksempel04C() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel4C.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3490).setScale(0)

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

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(256.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(3493.38).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(3490).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(5999).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(3749.38).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(3749.38).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(3493.38).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 4D - kostnadsberegnet bidrag - sjekk mot 12%-regel hvor beløpshistorikk går ut over beregningsperioden")
    fun testBarnebidrag_Eksempel04D() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel4D.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2025-01"), null)

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(3830).setScale(0)

        // Bidragsevne
        forventetBidragsevne = BigDecimal.valueOf(7405).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(60390.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(7225.05).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(34573.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(102188.05).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2275903118).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(9354.17).setScale(2)

        // Underholdskostnad
        forventetUnderholdskostnad = BigDecimal.valueOf(8471.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.4994438265).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(4230.79).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.4994438265).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.ZERO.setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(4230.79).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(4230).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(8471).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(4230.79).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(4230.79).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(4230.79).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(4230.79).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(4230.79).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(4230.79).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Sjabloner
        forventetAntallSjablonSjablontall = 12

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0
        forventetAntallInntektrapporteringSB = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 5 - søknadsbarnet bor hos BP - bidrag skal ikke beregnes")
    fun testBarnebidrag_Eksempel05() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel5.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(1) },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            // Total
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2020-08"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isNull() },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(1) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2020-08"), null)) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isNull() },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isNull() },
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
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isFalse() },
            { assertThat(endeligBidragResultatListe[0].bidragJustertTilForskuddssats).isFalse() },
            { assertThat(endeligBidragResultatListe[0].begrensetRevurderingUtført).isFalse() },
            { assertThat(endeligBidragResultatListe[0].ikkeOmsorgForBarnet).isTrue() },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2020-08"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isFalse() },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    // Sluttperiode settes til måneden etter barnet fyller 18 år
    @Test
    @DisplayName("Barnebidrag - eksempel 6A - ordinært bidrag - kostnadsberegnet hvor barnet blir 18 år i beregningsperioden")
    fun testBarnebidrag_Eksempel06A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel6A.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2020-11"))

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(5550).setScale(0)

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
        forventetUnderholdskostnad = BigDecimal.valueOf(9724.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(528.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(5549.50).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5550).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(9724).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 6B - 18-års-bidrag - skal være tillatt at barnet er 18 år i beregningsperioden")
    fun testBarnebidrag_Eksempel06B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel6B.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2021-01"))

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(5550).setScale(0)

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
        forventetUnderholdskostnad = BigDecimal.valueOf(9724.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(528.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(5549.50).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5550).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(9724).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 7A - 18-års-bidrag - sjekk mot 12%-regel og endring er over grense")
    fun testBarnebidrag_Eksempel07A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel7A.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2021-01"))

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(5550).setScale(0)

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
        forventetUnderholdskostnad = BigDecimal.valueOf(9724.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(528.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(5549.50).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5550).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(9724).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Endring sjekk grense
        forventetEndringErOverGrense = true

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 7B - 18-års-bidrag - sjekk mot 12%-regel og endring er ikke over grense")
    fun testBarnebidrag_Eksempel07B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel7B.json"

        // Beregningsperiode
        forventetBeregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2021-01"))

        // Resultat
        forventetEndeligResultatbeløp = BigDecimal.valueOf(5400).setScale(0)

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
        forventetUnderholdskostnad = BigDecimal.valueOf(9724.00).setScale(2)

        // BP andel underholdskostnad
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.625).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)

        // Samværsfradrag
        forventetSamværsfradrag = BigDecimal.valueOf(528.00).setScale(2)

        // Endelig bidrag
        forventetBeregnetBeløp = BigDecimal.valueOf(5549.50).setScale(2)
        forventetResultatbeløp = BigDecimal.valueOf(5550).setScale(0)
        forventetUMinusNettoBarnetilleggBM = BigDecimal.valueOf(9724).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBM = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterBarnetilleggBM = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBruttoBidragJustertForEvneOg25Prosent = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBegrensetRevurdering = BigDecimal.valueOf(6077.50).setScale(2)
        forventetBruttoBidragEtterBarnetilleggBP = BigDecimal.valueOf(6077.50).setScale(2)
        forventetNettoBidragEtterSamværsfradrag = BigDecimal.valueOf(5549.50).setScale(2)
        forventetBpAndelAvUVedDeltBostedFaktor = BigDecimal.ZERO.setScale(10)
        forventetBpAndelAvUVedDeltBostedBeløp = BigDecimal.ZERO.setScale(2)

        // Endring sjekk grense
        forventetEndringErOverGrense = true

        // Grunnlag
        forventetAntallBarnetilleggBP = 0
        forventetAntallBarnetilleggBM = 0

        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 8A - privat avtale og ingen beløpshistorikk - sjekk mot 12%-regel og endring er over grense")
    fun testBarnebidrag_Eksempel08A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel8A.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningPrivatAvtalePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                    beløp = it.innhold.beløp,
                )
            }

        val beløpshistorikkGrunnlag = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkGrunnlag(
                    tidspunktInnhentet = it.innhold.tidspunktInnhentet,
                    førsteIndeksreguleringsår = it.innhold.førsteIndeksreguleringsår,
                    beløpshistorikk = it.innhold.beløpshistorikk,
                )
            }

        val beløpshistorikkPeriodeGrunnlagListe = beløpshistorikkGrunnlag.flatMap { it.beløpshistorikk }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }
            .filterNot { it.contains("Privat_Avtale_Grunnlag") }
            .filterNot { it.contains("Privat_Avtale_Periode_Grunnlag") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).isNotNull },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(2) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202408_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202408") },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202408") },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(2) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), YearMonth.parse("2025-01"))) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-01"), null)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isTrue() },

            // Endring sjekk grense periode
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe).hasSize(2) },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isTrue() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isTrue() },

            // Privat avtale periode
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe).hasSize(1) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(500)) },

            // Beløpshistorikk
            { assertThat(beløpshistorikkPeriodeGrunnlagListe).hasSize(0) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 8B - privat avtale og ingen beløpshistorikk - sjekk mot 12%-regel og endring er under grense")
    fun testBarnebidrag_Eksempel08B() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel8B.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningPrivatAvtalePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                    beløp = it.innhold.beløp,
                )
            }

        val beløpshistorikkGrunnlag = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkGrunnlag(
                    tidspunktInnhentet = it.innhold.tidspunktInnhentet,
                    førsteIndeksreguleringsår = it.innhold.førsteIndeksreguleringsår,
                    beløpshistorikk = it.innhold.beløpshistorikk,
                )
            }

        val beløpshistorikkPeriodeGrunnlagListe = beløpshistorikkGrunnlag.flatMap { it.beløpshistorikk }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }
            .filterNot { it.contains("Privat_Avtale_Grunnlag") }
            .filterNot { it.contains("Privat_Avtale_Periode_Grunnlag") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).isNotNull },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(2) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(4800)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202408_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202408") },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(4800)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202408") },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(2) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), YearMonth.parse("2025-01"))) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-01"), null)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isFalse() },

            // Endring sjekk grense periode
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe).hasSize(2) },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isFalse() },

            // Privat avtale periode
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe).hasSize(1) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(4800)) },

            // Beløpshistorikk
            { assertThat(beløpshistorikkPeriodeGrunnlagListe).hasSize(0) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 8C - privat avtale og beløpshistorikk deler av perioden - sjekk mot 12%-regel og endring er under grense")
    fun testBarnebidrag_Eksempel08C() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel8C.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningPrivatAvtalePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                    beløp = it.innhold.beløp,
                )
            }

        val beløpshistorikkGrunnlag = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkGrunnlag(
                    tidspunktInnhentet = it.innhold.tidspunktInnhentet,
                    førsteIndeksreguleringsår = it.innhold.førsteIndeksreguleringsår,
                    beløpshistorikk = it.innhold.beløpshistorikk,
                )
            }

        val beløpshistorikkPeriodeGrunnlagListe = beløpshistorikkGrunnlag.flatMap { it.beløpshistorikk }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }
            .filterNot { it.contains("Privat_Avtale_Grunnlag") }
            .filterNot { it.contains("Privat_Avtale_Periode_Grunnlag") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).isNotNull },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(2) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(4900)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202408_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe).contains("Mottatt_BeløpshistorikkBidrag_202408") },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(4800)) },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("sluttberegning_Person_Søknadsbarn_202501") },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe).contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202408") },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(2) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), YearMonth.parse("2025-01"))) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-01"), null)) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isFalse() },

            // Endring sjekk grense periode
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe).hasSize(2) },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isFalse() },

            // Privat avtale periode
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe).hasSize(1) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(4800)) },

            // Beløpshistorikk
            { assertThat(beløpshistorikkPeriodeGrunnlagListe).hasSize(1) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-08"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[0].beløp).isEqualTo(BigDecimal.valueOf(4900)) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 8D - privat avtale og beløpshistorikk flere perioder - sjekk mot 12%-regel og endring er over grense")
    fun testBarnebidrag_Eksempel08D() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel8D.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningPrivatAvtalePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                    beløp = it.innhold.beløp,
                )
            }

        val beløpshistorikkGrunnlag = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkGrunnlag(
                    tidspunktInnhentet = it.innhold.tidspunktInnhentet,
                    førsteIndeksreguleringsår = it.innhold.førsteIndeksreguleringsår,
                    beløpshistorikk = it.innhold.beløpshistorikk,
                )
            }

        val beløpshistorikkPeriodeGrunnlagListe = beløpshistorikkGrunnlag.flatMap { it.beløpshistorikk }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).isNotNull },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(11) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2021-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(3490)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202101_202107")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-07"),
                        YearMonth.parse("2022-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(4310)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202107_202201")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].resultat.beløp).isEqualTo(BigDecimal.valueOf(4310)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202201_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2022-10"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].resultat.beløp).isEqualTo(BigDecimal.valueOf(4440)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202207_202301")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202207_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].resultat.beløp).isEqualTo(BigDecimal.valueOf(4440)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202207_202301")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-01"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].resultat.beløp).isEqualTo(BigDecimal.valueOf(4440)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202301_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].resultat.beløp).isEqualTo(BigDecimal.valueOf(4440)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202301_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].resultat.beløp).isEqualTo(BigDecimal.valueOf(4730)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202307_202401")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-01"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].resultat.beløp).isEqualTo(BigDecimal.valueOf(4730)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202401_202407")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].resultat.beløp).isEqualTo(BigDecimal.valueOf(4850)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202407_202501")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].resultat.beløp).isEqualTo(BigDecimal.valueOf(4850)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202501")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(9) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-01"), YearMonth.parse("2021-07"))) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(3490)) },
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-07"), YearMonth.parse("2022-01"))) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(4310)) },
            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2022-01"), YearMonth.parse("2022-07"))) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(4310)) },
            { assertThat(endeligBidragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2022-07"), YearMonth.parse("2023-01"))) },
            { assertThat(endeligBidragResultatListe[3].resultatBeløp).isEqualTo(BigDecimal.valueOf(4440)) },
            { assertThat(endeligBidragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2023-01"), YearMonth.parse("2023-07"))) },
            { assertThat(endeligBidragResultatListe[4].resultatBeløp).isEqualTo(BigDecimal.valueOf(4440)) },
            { assertThat(endeligBidragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2023-07"), YearMonth.parse("2024-01"))) },
            { assertThat(endeligBidragResultatListe[5].resultatBeløp).isEqualTo(BigDecimal.valueOf(4730)) },
            { assertThat(endeligBidragResultatListe[6].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-01"), YearMonth.parse("2024-07"))) },
            { assertThat(endeligBidragResultatListe[6].resultatBeløp).isEqualTo(BigDecimal.valueOf(4730)) },
            { assertThat(endeligBidragResultatListe[7].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2025-01"))) },
            { assertThat(endeligBidragResultatListe[7].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(endeligBidragResultatListe[8].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-01"), null)) },
            { assertThat(endeligBidragResultatListe[8].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-01"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isTrue() },

            // Endring sjekk grense periode
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe).hasSize(11) },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2021-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-07"),
                        YearMonth.parse("2022-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isTrue() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[2].endringErOverGrense).isTrue() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2022-10"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[3].endringErOverGrense).isTrue() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[4].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[4].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[5].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-01"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[5].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[6].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[6].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[7].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[7].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[8].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-01"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[8].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[9].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[9].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[10].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[10].endringErOverGrense).isFalse() },

            // Privat avtale periode
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe).hasSize(4) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(3500)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(3610)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(3860)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(4040)) },

            // Beløpshistorikk
            { assertThat(beløpshistorikkPeriodeGrunnlagListe).hasSize(3) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[0].beløp).isEqualTo(BigDecimal.valueOf(4400)) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[1].beløp).isEqualTo(BigDecimal.valueOf(4600)) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-03"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[2].beløp).isEqualTo(BigDecimal.valueOf(5500)) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 8E - privat avtale og beløpshistorikk flere perioder - sjekk mot 12%-regel og endring er under grense")
    fun testBarnebidrag_Eksempel08E() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidrag_eksempel8E.json"
        val request = lesFilOgByggRequest(filnavn)

        val barnebidragResultat = api.beregn(request)
        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

        val delberegningEndringSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningEndringSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningPrivatAvtalePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                    beløp = it.innhold.beløp,
                )
            }

        val beløpshistorikkGrunnlag = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkGrunnlag(
                    tidspunktInnhentet = it.innhold.tidspunktInnhentet,
                    førsteIndeksreguleringsår = it.innhold.førsteIndeksreguleringsår,
                    beløpshistorikk = it.innhold.beløpshistorikk,
                )
            }

        val beløpshistorikkPeriodeGrunnlagListe = beløpshistorikkGrunnlag.flatMap { it.beløpshistorikk }

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }

        assertAll(
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).isNotNull },
            { assertThat(barnebidragResultatGrunnlagListe).isNotNull },

            // Resultat
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(11) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2021-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(3500)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202101_202107")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-07"),
                        YearMonth.parse("2022-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(3500)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202107_202201")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[1].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].resultat.beløp).isEqualTo(BigDecimal.valueOf(3500)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202201_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[2].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202101_202207")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2022-10"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].resultat.beløp).isEqualTo(BigDecimal.valueOf(3610)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202207_202210")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[3].grunnlagsreferanseListe)
                    .contains("delberegning_DELBEREGNING_PRIVAT_AVTALE_PERIODE_Person_Bidragspliktig_Person_Søknadsbarn_202207_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].resultat.beløp).isEqualTo(BigDecimal.valueOf(4400)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202210_202301")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[4].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-01"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].resultat.beløp).isEqualTo(BigDecimal.valueOf(4400)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202301_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[5].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].resultat.beløp).isEqualTo(BigDecimal.valueOf(4600)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202301_202307")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[6].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].resultat.beløp).isEqualTo(BigDecimal.valueOf(4600)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202307_202401")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[7].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-01"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].resultat.beløp).isEqualTo(BigDecimal.valueOf(4600)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202401_202407")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[8].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].resultat.beløp).isEqualTo(BigDecimal.valueOf(5500)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202407_202501")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[9].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].resultat.beløp).isEqualTo(BigDecimal.valueOf(5500)) },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].grunnlagsreferanseListe)
                    .contains("sluttberegning_Person_Søknadsbarn_202501")
            },
            {
                assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[10].grunnlagsreferanseListe)
                    .contains("Mottatt_BeløpshistorikkBidrag_202101")
            },

            // Endelig bidrag
            { assertThat(endeligBidragResultatListe).hasSize(10) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-01"), YearMonth.parse("2021-07"))) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(BigDecimal.valueOf(3490)) },
            { assertThat(endeligBidragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-07"), YearMonth.parse("2022-01"))) },
            { assertThat(endeligBidragResultatListe[1].resultatBeløp).isEqualTo(BigDecimal.valueOf(3380)) },
            { assertThat(endeligBidragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2022-01"), YearMonth.parse("2022-07"))) },
            { assertThat(endeligBidragResultatListe[2].resultatBeløp).isEqualTo(BigDecimal.valueOf(3380)) },
            { assertThat(endeligBidragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2022-07"), YearMonth.parse("2022-10"))) },
            { assertThat(endeligBidragResultatListe[3].resultatBeløp).isEqualTo(BigDecimal.valueOf(3480)) },
            { assertThat(endeligBidragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2022-10"), YearMonth.parse("2023-01"))) },
            { assertThat(endeligBidragResultatListe[4].resultatBeløp).isEqualTo(BigDecimal.valueOf(4440)) },
            { assertThat(endeligBidragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2023-01"), YearMonth.parse("2023-07"))) },
            { assertThat(endeligBidragResultatListe[5].resultatBeløp).isEqualTo(BigDecimal.valueOf(4440)) },
            { assertThat(endeligBidragResultatListe[6].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2023-07"), YearMonth.parse("2024-01"))) },
            { assertThat(endeligBidragResultatListe[6].resultatBeløp).isEqualTo(BigDecimal.valueOf(4730)) },
            { assertThat(endeligBidragResultatListe[7].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-01"), YearMonth.parse("2024-07"))) },
            { assertThat(endeligBidragResultatListe[7].resultatBeløp).isEqualTo(BigDecimal.valueOf(4730)) },
            { assertThat(endeligBidragResultatListe[8].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2025-01"))) },
            { assertThat(endeligBidragResultatListe[8].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },
            { assertThat(endeligBidragResultatListe[9].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-01"), null)) },
            { assertThat(endeligBidragResultatListe[9].resultatBeløp).isEqualTo(BigDecimal.valueOf(4850)) },

            // Endring sjekk grense
            { assertThat(delberegningEndringSjekkGrenseResultatListe).hasSize(1) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-01"), null)) },
            { assertThat(delberegningEndringSjekkGrenseResultatListe[0].endringErOverGrense).isFalse() },

            // Endring sjekk grense periode
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe).hasSize(11) },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2021-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-07"),
                        YearMonth.parse("2022-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[2].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2022-10"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[3].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[4].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[4].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[5].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-01"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[5].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[6].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[6].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[7].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[7].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[8].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-01"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[8].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[9].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-01"),
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[9].endringErOverGrense).isFalse() },
            {
                assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[10].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2025-01"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningEndringSjekkGrensePeriodeResultatListe[10].endringErOverGrense).isFalse() },

            // Privat avtale periode
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe).hasSize(4) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2021-01"),
                        YearMonth.parse("2022-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(3500)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-07"),
                        YearMonth.parse("2023-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(3610)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-07"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(3860)) },
            {
                assertThat(delberegningPrivatAvtalePeriodeResultatListe[3].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        null,
                    ),
                )
            },
            { assertThat(delberegningPrivatAvtalePeriodeResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(4040)) },

            // Beløpshistorikk
            { assertThat(beløpshistorikkPeriodeGrunnlagListe).hasSize(3) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[0].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2022-10"),
                        YearMonth.parse("2023-04"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[0].beløp).isEqualTo(BigDecimal.valueOf(4400)) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[1].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2023-04"),
                        YearMonth.parse("2024-07"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[1].beløp).isEqualTo(BigDecimal.valueOf(4600)) },
            {
                assertThat(beløpshistorikkPeriodeGrunnlagListe[2].periode).isEqualTo(
                    ÅrMånedsperiode(
                        YearMonth.parse("2024-07"),
                        YearMonth.parse("2025-03"),
                    ),
                )
            },
            { assertThat(beløpshistorikkPeriodeGrunnlagListe[2].beløp).isEqualTo(BigDecimal.valueOf(5500)) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatBarnebidrag() {
        val request = lesFilOgByggRequest(filnavn)

        val exception: RuntimeException
        var feilmelding = ""
        var perioderMedFeilListe = emptyList<ÅrMånedsperiode>()
        val barnebidragResultat: BeregnetBarnebidragResultat

        if (forventetExceptionBegrensetRevurderingBeregnetBidragErLavereEnnLøpendeBidrag == true) {
            exception = assertThrows(BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException::class.java) {
                api.beregn(request)
            }
            barnebidragResultat = exception.data
            perioderMedFeilListe = exception.periodeListe
            feilmelding = exception.message!!
        } else if (forventetExceptionBegrensetRevurderingLøpendeForskuddMangler == true) {
            exception = assertThrows(BegrensetRevurderingLøpendeForskuddManglerException::class.java) {
                api.beregn(request)
            }
            barnebidragResultat = exception.data
            perioderMedFeilListe = exception.periodeListe
            feilmelding = exception.message!!
        } else {
            barnebidragResultat = api.beregn(request)
        }

        val barnebidragResultatGrunnlagListe = barnebidragResultat.grunnlagListe
        printJson(barnebidragResultat)

        val alleReferanser = hentAlleReferanser(barnebidragResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = barnebidragResultatGrunnlagListe,
            barnebidragResultat = barnebidragResultat,
        )

        // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
        val alleReferanserFiltrert = alleReferanser
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
            .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

        // Fjerner referanser som ikke er med i inputen til beregning
        val alleRefererteReferanserFiltrert = alleRefererteReferanser
            .filterNot { it.contains("innhentet_husstandsmedlem") }

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

        val delberegningSjekkGrensePeriodeResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val delberegningSjekkGrenseResultatListe = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val endeligBidragResultatListe = hentSluttberegning(barnebidragResultatGrunnlagListe)

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
            .filter { it.gjelderBarnReferanse == null }
            .size

        val antallBostatusPeriodeSB = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .filter { it.gjelderBarnReferanse == referanseSB }
            .size

        val antallUnderholdskostnad = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallSamværsklasse = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallBarnetilleggBM = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBM,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        val antallBarnetilleggBP = barnebidragResultatGrunnlagListe
            .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                referanse = referanseBP,
            )
            .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
            .flatMap { it.innhold.inntektspostListe }
            .size

        val antallDelberegningEndringSjekkGrensePeriode = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE }
            .size

        val antallDelberegningEndringSjekkGrense = barnebidragResultatGrunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE }
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
            { assertThat(barnebidragResultat).isNotNull },
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe).hasSize(1) },
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
            { assertThat(delberegningSjekkGrensePeriodeResultatListe).isNotNull },
            { assertThat(delberegningSjekkGrensePeriodeResultatListe).hasSize(forventetAntallEndringSjekkGrense) },
            { assertThat(delberegningSjekkGrenseResultatListe).isNotNull },
            { assertThat(delberegningSjekkGrenseResultatListe).hasSize(forventetAntallEndringSjekkGrense) },

            { assertThat(feilmelding).isEqualTo(forventetFeilmelding) },
            { assertThat(perioderMedFeilListe).isEqualTo(forventetPerioderMedFeilListe) },

            // Resultat
            // Total
            { assertThat(barnebidragResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(forventetEndeligResultatbeløp) },

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
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBegrensetRevurdering).isEqualTo(forventetBruttoBidragEtterBegrensetRevurdering) },
            { assertThat(endeligBidragResultatListe[0].bruttoBidragEtterBarnetilleggBP).isEqualTo(forventetBruttoBidragEtterBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].nettoBidragEtterSamværsfradrag).isEqualTo(forventetNettoBidragEtterSamværsfradrag) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedFaktor).isEqualTo(forventetBpAndelAvUVedDeltBostedFaktor) },
            { assertThat(endeligBidragResultatListe[0].bpAndelAvUVedDeltBostedBeløp).isEqualTo(forventetBpAndelAvUVedDeltBostedBeløp) },
            { assertThat(endeligBidragResultatListe[0].løpendeForskudd).isEqualTo(forventetLøpendeForskudd) },
            { assertThat(endeligBidragResultatListe[0].løpendeBidrag).isEqualTo(forventetLøpendeBidrag) },
            { assertThat(endeligBidragResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørgetBp) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForDeltBosted).isEqualTo(forventetBidragJustertForDeltBosted) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBP).isEqualTo(forventetBidragJustertForNettoBarnetilleggBP) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertForNettoBarnetilleggBM).isEqualTo(forventetBidragJustertForNettoBarnetilleggBM) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTilEvne).isEqualTo(forventetBidragJustertNedTilEvne) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertNedTil25ProsentAvInntekt).isEqualTo(forventetBidragJustertNedTil25ProsentAvInntekt) },
            { assertThat(endeligBidragResultatListe[0].bidragJustertTilForskuddssats).isEqualTo(forventetBidragJustertTilForskuddssats) },
            { assertThat(endeligBidragResultatListe[0].begrensetRevurderingUtført).isEqualTo(forventetBegrensetRevurderingUtført) },

            // Grunnlag
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(forventetAntallInntektrapporteringBP) },
            { assertThat(antallInntektRapporteringPeriodeBM).isEqualTo(forventetAntallInntektrapporteringBM) },
            { assertThat(antallInntektRapporteringPeriodeSB).isEqualTo(forventetAntallInntektrapporteringSB) },
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
            { assertThat(antallDelberegningEndringSjekkGrensePeriode).isEqualTo(forventetAntallEndringSjekkGrense) },
            { assertThat(antallDelberegningEndringSjekkGrense).isEqualTo(forventetAntallEndringSjekkGrense) },
            { assertThat(antallBostatusPeriodeBP).isEqualTo(1) },
            { assertThat(antallBostatusPeriodeSB).isEqualTo(1) },
            { assertThat(antallUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallSamværsklasse).isEqualTo(1) },
            { assertThat(antallBarnetilleggBP).isEqualTo(forventetAntallBarnetilleggBP) },
            { assertThat(antallBarnetilleggBM).isEqualTo(forventetAntallBarnetilleggBM) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },
            { assertThat(antallSjablonBidragsevne).isEqualTo(1) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(1) },
            { assertThat(antallSjablonSamværsfradrag).isEqualTo(1) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
            { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
        )
    }
}
