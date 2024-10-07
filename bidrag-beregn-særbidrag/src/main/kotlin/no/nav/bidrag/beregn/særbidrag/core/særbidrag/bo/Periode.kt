package no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import java.math.BigDecimal

data class BetaltAvBpPeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class BidragsevnePeriode(
    val referanse: String,
    private val periode: Periode,
    val beløp: BigDecimal,
    val skatt: DelberegningBidragsevne.Skatt,
    val underholdBarnEgenHusstand: BigDecimal,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class SumLøpendeBidragPeriode(val referanse: String, private val periode: Periode, val sumLøpendeBidrag: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}

data class BPsAndelSærbidragPeriode(
    val referanse: String,
    private val periode: Periode,
    val endeligAndelFaktor: BigDecimal,
    val andelBeløp: BigDecimal,
    val beregnetAndelFaktor: BigDecimal,
    val barnEndeligInntekt: BigDecimal,
    val barnetErSelvforsørget: Boolean,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = periode
}
