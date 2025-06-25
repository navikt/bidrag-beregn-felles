package no.nav.bidrag.beregn.barnebidrag.service.external

import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto

interface BeregningStønadConsumer {
    fun hentHistoriskeStønader(hentStønadHistoriskRequest: HentStønadHistoriskRequest): StønadDto?
}
