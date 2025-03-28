package no.nav.bidrag.beregn.barnebidrag.service.external

import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.response.HentVedtakForStønadResponse
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto

interface BeregningVedtakConsumer {
    fun hentVedtakForStønad(hentVedtakForStønadRequest: HentVedtakForStønadRequest): HentVedtakForStønadResponse
    fun hentVedtak(vedtaksid: Int): VedtakDto?
}
