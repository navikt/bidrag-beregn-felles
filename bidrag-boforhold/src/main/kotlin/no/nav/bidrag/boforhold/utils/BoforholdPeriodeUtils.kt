package no.nav.bidrag.boforhold.utils

import no.nav.bidrag.beregn.core.util.justerPeriodeTilOpphørsdato
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import java.time.LocalDate

fun List<BoforholdResponseV2>.justerBoforholdPerioderForOpphørsdato(opphørsdato: LocalDate?): List<BoforholdResponseV2> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { it.periodeFom.isBefore(opphørsdato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode) {
                periode.copy(periodeTom = justerPeriodeTilOpphørsdato(opphørsdato))
            } else {
                periode
            }
        }
}
fun List<Bostatus>.justerBostatusPerioderForOpphørsdato(opphørsdato: LocalDate?): List<Bostatus> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { it.periodeFom!!.isBefore(opphørsdato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom!! }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode) {
                periode.copy(periodeTom = justerPeriodeTilOpphørsdato(opphørsdato))
            } else {
                periode
            }
        }
}
