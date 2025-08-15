package no.nav.bidrag.indeksregulering

import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.indeksregulering.service.BeregnIndeksreguleringGrunnlag
import no.nav.bidrag.indeksregulering.service.BeregnIndeksreguleringService
import org.springframework.stereotype.Service

/**
 * BeregnIndeksreguleringApi eksponerer api for å indeksregulere stønad.
 *
 */
@EnableSjablonProvider
@Service
class BeregnIndeksreguleringApi {
    private val service = BeregnIndeksreguleringService()
    fun beregnIndeksreguleringBarnebidrag(grunnlag: BeregnIndeksreguleringGrunnlag) = service.beregnIndeksreguleringBarnebidrag(grunnlag)

    fun beregnIndeksreguleringForskudd(grunnlag: BeregnIndeksreguleringGrunnlag) = service.beregnIndeksreguleringForskudd(grunnlag)
}
