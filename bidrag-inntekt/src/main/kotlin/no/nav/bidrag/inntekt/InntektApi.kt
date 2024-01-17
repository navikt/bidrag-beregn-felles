package no.nav.bidrag.inntekt

import jakarta.annotation.PostConstruct
import no.nav.bidrag.commons.service.KodeverkProvider
import no.nav.bidrag.inntekt.service.InntektService
import no.nav.bidrag.transport.behandling.inntekt.request.TransformerInntekterRequest
import no.nav.bidrag.transport.behandling.inntekt.response.TransformerInntekterResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * InntektApi eksponerer api for å transformere inntekter som hentes fra bidrag-grunnlag
 *
 * For å ta i bruk apiet må følgende gjøres:
 *
 * Legg til Import annotering i konfigurasjonen for å initalisere InntektApi bønnen
 * ```kotlin
 * @Import(InntektApi::class)
 * ```
 *
 * Definer KODEVERK_URL miljøvariabler i nais konfigurasjonen.
 * ```yaml
 *   KODEVERK_URL: https://kodeverk.<dev|prod>-fss-pub.nais.io
 * ```
 *
 *  Åpne outbound traffik for `KODEVERK_URL` i nais konfigurasjonen
 */
@Service
class InntektApi(
    @Value("\${KODEVERK_URL}") val url: String,
) {
    private val inntektService: InntektService = InntektService()

    @PostConstruct
    fun initKodeverkCache() {
        KodeverkProvider.initialiser(url)
    }

    fun transformerInntekter(request: TransformerInntekterRequest): TransformerInntekterResponse {
        return inntektService.transformerInntekter(request)
    }
}
