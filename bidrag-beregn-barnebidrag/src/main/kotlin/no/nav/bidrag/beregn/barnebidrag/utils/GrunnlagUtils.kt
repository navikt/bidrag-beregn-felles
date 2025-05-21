package no.nav.bidrag.beregn.barnebidrag.utils

import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.time.YearMonth

fun List<VedtakPeriodeDto>.hentSisteLøpendePeriode() = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(YearMonth.now()) }

fun List<StønadPeriodeDto>.hentSisteLøpendePeriode() = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(YearMonth.now()) }

fun List<GrunnlagDto>.hentPersonForNyesteIdent(identUtils: IdentUtils, identFraVedtak: Personident): BaseGrunnlag? {
    val kravhaverNyesteIdent = identUtils.hentNyesteIdent(identFraVedtak)
    return hentAllePersoner().find {
        val personNyesteIdent = identUtils.hentNyesteIdent(Personident(it.personIdent!!))
        personNyesteIdent.verdi == kravhaverNyesteIdent.verdi
    }
}
