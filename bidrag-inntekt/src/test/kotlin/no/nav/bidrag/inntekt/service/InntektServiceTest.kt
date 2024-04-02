package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.InntektApi
import no.nav.bidrag.inntekt.TestUtil
import no.nav.bidrag.inntekt.testdata.StubUtils.Companion.kodeverkUrl
import no.nav.bidrag.inntekt.util.VersionProvider.Companion.APP_VERSJON
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
        val transformerteInntekterResponseDto = inntektService.transformerInntekter(inntektRequest.copy(ainntektHentetDato = ainntektHentetDato))

        TestUtil.printJson(transformerteInntekterResponseDto)

        assertSoftly {
            transformerteInntekterResponseDto.shouldNotBeNull()
            transformerteInntekterResponseDto.versjon shouldBe APP_VERSJON
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldHaveSize(22)

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT }.size shouldBe 1
            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].kode shouldBe "fastloenn"
            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].visningsnavn shouldBe "Fastlønn"

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_3MND }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_12MND }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.LIGNINGSINNTEKT }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KAPITALINNTEKT }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KONTANTSTØTTE }.size shouldBe 5

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.SMÅBARNSTILLEGG }.size shouldBe 3

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.UTVIDET_BARNETRYGD }.size shouldBe 2

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.BARNETILLEGG }.size shouldBe 5

            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldHaveSize(15)
            transformerteInntekterResponseDto.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2021 }.sumOf { it.sumInntekt.toInt() }.shouldBe(0)
            transformerteInntekterResponseDto.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2022 }.sumOf { it.sumInntekt.toInt() }.shouldBe(450000)
            transformerteInntekterResponseDto.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2023 }.sumOf { it.sumInntekt.toInt() }.shouldBe(468000)
        }
    }
}
