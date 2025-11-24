package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtalePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidragV2
import java.math.BigDecimal

data class EndringSjekkGrensePeriodePeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val sluttberegningPeriodeGrunnlagListe: List<SluttberegningPeriodeGrunnlag>,
    val beløpshistorikkBidragPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
    val privatAvtaleIndeksregulertPeriodeGrunnlagListe: List<PrivatAvtaleIndeksregulertPeriodeGrunnlag>,
    val sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
)

data class EndringSjekkGrensePeriodePeriodeGrunnlagV2(
    val beregningsperiode: ÅrMånedsperiode,
    val sluttberegningPeriodeGrunnlagListe: List<SluttberegningPeriodeGrunnlagV2>,
    val beløpshistorikkBidragPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
    val privatAvtaleIndeksregulertPeriodeGrunnlagListe: List<PrivatAvtaleIndeksregulertPeriodeGrunnlag>,
    val sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
)

data class SluttberegningPeriodeGrunnlag(val referanse: String, val sluttberegningPeriode: SluttberegningBarnebidrag)
data class SluttberegningPeriodeGrunnlagV2(val referanse: String, val sluttberegningPeriode: SluttberegningBarnebidragV2)

data class PrivatAvtaleIndeksregulertPeriodeGrunnlag(val referanse: String, val privatAvtaleIndeksregulertPeriode: DelberegningPrivatAvtalePeriode)

data class EndringSjekkGrensePeriodePeriodeResultat(val periode: ÅrMånedsperiode, val resultat: EndringSjekkGrensePeriodeBeregningResultat)

data class EndringSjekkGrensePeriodeBeregningGrunnlag(
    val beregnetBidragBeregningGrunnlag: BeregnetBidragBeregningGrunnlag,
    val løpendeBidragBeregningGrunnlag: LøpendeBidragBeregningGrunnlag?,
    val privatAvtaleBeregningGrunnlag: PrivatAvtaleBeregningGrunnlag?,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
)

data class BeregnetBidragBeregningGrunnlag(val referanse: String, val beløp: BigDecimal?)

data class LøpendeBidragBeregningGrunnlag(val referanse: String, val beløp: BigDecimal?)

data class PrivatAvtaleBeregningGrunnlag(val referanse: String, val beløp: BigDecimal?)

data class EndringSjekkGrensePeriodeBeregningResultat(
    val løpendeBidragBeløp: BigDecimal?,
    val løpendeBidragFraPrivatAvtale: Boolean = false,
    val beregnetBidragBeløp: BigDecimal?,
    val faktiskEndringFaktor: BigDecimal?,
    val endringErOverGrense: Boolean,
    val grunnlagsreferanseListe: List<String>,
)
