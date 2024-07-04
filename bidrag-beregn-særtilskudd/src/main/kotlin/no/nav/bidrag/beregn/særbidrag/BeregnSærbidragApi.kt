package no.nav.bidrag.beregn.særbidrag

import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.særtilskudd.BeregnetSærtilskuddResultat
import org.springframework.stereotype.Service

/**
 * BeregnSærtilskuddApi eksponerer api for å beregne forskudd.
 *
 * For å ta i bruk beregnings-apiet må følgende gjøres:
 *
 * Legg til Import-annotering i konfigurasjonen for å initalisere BeregnSærtilskuddApi-bønnen
 * ```kotlin
 * @Import(BeregnSærtilskuddApi::class)
 * ```
 *
 * Definer BIDRAG_SJABLON_URL miljøvariabler i naiskonfigurasjonen.
 * ```yaml
 *   BIDRAG_SJABLON_URL: https://bidrag-sjablon.<prod-fss|dev-fss>-pub.nais.io/bidrag-sjablon
 * ```
 *
 *  Åpne outbound traffik for `BIDRAG_SJABLON_URL` i naiskonfigurasjonen
 */
@EnableSjablonProvider
@Service
class BeregnSærbidragApi {
    private val service = BeregnSærbidragService()

    fun beregn(beregnSærtilskuddGrunnlag: BeregnGrunnlag): BeregnetSærtilskuddResultat = service.beregn(beregnSærtilskuddGrunnlag)
}
