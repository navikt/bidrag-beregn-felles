package no.nav.bidrag.indeksregulering

import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.indeksregulering.service.IndeksreguleringService
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.springframework.stereotype.Service

/**
 * IndeksreguleringdApi eksponerer api for å indeksregulere stønad.
 *
 */
@EnableSjablonProvider
@Service
class BeregnIndeksreguleringApi {
    private val service = IndeksreguleringService()
    fun beregnIndeksregulering(grunnlag: BeregnGrunnlag) = service.beregn(grunnlag)
}
