package no.nav.bidrag.beregn.barnebidrag.utils

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.felles.toCompactString
import java.time.LocalDate
import java.time.LocalDateTime

fun BaseGrunnlag.tilDto() = GrunnlagDto(
    referanse,
    type,
    innhold,
    grunnlagsreferanseListe,
    gjelderReferanse,
    gjelderBarnReferanse,
)
fun VedtakDto.finnBeløpshistorikkGrunnlag(stønad: Stønadsid, identUtils: IdentUtils): BeløpshistorikkGrunnlag? {
    val grunnlagstype = when (stønad.type) {
        Stønadstype.BIDRAG18AAR -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
        Stønadstype.FORSKUDD -> Grunnlagstype.BELØPSHISTORIKK_FORSKUDD
        else -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG
    }
    val søknadsbarn = grunnlagListe.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)!!
    return grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(grunnlagType = grunnlagstype)
        .firstOrNull { it.gjelderBarnReferanse == søknadsbarn.referanse }?.innhold
}
fun StønadDto?.tilGrunnlag(personer: List<GrunnlagDto>, stønadsid: Stønadsid, identUtils: IdentUtils): GrunnlagDto {
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
            stønadsid.type == Stønadstype.BIDRAG -> personer.bidragspliktig?.referanse
            stønadsid.type == Stønadstype.BIDRAG18AAR -> personer.bidragspliktig?.referanse
            else -> personer.bidragsmottaker?.referanse
        },
        gjelderBarnReferanse =
        personer.hentPersonMedIdent(stønadsid.kravhaver.verdi)?.referanse
            ?: personer.hentPersonForNyesteIdent(identUtils, stønadsid.kravhaver)?.referanse,
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

fun List<GrunnlagDto>.finnBeløpshistorikk(): GrunnlagDto = firstOrNull { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG }
    ?: firstOrNull { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR }
    ?: firstOrNull { it.type == Grunnlagstype.BELØPSHISTORIKK_FORSKUDD }
    ?: throw IllegalStateException("Fant ingen beløpshistorikk i grunnlagsliste: ${this.map { it.referanse }}")
