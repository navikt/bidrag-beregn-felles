package no.nav.bidrag.boforhold

import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.BoforholdBarnRequestV3
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.BoforholdVoksneRequest
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.boforhold.service.BoforholdAndreVoksneService
import no.nav.bidrag.boforhold.service.BoforholdBarnServiceV2
import no.nav.bidrag.boforhold.service.BoforholdBarnServiceV3
import no.nav.bidrag.boforhold.service.BoforholdServiceV1
import no.nav.bidrag.domene.enums.behandling.TypeBehandling
import java.time.LocalDate

/**
 * BoforholdApi eksponerer api for å beregne tidlinje for en persons bostatus.
 *
 */
class BoforholdApi {
    companion object {
        private val boforholdServiceV1 = BoforholdServiceV1()
        fun beregnV1(virkningstidspunkt: LocalDate, boforholdGrunnlagDtoListe: List<RelatertPerson>): List<BoforholdBeregnet> =
            boforholdServiceV1.beregnEgneBarn(virkningstidspunkt, boforholdGrunnlagDtoListe)

        private val boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        fun beregnBoforholdBarnV2(virkningstidspunkt: LocalDate, boforholdBarnRequestListe: List<BoforholdBarnRequest>): List<BoforholdResponse> =
            boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, boforholdBarnRequestListe)

        private val boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        fun beregnBoforholdBarnV3(
            virkningstidspunkt: LocalDate,
            // Angir hvilken type behandling som kaller beregningen
            typeBehandling: TypeBehandling? = TypeBehandling.FORSKUDD,
            boforholdBarnRequestV3Liste: List<BoforholdBarnRequestV3>,
        ): List<BoforholdResponseV2> = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, typeBehandling, boforholdBarnRequestV3Liste)

        private val boforholdAndreVoksneService = BoforholdAndreVoksneService()
        fun beregnBoforholdAndreVoksne(virkningstidspunkt: LocalDate, boforholdVoksneRequest: BoforholdVoksneRequest): List<Bostatus> =
            boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, boforholdVoksneRequest)
    }
}
