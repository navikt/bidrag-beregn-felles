package no.nav.bidrag.vedtak

import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId

fun List<StønadsendringDto>.erEndring() = this.any { Beslutningstype.ENDRING == it.beslutning }

fun VedtakDto.erEndring() = Vedtakstype.ENDRING == this.type || this.stønadsendringListe.erEndring()

private fun List<StønadsendringDto>.erAvvisning() =
    this.find { Beslutningstype.AVVIST == it.beslutning } != null

private fun List<StønadsendringDto>.erStadfestelse() =
    this.any { Beslutningstype.STADFESTELSE == it.beslutning } || this.any { p -> p.periodeListe.any { Beslutningsårsak.IKKE_FRITATT_ETTER_KLAGE.kode == it.resultatkode } }

fun VedtakDto.erResultatFraAnnetVedtak() =
    this.stønadsendringListe.any { p -> p.periodeListe.any { Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK.kode == it.resultatkode } }

fun VedtakDto.erKlage() = Vedtakstype.KLAGE == this.type && this.søknadKlageRefId != null

fun VedtakDto.erAutomatiskVedtak() = Vedtakskilde.AUTOMATISK == kilde || bisysBatchBrukerid == this.opprettetAv

fun VedtakDto.omgjørVedtaksid() = this.stønadsendringListe.first { it.omgjørVedtakId != null }.omgjørVedtakId

fun VedtakDto.erOmgjøring() =
    Vedtakstype.ENDRING == this.type && Vedtakskilde.MANUELT == this.kilde && this.stønadsendringListe.omgjørVedtak() && !this.behandlingsreferanseListe.any { BehandlingsrefKilde.BISYS_KLAGE_REF_SØKNAD == it.kilde }

fun VedtakDto.idTilOmgjortVedtak() = this.stønadsendringListe.find { it.omgjørVedtakId != null }?.omgjørVedtakId?.toLong()

fun List<StønadsendringDto>.omgjørVedtak() = this.any { it.omgjørVedtakId != null }

fun VedtakDto.erIngenEndringPga12Prosentregel() = this.stønadsendringListe.erIngenEndring12Prosent()

fun List<StønadsendringDto>.erIngenEndring12Prosent() =
    this.any { p -> p.periodeListe.any { Beslutningsårsak.INGEN_ENDRING_12_PROSENT.kode == it.resultatkode } }

fun VedtakDto.tilVedtaksdetaljer(): Collection<Vedtaksdetaljer> =
    stønadsendringListe.flatMap { it.periodeListe }.map {
        Vedtaksdetaljer(
            vedtak = this,
            periode = it,
        )
    }

fun Collection<VedtakDto>.tilVedtaksdetaljer() = this.flatMap { it.tilVedtaksdetaljer() }

val bisysBatchBrukerid = "RTV9999"

enum class Beslutningsårsak(val kode: String) {
    INGEN_ENDRING_12_PROSENT("VO"),
    IKKE_FRITATT_ETTER_KLAGE("GKIF"),
    INDEKSREGULERING("IREG"),
    INNVILGETT_VEDTAK("IV"),
    KOSTNADSBEREGNET_BIDRAG("KBB"),
    RESULTAT_FRA_ANNET_VEDTAK("RAV");
}