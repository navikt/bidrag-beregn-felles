package no.nav.bidrag.beregn.særtilskudd.core.felles

import no.nav.bidrag.beregn.core.bo.Periode
import java.time.LocalDate

abstract class FellesPeriode {
    protected fun mergeSluttperiode(periodeListe: MutableList<Periode>, datoTil: LocalDate) {
        if (periodeListe.size > 1) {
            val nestSisteTilDato = periodeListe[periodeListe.size - 2].datoTil
            val sisteTilDato = periodeListe[periodeListe.size - 1].datoTil
            if (datoTil == nestSisteTilDato && null == sisteTilDato) {
                val nyPeriode = Periode(datoFom = periodeListe[periodeListe.size - 2].datoFom, datoTil = null)
                periodeListe.removeAt(periodeListe.size - 1)
                periodeListe.removeAt(periodeListe.size - 1)
                periodeListe.add(nyPeriode)
            }
        }
    }
}
