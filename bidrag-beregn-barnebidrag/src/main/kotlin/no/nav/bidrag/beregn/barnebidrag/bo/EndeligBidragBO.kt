package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.math.BigDecimal
import java.util.Collections.emptyList

data class EndeligBidragPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragsevneDelberegningPeriodeGrunnlagListe: List<BidragsevneDelberegningPeriodeGrunnlag>,
    val underholdskostnadDelberegningPeriodeGrunnlagListe: List<UnderholdskostnadDelberegningPeriodeGrunnlag>,
    val bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe: List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
    val samværsklassePeriodeGrunnlagListe: List<SamværsklassePeriodeGrunnlag>,
    val bostatusPeriodeGrunnlagListe: List<BostatusPeriodeGrunnlag>,
    val nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe: List<NettoBarnetilleggDelberegningPeriodeGrunnlag>,
    val nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe: List<NettoBarnetilleggDelberegningPeriodeGrunnlag>,
    val beløpshistorikkForskuddPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
    val beløpshistorikkBidragPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
    val begrensetRevurderingPeriodeGrunnlag: BegrensetRevurderingPeriodeGrunnlag?,
)

data class BidragsevneDelberegningPeriodeGrunnlag(val referanse: String, val bidragsevnePeriode: DelberegningBidragsevne)

data class BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bpAndelUnderholdskostnadPeriode: DelberegningBidragspliktigesAndel,
)

data class SamværsfradragDelberegningPeriodeGrunnlag(val referanse: String, val samværsfradragPeriode: DelberegningSamværsfradrag)

data class BostatusPeriodeGrunnlag(val referanse: String, val bostatusPeriode: BostatusPeriode)

data class NettoBarnetilleggDelberegningPeriodeGrunnlag(val referanse: String, val nettoBarnetilleggPeriode: DelberegningNettoBarnetillegg)

data class BegrensetRevurderingPeriodeGrunnlag(val referanse: String, val begrensetRevurdering: Boolean)

data class EndeligBidragPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: EndeligBidragBeregningResultat)

data class EndeligBidragBeregningGrunnlag(
    val bidragsevneBeregningGrunnlag: BidragsevneDelberegningBeregningGrunnlag,
    val underholdskostnadBeregningGrunnlag: UnderholdskostnadDelberegningBeregningGrunnlag,
    val bpAndelUnderholdskostnadBeregningGrunnlag: BpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
    val deltBostedBeregningGrunnlag: DeltBostedBeregningGrunnlag,
    val søknadsbarnetBorHosBpGrunnlag: SøknadsbarnetBorHosBpGrunnlag,
    val barnetilleggBPBeregningGrunnlag: BarnetilleggDelberegningBeregningGrunnlag?,
    val barnetilleggBMBeregningGrunnlag: BarnetilleggDelberegningBeregningGrunnlag?,
    val løpendeForskuddBeløp: BigDecimal?,
    val løpendeBidragBeløp: BigDecimal?,
    val utførBegrensetRevurdering: Boolean,
    val engangsreferanser: List<String> = emptyList(),
)

data class EndeligBidragBeregningAldersjusteringGrunnlag(
    val underholdskostnad: UnderholdskostnadDelberegningBeregningGrunnlag,
    val bpAndelFaktor: KopiBpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val samværsfradrag: SamværsfradragDelberegningBeregningGrunnlag,
)

data class BidragsevneDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal, val sumInntekt25Prosent: BigDecimal)

data class UnderholdskostnadDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val andelBeløp: BigDecimal,
    val andelFaktor: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)

data class KopiBpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val andelFaktor: BigDecimal,
)

data class SamværsfradragDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class DeltBostedBeregningGrunnlag(val referanse: String, val deltBosted: Boolean)

data class SøknadsbarnetBorHosBpGrunnlag(val referanse: String, val søknadsbarnetBorHosBp: Boolean)

data class BarnetilleggDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class EndeligBidragBeregningResultat(
    val beregnetBeløp: BigDecimal? = BigDecimal.ZERO.avrundetMedToDesimaler,
    val resultatBeløp: BigDecimal? = BigDecimal.ZERO.avrundetMedNullDesimaler,
    val uMinusNettoBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragEtterBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBidragEtterBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragJustertForEvneOg25Prosent: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragEtterBegrensetRevurdering: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragEtterBarnetilleggBP: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBidragEtterSamværsfradrag: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bpAndelAvUVedDeltBostedFaktor: BigDecimal = BigDecimal.ZERO.avrundetMedTiDesimaler,
    val bpAndelAvUVedDeltBostedBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val løpendeForskudd: BigDecimal? = null,
    val løpendeBidrag: BigDecimal? = null,
    val barnetErSelvforsørget: Boolean = false,
    val bidragJustertForDeltBosted: Boolean = false,
    val bidragJustertForNettoBarnetilleggBP: Boolean = false,
    val bidragJustertForNettoBarnetilleggBM: Boolean = false,
    val bidragJustertNedTilEvne: Boolean = false,
    val bidragJustertNedTil25ProsentAvInntekt: Boolean = false,
    val bidragJustertTilForskuddssats: Boolean = false,
    val beregnetBeløpErLavereEnnLøpendeBidrag: Boolean = false,
    val begrensetRevurderingUtført: Boolean = false,
    val ikkeOmsorgForBarnet: Boolean = false,
    val beregnetBidragErLavereEnnLøpendeBidrag: Boolean = false,
    val løpendeForskuddMangler: Boolean = false,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class EndeligBidragBeregningAldersjusteringResultat(
    val beregnetBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val resultatBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedNullDesimaler,
    val bpAndelBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class BeregnEndeligBidragServiceRespons(
    val grunnlagListe: List<GrunnlagDto>,
    val feilmelding: String,
    val perioderMedFeilListe: List<ÅrMånedsperiode>,
    val skalKasteBegrensetRevurderingException: Boolean = false,
)
