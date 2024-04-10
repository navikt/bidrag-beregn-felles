package no.nav.bidrag.boforhold

import no.nav.bidrag.boforhold.dto.BoforholdRequest
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.boforhold.service.BoforholdServiceV1
import no.nav.bidrag.boforhold.service.BoforholdServiceV2
import java.time.LocalDate

/**
 * BoforholdApi eksponerer api for å beregne tidlinje for en persons bostatus.
 *
 */
class BoforholdApi {
    companion object {
        private val boforholdServiceV1 = BoforholdServiceV1()
        fun beregnV1(virkningstidspunkt: LocalDate, boforholdGrunnlagDtoListe: List<RelatertPerson>): List<BoforholdBeregnet> {
            return boforholdServiceV1.beregnEgneBarn(virkningstidspunkt, boforholdGrunnlagDtoListe)
        }
        private val boforholdServiceV2 = BoforholdServiceV2()
        fun beregnV2(virkningstidspunkt: LocalDate, boforholdRequestListe: List<BoforholdRequest>): List<BoforholdResponse> {
            return boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, boforholdRequestListe)
        }
    }
}
