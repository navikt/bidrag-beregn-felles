package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningAndelAvBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragJustertForBPBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordelingLøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEvne25ProsentAvInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragPeriode
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

data class SamværsfradragDelberegningPeriodeGrunnlag(
    val referanse: String,
    val barnReferanse: Grunnlagsreferanse? = null,
    val samværsfradragPeriode: DelberegningSamværsfradrag,
)

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
    val deltBosted: DeltBostedBeregningGrunnlag,
)

data class BidragsevneDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal, val sumInntekt25Prosent: BigDecimal)

data class UnderholdskostnadDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val andelBeløp: BigDecimal,
    val andelFaktor: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)

data class KopiBpAndelUnderholdskostnadDelberegningBeregningGrunnlag(val referanse: String, val andelFaktor: BigDecimal)

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
    val bpAndelFaktorVedDeltBosted: BigDecimal? = null,
    val deltBosted: Boolean = false,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class BeregnEndeligBidragServiceRespons(
    val grunnlagListe: List<GrunnlagDto>,
    val feilmelding: String,
    val perioderMedFeilListe: List<ÅrMånedsperiode>,
    val skalKasteBegrensetRevurderingException: Boolean = false,
)

// Nytt ifbm forholdsmessig fordeling

data class BidragTilFordelingPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val underholdskostnadDelberegningPeriodeGrunnlagListe: List<UnderholdskostnadDelberegningPeriodeGrunnlag>,
    val bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe: List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag>,
    val nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe: List<NettoBarnetilleggDelberegningPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
)

data class BidragTilFordelingPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: BidragTilFordelingBeregningResultat)

data class BidragTilFordelingBeregningGrunnlag(
    val underholdskostnadBeregningGrunnlag: UnderholdskostnadDelberegningBeregningGrunnlag,
    val bpAndelUnderholdskostnadBeregningGrunnlag: BpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val barnetilleggBMBeregningGrunnlag: BarnetilleggDelberegningBeregningGrunnlag?,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
)

data class BidragTilFordelingBeregningResultat(
    val uMinusNettoBarnetilleggBM: BigDecimal,
    val bpAndelAvUMinusSamværsfradrag: BigDecimal,
    val bidragTilFordeling: BigDecimal,
    val nettoBidragEtterBarnetilleggBM: BigDecimal,
    val bruttoBidragEtterBarnetilleggBM: BigDecimal,
    val erBidragJustertForNettoBarnetilleggBM: Boolean,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class BidragTilFordelingDelberegningPeriodeGrunnlag(val referanse: String, val bidragTilFordelingPeriode: DelberegningBidragTilFordeling)

data class BidragTilFordelingDelberegningBeregningGrunnlag(val referanse: String, val bidragTilFordeling: BigDecimal)

data class SumBidragTilFordelingPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragTilFordelingDelberegningPeriodeGrunnlagListe: List<BidragTilFordelingDelberegningPeriodeGrunnlag>,
    val bidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlagListe: List<BidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlag>,
)

data class SumBidragTilFordelingPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: SumBidragTilFordelingBeregningResultat)

data class SumBidragTilFordelingBeregningGrunnlag(
    val bidragTilFordelingBeregningGrunnlagListe: List<BidragTilFordelingDelberegningBeregningGrunnlag>,
    val bidragTilFordelingLøpendeBidragBeregningGrunnlagListe: List<BidragTilFordelingLøpendeBidragDelberegningBeregningGrunnlag>,
)

data class SumBidragTilFordelingBeregningResultat(
    val sumBidragTilFordeling: BigDecimal,
    val sumPrioriterteBidragTilFordeling: BigDecimal,
    val erKompletteGrunnlagForAlleLøpendeBidrag: Boolean,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class SumBidragTilFordelingDelberegningPeriodeGrunnlag(
    val referanse: String,
    val sumBidragTilFordelingPeriode: DelberegningSumBidragTilFordeling,
)

data class SumBidragTilFordelingDelberegningBeregningGrunnlag(val referanse: String, val sumBidragTilFordeling: BigDecimal)

data class LøpendeBidragPeriodeGrunnlag(val referanse: String, val løpendeBidragPeriode: LøpendeBidragPeriode)

data class Evne25ProsentAvInntektPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragsevneDelberegningPeriodeGrunnlagListe: List<BidragsevneDelberegningPeriodeGrunnlag>,
)

data class Evne25ProsentAvInntektPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: Evne25ProsentAvInntektBeregningResultat)

data class Evne25ProsentAvInntektBeregningGrunnlag(val bidragsevneBeregningGrunnlag: BidragsevneDelberegningBeregningGrunnlag)

data class Evne25ProsentAvInntektBeregningResultat(
    val evneJustertFor25ProsentAvInntekt: BigDecimal,
    val erEvneJustertNedTil25ProsentAvInntekt: Boolean,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class Evne25ProsentAvInntektDelberegningPeriodeGrunnlag(
    val referanse: String,
    val evne25ProsentAvInntektPeriode: DelberegningEvne25ProsentAvInntekt,
)

data class Evne25ProsentAvInntektDelberegningBeregningGrunnlag(val referanse: String, val evneJustertFor25ProsentAvInntekt: BigDecimal)

data class AndelAvBidragsevnePeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val evne25ProsentAvInntektDelberegningPeriodeGrunnlagListe: List<Evne25ProsentAvInntektDelberegningPeriodeGrunnlag>,
    val sumBidragTilFordelingDelberegningPeriodeGrunnlagListe: List<SumBidragTilFordelingDelberegningPeriodeGrunnlag>,
    val bidragTilFordelingDelberegningPeriodeGrunnlagListe: List<BidragTilFordelingDelberegningPeriodeGrunnlag>,
)

data class AndelAvBidragsevnePeriodeResultat(val periode: ÅrMånedsperiode, val resultat: AndelAvBidragsevneBeregningResultat)

data class AndelAvBidragsevneBeregningGrunnlag(
    val evne25ProsentAvInntektBeregningGrunnlag: Evne25ProsentAvInntektDelberegningBeregningGrunnlag,
    val sumBidragTilFordelingBeregningGrunnlag: SumBidragTilFordelingDelberegningBeregningGrunnlag,
    val bidragTilFordelingBeregningGrunnlag: BidragTilFordelingDelberegningBeregningGrunnlag,
)

data class AndelAvBidragsevneBeregningResultat(
    val andelAvSumBidragTilFordelingFaktor: BigDecimal,
    val andelAvEvneBeløp: BigDecimal,
    val bidragEtterFordeling: BigDecimal,
    val bruttoBidragJustertForEvneOg25Prosent: BigDecimal,
    val harBPFullEvne: Boolean,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class AndelAvBidragsevneDelberegningPeriodeGrunnlag(val referanse: String, val andelAvBidragsevnePeriode: DelberegningAndelAvBidragsevne)

data class AndelAvBidragsevneDelberegningBeregningGrunnlag(val referanse: String, val bidragEtterFordeling: BigDecimal)

data class BidragTilFordelingLøpendeBidragPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val løpendeBidragPeriodeGrunnlagListe: List<LøpendeBidragPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
)

data class BidragTilFordelingLøpendeBidragPeriodeResultat(
    val periode: ÅrMånedsperiode,
    val resultat: BidragTilFordelingLøpendeBidragBeregningResultat,
)

data class BidragTilFordelingLøpendeBidragBeregningGrunnlag(
    val løpendeBidragBeregningGrunnlag: LøpendeBidragTilFordelingBeregningGrunnlag,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
)

data class BidragTilFordelingLøpendeBidragBeregningResultat(
    val reduksjonUnderholdskostnad: BigDecimal,
    val bidragTilFordeling: BigDecimal,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class BidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bidragTilFordelingLøpendeBidragPeriode: DelberegningBidragTilFordelingLøpendeBidrag,
)

data class BidragTilFordelingLøpendeBidragDelberegningBeregningGrunnlag(val referanse: String, val bidragTilFordeling: BigDecimal)

data class LøpendeBidragTilFordelingBeregningGrunnlag(val referanse: String, val løpendeBidrag: LøpendeBidragPeriode)

data class BidragJustertForBPBarnetilleggPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val andelAvBidragsevneDelberegningPeriodeGrunnlagListe: List<AndelAvBidragsevneDelberegningPeriodeGrunnlag>,
    val nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe: List<NettoBarnetilleggDelberegningPeriodeGrunnlag>,
)

data class BidragJustertForBPBarnetilleggPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: BidragJustertForBPBarnetilleggBeregningResultat)

data class BidragJustertForBPBarnetilleggBeregningGrunnlag(
    val andelAvBidragsevneBeregningGrunnlag: AndelAvBidragsevneDelberegningBeregningGrunnlag,
    val barnetilleggBPBeregningGrunnlag: BarnetilleggDelberegningBeregningGrunnlag?,
)

data class BidragJustertForBPBarnetilleggBeregningResultat(
    val bidragJustertForNettoBarnetilleggBP: BigDecimal,
    val erBidragJustertTilNettoBarnetilleggBP: Boolean,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

data class BidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bidragJustertForBPBarnetilleggPeriode: DelberegningBidragJustertForBPBarnetillegg,
)

data class BidragJustertForBPBarnetilleggDelberegningBeregningGrunnlag(val referanse: String, val bidragJustertForNettoBarnetilleggBP: BigDecimal)

data class SluttberegningBarnebidragV2PeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlagListe: List<BidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
    val bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe: List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag>,
    val bostatusPeriodeGrunnlagListe: List<BostatusPeriodeGrunnlag>,
)

data class SluttberegningBarnebidragV2PeriodeResultat(val periode: ÅrMånedsperiode, val resultat: SluttberegningBarnebidragV2BeregningResultat)

data class SluttberegningBarnebidragV2BeregningGrunnlag(
    val bidragJustertForBPBarnetilleggBeregningGrunnlag: BidragJustertForBPBarnetilleggDelberegningBeregningGrunnlag,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
    val bpAndelUnderholdskostnadBeregningGrunnlag: BpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val søknadsbarnetBorHosBpGrunnlag: SøknadsbarnetBorHosBpGrunnlag,
)

data class SluttberegningBarnebidragV2BeregningResultat(
    val beregnetBeløp: BigDecimal?,
    val resultatBeløp: BigDecimal?,
    val barnetErSelvforsørget: Boolean = false,
    val ikkeOmsorgForBarnet: Boolean = false,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)
