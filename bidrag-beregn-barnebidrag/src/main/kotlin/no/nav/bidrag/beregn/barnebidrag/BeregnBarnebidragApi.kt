package no.nav.bidrag.beregn.barnebidrag

import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.springframework.stereotype.Service

/**
 * BeregnBarnebidragApi eksponerer api for å beregne barnebidrag.
 *
 * For å ta i bruk beregnings-apiet må følgende gjøres:
 *
 * Legg til Import-annotering i konfigurasjonen for å initalisere BeregnBarnebidragApi-bønnen
 * ```kotlin
 * @Import(BeregnBarnebidragApi::class)
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
class BeregnBarnebidragApi {
    private val service = BeregnBarnebidragService()

    fun beregnBidragsevne(beregnGrunnlag: BeregnGrunnlag) = service.beregnBidragsevne(beregnGrunnlag)

    fun beregnBpsAndelUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag) = service.beregnBpAndelUnderholdskostnad(beregnGrunnlag)

    fun beregnSamværsfradrag(beregnGrunnlag: BeregnGrunnlag) = service.beregnSamværsfradrag(beregnGrunnlag)
}
