package no.nav.bidrag.indeksregulering

import no.nav.bidrag.indeksregulering.service.IndeksreguleringService
import java.time.LocalDate

/**
 * IndeksreguleringdApi eksponerer api for å beregne indeksregulere stønad.
 *
 */
class IndeksreguleringApi {
    companion object {
        private val indeksreguleringService = IndeksreguleringService()
        fun beregn(virkningstidspunkt: LocalDate, grunnlag: BeløpshistorikkGrunnlag): Unit =
            indeksreguleringService.beregn(virkningstidspunkt, grunnlag)
    }
}
