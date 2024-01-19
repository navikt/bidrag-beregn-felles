package no.nav.bidrag.inntekt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.transport.behandling.grunnlag.response.SkattegrunnlagspostDto
import no.nav.bidrag.transport.behandling.inntekt.request.Barnetillegg
import no.nav.bidrag.transport.behandling.inntekt.request.Kontantstøtte
import no.nav.bidrag.transport.behandling.inntekt.request.SkattegrunnlagForLigningsår
import no.nav.bidrag.transport.behandling.inntekt.request.TransformerInntekterRequest
import no.nav.bidrag.transport.behandling.inntekt.request.UtvidetBarnetrygdOgSmåbarnstillegg
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate

class TestUtil {
    companion object {
        inline fun <reified T> fileToObject(path: String): T {
            val jsonString = TestUtil::class.java.getResource(path).readText()
            return ObjectMapper().findAndRegisterModules().readValue(jsonString, T::class.java)
        }

        fun byggSkattegrunnlagDto() = listOf(
            SkattegrunnlagForLigningsår(
                ligningsår = 2021,
                skattegrunnlagsposter = byggSkattegrunnlagPostListe(),
            ),
            SkattegrunnlagForLigningsår(
                ligningsår = 2022,
                skattegrunnlagsposter = byggSkattegrunnlagPostListe(),
            ),
        )

        private fun byggSkattegrunnlagPostListe() = listOf(
            // KAPS
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // KAPS, MINUS, NEI
                inntektType = "andelIFellesTapVedSalgAvAndelISDF",
                belop = BigDecimal.valueOf(1000),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // KAPS, MINUS, NEI
                inntektType = "andreFradragsberettigedeKostnader",
                belop = BigDecimal.valueOf(500),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // KAPS, PLUSS, NEI
                inntektType = "annenSkattepliktigKapitalinntektFraAnnetFinansprodukt",
                belop = BigDecimal.valueOf(1500),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // KAPS, PLUSS, JA
                inntektType = "samledeOpptjenteRenterIUtenlandskeBanker",
                belop = BigDecimal.valueOf(1700),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                inntektType = "ukjent",
                belop = BigDecimal.valueOf(100000),
            ),
            // LIGS
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // LIGS, PLUSS, NEI
                inntektType = "alderspensjonFraIPAOgIPS",
                belop = BigDecimal.valueOf(100),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                inntektType = "ukjent",
                belop = BigDecimal.valueOf(1700),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // LIGS, PLUSS, NEI
                inntektType = "annenArbeidsinntekt",
                belop = BigDecimal.valueOf(200),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // LIGS, PLUSS, NEI
                inntektType = "annenPensjonFoerAlderspensjon",
                belop = BigDecimal.valueOf(300),
            ),
            SkattegrunnlagspostDto(
                skattegrunnlagType = "Ordinær",
                // LIGS, PLUSS, NEI
                inntektType = "arbeidsavklaringspenger",
                belop = BigDecimal.valueOf(400),
            ),
        )

        fun byggKontantstøtte() = listOf(
            // barn 1
            Kontantstøtte(
                periodeFra = LocalDate.parse("2022-09-01"),
                periodeTil = LocalDate.parse("2023-01-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "98765432109",
            ),
            Kontantstøtte(
                periodeFra = LocalDate.parse("2023-05-01"),
                periodeTil = null,
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "98765432109",
            ),
            // barn 2
            Kontantstøtte(
                periodeFra = LocalDate.parse("2021-11-01"),
                periodeTil = LocalDate.parse("2022-07-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
            Kontantstøtte(
                periodeFra = LocalDate.parse("2022-10-01"),
                periodeTil = LocalDate.parse("2023-02-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
            Kontantstøtte(
                periodeFra = LocalDate.parse("2023-05-01"),
                periodeTil = LocalDate.parse("2023-08-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
        )

        fun byggUtvidetBarnetrygdOgSmåbarnstillegg() = listOf(
            // Utvidet barnetrygd
            UtvidetBarnetrygdOgSmåbarnstillegg(
                type = "UTVIDET",
                periodeFra = LocalDate.parse("2019-01-01"),
                periodeTil = LocalDate.parse("2019-10-01"),
                beløp = BigDecimal.valueOf(1054),
            ),
            UtvidetBarnetrygdOgSmåbarnstillegg(
                type = "UTVIDET",
                periodeFra = LocalDate.parse("2020-11-01"),
                periodeTil = LocalDate.parse("2022-10-01"),
                beløp = BigDecimal.valueOf(1054),
            ),
            // Småbarnstillegg
            UtvidetBarnetrygdOgSmåbarnstillegg(
                type = "SMÅBARNSTILLEGG",
                periodeFra = LocalDate.parse("2021-11-01"),
                periodeTil = LocalDate.parse("2022-04-01"),
                beløp = BigDecimal.valueOf(660),
            ),
            UtvidetBarnetrygdOgSmåbarnstillegg(
                type = "SMÅBARNSTILLEGG",
                periodeFra = LocalDate.parse("2022-06-01"),
                periodeTil = LocalDate.parse("2022-08-01"),
                beløp = BigDecimal.valueOf(660),
            ),
            UtvidetBarnetrygdOgSmåbarnstillegg(
                type = "SMÅBARNSTILLEGG",
                periodeFra = LocalDate.parse("2022-10-01"),
                periodeTil = null,
                beløp = BigDecimal.valueOf(660),
            ),
        )

        fun byggBarnetilleggPensjon() = listOf(
            // barn 1
            Barnetillegg(
                periodeFra = LocalDate.parse("2022-09-01"),
                periodeTil = LocalDate.parse("2023-01-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "98765432109",
            ),
            Barnetillegg(
                periodeFra = LocalDate.parse("2023-05-01"),
                periodeTil = null,
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "98765432109",
            ),
            // barn 2
            Barnetillegg(
                periodeFra = LocalDate.parse("2021-11-01"),
                periodeTil = LocalDate.parse("2022-07-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
            Barnetillegg(
                periodeFra = LocalDate.parse("2022-10-01"),
                periodeTil = LocalDate.parse("2023-02-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
            Barnetillegg(
                periodeFra = LocalDate.parse("2023-05-01"),
                periodeTil = LocalDate.parse("2023-08-01"),
                beløp = BigDecimal.valueOf(7500),
                barnPersonId = "12345678901",
            ),
        )

        fun byggInntektRequest(filnavn: String): TransformerInntekterRequest {
            val objectMapper = ObjectMapper()
            objectMapper.registerKotlinModule()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

            val file = File(filnavn)

            return objectMapper.readValue(file, TransformerInntekterRequest::class.java)
        }

        fun <T> printJson(json: List<T>) {
            val objectMapper = ObjectMapper()
            objectMapper.registerKotlinModule()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

            println(objectMapper.writeValueAsString(json))
        }

        fun <T> printJson(json: T) {
            val objectMapper = ObjectMapper()
            objectMapper.registerKotlinModule()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

            println(objectMapper.writeValueAsString(json))
        }
    }
}
