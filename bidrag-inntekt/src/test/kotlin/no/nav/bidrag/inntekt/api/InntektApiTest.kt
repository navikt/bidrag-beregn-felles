package no.nav.bidrag.inntekt.api

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainAll
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
import java.time.YearMonth

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
    fun `skal transformere månedsinntekter med grunnlagsreferanser`() {
        val filnavnEksempelRequest = "/testfiler/eksempel_request_referanse.json"

        val transformerteInntekter = inntektApi.transformerInntekter(
            fileToObject<TransformerInntekterRequest>(filnavnEksempelRequest).copy(ainntektHentetDato = LocalDate.parse("2024-02-11")),
        )

        assertSoftly {
            transformerteInntekter.shouldNotBeNull()
            transformerteInntekter.summertÅrsinntektListe.shouldHaveSize(14)
            transformerteInntekter.summertMånedsinntektListe.shouldHaveSize(11)

            assertSoftly(transformerteInntekter.summertMånedsinntektListe) {
                shouldHaveSize(11)
                assertSoftly(find { it.gjelderÅrMåned == YearMonth.of(2023, 11) }) {
                    shouldNotBeNull()
                    inntektPostListe.shouldHaveSize(1)
                    grunnlagsreferanseListe.shouldHaveSize(2)
                    grunnlagsreferanseListe.shouldContainAll(
                        "innhentet_ainntekt_20230201",
                        "innhentet_ainntekt_20231101",
                    )
                }
                assertSoftly(find { it.gjelderÅrMåned == YearMonth.of(2023, 5) }) {
                    shouldNotBeNull()
                    inntektPostListe.shouldHaveSize(1)
                    grunnlagsreferanseListe.shouldHaveSize(1)
                    grunnlagsreferanseListe.shouldContainAll(
                        "innhentet_ainntekt_20230501",
                    )
                }
            }
        }
    }

    @Test
    fun `skal transformere årsinntekter med grunnlagsreferanser`() {
        val filnavnEksempelRequest = "/testfiler/eksempel_request_referanse.json"

        val transformerteInntekter = inntektApi.transformerInntekter(
            fileToObject<TransformerInntekterRequest>(filnavnEksempelRequest).copy(ainntektHentetDato = LocalDate.parse("2024-02-11")),
        )

        assertSoftly {
            transformerteInntekter.shouldNotBeNull()
            transformerteInntekter.summertÅrsinntektListe.shouldHaveSize(14)
            transformerteInntekter.summertMånedsinntektListe.shouldHaveSize(11)

            assertSoftly(transformerteInntekter.summertÅrsinntektListe) {
                shouldHaveSize(14)
                assertSoftly(filter { it.inntektRapportering == Inntektsrapportering.KONTANTSTØTTE }) {
                    shouldHaveSize(2)
                    it[0].grunnlagsreferanseListe.shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldContainAll(
                        "kontantstøtte_20230101",
                    )
                }
                assertSoftly(filter { it.inntektRapportering == Inntektsrapportering.SMÅBARNSTILLEGG }) {
                    shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldContainAll(
                        "småbarnstillegg_20230101",
                    )
                }
                assertSoftly(filter { it.inntektRapportering == Inntektsrapportering.UTVIDET_BARNETRYGD }) {
                    shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldContainAll(
                        "utvidetBarnetrygd_20230101",
                    )
                }
                assertSoftly(filter { it.inntektRapportering == Inntektsrapportering.BARNETILLEGG }) {
                    shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldHaveSize(1)
                    it[0].grunnlagsreferanseListe.shouldContainAll(
                        "barnetillegg_20230101",
                    )
                }

                assertSoftly(find { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_3MND }) {
                    shouldNotBeNull()
                    inntektPostListe.shouldHaveSize(1)
                    grunnlagsreferanseListe.shouldHaveSize(4)
                    grunnlagsreferanseListe.shouldContainAll(
                        "innhentet_ainntekt_20230201",
                        "innhentet_ainntekt_20231101",
                        "innhentet_ainntekt_20231201",
                        "innhentet_ainntekt_20240101",
                    )
                }
                assertSoftly(filter { it.inntektRapportering == Inntektsrapportering.LIGNINGSINNTEKT }) {
                    shouldHaveSize(3)
                    assertSoftly(it[0]) {
                        periode.fom.shouldBe(YearMonth.parse("2021-01"))
                        grunnlagsreferanseListe.shouldHaveSize(1)
                        grunnlagsreferanseListe.shouldContainAll("innhentet_skattegrunnlag_20210101")
                    }
                    assertSoftly(it[1]) {
                        periode.fom.shouldBe(YearMonth.parse("2022-01"))
                        grunnlagsreferanseListe.shouldHaveSize(1)
                        grunnlagsreferanseListe.shouldContainAll("innhentet_skattegrunnlag_20220101")
                    }
                    assertSoftly(it[2]) {
                        periode.fom.shouldBe(YearMonth.parse("2023-01"))
                        grunnlagsreferanseListe.shouldHaveSize(1)
                        grunnlagsreferanseListe.shouldContainAll("innhentet_skattegrunnlag_20230101")
                    }
                }
                assertSoftly(find { it.inntektRapportering == Inntektsrapportering.AINNTEKT_BEREGNET_12MND }) {
                    shouldNotBeNull()
                    inntektPostListe.shouldHaveSize(1)
                    grunnlagsreferanseListe.shouldHaveSize(12)
                    grunnlagsreferanseListe.shouldContainAll(
                        "innhentet_ainntekt_20230201",
                        "innhentet_ainntekt_20230301",
                        "innhentet_ainntekt_20230401",
                        "innhentet_ainntekt_20230501",
                        "innhentet_ainntekt_20230601",
                        "innhentet_ainntekt_20230701",
                        "innhentet_ainntekt_20230801",
                        "innhentet_ainntekt_20230901",
                        "innhentet_ainntekt_20231001",
                        "innhentet_ainntekt_20231101",
                        "innhentet_ainntekt_20231201",
                        "innhentet_ainntekt_20240101",
                    )
                }
            }
        }
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
