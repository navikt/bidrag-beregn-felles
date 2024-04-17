package no.nav.bidrag.sivilstand

import no.nav.bidrag.sivilstand.dto.Sivilstand
import no.nav.bidrag.sivilstand.dto.SivilstandRequest
import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.service.SivilstandServiceV1
import no.nav.bidrag.sivilstand.service.SivilstandServiceV2
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

/**
 * SivilstandApi eksponerer api for å beregne tidlinje for sivilstand.
 *
 */
class SivilstandApi {
    companion object {
        private val serviceV1 = SivilstandServiceV1()
        fun beregnV1(virkningstidspunkt: LocalDate, sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
            return serviceV1.beregn(virkningstidspunkt, sivilstandGrunnlagDtoListe)
        }
        private val serviceV2 = SivilstandServiceV2()
        fun beregnV2(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
            return serviceV2.beregn(virkningstidspunkt, sivilstandRequest)
        }
    }
}
