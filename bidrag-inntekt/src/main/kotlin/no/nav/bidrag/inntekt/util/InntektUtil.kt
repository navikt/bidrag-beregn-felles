package no.nav.bidrag.inntekt.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import de.focus_shift.jollyday.core.HolidayCalendar
import de.focus_shift.jollyday.core.HolidayManager
import de.focus_shift.jollyday.core.ManagerParameters
import no.nav.bidrag.inntekt.service.Beskrivelser
import no.nav.bidrag.inntekt.service.YtelserService
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month

open class InntektUtil {
    companion object {
        const val KEY_3MND = "3MND"
        const val KEY_12MND = "12MND"
        const val KEY_3MND_OV = "3MND_OV"
        const val KEY_12MND_OV = "12MND_OV"
        const val PERIODE_ÅR = "ÅR"
        const val PERIODE_MÅNED = "MND"
        const val BRUDD_MÅNED_OVERGANSSTØNAD = 5

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
            return if ((ainntektHentetTidspunkt.month == Month.JANUARY) &&
                (ainntektHentetTidspunkt.dayOfMonth <= finnCutOffDag(ainntektHentetTidspunkt))
            ) {
                ainntektHentetTidspunkt.year.minus(2)
            } else {
                ainntektHentetTidspunkt.year.minus(1)
            }
        }

        // Finner ut hvilken dato i måneden ainntekt ble hentet som er fristen arbeidsgiver har for å levere a-meldingen for forrige måned.
        // Denne datoen brukes til å styre hvilke inntekter som skal returneres (hvis ainntektHentetDato er før fristen går vi en måned lengre tilbake).
        // Følgende regelverk gjelder (se https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/frister-og-betaling-i-a-meldingen/):
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

        fun filtrerInntekterPåYtelse(ainntektListeInn: List<Ainntektspost>, beskrivelserListe: List<String>) = ainntektListeInn.filter {
            it.beskrivelse in beskrivelserListe
        }

        // les innhold fra fil mapping_ytelser.yaml og returner dette som en map
        fun hentMappingYtelser(): Map<String, Beskrivelser> {
            val objectmapper = ObjectMapper(YAMLFactory()).findAndRegisterModules().registerKotlinModule()
            val fil = YtelserService::class.java.getResource("/files/mapping_ytelser.yaml")
                ?: throw RuntimeException("Fant ingen fil på sti mapping_ytelser.yaml")
            return objectmapper.readValue(fil)
        }
    }
}

fun String.isNumeric(): Boolean {
    return this.all { it.isDigit() }
}
