package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.inntekt.request.Kontantstøtte
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Suppress("NonAsciiCharacters")
class KontantstøtteService {
    // Summerer mottatt periode opp til beløp for 12 måneder og returnerer
    fun beregnKontantstøtte(kontantstøttelisteInn: List<Kontantstøtte>): List<SummertÅrsinntekt> {
        val kontantstøtteListeUt = mutableListOf<SummertÅrsinntekt>()

        // Lager et sett med unike barnPersonId fra inputliste
        val barnPersonIdListe = kontantstøttelisteInn.distinctBy { it.barnPersonId }.map { it.barnPersonId }.toSet()

        barnPersonIdListe.sortedWith(compareBy { it }).forEach { barnPersonId ->
            val kontantstøtteListePerBarn = kontantstøttelisteInn.filter { it.barnPersonId == barnPersonId }
            beregnKontantstøttePerBarn(kontantstøtteListePerBarn).forEach {
                kontantstøtteListeUt.add(it)
            }
        }
        return kontantstøtteListeUt
    }

    // Summerer kontantstøtte for angitt barn
    fun beregnKontantstøttePerBarn(kontantstøtteListePerBarn: List<Kontantstøtte>): List<SummertÅrsinntekt> {
        return if (kontantstøtteListePerBarn.isNotEmpty()) {
            val kontantstøtteListeUt = mutableListOf<SummertÅrsinntekt>()
            val barnPersonId = kontantstøtteListePerBarn.first().barnPersonId

            kontantstøtteListePerBarn.forEach {
                kontantstøtteListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = Inntektsrapportering.KONTANTSTØTTE,
                        visningsnavn = Inntektsrapportering.KONTANTSTØTTE.visningsnavn.intern,
                        referanse = "",
                        sumInntekt = it.beløp.times(BigDecimal.valueOf(12)),
                        periode =
                        ÅrMånedsperiode(
                            fom = YearMonth.of(it.periodeFra.year, it.periodeFra.month),
                            til = finnPeriodeTil(it.periodeTil),
                        ),
                        gjelderBarnPersonId = barnPersonId,
                        inntektPostListe = emptyList(),
                    ),
                )
            }
            kontantstøtteListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
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
