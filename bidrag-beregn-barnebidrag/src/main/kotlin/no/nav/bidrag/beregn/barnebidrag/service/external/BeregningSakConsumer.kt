package no.nav.bidrag.beregn.barnebidrag.service.external

import no.nav.bidrag.transport.sak.BidragssakDto

interface BeregningSakConsumer {

    fun hentSak(verdi: String): BidragssakDto
}
