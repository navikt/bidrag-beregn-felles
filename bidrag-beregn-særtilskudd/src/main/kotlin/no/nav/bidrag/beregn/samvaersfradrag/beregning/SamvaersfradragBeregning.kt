package no.nav.bidrag.beregn.samvaersfradrag.beregning

import no.nav.bidrag.beregn.samvaersfradrag.bo.GrunnlagBeregningPeriodisert
import no.nav.bidrag.beregn.samvaersfradrag.bo.ResultatBeregning

fun interface SamvaersfradragBeregning {

    fun beregn(grunnlag: GrunnlagBeregningPeriodisert): List<ResultatBeregning>

    companion object {
        fun getInstance(): SamvaersfradragBeregning = SamvaersfradragBeregningImpl()
    }
}
