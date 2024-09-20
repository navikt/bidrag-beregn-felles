package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.diverse.PlussMinus
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.inntekt.util.hentMappingerKapitalinntekt
import no.nav.bidrag.inntekt.util.hentMappingerLigs
import no.nav.bidrag.transport.behandling.inntekt.request.SkattegrunnlagForLigningsår
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Month
import java.time.Year
import java.time.YearMonth

class SkattegrunnlagService {
    fun beregnSkattegrunnlag(
        skattegrunnlagListe: List<SkattegrunnlagForLigningsår>,
        inntektsrapportering: Inntektsrapportering,
    ): List<SummertÅrsinntekt> = if (skattegrunnlagListe.isNotEmpty()) {
        val mapping =
            if (inntektsrapportering == Inntektsrapportering.KAPITALINNTEKT) hentMappingerKapitalinntekt() else hentMappingerLigs()
        beregnInntekt(
            skattegrunnlagListe = skattegrunnlagListe,
            mapping = mapping,
            inntektRapportering = inntektsrapportering,
        )
    } else {
        emptyList()
    }

    private fun beregnInntekt(
        skattegrunnlagListe: List<SkattegrunnlagForLigningsår>,
        mapping: List<MappingPoster>,
        inntektRapportering: Inntektsrapportering,
    ): List<SummertÅrsinntekt> {
        val summertÅrsinntektListe = mutableListOf<SummertÅrsinntekt>()
        val grunnlagListe = mutableSetOf<String>()

        skattegrunnlagListe.forEach { skattegrunnlagForLigningsår ->
            if (skattegrunnlagForLigningsår.skattegrunnlagsposter.isEmpty()) {
                return@forEach
            }

            val inntektPostListe = mutableListOf<InntektPost>()
            var sumInntekt = BigDecimal.ZERO

            skattegrunnlagForLigningsår.skattegrunnlagsposter.forEach { post ->
                val match = mapping.find { it.fulltNavnInntektspost == post.kode }
                if (match != null) {
                    if (match.plussMinus == PlussMinus.PLUSS) {
                        sumInntekt += post.beløp
                    } else {
                        sumInntekt -= post.beløp
                    }

                    grunnlagListe.add(skattegrunnlagForLigningsår.referanse)
                    inntektPostListe.add(
                        InntektPost(
                            kode = match.fulltNavnInntektspost,
                            beløp = post.beløp.setScale(0, RoundingMode.HALF_UP),
                        ),
                    )
                }
            }
            summertÅrsinntektListe.add(
                SummertÅrsinntekt(
                    inntektRapportering = inntektRapportering,
                    sumInntekt = sumInntekt.setScale(0, RoundingMode.HALF_UP),
                    periode =
                    ÅrMånedsperiode(
                        fom = YearMonth.of(skattegrunnlagForLigningsår.ligningsår, Month.JANUARY),
                        til = YearMonth.of(skattegrunnlagForLigningsår.ligningsår, Month.DECEMBER),
                    ),
                    inntektPostListe = inntektPostListe,
                    grunnlagsreferanseListe = listOf(skattegrunnlagForLigningsår.referanse),
                ),
            )
        }

        return summertÅrsinntektListe
    }
}

data class Post(val fulltNavnInntektspost: String)

data class PostKonfig(val plussMinus: String, val sekkepost: String, val fom: String, val tom: String)

data class MappingPoster(val fulltNavnInntektspost: String, val plussMinus: PlussMinus, val sekkepost: Boolean, val fom: Year, val tom: Year)
