package no.nav.bidrag.beregn.barnebidrag.utils

import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth

fun List<VedtakPeriodeDto>.hentSisteLøpendePeriode(periodeEtter: YearMonth = YearMonth.now()) = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(periodeEtter) }

fun List<StønadPeriodeDto>.hentSisteLøpendePeriode(periodeEtter: YearMonth = YearMonth.now()) = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(periodeEtter) }

fun List<GrunnlagDto>.hentPersonForNyesteIdent(identUtils: IdentUtils, identFraVedtak: Personident): BaseGrunnlag? {
    val kravhaverNyesteIdent = identUtils.hentNyesteIdent(identFraVedtak)
    return hentAllePersoner().find {
        val personNyesteIdent = identUtils.hentNyesteIdent(Personident(it.personIdent!!))
        personNyesteIdent.verdi == kravhaverNyesteIdent.verdi
    }
}

fun opprettStønad(stønadsid: Stønadsid) = StønadDto(
    stønadsid = -1,
    type = stønadsid.type,
    kravhaver = stønadsid.kravhaver,
    skyldner = stønadsid.skyldner,
    sak = stønadsid.sak,
    mottaker = Personident(""),
    førsteIndeksreguleringsår = Year.now().plusYears(1).value,
    nesteIndeksreguleringsår = Year.now().plusYears(1).value,
    innkreving = Innkrevingstype.MED_INNKREVING,
    opprettetAv = "",
    opprettetTidspunkt = LocalDateTime.now(),
    endretAv = "",
    endretTidspunkt = LocalDateTime.now(),
    periodeListe = emptyList(),
)
