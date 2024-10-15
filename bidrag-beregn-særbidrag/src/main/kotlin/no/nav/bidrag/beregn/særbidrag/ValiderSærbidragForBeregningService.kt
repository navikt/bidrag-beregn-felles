package no.nav.bidrag.beregn.særbidrag

import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import org.springframework.stereotype.Service

@EnableSjablonProvider
@Service
class ValiderSærbidragForBeregningService {
    private val service = BeregnSærbidragService()

    fun validerForBeregning(vedtakstype: Vedtakstype, delberegningUtgift: DelberegningUtgift): Resultatkode? =
        service.validerForBeregning(vedtakstype, delberegningUtgift)

    // Service for å sjekke at alle løpende bidrag har gyldig valutakode for beregning. Kun "NOK" er gyldig i starten.
    fun validerGyldigValuta(løpendeBidragGrunnlag: LøpendeBidragGrunnlag): Boolean = service.validerValutakode(løpendeBidragGrunnlag)
}
