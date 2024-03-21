package no.nav.bidrag.inntekt.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit

open class InntektUtil {
    companion object {
        const val KEY_3MND = "3MND"
        const val KEY_12MND = "12MND"
        const val PERIODE_AAR = "AAR"
        const val PERIODE_MAANED = "MND"
        const val CUT_OFF_DATO = 6
        const val SUMMERT_SKATTEGRUNNLAG = "Summert skattegrunnlag"
        const val LOENNSBESKRIVELSE = "Loennsbeskrivelse"

        fun tilJson(json: Any): String {
            val objectMapper = ObjectMapper()
            objectMapper.registerKotlinModule()
            objectMapper.writerWithDefaultPrettyPrinter()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
            return objectMapper.writeValueAsString(json)
        }

        // Finner siste hele år som skal rapporteres
        fun finnSisteAarSomSkalRapporteres(ainntektHentetTidspunkt: LocalDate): Int {
            return if ((ainntektHentetTidspunkt.month == Month.JANUARY) && (ainntektHentetTidspunkt.dayOfMonth <= CUT_OFF_DATO)) {
                ainntektHentetTidspunkt.year.minus(2)
            } else {
                ainntektHentetTidspunkt.year.minus(1)
            }
        }

        // Finner antall måneder som overlapper med angitt periode
        fun finnAntallMndOverlapp(
            periodeFra: YearMonth,
            periodeTil: YearMonth,
            forstePeriodeIIntervall: YearMonth,
            sistePeriodeIIntervall: YearMonth,
        ): Int {
            return when {
                !(periodeTil.isAfter(forstePeriodeIIntervall)) -> 0
                !(periodeFra.isBefore(sistePeriodeIIntervall)) -> 0
                !(periodeFra.isAfter(forstePeriodeIIntervall)) && !(periodeTil.isBefore(sistePeriodeIIntervall)) ->
                    ChronoUnit.MONTHS.between(forstePeriodeIIntervall, sistePeriodeIIntervall).toInt()

                !(periodeFra.isAfter(forstePeriodeIIntervall)) && (periodeTil.isBefore(sistePeriodeIIntervall)) ->
                    ChronoUnit.MONTHS.between(forstePeriodeIIntervall, periodeTil).toInt()

                (periodeFra.isAfter(forstePeriodeIIntervall)) && !(periodeTil.isBefore(sistePeriodeIIntervall)) ->
                    ChronoUnit.MONTHS.between(periodeFra, sistePeriodeIIntervall).toInt()

                else -> ChronoUnit.MONTHS.between(periodeFra, periodeTil).toInt()
            }
        }
    }
}

fun String.isNumeric(): Boolean {
    return this.all { it.isDigit() }
}

fun beregneBeløpPerMåned(beløp: BigDecimal, antallMnd: Int): BigDecimal {
    return if (antallMnd == 0) {
        BigDecimal.valueOf(0)
    } else {
        beløp.div(antallMnd.toBigDecimal())
    }
}
