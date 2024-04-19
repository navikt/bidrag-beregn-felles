package no.nav.bidrag.inntekt.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.bidrag.commons.service.finnVisningsnavn
import no.nav.bidrag.domene.enums.diverse.PlussMinus
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavnIntern
import no.nav.bidrag.transport.behandling.inntekt.request.SkattegrunnlagForLigningsår
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.math.BigDecimal
import java.time.Month
import java.time.Year
import java.time.YearMonth

class SkattegrunnlagService {
    fun beregnSkattegrunnlag(
        skattegrunnlagListe: List<SkattegrunnlagForLigningsår>,
        inntektsrapportering: Inntektsrapportering,
    ): List<SummertÅrsinntekt> {
        return if (skattegrunnlagListe.isNotEmpty()) {
            val filnavn =
                if (inntektsrapportering == Inntektsrapportering.KAPITALINNTEKT) "/files/mapping_kaps.yaml" else "files/mapping_ligs.yaml"
            val mapping = hentMapping(filnavn)
            beregnInntekt(
                skattegrunnlagListe = skattegrunnlagListe,
                mapping = mapping,
                inntektRapportering = inntektsrapportering,
            )
        } else {
            emptyList()
        }
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
                            visningsnavn = finnVisningsnavn(match.fulltNavnInntektspost),
                            beløp = post.beløp,
                        ),
                    )
                }
            }
            summertÅrsinntektListe.add(
                SummertÅrsinntekt(
                    inntektRapportering = inntektRapportering,
                    visningsnavn = inntektRapportering.visningsnavnIntern(skattegrunnlagForLigningsår.ligningsår),
                    sumInntekt = sumInntekt,
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

    private fun hentMapping(path: String): List<MappingPoster> {
        try {
            val objectMapper = ObjectMapper(YAMLFactory())
            objectMapper.findAndRegisterModules()
            val pathKapsfil = ClassPathResource(path).inputStream
            val mapping: Map<Post, List<PostKonfig>> = objectMapper.readValue(pathKapsfil)
            return mapping.flatMap { (post, postKonfigs) ->
                postKonfigs.map { postKonfig ->
                    MappingPoster(
                        fulltNavnInntektspost = post.fulltNavnInntektspost,
                        plussMinus = PlussMinus.valueOf(postKonfig.plussMinus),
                        sekkepost = postKonfig.sekkepost == "JA",
                        fom = Year.parse(postKonfig.fom),
                        tom = Year.parse(postKonfig.tom),
                    )
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Kunne ikke laste fil", e)
        }
    }
}

data class Post(
    val fulltNavnInntektspost: String,
)

data class PostKonfig(
    val plussMinus: String,
    val sekkepost: String,
    val fom: String,
    val tom: String,
)

data class MappingPoster(
    val fulltNavnInntektspost: String,
    val plussMinus: PlussMinus,
    val sekkepost: Boolean,
    val fom: Year,
    val tom: Year,
)
