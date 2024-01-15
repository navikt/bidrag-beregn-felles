package no.nav.bidrag.beregn.core.bo

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Periode(
    var datoFom: LocalDate,
    val datoTil: LocalDate?,
) : PeriodisertGrunnlag {
    companion object {
        // Juster dato til den første i neste måned (hvis ikke dato er den første i inneværende måned)
        internal fun justerDato(dato: LocalDate?): LocalDate? {
            return if (dato == null || dato.dayOfMonth == 1) dato else dato.with(TemporalAdjusters.firstDayOfNextMonth())
        }
    }

    constructor(periode: Periode) : this(justerDato(periode.datoFom) ?: periode.datoFom, justerDato(periode.datoTil))

    override fun getPeriode(): Periode {
        return this
    }

    // Sjekker at en denne perioden overlapper med annenPeriode (intersect)
    fun overlapperMed(annenPeriode: Periode): Boolean {
        return (
            (annenPeriode.datoTil == null || datoFom.isBefore(annenPeriode.datoTil)) &&
                (datoTil == null || datoTil.isAfter(annenPeriode.datoFom))
        )
    }

    // Sjekk om perioden overlapper (datoFom i denne perioden kommer tidligere enn datoTil i forrige periode)
    // Hvis forrige periode er null, er dette den første perioden. Ingen kontroll nødvendig
    fun overlapper(forrigePeriode: Periode?): Boolean {
        if (forrigePeriode?.datoTil == null) return false

        return datoFom.isBefore(forrigePeriode.datoTil)
    }

    // Sjekk om det er opphold (gap) mellom periodene (datoFom i denne perioden kommer senere enn datoTil i forrige periode)
    // Legger en dag til datoTil for å ikke feilmarkere scenarioer hvor datoTil er satt til siste dag i en måned
    // Hvis forrige periode er null, er dette den første perioden. Ingen kontroll nødvendig
    fun harOpphold(forrigePeriode: Periode?): Boolean {
        if (forrigePeriode == null) return false

        return forrigePeriode.datoTil != null && datoFom.isAfter(forrigePeriode.datoTil.plusDays(1))
    }

    // Sjekk om datoFom er tidligere eller lik datoTil
    fun datoTilErEtterDatoFom(): Boolean {
        return datoTil == null || datoTil.isAfter(datoFom)
    }

    // Juster datoer i perioden
    fun justerDatoer(): Periode {
        val datoFom = justerDato(datoFom)
        val datoTil = justerDato(datoTil)

        return Periode(datoFom as LocalDate, datoTil)
    }
}
