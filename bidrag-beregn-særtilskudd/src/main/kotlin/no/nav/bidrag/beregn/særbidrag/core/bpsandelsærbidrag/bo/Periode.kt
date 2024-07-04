package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class UtgiftPeriode(val referanse: String, private val periode: Periode, val beløp: BigDecimal) : PeriodisertGrunnlag {
    constructor(utgiftPeriode: UtgiftPeriode) : this(
        utgiftPeriode.referanse,
        utgiftPeriode.periode.justerDatoer(),
        utgiftPeriode.beløp,
    )

    override fun getPeriode(): Periode = periode
}

data class InntektPeriode(val referanse: String, private val periode: Periode, val type: String, val beløp: BigDecimal) : PeriodisertGrunnlag {
    constructor(inntektPeriode: InntektPeriode) : this(
        inntektPeriode.referanse,
        inntektPeriode.periode.justerDatoer(),
        inntektPeriode.type,
        inntektPeriode.beløp,
    )

    override fun getPeriode(): Periode = periode
}
