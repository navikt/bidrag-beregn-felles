package no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import no.nav.bidrag.domene.enums.beregning.Særfradragskode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.math.BigDecimal

data class InntektPeriode(val referanse: String, val periodeDatoFraTil: Periode, val inntektType: String, val inntektBelop: BigDecimal) :
    PeriodisertGrunnlag {
    constructor(inntektPeriode: InntektPeriode) : this(
        inntektPeriode.referanse,
        inntektPeriode.periodeDatoFraTil.justerDatoer(),
        inntektPeriode.inntektType,
        inntektPeriode.inntektBelop,
    )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class SkatteklassePeriode(val referanse: String, val periodeDatoFraTil: Periode, val skatteklasse: Int) : PeriodisertGrunnlag {
    constructor(skatteklassePeriode: SkatteklassePeriode) : this(
        skatteklassePeriode.referanse,
        skatteklassePeriode.periodeDatoFraTil.justerDatoer(),
        skatteklassePeriode.skatteklasse,
    )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class BostatusPeriode(val referanse: String, val periodeDatoFraTil: Periode, val bostatusKode: Bostatuskode) : PeriodisertGrunnlag {
    constructor(
        bostatusPeriode: BostatusPeriode,
    ) : this(bostatusPeriode.referanse, bostatusPeriode.periodeDatoFraTil.justerDatoer(), bostatusPeriode.bostatusKode)

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class BarnIHusstandPeriode(val referanse: String, val periodeDatoFraTil: Periode, val antallBarn: Double) : PeriodisertGrunnlag {
    constructor(barnIHusstandPeriode: BarnIHusstandPeriode) : this(
        barnIHusstandPeriode.referanse,
        barnIHusstandPeriode.periodeDatoFraTil.justerDatoer(),
        barnIHusstandPeriode.antallBarn,
    )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class VoksneIHusstandPeriode(val referanse: String, val periodeDatoFraTil: Periode, val borMedAndre: Boolean) : PeriodisertGrunnlag {
    constructor(voksneIHusstandPeriode: VoksneIHusstandPeriode) : this(
        voksneIHusstandPeriode.referanse,
        voksneIHusstandPeriode.periodeDatoFraTil.justerDatoer(),
        voksneIHusstandPeriode.borMedAndre,
    )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class SaerfradragPeriode(val referanse: String, val periodeDatoFraTil: Periode, val saerfradragKode: Særfradragskode) : PeriodisertGrunnlag {
    constructor(saerfradragPeriode: SaerfradragPeriode) : this(
        saerfradragPeriode.referanse,
        saerfradragPeriode.periodeDatoFraTil.justerDatoer(),
        saerfradragPeriode.saerfradragKode,
    )

    override fun getPeriode(): Periode = periodeDatoFraTil
}
