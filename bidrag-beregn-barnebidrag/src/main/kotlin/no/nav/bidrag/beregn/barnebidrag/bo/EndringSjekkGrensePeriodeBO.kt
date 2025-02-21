package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import java.math.BigDecimal

data class EndringSjekkGrensePeriodePeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val sluttberegningPeriodeGrunnlagListe: List<SluttberegningPeriodeGrunnlag>,
    val beløpshistorikkBidragPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
    val sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
)

data class SluttberegningPeriodeGrunnlag(val referanse: String, val sluttberegningPeriode: SluttberegningBarnebidrag)

data class EndringSjekkGrensePeriodePeriodeResultat(val periode: ÅrMånedsperiode, val resultat: EndringSjekkGrensePeriodeBeregningResultat)

data class EndringSjekkGrensePeriodeBeregningGrunnlag(
    val beregnetBidragBeregningGrunnlag: BeregnetBidragBeregningGrunnlag,
    val løpendeBidragBeregningGrunnlag: LøpendeBidragBeregningGrunnlag?,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
)

data class BeregnetBidragBeregningGrunnlag(val referanse: String, val beløp: BigDecimal?)

data class LøpendeBidragBeregningGrunnlag(val referanse: String, val beløp: BigDecimal?)

data class EndringSjekkGrensePeriodeBeregningResultat(
    val faktiskEndringFaktor: BigDecimal?,
    val endringErOverGrense: Boolean,
    val grunnlagsreferanseListe: List<String>,
)
