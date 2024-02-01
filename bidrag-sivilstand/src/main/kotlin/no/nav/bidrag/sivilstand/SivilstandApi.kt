package no.nav.bidrag.sivilstand

import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.service.SivilstandService
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * SivilstandApi eksponerer api for å beregne tidlinje for sivilstand.
 *
 */
@Service
class SivilstandApi {
    private val service = SivilstandService()

    fun beregn(virkningstidspunkt: LocalDate, sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
        return service.beregn(virkningstidspunkt, sivilstandGrunnlagDtoListe)
    }
}
