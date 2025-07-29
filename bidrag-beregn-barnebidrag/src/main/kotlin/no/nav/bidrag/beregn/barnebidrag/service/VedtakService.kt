package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.hentPersonForNyesteIdent
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.barnebidrag.utils.tilGrunnlag
import no.nav.bidrag.beregn.core.util.justerVedtakstidspunkt
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.felles.toCompactString
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import kotlin.text.compareTo

data class SisteManuelleVedtak(val vedtaksId: Int, val vedtak: VedtakDto)

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
        val vedtakListe = vedtakForStønad.vedtakListe
            .filter { ignorerVedtaksid == null || it.vedtaksid.toInt() != ignorerVedtaksid }
            .filter { it.stønadsendring.periodeListe.isNotEmpty() }
            .sortedBy { it.stønadsendring.periodeListe.maxBy { p -> p.periode.fom }.periode.fom }

        if (fraPeriode == null) {
            return vedtakListe.map { it.justerVedtakstidspunkt() }
        }

        val filtrertVedtakListe = vedtakListe.fold(emptyList<VedtakForStønad>()) { acc, vedtak ->
            val sistePeriodeFom = vedtak.stønadsendring.periodeListe.maxBy { it.periode.fom!! }.periode.fom!!
            if (sistePeriodeFom > fraPeriode) {
                val forrigeVedtak = acc.lastOrNull()
                if (forrigeVedtak != null) {
                    acc
                } else {
                    val forrigeVedtakFraOriginalListe = vedtakListe.getOrNull(vedtakListe.indexOf(vedtak) - 1)
                    if (forrigeVedtakFraOriginalListe != null) {
                        listOf(forrigeVedtakFraOriginalListe, vedtak)
                    } else {
                        listOf(vedtak)
                    }
                }
            } else {
                acc
            }
        }.toMutableList()

        val førstePeriode = filtrertVedtakListe.minByOrNull {
            it.stønadsendring.periodeListe.minBy { it.periode.fom }.periode.fom
        }?.stønadsendring?.periodeListe?.maxByOrNull { it.periode.fom }

        if (førstePeriode?.periode?.fom?.isAfter(fraPeriode) ?: false) {
            // Legg til siste løpende periode som kommer før fraPeriode
            vedtakListe.lastOrNull {
                it.stønadsendring.periodeListe.maxBy { it.periode.fom }.periode.fom < fraPeriode
            }?.let { sisteVedtak ->
                val harLøpendePeriode = sisteVedtak.stønadsendring.periodeListe.any { it.periode.til == null }
                if (harLøpendePeriode) filtrertVedtakListe.add(sisteVedtak)
            }
        }

        return filtrertVedtakListe.map { it.justerVedtakstidspunkt() }
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
                secureLogger.info { "Fant ingen løpende ${stønadsid.type} for $stønadsid" }
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
                secureLogger.info { "Fant ingen løpende historisk ${stønadsid.type} for $stønadsid" }
                return null
            }
        return stønad.periodeListe.hentSisteLøpendePeriode() ?: run {
            secureLogger.info {
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
        return vedtakConsumer.hentVedtak(resultat.vedtaksid.toInt())?.let {
            SisteManuelleVedtak(resultat.vedtaksid.toInt(), it)
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
}
