package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.grunnlag.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.resultat.BidragsevneBeregningResultat
import java.math.BigDecimal

internal object BidragsevneBeregning {
    fun beregn(bidragsevneBeregningGrunnlag: BidragsevneBeregningGrunnlag): BidragsevneBeregningResultat =
        BidragsevneBeregningResultat(beløp = BigDecimal.ZERO, inntekt25Prosent = BigDecimal.ZERO)
}
