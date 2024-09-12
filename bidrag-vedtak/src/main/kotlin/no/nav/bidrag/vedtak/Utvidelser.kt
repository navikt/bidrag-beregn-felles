package no.nav.bidrag.vedtak

import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId

fun List<StønadsendringDto>.erEndring() = !this.erAvvisning() && !this.erStadfestelse()

private fun List<StønadsendringDto>.erAvvisning() =
    this.find { Beslutningstype.AVVIST == it.beslutning } != null

private fun List<StønadsendringDto>.erStadfestelse() =
    this.any { Beslutningstype.STADFESTELSE == it.beslutning } || this.any { p -> p.periodeListe.any { Beslutningsårsak.IKKE_FRITATT_ETTER_KLAGE.kode == it.resultatkode } }


fun VedtakDto.erResultatFraAnnetVedtak() =
    this.stønadsendringListe.any { p -> p.periodeListe.any { Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK.kode == it.resultatkode } }

fun VedtakDto.erKlage() =
    this.søknadKlageRefId != null

fun VedtakDto.erAutomatiskVedtak() = Vedtakskilde.AUTOMATISK == kilde || bisysBatchBrukerid == this.opprettetAv

fun VedtakDto.erOmgjøring() = this.stønadsendringListe.omgjørVedtak()

fun VedtakDto.idTilOmgjortVedtak() = this.stønadsendringListe.find { it.omgjørVedtakId != null }?.omgjørVedtakId?.toLong()

fun List<StønadsendringDto>.omgjørVedtak() =
    this.find { it.omgjørVedtakId != null } != null

fun VedtakDto.erIngenEndringPga12Prosentregel() = this.stønadsendringListe.erIngenEndring12Prosent()

fun List<StønadsendringDto>.erIngenEndring12Prosent() =
    this.any { p -> p.periodeListe.any { Beslutningsårsak.ENDRING_12_PROSENT.kode == it.resultatkode } }

fun VedtakDto.tilVedtaksperioder(): Collection<Vedtaksperiode> =
    stønadsendringListe.flatMap { it.periodeListe }.map {
        Vedtaksperiode(
            vedtak = this,
            periode = it,
        )
    }

fun Collection<VedtakDto>.tilVedtaksperioder() = this.flatMap { it.tilVedtaksperioder() }

val bisysBatchBrukerid = "RTV9999"

enum class Beslutningsårsak(val kode: String) {
    ENDRING_12_PROSENT("VO"),
    IKKE_FRITATT_ETTER_KLAGE("GKIF"),
    INNVILGETT_VEDTAK("IV"),
    RESULTAT_FRA_ANNET_VEDTAK("RAV");

}