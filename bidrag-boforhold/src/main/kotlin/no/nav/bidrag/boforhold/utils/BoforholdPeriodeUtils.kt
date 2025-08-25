package no.nav.bidrag.boforhold.utils

import no.nav.bidrag.beregn.core.util.justerPeriodeTomOpphørsdato
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import java.time.LocalDate

fun List<BoforholdResponseV2>.justerBoforholdPerioderForOpphørsdato(opphørsdato: LocalDate?, beregnTilDato: LocalDate?): List<BoforholdResponseV2> {
    if (opphørsdato == null && beregnTilDato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { it.periodeFom.isBefore(beregnTilDato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode && opphørsdato != null) {
                periode.copy(periodeTom = justerPeriodeTomOpphørsdato(opphørsdato))
            } else {
                periode
            }
        }
}
fun List<Bostatus>.justerBostatusPerioderForOpphørsdato(opphørsdato: LocalDate?, beregnTilDato: LocalDate?): List<Bostatus> {
    if (opphørsdato == null && beregnTilDato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { it.periodeFom!!.isBefore(beregnTilDato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom!! }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode && opphørsdato != null) {
                periode.copy(periodeTom = justerPeriodeTomOpphørsdato(opphørsdato))
            } else {
                periode
            }
        }
}
