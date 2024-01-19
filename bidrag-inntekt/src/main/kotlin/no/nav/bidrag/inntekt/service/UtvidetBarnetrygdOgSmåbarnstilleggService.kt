package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.inntekt.request.UtvidetBarnetrygdOgSmåbarnstillegg
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
@Suppress("NonAsciiCharacters")
class UtvidetBarnetrygdOgSmåbarnstilleggService() {
    // Summerer, grupperer og transformerer utvidet barnetrygd og småbarnstillegg pr år
    fun beregnUtvidetBarnetrygdOgSmåbarnstillegg(
        utvidetBarnetrygdOgSmåbarnstillegglisteInn: List<UtvidetBarnetrygdOgSmåbarnstillegg>,
    ): List<SummertÅrsinntekt> {
        return if (utvidetBarnetrygdOgSmåbarnstillegglisteInn.isNotEmpty()) {
            val utvidetBarnetrygdListe = utvidetBarnetrygdOgSmåbarnstillegglisteInn.filter { it.type == "UTVIDET" }
            val småbarnstilleggListe = utvidetBarnetrygdOgSmåbarnstillegglisteInn.filter { it.type == "SMÅBARNSTILLEGG" }

            val utvidetBarnetrygdOgSmåbarnstilleggListeUt = mutableListOf<SummertÅrsinntekt>()

            utvidetBarnetrygdListe.forEach {
                utvidetBarnetrygdOgSmåbarnstilleggListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = Inntektsrapportering.UTVIDET_BARNETRYGD,
                        visningsnavn = Inntektsrapportering.UTVIDET_BARNETRYGD.visningsnavn.intern,
                        referanse = "",
                        sumInntekt = it.beløp.times(BigDecimal.valueOf(12)),
                        periode =
                        ÅrMånedsperiode(
                            fom = YearMonth.of(it.periodeFra.year, it.periodeFra.month),
                            til = finnPeriodeTil(it.periodeTil),
                        ),
                        inntektPostListe = emptyList(),
                    ),
                )
            }
            småbarnstilleggListe.forEach {
                utvidetBarnetrygdOgSmåbarnstilleggListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = Inntektsrapportering.SMÅBARNSTILLEGG,
                        visningsnavn = Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern,
                        referanse = "",
                        sumInntekt = it.beløp.times(BigDecimal.valueOf(12)),
                        periode =
                        ÅrMånedsperiode(
                            fom = YearMonth.of(it.periodeFra.year, it.periodeFra.month),
                            til = finnPeriodeTil(it.periodeTil),
                        ),
                        inntektPostListe = emptyList(),
                    ),
                )
            }
            utvidetBarnetrygdOgSmåbarnstilleggListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
        } else {
            emptyList()
        }
    }

    private fun finnPeriodeTil(periodeTil: LocalDate?): YearMonth? {
        return periodeTil?.minusMonths(1)?.let {
            YearMonth.of(it.year, it.month)
        }
    }
}
