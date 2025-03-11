package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import java.math.BigDecimal

data class EndringSjekkGrensePeriodeGrunnlag(
    val endringSjekkGrensePeriodePeriodeGrunnlagListe: List<EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag>,
)

data class EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag(
    val referanse: String,
    val endringSjekkGrensePeriodePeriode: DelberegningEndringSjekkGrensePeriode,
    val referanseListe: List<String> = emptyList(),
)

data class EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag(
    val referanse: String,
    val endringErOverGrense: Boolean,
    val løpendeBidragBeløp: BigDecimal?,
    val beregnetBidragBeløp: BigDecimal?,
)

data class EndringSjekkGrenseBeregningResultat(val endringErOverGrense: Boolean, val grunnlagsreferanseListe: List<String>)
