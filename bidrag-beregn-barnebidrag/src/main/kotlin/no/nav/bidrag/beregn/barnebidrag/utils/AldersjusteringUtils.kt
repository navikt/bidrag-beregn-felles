package no.nav.bidrag.beregn.barnebidrag.utils

import java.time.LocalDate
import java.time.YearMonth

val aldersjusteringAldersgrupper = listOf(6, 11, 15)

object AldersjusteringUtils {
    fun skalAldersjusteres(fødselsdato: LocalDate, aldersjusteresForÅr: Int = YearMonth.now().year): Boolean {
        val alder = aldersjusteresForÅr - fødselsdato.year
        return aldersjusteringAldersgrupper.contains(alder)
    }
}
