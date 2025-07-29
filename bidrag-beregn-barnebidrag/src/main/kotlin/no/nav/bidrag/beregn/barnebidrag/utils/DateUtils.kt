package no.nav.bidrag.beregn.barnebidrag.utils

import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, monthValue)
fun LocalDateTime.toYearMonth(): YearMonth = YearMonth.of(year, monthValue)
val BeregnetBarnebidragResultat.beregnetFraDato get() = this.beregnetBarnebidragPeriodeListe.minOf { it.periode.fom }.atDay(1)
