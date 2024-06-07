package no.nav.bidrag.beregn.bidragsevne.beregning

import no.nav.bidrag.beregn.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bidragsevne.bo.ResultatBeregning
import java.math.BigDecimal

interface BidragsevneBeregning {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning

    fun beregnMinstefradrag(
        grunnlag: GrunnlagBeregning,
        minstefradragInntektSjablonBelop: BigDecimal,
        minstefradragInntektSjablonProsent: BigDecimal,
    ): BigDecimal

    fun beregnSkattetrinnBelop(grunnlag: GrunnlagBeregning): BigDecimal

    companion object {
        fun getInstance(): BidragsevneBeregning = BidragsevneBeregningImpl()
    }
}
