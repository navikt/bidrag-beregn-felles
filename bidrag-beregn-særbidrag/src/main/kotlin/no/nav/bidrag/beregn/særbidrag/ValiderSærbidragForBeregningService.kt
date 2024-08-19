package no.nav.bidrag.beregn.særbidrag

import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import org.springframework.stereotype.Service

@EnableSjablonProvider
@Service
class ValiderSærbidragForBeregningService {
    private val service = BeregnSærbidragService()

    fun validerForBeregning(vedtakstype: Vedtakstype, delberegningUtgift: DelberegningUtgift): Resultatkode? =
        service.validerForBeregning(vedtakstype, delberegningUtgift)
}
