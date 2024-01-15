package no.nav.bidrag.beregn.forskudd.core.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import java.math.BigDecimal

data class BostatusPeriode(
    val referanse: String,
    val bostatusPeriode: Periode,
    val kode: Bostatuskode,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode {
        return bostatusPeriode
    }
}

data class InntektPeriode(
    val referanse: String,
    val inntektPeriode: Periode,
    val type: String,
    val belop: BigDecimal,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode {
        return inntektPeriode
    }
}

data class SivilstandPeriode(
    val referanse: String,
    val sivilstandPeriode: Periode,
    val kode: Sivilstandskode,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode {
        return sivilstandPeriode
    }
}

data class AlderPeriode(
    val referanse: String,
    val alderPeriode: Periode,
    val alder: Int,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode {
        return alderPeriode
    }
}

data class BarnIHusstandenPeriode(
    val referanse: String,
    val barnIHusstandenPeriode: Periode,
) : PeriodisertGrunnlag {
    override fun getPeriode(): Periode {
        return barnIHusstandenPeriode
    }
}
