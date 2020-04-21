package no.nav.bidrag.beregn.bidragsevne.bo

import java.math.BigDecimal

data class ResultatBeregning(
    val resultatBelopEvne: Double

) {
  // Sjekker om 2 tilgrensende perioder kan merges fordi resultatet er det samme
  fun kanMergesMed(periodeResultatForrige: ResultatBeregning): Boolean {
    return resultatBelopEvne == periodeResultatForrige.resultatBelopEvne
  }
}