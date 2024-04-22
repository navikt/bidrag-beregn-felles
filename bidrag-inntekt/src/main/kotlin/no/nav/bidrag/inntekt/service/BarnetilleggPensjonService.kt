package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.inntekt.request.Barnetillegg
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

class BarnetilleggPensjonService {
    // Summerer mottatt periode opp til beløp for 12 måneder og returnerer
    fun beregnBarnetilleggPensjon(barnetilleggslisteInn: List<Barnetillegg>): List<SummertÅrsinntekt> {
        val barnetilleggslisteUt = mutableListOf<SummertÅrsinntekt>()

        // Lager et sett med unike barnPersonId fra inputliste
        val barnPersonIdListe = barnetilleggslisteInn.distinctBy { it.barnPersonId }.map { it.barnPersonId }.toSet()

        barnPersonIdListe.sortedWith(compareBy { it }).forEach { barnPersonId ->
            val barnetilleggListePerBarn = barnetilleggslisteInn.filter { it.barnPersonId == barnPersonId }
            beregnBarnetilleggPerBarn(barnetilleggListePerBarn).forEach {
                barnetilleggslisteUt.add(it)
            }
        }
        return barnetilleggslisteUt
    }

    // Summerer barentillegg for angitt barn
    fun beregnBarnetilleggPerBarn(barnetilleggListePerBarn: List<Barnetillegg>): List<SummertÅrsinntekt> {
        return if (barnetilleggListePerBarn.isNotEmpty()) {
            val barnetilleggListeUt = mutableListOf<SummertÅrsinntekt>()
            val barnPersonId = barnetilleggListePerBarn.first().barnPersonId

            barnetilleggListePerBarn.forEach {
                barnetilleggListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = Inntektsrapportering.BARNETILLEGG,
                        visningsnavn = Inntektsrapportering.BARNETILLEGG.visningsnavn.intern,
                        sumInntekt = it.beløp.times(BigDecimal.valueOf(12)).setScale(0, RoundingMode.HALF_UP),
                        periode =
                        ÅrMånedsperiode(
                            fom = YearMonth.of(it.periodeFra.year, it.periodeFra.month),
                            til = finnPeriodeTil(it.periodeTil),
                        ),
                        gjelderBarnPersonId = barnPersonId,
                        inntektPostListe = emptyList(),
                        grunnlagsreferanseListe = listOf(it.referanse),
                    ),
                )
            }
            barnetilleggListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
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
