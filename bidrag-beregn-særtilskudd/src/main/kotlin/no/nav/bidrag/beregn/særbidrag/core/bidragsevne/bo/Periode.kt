package no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class InntektPeriode(val referanse: String, private val periode: Periode, val type: String, val beløp: BigDecimal) : PeriodisertGrunnlag {
    constructor(inntektPeriode: InntektPeriode) : this(
        inntektPeriode.referanse,
        inntektPeriode.periode.justerDatoer(),
        inntektPeriode.type,
        inntektPeriode.beløp,
    )

    override fun getPeriode(): Periode = periode
}

data class BarnIHusstandPeriode(val referanse: String, private val periode: Periode, val antall: Double) : PeriodisertGrunnlag {
    constructor(barnIHusstandPeriode: BarnIHusstandPeriode) : this(
        barnIHusstandPeriode.referanse,
        barnIHusstandPeriode.periode.justerDatoer(),
        barnIHusstandPeriode.antall,
    )

    override fun getPeriode(): Periode = periode
}

data class VoksneIHusstandPeriode(val referanse: String, private val periode: Periode, val borMedAndre: Boolean) : PeriodisertGrunnlag {
    constructor(voksneIHusstandPeriode: VoksneIHusstandPeriode) : this(
        voksneIHusstandPeriode.referanse,
        voksneIHusstandPeriode.periode.justerDatoer(),
        voksneIHusstandPeriode.borMedAndre,
    )

    override fun getPeriode(): Periode = periode
}
