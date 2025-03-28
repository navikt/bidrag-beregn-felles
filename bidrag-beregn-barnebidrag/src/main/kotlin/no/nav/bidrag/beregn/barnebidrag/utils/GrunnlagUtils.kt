package no.nav.bidrag.beregn.barnebidrag.utils

import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.time.YearMonth

fun List<VedtakPeriodeDto>.hentSisteLøpendePeriode() = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(YearMonth.now()) }

fun List<StønadPeriodeDto>.hentSisteLøpendePeriode() = maxByOrNull { it.periode.fom }
    ?.takeIf { it.periode.til == null || it.periode.til!!.isAfter(YearMonth.now()) }
