package no.nav.bidrag.boforhold.utils

import no.nav.bidrag.beregn.core.util.justerPeriodeTilOpphørsdato
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import java.time.LocalDate

fun List<BoforholdResponseV2>.justerBoforholdPerioderForOpphørsdato(opphørsdato: LocalDate?): List<BoforholdResponseV2> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    return filter {
        it.periodeFom.isBefore(opphørsdato)
    }
        .map { grunnlag ->
            if (grunnlag.periodeTom == null || grunnlag.periodeTom.isAfter(opphørsdato)) {
                grunnlag.copy(periodeTom = justerPeriodeTilOpphørsdato(opphørsdato))
            } else {
                grunnlag
            }
        }
}
fun List<Bostatus>.justerBostatusPerioderForOpphørsdato(opphørsdato: LocalDate?): List<Bostatus> {
    if (opphørsdato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    return filter {
        it.periodeFom!!.isBefore(opphørsdato)
    }
        .map { grunnlag ->
            if (grunnlag.periodeTom == null || grunnlag.periodeTom.isAfter(opphørsdato)) {
                grunnlag.copy(periodeTom = justerPeriodeTilOpphørsdato(opphørsdato))
            } else {
                grunnlag
            }
        }
}
