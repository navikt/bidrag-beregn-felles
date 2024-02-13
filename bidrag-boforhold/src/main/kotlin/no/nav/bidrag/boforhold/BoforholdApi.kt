package no.nav.bidrag.boforhold

import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.boforhold.service.BoforholdService
import java.time.LocalDate

/**
 * BoforholdApi eksponerer api for å beregne tidlinje for en persons bostatus.
 *
 */
class BoforholdApi {
    companion object {
        private val boforholdService = BoforholdService()
        fun beregn(virkningstidspunkt: LocalDate, boforholdGrunnlagDtoListe: List<RelatertPerson>): List<BoforholdBeregnet> {
            return boforholdService.beregnEgneBarn(virkningstidspunkt, boforholdGrunnlagDtoListe)
        }
    }
}
