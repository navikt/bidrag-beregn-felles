package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningResultat
import java.math.BigDecimal

internal object NettoTilsynsutgiftBeregning {
    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat = NettoTilsynsutgiftBeregningResultat(
        beløp = BigDecimal.ZERO,
        grunnlagsreferanseListe = emptyList(),
    )
}
