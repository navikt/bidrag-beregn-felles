package no.nav.bidrag.inntekt.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.focus_shift.jollyday.core.HolidayCalendar
import de.focus_shift.jollyday.core.HolidayManager
import de.focus_shift.jollyday.core.ManagerParameters
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
            return if ((ainntektHentetTidspunkt.month == Month.JANUARY) && (
                    ainntektHentetTidspunkt.dayOfMonth <= finnCutOffDag(
                        ainntektHentetTidspunkt,
                    )
                    )
            ) {
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

        // Finner ut hvilken dato i måneden ainntekt ble hentet som er fristen arbeidsgiver har for å levere a-meldingen for forrige måned.
        // Denne datoen brukes til å styre hvilke inntekter som skal returneres (hvis ainntektHentetDato er før fristen går vi en måned lengre tilbake).
        // Følgende regelverk gjelder (se https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/frister-og-betaling-i-a-meldingen/:
        // - Fristen for å levere a-meldingen er den 5. i hver måned
        // - Hvis den 5. er helg eller helligdag, er fristen første påfølgende hverdag
        fun finnCutOffDag(ainntektHentetDato: LocalDate): Int {
            val holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.NORWAY))
            var cutOffDato = LocalDate.of(ainntektHentetDato.year, ainntektHentetDato.month, 5)
            while (holidayManager.isHoliday(cutOffDato) || cutOffDato.dayOfWeek.value > 5) {
                cutOffDato = cutOffDato.plusDays(1)
            }
            return cutOffDato.dayOfMonth
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
