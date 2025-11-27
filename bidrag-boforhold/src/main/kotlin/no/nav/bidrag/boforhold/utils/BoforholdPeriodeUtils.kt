package no.nav.bidrag.boforhold.utils

import no.nav.bidrag.beregn.core.util.justerPeriodeTomOpphørsdato
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import java.time.LocalDate

fun List<BoforholdResponseV2>.justerBoforholdPerioderForOpphørsdatoOgBeregnTilDato(
    opphørsdato: LocalDate?,
    beregnTilDato: LocalDate?,
): List<BoforholdResponseV2> {
    if (opphørsdato == null && beregnTilDato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { beregnTilDato == null || it.periodeFom.isBefore(beregnTilDato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode && opphørsdato != null) {
                periode.copy(periodeTom = justerPeriodeTomOpphørsdato(opphørsdato))
            } else {
                if (periode.periodeTom == null || beregnTilDato == null) {
                    // Ingen endring av periodeTom
                    periode
                } else {
                    if (periode == sistePeriode && periode.periodeTom.isAfter(beregnTilDato.minusDays(1))) {
                        // Setter periodeTom = null hvis perioden løper utover beregningsperioden
                        periode.copy(periodeTom = null)
                    } else {
                        periode
                    }
                }
            }
        }
}

fun List<Bostatus>.justerBostatusPerioderForOpphørsdatoOgBeregnTilDato(opphørsdato: LocalDate?, beregnTilDato: LocalDate?): List<Bostatus> {
    if (opphørsdato == null && beregnTilDato == null) return this
    // Antar at opphørsdato er måneden perioden skal opphøre
    val filtrertePerioder = filter { it.periodeFom == null || beregnTilDato == null || it.periodeFom.isBefore(beregnTilDato) }
    val sistePeriode = filtrertePerioder.maxByOrNull { it.periodeFom!! }
    return filtrertePerioder
        .map { periode ->
            if (periode == sistePeriode && opphørsdato != null) {
                periode.copy(periodeTom = justerPeriodeTomOpphørsdato(opphørsdato))
            } else {
                if (periode.periodeTom == null || beregnTilDato == null) {
                    // Ingen endring av periodeTom
                    periode
                } else {
                    if (periode == sistePeriode && periode.periodeTom.isAfter(beregnTilDato.minusDays(1))) {
                        // Setter periodeTom = null hvis perioden løper utover beregningsperioden
                        periode.copy(periodeTom = null)
                    } else {
                        periode
                    }
                }
            }
        }
}
