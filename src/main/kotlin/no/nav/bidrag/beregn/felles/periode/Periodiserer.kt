package no.nav.bidrag.beregn.felles.periode

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import java.time.LocalDate
import java.util.stream.Collectors

class Periodiserer {
    private val bruddpunkter: MutableSet<LocalDate> = HashSet()
    private var aapenSluttdato = false

    fun addBruddpunkt(dato: LocalDate): Periodiserer {
        bruddpunkter.add(dato)
        return this
    }

    private fun addBruddpunkter(periode: Periode) {
        addBruddpunkt(periode.datoFom)

        if (periode.datoTil == null) {
            aapenSluttdato = true
        } else {
            addBruddpunkt(periode.datoTil)
        }
    }

    fun addBruddpunkter(grunnlag: PeriodisertGrunnlag): Periodiserer {
        addBruddpunkter(grunnlag.getPeriode())
        return this
    }

    fun addBruddpunkter(grunnlagListe: Iterable<PeriodisertGrunnlag>): Periodiserer {
        for (grunnlag in grunnlagListe) {
            addBruddpunkter(grunnlag)
        }

        return this
    }

    // Setter perioder basert på fra- og til-dato
    fun finnPerioder(
        beregnDatoFom: LocalDate,
        beregnDatoTil: LocalDate,
    ): List<Periode> {
        val sortertBruddpunktListe =
            bruddpunkter.stream()
                .filter { dato: LocalDate -> dato.isAfter(beregnDatoFom.minusDays(1)) }
                .filter { dato: LocalDate -> dato.isBefore(beregnDatoTil.plusDays(1)) }
                .sorted().collect(Collectors.toList())

        val perioder: MutableList<Periode> = ArrayList()
        val bruddpunktIt = sortertBruddpunktListe.iterator()

        if (bruddpunktIt.hasNext()) {
            var start: LocalDate? = bruddpunktIt.next()

            while (bruddpunktIt.hasNext()) {
                val end = bruddpunktIt.next()
                perioder.add(Periode(start!!, end))
                start = end
            }

            if (aapenSluttdato) {
                perioder.add(Periode(start!!, null))
            }
        }

        return perioder
    }
}
