package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrenseBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag

internal object EndringSjekkGrenseBeregning {

    fun beregn(grunnlag: List<EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag>): EndringSjekkGrenseBeregningResultat {
        // Hvis minst en av endringene i grunnlaget er over grense settes resultatet til true.
        // Hvis alle løpende beløp er null og alle beregnede beløp er null eller 0 settes resultatet til true (antar da at beløpshistorikk mangler og
        // at det er avslag i hele beregningsperioden).
        // I alle andre tilfeller settes resultatet til false.
        val endringErOverGrense = grunnlag.any { it.endringErOverGrense } ||
            (
                grunnlag.all { it.løpendeBidragBeløp == null } &&
                    grunnlag.all { it.beregnetBidragBeløp == null }
                )

        return EndringSjekkGrenseBeregningResultat(
            endringErOverGrense = endringErOverGrense,
            grunnlagsreferanseListe =
            grunnlag.map { it.referanse },
        )
    }
}
