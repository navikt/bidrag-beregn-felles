package no.nav.bidrag.beregn.core

import no.nav.bidrag.beregn.core.inntekt.service.BeregnInntektService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnValgteInntekterGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnValgteInntekterResultat
import org.springframework.stereotype.Service

/**
 * BeregnApi eksponerer api for fellesfunksjoner i beregning.
 *
 * For å ta i bruk beregning apiet må følgende gjøres:
 *
 * Legg til Import annotering i konfigurasjonen for å initalisere BeregnApi bønnen
 * ```kotlin
 * @Import(BeregnApi::class)
 * ```
 *
 * Definer BIDRAG_SJABLON_URL miljøvariabler i nais konfigurasjonen.
 * ```yaml
 *   BIDRAG_SJABLON_URL: https://bidrag-sjablon.<prod-fss|dev-fss>-pub.nais.io/bidrag-sjablon
 * ```
 *
 *  Åpne outbound traffik for `BIDRAG_SJABLON_URL` i nais konfigurasjonen
 */
@EnableSjablonProvider
@Service
class BeregnApi {
    private val inntektService = BeregnInntektService()

    fun beregnInntekt(beregnForskuddGrunnlag: BeregnValgteInntekterGrunnlag): BeregnValgteInntekterResultat =
        inntektService.beregn(beregnForskuddGrunnlag)
}
