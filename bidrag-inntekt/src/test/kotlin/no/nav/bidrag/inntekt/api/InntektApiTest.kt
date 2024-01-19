package no.nav.bidrag.inntekt.api

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.commons.service.KodeverkProvider
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.InntektApi
import no.nav.bidrag.inntekt.TestUtil.Companion.fileToObject
import no.nav.bidrag.inntekt.tesdata.StubUtils
import no.nav.bidrag.inntekt.tesdata.StubUtils.Companion.kodeverkUrl
import no.nav.bidrag.transport.behandling.inntekt.request.TransformerInntekterRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InntektApiTest {
    private val inntektApi = InntektApi(kodeverkUrl)
    private val ainntektHentetDato = LocalDate.of(2023, 9, 1)

    @BeforeEach
    fun initKodeverk() {
        StubUtils.wireMockServer.start()
        KodeverkProvider.initialiser(kodeverkUrl)
        StubUtils.stubKodeverkSkattegrunnlag()
        StubUtils.stubKodeverkLønnsbeskrivelse()
        StubUtils.stubKodeverkYtelsesbeskrivelser()
        StubUtils.stubKodeverkPensjonsbeskrivelser()
        StubUtils.stubKodeverkNaeringsinntektsbeskrivelser()
    }

    @AfterEach
    fun clearMocks() {
        StubUtils.wireMockServer.stop()
    }

    @Test
    fun `skal transformere inntekter`() {
        val filnavnEksempelRequest = "/testfiler/eksempel_request.json"

        val transformerteInntekter = inntektApi.transformerInntekter(
            fileToObject<TransformerInntekterRequest>(filnavnEksempelRequest).copy(ainntektHentetDato = ainntektHentetDato),
        )

        assertSoftly {
            transformerteInntekter.shouldNotBeNull()
            transformerteInntekter.summertÅrsinntektListe.shouldNotBeEmpty()
            transformerteInntekter.summertÅrsinntektListe.shouldHaveSize(23)

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT }.size.shouldBe(2)
            transformerteInntekter.summertÅrsinntektListe[0].inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"
            transformerteInntekter.summertÅrsinntektListe[0].inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_3MND }.size.shouldBe(1)

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_12MND }.size.shouldBe(1)

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.LIGNINGSINNTEKT }.size.shouldBe(2)

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.KAPITALINNTEKT }.size.shouldBe(2)

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.SMÅBARNSTILLEGG }.size shouldBe 3

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.UTVIDET_BARNETRYGD }.size shouldBe 2

            transformerteInntekter.summertÅrsinntektListe
                .filter { it.inntektRapportering == Inntektsrapportering.BARNETILLEGG }.size shouldBe 5

            transformerteInntekter.summertMånedsinntektListe.shouldNotBeEmpty()
            transformerteInntekter.summertMånedsinntektListe.shouldHaveSize(20)
            transformerteInntekter.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2021 }.sumOf { it.sumInntekt.toInt() }.shouldBe(4000)
            transformerteInntekter.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2022 }.sumOf { it.sumInntekt.toInt() }.shouldBe(446000)
            transformerteInntekter.summertMånedsinntektListe
                .filter { it.gjelderÅrMåned.year == 2023 }.sumOf { it.sumInntekt.toInt() }.shouldBe(468000)
        }
    }
}
