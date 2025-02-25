package no.nav.bidrag.indeksregulering

import no.nav.bidrag.indeksregulering.service.IndeksreguleringService
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

/**
 * IndeksreguleringdApi eksponerer api for å beregne indeksregulere stønad.
 *
 */
class IndeksreguleringApi {
    companion object {
        private val indeksreguleringService = IndeksreguleringService()
        fun beregn(grunnlag: BeregnGrunnlag): List<GrunnlagDto> = indeksreguleringService.beregn(grunnlag)
    }
}
