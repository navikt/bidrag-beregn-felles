package no.nav.bidrag.beregn.sivilstand

import no.nav.bidrag.beregn.sivilstand.service.SivilstandService
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

    fun beregn(sivilstandGrunnlagDto: SivilstandGrunnlagDto): SivilstandBeregnet {
        return service.beregn(sivilstandGrunnlagDto)
    }
}
