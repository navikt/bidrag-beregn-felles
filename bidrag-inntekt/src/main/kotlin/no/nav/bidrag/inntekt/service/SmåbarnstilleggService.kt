package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.inntekt.request.Småbarnstillegg
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Service
class SmåbarnstilleggService {
    // Summerer, grupperer og transformerer småbarnstillegg pr år
    fun beregnSmåbarnstillegg(småbarnstilleggListeInn: List<Småbarnstillegg>): List<SummertÅrsinntekt> {
        val småbarnstilleggListeUt = mutableListOf<SummertÅrsinntekt>()

        småbarnstilleggListeInn.forEach {
            småbarnstilleggListeUt.add(
                SummertÅrsinntekt(
                    inntektRapportering = Inntektsrapportering.SMÅBARNSTILLEGG,
                    visningsnavn = Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern,
                    sumInntekt = it.beløp.times(BigDecimal.valueOf(12)).setScale(0, RoundingMode.HALF_UP),
                    periode =
                    ÅrMånedsperiode(
                        fom = YearMonth.of(it.periodeFra.year, it.periodeFra.month),
                        til = finnPeriodeTil(it.periodeTil),
                    ),
                    inntektPostListe = emptyList(),
                    grunnlagsreferanseListe = listOf(it.referanse),
                ),
            )
        }

        return småbarnstilleggListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
    }

    private fun finnPeriodeTil(periodeTil: LocalDate?): YearMonth? {
        return periodeTil?.minusMonths(1)?.let {
            YearMonth.of(it.year, it.month)
        }
    }
}
