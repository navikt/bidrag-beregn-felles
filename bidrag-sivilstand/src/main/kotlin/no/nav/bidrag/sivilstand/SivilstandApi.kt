package no.nav.bidrag.sivilstand

import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.service.SivilstandService
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import org.springframework.stereotype.Service

/**
 * SivilstandApi eksponerer api for å beregne tidlinje for sivilstand.
 *
 * For å ta i bruk beregning apiet må følgende gjøres:
 *
 * Legg til Import annotering i konfigurasjonen for å initalisere SivilstandApi-bønnen
 * ```kotlin
 * @Import(SivilstandApi::class)
 */
@Service
class SivilstandApi {
    private val service = SivilstandService()

    fun beregn(sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
        return service.beregn(sivilstandGrunnlagDtoListe)
    }
}
