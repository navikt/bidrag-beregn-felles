package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId

fun StønadsendringDto.erEndring() = Beslutningstype.ENDRING == this.beslutning

fun VedtakForStønad.erEndring() = Vedtakstype.ENDRING == this.type || this.stønadsendring.erEndring()

private fun List<StønadsendringDto>.erAvvisning() = this.find { Beslutningstype.AVVIST == it.beslutning } != null

private fun List<StønadsendringDto>.erStadfestelse() = this.any {
    Beslutningstype.STADFESTELSE == it.beslutning
} || this.any { p -> p.periodeListe.any { Beslutningsårsak.IKKE_FRITATT_ETTER_KLAGE.kode == it.resultatkode } }

fun VedtakForStønad.erResultatFraAnnetVedtak() =
    this.stønadsendring.periodeListe.any { Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK.kode == it.resultatkode }

fun VedtakForStønad.erKlage() = Vedtakstype.KLAGE == this.type || this.søknadKlageRefId != null

fun VedtakForStønad.erAutomatiskVedtak() = Vedtakskilde.AUTOMATISK == kilde

fun VedtakForStønad.omgjørVedtaksid() = this.stønadsendring.omgjørVedtakId

fun VedtakForStønad.erOmgjøring() =
    Vedtakstype.ENDRING == this.type && Vedtakskilde.MANUELT == this.kilde && this.stønadsendring.omgjørVedtak() && !this.behandlingsreferanser.any {
        BehandlingsrefKilde.BISYS_KLAGE_REF_SØKNAD == it.kilde
    }

fun VedtakForStønad.idTilOmgjortVedtak() = this.stønadsendring.omgjørVedtakId?.toLong()

fun StønadsendringDto.omgjørVedtak() = this.omgjørVedtakId != null

fun VedtakForStønad.erIngenEndringPga12Prosentregel() = this.stønadsendring.erIngenEndring12Prosent()

fun StønadsendringDto.erIngenEndring12Prosent() = this.periodeListe.any { Beslutningsårsak.INGEN_ENDRING_12_PROSENT.kode == it.resultatkode }

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
