package no.nav.bidrag.boforhold.utils

import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import java.time.LocalDate

fun List<BoforholdResponseV2>.justerBoforholdPerioderForOpphørsdato(opphørsdato: LocalDate?): List<BoforholdResponseV2> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val justerOpphørsdato = opphørsdato.withDayOfMonth(1).minusDays(1)
    return filter {
        it.periodeFom.isBefore(opphørsdato)
    }
        .map { grunnlag ->
            if (grunnlag.periodeTom == null || grunnlag.periodeTom.isAfter(justerOpphørsdato)) {
                grunnlag.copy(periodeTom = justerOpphørsdato)
            } else {
                grunnlag
            }
        }
}
fun List<Bostatus>.justerBostatusPerioderForOpphørsdato(opphørsdato: LocalDate?): List<Bostatus> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val justerOpphørsdato = opphørsdato.withDayOfMonth(1).minusDays(1)
    return filter {
        it.periodeFom!!.isBefore(justerOpphørsdato)
    }
        .map { grunnlag ->
            if (grunnlag.periodeTom == null || grunnlag.periodeTom.isAfter(justerOpphørsdato)) {
                grunnlag.copy(periodeTom = justerOpphørsdato)
            } else {
                grunnlag
            }
        }
}
