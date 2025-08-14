package no.nav.bidrag.beregn.core.service.external

import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto

interface BeregningBeløpshistorikkConsumer {
    fun hentHistoriskeStønader(hentStønadHistoriskRequest: HentStønadHistoriskRequest): StønadDto?
    fun hentLøpendeStønad(hentStønadRequest: HentStønadRequest): StønadDto?
}
