package no.nav.bidrag.beregn.forskudd.core.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import java.math.BigDecimal

data class BostatusPeriode(val referanse: String, val bostatusPeriode: Periode, val kode: Bostatuskode) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = bostatusPeriode
}

data class InntektPeriode(val referanse: String, val inntektPeriode: Periode, val type: String, val beløp: BigDecimal) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = inntektPeriode
}

data class SivilstandPeriode(val referanse: String, val sivilstandPeriode: Periode, val kode: Sivilstandskode) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = sivilstandPeriode
}

data class AlderPeriode(val referanse: String, val alderPeriode: Periode, val alder: Int) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = alderPeriode
}

data class BarnIHusstandenPeriode(val referanse: String, val barnIHusstandenPeriode: Periode, val antall: Int) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode = barnIHusstandenPeriode
}
