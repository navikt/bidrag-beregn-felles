package no.nav.bidrag.inntekt.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.bidrag.domene.enums.diverse.PlussMinus
import no.nav.bidrag.inntekt.service.MappingPoster
import no.nav.bidrag.inntekt.service.Post
import no.nav.bidrag.inntekt.service.PostKonfig
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.time.Year

fun hentMappingerKapitalinntekt(): List<MappingPoster> = hentMapping("/files/mapping_kaps.yaml")
fun hentMappingerLigs(): List<MappingPoster> = hentMapping("/files/mapping_ligs.yaml")
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
