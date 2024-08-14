package no.nav.bidrag.beregn.særbidrag

import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.særbidrag.BeregnetSærbidragResultat
import org.springframework.stereotype.Service

/**
 * BeregnSærbidragApi eksponerer api for å beregne særbidrag.
 *
 * For å ta i bruk beregnings-apiet må følgende gjøres:
 *
 * Legg til Import-annotering i konfigurasjonen for å initalisere BeregnSærbidragApi-bønnen
 * ```kotlin
 * @Import(BeregnSærbidragApi::class)
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

    fun beregn(beregnSærbidragGrunnlag: BeregnGrunnlag, vedtakstype: Vedtakstype): BeregnetSærbidragResultat =
        service.beregn(beregnSærbidragGrunnlag, vedtakstype)
}
