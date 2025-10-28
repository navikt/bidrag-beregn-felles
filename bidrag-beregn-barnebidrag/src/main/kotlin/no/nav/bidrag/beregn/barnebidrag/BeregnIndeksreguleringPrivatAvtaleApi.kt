package no.nav.bidrag.beregn.barnebidrag

import no.nav.bidrag.beregn.barnebidrag.service.beregning.BeregnIndeksreguleringPrivatAvtaleService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.springframework.stereotype.Service

@EnableSjablonProvider
@Service
class BeregnIndeksreguleringPrivatAvtaleApi {

    private val service = BeregnIndeksreguleringPrivatAvtaleService

    fun beregnIndeksreguleringPrivatAvtale(beregnGrunnlag: BeregnGrunnlag) = service.delberegningPrivatAvtalePeriode(beregnGrunnlag)
}
