package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningStønadConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.felles.personidentNav
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.stonad.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.felles.toCompactString
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

data class SisteManuelleVedtak(val vedtaksId: Int, val vedtak: VedtakDto)

@Service
class VedtakService(
    private val vedtakConsumer: BeregningVedtakConsumer,
    private val stønadConsumer: BeregningStønadConsumer,
    private val vedtakFilter: Vedtaksfiltrering,
) {
    fun hentBeløpshistorikk(stønadsid: Stønadsid, personer: List<GrunnlagDto>): GrunnlagDto = stønadConsumer
        .hentHistoriskeStønader(
            HentStønadHistoriskRequest(
                type = stønadsid.type,
                sak = stønadsid.sak,
                skyldner = personidentNav,
                kravhaver = stønadsid.kravhaver,
                gyldigTidspunkt = LocalDateTime.now(),
            ),
        ).tilGrunnlag(personer, stønadsid)

    fun hentLøpendeStønad(stønadsid: Stønadsid, tidspunkt: LocalDateTime = LocalDateTime.now()): StønadPeriodeDto? {
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
                secureLogger.info { "Fant ingen løpende ${stønadsid.type} for $stønadsid" }
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
            vedtakFilter.finneSisteManuelleVedtak(
                forskuddVedtakISak.vedtakListe,
            )
        return vedtakConsumer.hentVedtak(resultat!!.vedtaksid.toInt())?.let {
            SisteManuelleVedtak(resultat.vedtaksid.toInt(), it)
        }
    }

    fun finnSisteVedtaksid(stønadsid: Stønadsid) = vedtakConsumer
        .hentVedtakForStønad(
            HentVedtakForStønadRequest(
                stønadsid.sak,
                stønadsid.type,
                stønadsid.skyldner,
                stønadsid.kravhaver,
            ),
        ).vedtakListe
        .maxBy { it.vedtakstidspunkt }
        .vedtaksid
}
private fun StønadDto?.tilGrunnlag(personer: List<GrunnlagDto>, stønadsid: Stønadsid): GrunnlagDto {
    val grunnlagstype =
        when (stønadsid.type) {
            Stønadstype.BIDRAG -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG
            Stønadstype.BIDRAG18AAR -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
            Stønadstype.FORSKUDD -> Grunnlagstype.BELØPSHISTORIKK_FORSKUDD
            else -> throw IllegalArgumentException("Ukjent stønadstype")
        }

    return GrunnlagDto(
        referanse =
        "${grunnlagstype}_${stønadsid.sak}_${stønadsid.kravhaver.verdi}_${stønadsid.skyldner.verdi}" +
            "_${this?.opprettetTidspunkt?.toCompactString() ?: LocalDate.now().toCompactString()}",
        type = grunnlagstype,
        gjelderReferanse =
        when {
            stønadsid.type == Stønadstype.BIDRAG -> personer.bidragspliktig!!.referanse
            stønadsid.type == Stønadstype.BIDRAG18AAR -> personer.bidragspliktig!!.referanse
            else -> personer.bidragsmottaker!!.referanse
        },
        gjelderBarnReferanse = personer.hentPersonMedIdent(stønadsid.kravhaver.verdi)!!.referanse,
        innhold =
        POJONode(
            BeløpshistorikkGrunnlag(
                tidspunktInnhentet = LocalDateTime.now(),
                nesteIndeksreguleringsår = this?.nesteIndeksreguleringsår ?: this?.førsteIndeksreguleringsår,
                beløpshistorikk =
                this?.periodeListe?.map {
                    BeløpshistorikkPeriode(
                        periode = it.periode,
                        beløp = it.beløp,
                        valutakode = it.valutakode,
                        vedtaksid = it.vedtaksid,
                    )
                } ?: emptyList(),
            ),
        ),
    )
}
