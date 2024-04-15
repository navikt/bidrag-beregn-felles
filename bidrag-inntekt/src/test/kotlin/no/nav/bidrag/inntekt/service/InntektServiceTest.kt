package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
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
    private val filnavnEksempelRequestAlleYtelser = "src/test/resources/testfiler/eksempel_request_alle_ytelser.json"
    private val inntektRequestAlleYtelser = TestUtil.byggInntektRequest(filnavnEksempelRequestAlleYtelser)
    private val filnavnEksempelRequestUtenInntekter = "src/test/resources/testfiler/eksempel_request_uten_inntekter.json"
    private val inntektRequestUtenInntekter = TestUtil.byggInntektRequest(filnavnEksempelRequestUtenInntekter)

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

    @Test
    fun `skal transformere inntekter inkludert ytelser`() {
        val transformerteInntekterResponseDto =
            inntektService.transformerInntekter(inntektRequestAlleYtelser.copy(ainntektHentetDato = ainntektHentetDato))

        TestUtil.printJson(transformerteInntekterResponseDto)

        assertSoftly {
            transformerteInntekterResponseDto.shouldNotBeNull()
            transformerteInntekterResponseDto.versjon shouldBe APP_VERSJON
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldHaveSize(13)

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT }.size shouldBe 2
            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].kode shouldBe "arbeidsavklaringspenger"
            transformerteInntekterResponseDto.summertÅrsinntektListe[0].inntektPostListe[0].visningsnavn shouldBe "Arbeidsavklaringspenger"

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_3MND }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_12MND }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.LIGNINGSINNTEKT }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KAPITALINNTEKT }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KONTANTSTØTTE }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.SMÅBARNSTILLEGG }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.UTVIDET_BARNETRYGD }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.BARNETILLEGG }.size shouldBe 0

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AAP }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.DAGPENGER }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.FORELDREPENGER }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.INTRODUKSJONSSTØNAD }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KVALIFISERINGSSTØNAD }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.PENSJON }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.SYKEPENGER }.size shouldBe 1

            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.OVERGANGSSTØNAD }.size shouldBe 2
            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.OVERGANGSSTØNAD }[0].visningsnavn shouldBe "Overgangsstønad 2021"
            transformerteInntekterResponseDto.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.OVERGANGSSTØNAD }[1].visningsnavn shouldBe "Overgangsstønad 2022"

            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldNotBeEmpty()
            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldHaveSize(17)
            transformerteInntekterResponseDto.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2021 }.sumOf { it.sumInntekt.toInt() }.shouldBe(7000)
            transformerteInntekterResponseDto.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2022 }.sumOf { it.sumInntekt.toInt() }.shouldBe(30000)
        }
    }

    @Test
    fun `skal returnere tom respons hvis request ikke inneholder data`() {
        val transformerteInntekterResponseDto =
            inntektService.transformerInntekter(inntektRequestUtenInntekter.copy(ainntektHentetDato = ainntektHentetDato))

        TestUtil.printJson(transformerteInntekterResponseDto)

        assertSoftly {
            transformerteInntekterResponseDto.shouldNotBeNull()
            transformerteInntekterResponseDto.versjon shouldBe APP_VERSJON
            transformerteInntekterResponseDto.summertÅrsinntektListe.shouldBeEmpty()
            transformerteInntekterResponseDto.summertMånedsinntektListe.shouldBeEmpty()
        }
    }
}
