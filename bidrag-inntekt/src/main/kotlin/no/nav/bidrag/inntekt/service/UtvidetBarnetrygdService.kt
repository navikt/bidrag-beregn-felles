package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.inntekt.request.UtvidetBarnetrygd
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Service
class UtvidetBarnetrygdService {
    // Summerer, grupperer og transformerer utvidet barnetrygd pr år
    fun beregnUtvidetBarnetrygd(utvidetBarnetrygdlisteInn: List<UtvidetBarnetrygd>): List<SummertÅrsinntekt> {
        val utvidetBarnetrygdListeUt = mutableListOf<SummertÅrsinntekt>()

        utvidetBarnetrygdlisteInn.forEach {
            utvidetBarnetrygdListeUt.add(
                SummertÅrsinntekt(
                    inntektRapportering = Inntektsrapportering.UTVIDET_BARNETRYGD,
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

        return utvidetBarnetrygdListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
    }

    private fun finnPeriodeTil(periodeTil: LocalDate?): YearMonth? = periodeTil?.minusMonths(1)?.let {
        YearMonth.of(it.year, it.month)
    }
}
