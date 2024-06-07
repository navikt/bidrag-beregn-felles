package no.nav.bidrag.beregn.særtilskudd.beregning

import no.nav.bidrag.beregn.særtilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.bo.ResultatBeregning

fun interface SaertilskuddBeregning {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning

    companion object {
        fun getInstance(): SaertilskuddBeregning = SaertilskuddBeregningImpl()
    }
}
