package no.nav.bidrag.beregn.felles.periode

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.ArrayList
import java.util.HashSet
import java.util.stream.Collectors

class Periodiserer {
    internal val bruddpunkter: MutableSet<LocalDate> = HashSet()
    internal var aapenSluttdato = false

    fun addBruddpunkt(dato: LocalDate): Periodiserer {
        bruddpunkter.add(dato)
        return this
    }

    private fun addBruddpunkter(periode: Periode) {
        addBruddpunkt(periode.datoFra)

        if (periode.datoTil == null) {
            aapenSluttdato = true
        } else {
            addBruddpunkt(periode.datoTil)
        }
    }

    fun addBruddpunkter(grunnlag: PeriodisertGrunnlag): Periodiserer {
        addBruddpunkter(grunnlag.getDatoFraTil())
        return this
    }

    fun addBruddpunkter(grunnlagListe: Iterable<PeriodisertGrunnlag>): Periodiserer {
        for (grunnlag in grunnlagListe) {
            addBruddpunkter(grunnlag)
        }

        return this
    }

    // Genererer brudd når søknadsbarnet passerer 11 år og 18 år
    // 0 og 11 år justeres til den første inneværende måned, 18 år justeres til den første neste måned
    fun addBruddpunkter(fodselDato: LocalDate, beregnDatoFra: LocalDate, beregnDatoTil: LocalDate): Periodiserer {
        val barn11AarDato = fodselDato.plusYears(11).with(TemporalAdjusters.firstDayOfMonth())
        val barn18AarDato = fodselDato.plusYears(18).with(TemporalAdjusters.firstDayOfNextMonth())
        val barn11AarIPerioden = barn11AarDato.isAfter(beregnDatoFra.minusDays(1)) && barn11AarDato.isBefore(beregnDatoTil.plusDays(1))
        val barn18AarIPerioden = barn18AarDato.isAfter(beregnDatoFra.minusDays(1)) && barn18AarDato.isBefore(beregnDatoTil.plusDays(1))

        if (barn11AarIPerioden) {
            addBruddpunkt(barn11AarDato)
        }

        if (barn18AarIPerioden) {
            addBruddpunkt(barn18AarDato)
        }

        return this
    }

    // Setter perioder basert på fra- og til-dato
    fun finnPerioder(beregnDatoFra: LocalDate, beregnDatoTil: LocalDate): List<Periode> {
        val sortertBruddpunktListe = bruddpunkter.stream()
                .filter { dato: LocalDate -> dato.isAfter(beregnDatoFra.minusDays(1)) }
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
