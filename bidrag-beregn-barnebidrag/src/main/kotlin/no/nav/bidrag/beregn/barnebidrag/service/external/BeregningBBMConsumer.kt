package no.nav.bidrag.beregn.barnebidrag.service.external

import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningRequestDto
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto

interface BeregningBBMConsumer {
    fun hentBeregning(request: BidragBeregningRequestDto): BidragBeregningResponsDto

    fun hentAlleBeregninger(request: BidragBeregningRequestDto): BidragBeregningResponsDto
}
