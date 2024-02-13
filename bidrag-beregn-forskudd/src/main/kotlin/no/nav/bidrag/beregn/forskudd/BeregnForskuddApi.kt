package no.nav.bidrag.beregn.forskudd

import no.nav.bidrag.beregn.forskudd.service.BeregnForskuddService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import org.springframework.stereotype.Service

/**
 * BeregnForskuddApi eksponerer api for å beregne forskudd.
 *
 * For å ta i bruk beregning apiet må følgende gjøres:
 *
 * Legg til Import annotering i konfigurasjonen for å initalisere BeregnForskuddApi bønnen
 * ```kotlin
 * @Import(BeregnForskuddApi::class)
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
class BeregnForskuddApi {
    private val service = BeregnForskuddService()

    fun beregn(beregnForskuddGrunnlag: BeregnGrunnlag): BeregnetForskuddResultat {
        return service.beregn(beregnForskuddGrunnlag)
    }
}
