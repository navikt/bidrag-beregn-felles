package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.InntektApi
import no.nav.bidrag.inntekt.TestUtil
import no.nav.bidrag.inntekt.tesdata.StubUtils.Companion.kodeverkUrl
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("InntektServiceTest")
class InntektServiceTest : AbstractServiceTest() {
    private val ainntektHentetDato = LocalDate.of(2023, 9, 1)
    private val inntektService = InntektApi(kodeverkUrl)
    private val filnavnEksempelRequest = "src/test/resources/testfiler/eksempel_request.json"
    private val inntektRequest = TestUtil.byggInntektRequest(filnavnEksempelRequest)

    @Test
    fun `skal transformere inntekter`() {
        val transformerteInntekterResponseDto =
            inntektService.transformerInntekter(
                inntektRequest.copy(ainntektHentetDato = ainntektHentetDato),
            )

        assertSoftly {
            transformerteInntekterResponseDto.shouldNotBeNull()
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertÅrsinntektListe.size shouldBe 8

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe.filter {
                it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_3MND
            }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe.filter {
                it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_12MND
            }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe.filter {
                it.inntektRapportering == Inntektsrapportering.LIGNINGSINNTEKT
            }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe.filter {
                it.inntektRapportering == Inntektsrapportering.KAPITALINNTEKT
            }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"

            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"

            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertMånedsinntektListe.size shouldBe 20
        }
    }
}
