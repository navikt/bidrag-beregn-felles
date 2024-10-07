package no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class BetaltAvBpPeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class BidragsevnePeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class SumLøpendeBidragPeriode(val referanse: String, private val periode: Periode, val sumLøpendeBidrag: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class BPsAndelSærbidragPeriode(
    val referanse: String,
    private val periode: Periode,
    val andelFaktor: BigDecimal,
    val andelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}
