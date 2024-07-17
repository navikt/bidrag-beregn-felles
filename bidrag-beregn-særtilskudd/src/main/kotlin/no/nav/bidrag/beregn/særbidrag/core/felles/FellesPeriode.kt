package no.nav.bidrag.beregn.særbidrag.core.felles

import no.nav.bidrag.beregn.core.bo.Periode
import java.time.LocalDate

abstract class FellesPeriode {
    protected fun mergeSluttperiode(periodeListe: MutableList<Periode>, datoTil: LocalDate, åpenSluttperiode: Boolean = true) {
        if (periodeListe.size > 1) {
            val nestSistePeriode = periodeListe[periodeListe.size - 2]
            val sistePeriode = periodeListe.last()

            if (datoTil == nestSistePeriode.datoTil && sistePeriode.datoTil == null) {
                val nyDatoTil = if (åpenSluttperiode) null else datoTil
                periodeListe[periodeListe.size - 2] = Periode(datoFom = nestSistePeriode.datoFom, datoTil = nyDatoTil)
                periodeListe.removeAt(periodeListe.size - 1)
            }
        }
    }
}
