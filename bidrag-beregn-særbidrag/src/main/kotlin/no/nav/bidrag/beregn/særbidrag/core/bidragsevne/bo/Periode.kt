package no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class InntektPeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class BarnIHusstandPeriode(val referanse: String, private val periode: Periode, val antall: Double) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class VoksneIHusstandPeriode(val referanse: String, private val periode: Periode, val borMedAndre: Boolean) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}
