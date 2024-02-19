package no.nav.bidrag.sivilstand

import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.response.SivilstandBeregningGrunnlagDto
import no.nav.bidrag.sivilstand.response.tilBeregningGrunnlagDto
import no.nav.bidrag.sivilstand.service.SivilstandService
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

/**
 * SivilstandApi eksponerer api for å beregne tidlinje for sivilstand.
 *
 */
class SivilstandApi {
    companion object {
        private val service = SivilstandService()

        @JvmName("beregn")
        fun beregn(virkningstidspunkt: LocalDate, sivilstandBeregningGrunnlagDtoListe: List<SivilstandBeregningGrunnlagDto>): SivilstandBeregnet {
            return service.beregn(virkningstidspunkt, sivilstandBeregningGrunnlagDtoListe)
        }

        @JvmName("beregnSivilstandGrunnlagDto")
        fun beregn(virkningstidspunkt: LocalDate, sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
            return beregn(virkningstidspunkt, sivilstandGrunnlagDtoListe.map { it.tilBeregningGrunnlagDto() })
        }
    }
}
