package no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class BidragsevnePeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    constructor(bidragsevnePeriode: BidragsevnePeriode) :
        this(
            bidragsevnePeriode.referanse,
            bidragsevnePeriode.periode.justerDatoer(),
            bidragsevnePeriode.beløp,
        )

    override fun getPeriode(): Periode = periode
}

data class BPsAndelSærbidragPeriode(
    val referanse: String,
    private val periode: Periode,
    val andelProsent: BigDecimal,
    val andelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
) : PeriodisertGrunnlag {
    constructor(bPsAndelSærbidragPeriode: BPsAndelSærbidragPeriode) :
        this(
            bPsAndelSærbidragPeriode.referanse,
            bPsAndelSærbidragPeriode.periode.justerDatoer(),
            bPsAndelSærbidragPeriode.andelProsent,
            bPsAndelSærbidragPeriode.andelBeløp,
            bPsAndelSærbidragPeriode.barnetErSelvforsørget,
        )

    override fun getPeriode(): Periode = periode
}
