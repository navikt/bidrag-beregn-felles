package no.nav.bidrag.boforhold

import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.boforhold.service.BoforholdBarnServiceV2
import no.nav.bidrag.boforhold.service.BoforholdServiceV1
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
        private val boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        fun beregnBoforholdBarnV2(virkningstidspunkt: LocalDate, boforholdBarnRequestListe: List<BoforholdBarnRequest>): List<BoforholdResponse> {
            return boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, boforholdBarnRequestListe)
        }
    }
}
