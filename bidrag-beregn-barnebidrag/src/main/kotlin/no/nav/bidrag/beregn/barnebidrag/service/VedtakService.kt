package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.barnebidrag.utils.tilGrunnlag
import no.nav.bidrag.beregn.core.util.justerVedtakstidspunkt
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth

data class SisteManuelleVedtak(val vedtaksId: Int, val vedtak: VedtakDto)

internal data class PåklagetVedtak(val vedtaksid: Int, val vedtakstidspunkt: LocalDateTime)

@Service
class VedtakService(
    private val vedtakConsumer: BeregningVedtakConsumer,
    private val stønadConsumer: BeregningBeløpshistorikkConsumer,
    private val vedtakFilter: Vedtaksfiltrering,
    private val identUtils: IdentUtils,
) {
    fun hentBeløpshistorikkTilGrunnlag(
        stønadsid: Stønadsid,
        personer: List<GrunnlagDto>,
        tidspunkt: LocalDateTime = LocalDateTime.now(),
    ): GrunnlagDto = stønadConsumer.hentHistoriskeStønader(
        HentStønadHistoriskRequest(
            type = stønadsid.type,
            sak = stønadsid.sak,
            skyldner = stønadsid.skyldner,
            kravhaver = stønadsid.kravhaver,
            gyldigTidspunkt = tidspunkt,
        ),
    ).tilGrunnlag(personer, stønadsid, identUtils)

    // Finner alle vedtaksider som er relatert til et påklaget vedtak.
    // Feks hvis det er opprettet klage på en vedtak som er klage på opprinnelig vedtak så vil også det vedtaket inkluderes
    fun hentAlleVedtaksiderRelatertTilOmgjortVedtak(stønadsid: Stønadsid, påklagetVedtaksid: Int): Set<Int> {
        val vedtakForStønad =
            vedtakConsumer.hentVedtakForStønad(
                HentVedtakForStønadRequest(
                    stønadsid.sak,
                    stønadsid.type,
                    stønadsid.skyldner,
                    stønadsid.kravhaver,
                ),
            )
        val vedtakslisteJustert = vedtakForStønad.vedtakListe.map { it.justerVedtakstidspunkt() }
        return vedtakslisteJustert.fold(emptyList<Int>()) { acc, stønad ->
            if (stønad.vedtaksid == påklagetVedtaksid) {
                acc + stønad.vedtaksid
            } else if (vedtakslisteJustert.hentOpprinneligPåklagetVedtak(stønad) == påklagetVedtaksid) {
                acc + vedtakslisteJustert.hentPåklagetVedtakListe(stønad).map { it.vedtaksid }
            } else {
                acc
            }
        }.toSet()
    }

    fun hentAlleVedtakForStønad(stønadsid: Stønadsid, fraPeriode: YearMonth? = null, ignorerVedtaksid: Int? = null): List<VedtakForStønad> {
        val vedtakForStønad =
            vedtakConsumer.hentVedtakForStønad(
                HentVedtakForStønadRequest(
                    stønadsid.sak,
                    stønadsid.type,
                    stønadsid.skyldner,
                    stønadsid.kravhaver,
                ),
            )
        val vedtakslisteJustert = vedtakForStønad.vedtakListe.map { it.justerVedtakstidspunkt() }
        val vedtakListe = vedtakslisteJustert.filter {
            ignorerVedtaksid == null ||
                it.vedtaksid != ignorerVedtaksid &&
                vedtakslisteJustert.hentOpprinneligPåklagetVedtak(it) != ignorerVedtaksid
        }
            .filter {
                it.stønadsendring.periodeListe.isNotEmpty() &&
                    it.stønadsendring.beslutning == Beslutningstype.ENDRING &&
                    it.stønadsendring.innkreving == Innkrevingstype.MED_INNKREVING
            }
            .groupBy { vedtak ->
                val perioder = vedtak.stønadsendring.periodeListe
                perioder.minOf { it.periode.fom } to perioder.maxOf { it.periode.fom }
            }
            .mapNotNull { (_, vedtakGruppe) ->
                vedtakGruppe.maxByOrNull { it.vedtakstidspunkt }
            }
            .sortedBy { it.vedtakstidspunkt }
            .fold(mutableListOf<VedtakForStønad>()) { acc, vedtak ->
                val sisteVedtak = acc.lastOrNull()
                if (sisteVedtak == null) {
                    (acc + vedtak).toMutableList()
                } else {
                    val nesteVedtakFom = vedtak.stønadsendring.periodeListe.minOf { it.periode.fom }
                    val nesteVedtakTil = vedtak.stønadsendring.periodeListe.maxOf { it.periode.til ?: YearMonth.of(9999, 12) }

                    val sisteVedtakFom = sisteVedtak.stønadsendring.periodeListe.minOf { it.periode.fom }
                    val sisteVedtakTil = sisteVedtak.stønadsendring.periodeListe.maxOf { it.periode.til ?: YearMonth.of(9999, 12) }

                    // Sjekk siste periode til i tilfelle det har blitt fattet opphør men startet vedtak på nytt
                    val nesteVedtakOverskriverSisteVedtak = nesteVedtakFom <= sisteVedtakFom // && nesteVedtakTil <= sisteVedtakTil
                    if (nesteVedtakOverskriverSisteVedtak) {
                        acc.remove(sisteVedtak)
                    }
                    (acc + vedtak).toMutableList()
                }
            }

        if (fraPeriode == null) {
            return vedtakListe
        }

        val filtrertVedtakListe = vedtakListe.filter { vedtak ->
            val sistePeriodeFom = vedtak.stønadsendring.periodeListe.maxOf { it.periode.fom }
            sistePeriodeFom >= fraPeriode
        }

        if (filtrertVedtakListe.isNotEmpty()) {
            return filtrertVedtakListe
        }

        // Hvis ingen vedtak er funnet etter filtrering, finn det siste vedtaket med en løpende periode
        return vedtakListe.lastOrNull { vedtak ->
            vedtak.stønadsendring.periodeListe.any { it.periode.til == null }
        }?.let { listOf(it) } ?: emptyList()
    }

    private fun List<VedtakForStønad>.hentOpprinneligPåklagetVedtak(vedtak: VedtakForStønad): Int? = hentPåklagetVedtakListe(vedtak).minBy {
        it.vedtakstidspunkt
    }.vedtaksid
    private fun List<VedtakForStønad>.hentPåklagetVedtakListe(vedtak: VedtakForStønad): Set<PåklagetVedtak> {
        val refererTilVedtakId = setOfNotNull(vedtak.stønadsendring.omgjørVedtakId)
        if (refererTilVedtakId.isNotEmpty()) {
            return refererTilVedtakId
                .flatMap { vedtaksid ->
                    val opprinneligVedtak = find { it.vedtaksid == vedtaksid }!!
                    hentPåklagetVedtakListe(opprinneligVedtak)
                }.toSet() + setOf(PåklagetVedtak(vedtak.vedtaksid, vedtak.vedtakstidspunkt))
        }
        return setOf(PåklagetVedtak(vedtak.vedtaksid, vedtak.vedtakstidspunkt))
    }

    fun hentLøpendeStønad(stønadsid: Stønadsid): StønadDto? {
        val stønad =
            stønadConsumer.hentLøpendeStønad(
                HentStønadRequest(
                    type = stønadsid.type,
                    sak = stønadsid.sak,
                    skyldner = stønadsid.skyldner,
                    kravhaver = stønadsid.kravhaver,
                ),
            ) ?: run {
                secureLogger.debug { "Fant ingen løpende ${stønadsid.type} for $stønadsid" }
                return null
            }
        return stønad
    }

    fun hentBeløpshistorikkSistePeriode(stønadsid: Stønadsid, tidspunkt: LocalDateTime = LocalDateTime.now()): StønadPeriodeDto? {
        val stønad =
            stønadConsumer.hentHistoriskeStønader(
                HentStønadHistoriskRequest(
                    type = stønadsid.type,
                    sak = stønadsid.sak,
                    skyldner = stønadsid.skyldner,
                    kravhaver = stønadsid.kravhaver,
                    gyldigTidspunkt = tidspunkt,
                ),
            ) ?: run {
                secureLogger.debug { "Fant ingen løpende historisk ${stønadsid.type} for $stønadsid" }
                return null
            }
        return stønad.periodeListe.hentSisteLøpendePeriode(YearMonth.from(tidspunkt)) ?: run {
            secureLogger.debug {
                "${stønadsid.type} i stønad $$stønadsid har opphørt før dagens dato. Det finnes ingen løpende ${stønadsid.type}"
            }
            null
        }
    }

    fun finnSisteManuelleVedtak(stønadsid: Stønadsid): SisteManuelleVedtak? {
        val forskuddVedtakISak =
            vedtakConsumer.hentVedtakForStønad(
                HentVedtakForStønadRequest(
                    stønadsid.sak,
                    stønadsid.type,
                    stønadsid.skyldner,
                    stønadsid.kravhaver,
                ),
            )
        val resultat =
            vedtakFilter.finneSisteManuelleVedtak(forskuddVedtakISak.vedtakListe) ?: return null
        return vedtakConsumer.hentVedtak(resultat.vedtaksid)?.let {
            SisteManuelleVedtak(resultat.vedtaksid, it)
        }
    }

    @Suppress("unused")
    fun finnSisteVedtaksid(stønadsid: Stønadsid) = vedtakConsumer
        .hentVedtakForStønad(
            HentVedtakForStønadRequest(
                stønadsid.sak,
                stønadsid.type,
                stønadsid.skyldner,
                stønadsid.kravhaver,
            ),
        ).vedtakListe
        .maxByOrNull { it.vedtakstidspunkt }
        ?.vedtaksid

    fun hentVedtak(vedtaksId: Int) = vedtakConsumer.hentVedtak(vedtaksId)
    fun oppdaterIdenterStønadsendringer(vedtak: VedtakDto) = vedtak.copy(
        stønadsendringListe = vedtak.stønadsendringListe.map { oppdaterIdenter(it) },
    )
    fun oppdaterIdenter(stønadsendringDto: StønadsendringDto) = stønadsendringDto.copy(
        mottaker = identUtils.hentNyesteIdent(stønadsendringDto.mottaker),
        kravhaver = identUtils.hentNyesteIdent(stønadsendringDto.kravhaver),
        skyldner = identUtils.hentNyesteIdent(stønadsendringDto.skyldner),
    )
}
