package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrenseBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag

internal object EndringSjekkGrenseBeregning {

    fun beregn(grunnlag: List<EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag>): EndringSjekkGrenseBeregningResultat {
        // Hvis minst en av endringene i grunnlaget er over grense settes resultatet til true; ellers false
        val endringErOverGrense = grunnlag.any { it.endringErOverGrense }

        return EndringSjekkGrenseBeregningResultat(
            endringErOverGrense = endringErOverGrense,
            grunnlagsreferanseListe =
            grunnlag.map { it.referanse },
        )
    }
}
