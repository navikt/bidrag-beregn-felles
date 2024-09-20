package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId

fun StønadsendringDto.erEndring() = Beslutningstype.ENDRING == beslutning

fun VedtakForStønad.erEndring() = Vedtakstype.ENDRING == type || stønadsendring.erEndring()

fun VedtakForStønad.erBidrag() = stønadsendring.type == Stønadstype.BIDRAG

fun VedtakForStønad.er18årsbidrag() = stønadsendring.type == Stønadstype.BIDRAG18AAR

fun VedtakForStønad.erOppfostringsbidrag() = stønadsendring.type == Stønadstype.OPPFOSTRINGSBIDRAG

fun VedtakForStønad.erInnkreving() =  stønadsendring.innkreving == Innkrevingstype.MED_INNKREVING

fun VedtakForStønad.erResultatFraAnnetVedtak() =
    this.stønadsendring.periodeListe.any { Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK.kode == it.resultatkode }

fun VedtakForStønad.erKlage() = Vedtakstype.KLAGE == type || søknadKlageRefId != null

fun VedtakForStønad.erAutomatiskVedtak() = Vedtakskilde.AUTOMATISK == kilde

fun VedtakForStønad.omgjørVedtaksid() = stønadsendring.omgjørVedtakId

fun VedtakForStønad.erOmgjøring() =
    Vedtakstype.ENDRING == type && Vedtakskilde.MANUELT == kilde && stønadsendring.omgjørVedtak() && !behandlingsreferanser.any {
        BehandlingsrefKilde.BISYS_KLAGE_REF_SØKNAD == it.kilde
    }

fun VedtakForStønad.idTilOmgjortVedtak() = stønadsendring.omgjørVedtakId?.toLong()

fun StønadsendringDto.omgjørVedtak() = omgjørVedtakId != null

fun VedtakForStønad.erIngenEndringPga12Prosentregel() = stønadsendring.erIngenEndring12Prosent()

fun StønadsendringDto.erIngenEndring12Prosent() = periodeListe.any { Beslutningsårsak.INGEN_ENDRING_12_PROSENT.kode == it.resultatkode }

fun VedtakForStønad.tilVedtaksdetaljer(): Collection<Vedtaksdetaljer> = stønadsendring.periodeListe.map {
    Vedtaksdetaljer(
        vedtak = this,
        periode = it,
    )
}

fun Collection<VedtakForStønad>.tilVedtaksdetaljer() = this.flatMap { it.tilVedtaksdetaljer() }

val bisysBatchBrukerid = "RTV9999"

enum class Beslutningsårsak(val kode: String) {
    INGEN_ENDRING_12_PROSENT("VO"),
    IKKE_FRITATT_ETTER_KLAGE("GKIF"),
    INDEKSREGULERING("IREG"),
    INNVILGETT_VEDTAK("IV"),
    KOSTNADSBEREGNET_BIDRAG("KBB"),
    RESULTAT_FRA_ANNET_VEDTAK("RAV"),
}
