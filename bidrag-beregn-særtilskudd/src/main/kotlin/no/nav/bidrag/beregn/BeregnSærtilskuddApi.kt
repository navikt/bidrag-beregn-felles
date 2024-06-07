package no.nav.bidrag.beregn

import no.nav.bidrag.beregn.service.BeregnSærtilskuddService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
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
class BeregnSærtilskuddApi {
    private val service = BeregnSærtilskuddService()

    fun beregn(beregnSærtilskuddGrunnlag: BeregnGrunnlag): BeregnetTotalSaertilskuddResultat {
        return service.beregn(beregnSærtilskuddGrunnlag)
    }
}
