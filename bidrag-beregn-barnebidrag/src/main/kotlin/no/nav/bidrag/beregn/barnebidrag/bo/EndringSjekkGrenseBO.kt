package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode

data class EndringSjekkGrensePeriodeGrunnlag(
    val endringSjekkGrensePeriodePeriodeGrunnlagListe: List<EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag>,
)

data class EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag(
    val referanse: String,
    val endringSjekkGrensePeriodePeriode: DelberegningEndringSjekkGrensePeriode
)

data class EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag(val referanse: String, val endringErOverGrense: Boolean)

data class EndringSjekkGrenseBeregningResultat(
    val endringErOverGrense: Boolean,
    val grunnlagsreferanseListe: List<String>,
)
