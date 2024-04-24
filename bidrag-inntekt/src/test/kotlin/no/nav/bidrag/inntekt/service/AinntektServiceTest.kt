package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@DisplayName("AinntektServiceTest")
class AinntektServiceTest : AbstractServiceTest() {
    private val filnavnEksempelRequest = "src/test/resources/testfiler/eksempel_request.json"

    private val inntektRequest = TestUtil.byggInntektRequest(filnavnEksempelRequest)

    @Nested
    internal inner class BeregnÅrsinntekt {
        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når ainntektHentetDato er 2023-01-01`() {
            val ainntektHentetDato = LocalDate.of(2023, 1, 1)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(ainntektListeInn = inntektRequest.ainntektsposter, ainntektHentetDato = ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 2)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    sumInntekt shouldBe BigDecimal.valueOf(395001)
                    periode.fom shouldBe YearMonth.of(2021, 12)
                    periode.til shouldBe YearMonth.of(2022, 11)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 395001
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    sumInntekt shouldBe BigDecimal.valueOf(660000)
                    periode.fom shouldBe YearMonth.of(2022, 9)
                    periode.til shouldBe YearMonth.of(2022, 11)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 660000
                    inntektPostListe[0].kode shouldBe "fastloenn"
                    inntektPostListe[0].beløp.toInt() shouldBe 660000
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når ainntektHentetDato er 2023-01-10`() {
            val ainntektHentetDato = LocalDate.of(2023, 1, 10)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(ainntektListeInn = inntektRequest.ainntektsposter, ainntektHentetDato = ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldNotBeEmpty()
                transformerteInntekter.size shouldBe 3

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    sumInntekt shouldBe BigDecimal.valueOf(450001)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    sumInntekt shouldBe BigDecimal.valueOf(450001)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    sumInntekt shouldBe BigDecimal.valueOf(660000)
                    periode.fom shouldBe YearMonth.of(2022, 10)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 660000
                    inntektPostListe[0].kode shouldBe "fastloenn"
                    inntektPostListe[0].beløp.toInt() shouldBe 660000
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når ainntektHentetDato er 2023-09-01`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(ainntektListeInn = inntektRequest.ainntektsposter, ainntektHentetDato = ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldNotBeEmpty()
                transformerteInntekter.size shouldBe 3

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    sumInntekt shouldBe BigDecimal.valueOf(450001)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    sumInntekt shouldBe BigDecimal.valueOf(743001)
                    periode.fom shouldBe YearMonth.of(2022, 8)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 4
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(743001)
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    sumInntekt shouldBe BigDecimal.valueOf(912000)
                    periode.fom shouldBe YearMonth.of(2023, 5)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 4
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 912000
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når ainntektHentetDato er 2023-09-01 og ett vedtakstidspunktOpprinneligeVedtak er inkludert`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val vedtakstidspunktOpprinneligeVedtak = listOf(LocalDate.parse("2023-07-07"))

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(
                    ainntektListeInn = inntektRequest.ainntektsposter,
                    ainntektHentetDato = ainntektHentetDato,
                    vedtakstidspunktOpprinneligeVedtak = vedtakstidspunktOpprinneligeVedtak,
                )

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldNotBeEmpty()
                transformerteInntekter.size shouldBe 5

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    sumInntekt shouldBe BigDecimal.valueOf(450001)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                    grunnlagsreferanseListe.shouldContainAll(
                        listOf(
                            "A1",
                            "A2",
                            "A3",
                            "A4",
                            "A5",
                            "A6",
                            "A7",
                            "A8",
                            "A9",
                            "A10",
                        ),
                    )
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    sumInntekt shouldBe BigDecimal.valueOf(743001)
                    periode.fom shouldBe YearMonth.of(2022, 8)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 4
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(743001)
                    grunnlagsreferanseListe.shouldContainAll(
                        listOf(
                            "A6",
                            "A7",
                            "A8",
                            "A9",
                            "A10",
                            "A11",
                            "A12",
                            "A13",
                            "A14",
                            "A15",
                            "A16",
                            "A17",
                            "A18",
                            "A19",
                        ),
                    )
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                    sumInntekt shouldBe BigDecimal.valueOf(708001)
                    periode.fom shouldBe YearMonth.of(2022, 7)
                    periode.til shouldBe YearMonth.of(2023, 6)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(708001)
                    grunnlagsreferanseListe.shouldContainAll(
                        listOf(
                            "A5",
                            "A6",
                            "A7",
                            "A8",
                            "A9",
                            "A10",
                            "A11",
                            "A12",
                            "A13",
                            "A14",
                            "A15",
                            "A16",
                            "A17",
                            "A18",
                        ),
                    )
                }

                with(transformerteInntekter[3]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    sumInntekt shouldBe BigDecimal.valueOf(912000)
                    periode.fom shouldBe YearMonth.of(2023, 5)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 4
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 912000
                    grunnlagsreferanseListe.shouldContainAll(listOf("A15", "A16", "A17", "A18", "A19"))
                }

                with(transformerteInntekter[4]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                    sumInntekt shouldBe BigDecimal.valueOf(812000)
                    periode.fom shouldBe YearMonth.of(2023, 4)
                    periode.til shouldBe YearMonth.of(2023, 6)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 812000
                    grunnlagsreferanseListe.shouldContainAll(listOf("A14", "A15", "A16", "A17", "A18"))
                }
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal transformere årsinntekter når ainntektHentetDato er 2023-09-01 og to vedtakstidspunktOpprinneligeVedtak er inkludert`() {
        val ainntektHentetDato = LocalDate.of(2023, 9, 1)
        val vedtakstidspunktOpprinneligeVedtak = listOf(LocalDate.parse("2023-07-07"), LocalDate.parse("2023-06-07"))

        val ainntektService = AinntektService()

        val transformerteInntekter =
            ainntektService.beregnAarsinntekt(
                ainntektListeInn = inntektRequest.ainntektsposter,
                ainntektHentetDato = ainntektHentetDato,
                vedtakstidspunktOpprinneligeVedtak = vedtakstidspunktOpprinneligeVedtak,
            )

        TestUtil.printJson(transformerteInntekter)

        assertSoftly {
            transformerteInntekter.shouldNotBeNull()
            transformerteInntekter.shouldNotBeEmpty()
            transformerteInntekter.size shouldBe 7

            with(transformerteInntekter[0]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                sumInntekt shouldBe BigDecimal.valueOf(450001)
                periode.fom shouldBe YearMonth.of(2022, 1)
                periode.til shouldBe YearMonth.of(2022, 12)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 3
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A1",
                        "A2",
                        "A3",
                        "A4",
                        "A5",
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                    ),
                )
            }

            with(transformerteInntekter[1]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                sumInntekt shouldBe BigDecimal.valueOf(743001)
                periode.fom shouldBe YearMonth.of(2022, 8)
                periode.til shouldBe YearMonth.of(2023, 7)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4
                inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(743001)
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                        "A11",
                        "A12",
                        "A13",
                        "A14",
                        "A15",
                        "A16",
                        "A17",
                        "A18",
                        "A19",
                    ),
                )
            }

            with(transformerteInntekter[2]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(675001)
                periode.fom shouldBe YearMonth.of(2022, 6)
                periode.til shouldBe YearMonth.of(2023, 5)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 1
                inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(675001)
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A4",
                        "A5",
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                        "A11",
                        "A12",
                        "A13",
                        "A14",
                        "A15",
                    ),
                )
            }

            with(transformerteInntekter[3]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(708001)
                periode.fom shouldBe YearMonth.of(2022, 7)
                periode.til shouldBe YearMonth.of(2023, 6)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 3
                inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(708001)
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A5",
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                        "A11",
                        "A12",
                        "A13",
                        "A14",
                        "A15",
                        "A16",
                        "A17",
                        "A18",
                    ),
                )
            }

            with(transformerteInntekter[4]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                sumInntekt shouldBe BigDecimal.valueOf(912000)
                periode.fom shouldBe YearMonth.of(2023, 5)
                periode.til shouldBe YearMonth.of(2023, 7)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 912000
                grunnlagsreferanseListe.shouldContainAll(listOf("A15", "A16", "A17", "A18", "A19"))
            }

            with(transformerteInntekter[5]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(720000)
                periode.fom shouldBe YearMonth.of(2023, 3)
                periode.til shouldBe YearMonth.of(2023, 5)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 1
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 720000
                grunnlagsreferanseListe.shouldContainAll(listOf("A13", "A14", "A15"))
            }

            with(transformerteInntekter[6]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(812000)
                periode.fom shouldBe YearMonth.of(2023, 4)
                periode.til shouldBe YearMonth.of(2023, 6)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 3
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 812000
                grunnlagsreferanseListe.shouldContainAll(listOf("A14", "A15", "A16", "A17", "A18"))
            }
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal transformere årsinntekter når ainntektHentetDato er 2023-09-01 og to vedtakstidspunktOpprinneligeVedtak er inkludert med fjerning av duplikater`() {
        val ainntektHentetDato = LocalDate.of(2023, 9, 1)
        val vedtakstidspunktOpprinneligeVedtak = listOf(LocalDate.parse("2023-07-01"), LocalDate.parse("2023-06-07"))

        val ainntektService = AinntektService()

        val transformerteInntekter =
            ainntektService.beregnAarsinntekt(
                ainntektListeInn = inntektRequest.ainntektsposter,
                ainntektHentetDato = ainntektHentetDato,
                vedtakstidspunktOpprinneligeVedtak = vedtakstidspunktOpprinneligeVedtak,
            )

        TestUtil.printJson(transformerteInntekter)

        assertSoftly {
            transformerteInntekter.shouldNotBeNull()
            transformerteInntekter.shouldNotBeEmpty()
            transformerteInntekter.size shouldBe 5

            with(transformerteInntekter[0]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                sumInntekt shouldBe BigDecimal.valueOf(450001)
                periode.fom shouldBe YearMonth.of(2022, 1)
                periode.til shouldBe YearMonth.of(2022, 12)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 3
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 450001
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A1",
                        "A2",
                        "A3",
                        "A4",
                        "A5",
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                    ),
                )
            }

            with(transformerteInntekter[1]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                sumInntekt shouldBe BigDecimal.valueOf(743001)
                periode.fom shouldBe YearMonth.of(2022, 8)
                periode.til shouldBe YearMonth.of(2023, 7)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4
                inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(743001)
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                        "A11",
                        "A12",
                        "A13",
                        "A14",
                        "A15",
                        "A16",
                        "A17",
                        "A18",
                        "A19",
                    ),
                )
            }

            with(transformerteInntekter[2]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(675001)
                periode.fom shouldBe YearMonth.of(2022, 6)
                periode.til shouldBe YearMonth.of(2023, 5)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 1
                inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(675001)
                grunnlagsreferanseListe.shouldContainAll(
                    listOf(
                        "A4",
                        "A5",
                        "A6",
                        "A7",
                        "A8",
                        "A9",
                        "A10",
                        "A11",
                        "A12",
                        "A13",
                        "A14",
                        "A15",
                    ),
                )
            }

            with(transformerteInntekter[3]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                sumInntekt shouldBe BigDecimal.valueOf(912000)
                periode.fom shouldBe YearMonth.of(2023, 5)
                periode.til shouldBe YearMonth.of(2023, 7)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 912000
                grunnlagsreferanseListe.shouldContainAll(listOf("A15", "A16", "A17", "A18", "A19"))
            }

            with(transformerteInntekter[4]) {
                inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                sumInntekt shouldBe BigDecimal.valueOf(720000)
                periode.fom shouldBe YearMonth.of(2023, 3)
                periode.til shouldBe YearMonth.of(2023, 5)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 1
                inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 720000
                grunnlagsreferanseListe.shouldContainAll(listOf("A13", "A14", "A15"))
            }
        }
    }

    @Nested
    internal inner class BeregnMånedsinntekt {
        @Test
        fun `skal transformere månedsinntekter`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnMaanedsinntekt(ainntektListeInn = inntektRequest.ainntektsposter, ainntektHentetDato = ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.size shouldBeExactly 15

                // Assertions for total sum of income in each year
                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2021 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 0

                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2022 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 450001

                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2023 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 468000

                // Assertions for første element
                with(transformerteInntekter[0]) {
                    gjelderÅrMåned shouldBe YearMonth.of(2022, 5)
                    sumInntekt shouldBe BigDecimal.valueOf(75000)
                    inntektPostListe.size shouldBeExactly 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBeExactly 75000
                    inntektPostListe[0].kode shouldBe "fastloenn"
                    inntektPostListe[0].beløp.toInt() shouldBeExactly 50000
                }
            }
        }

        @Test
        fun `skal runde beregnet månedsinntekt opp hvis den er et desimaltall med minst fem tideler`() {
            val periodeIMnd: Long = 3
            val tremånederslønnInt = 1234565
            val tremånederslønn = BigDecimal(tremånederslønnInt)
            val månedslønnInt = tremånederslønnInt.div(3)
            val månedslønn = tremånederslønn.div(BigDecimal(periodeIMnd))

            // Verifisere at BigDecimal månedslønn er rundet opp til nærmeste heltall
            assertTrue(månedslønn.toInt() - månedslønnInt == 1)
        }

        @Test
        fun `skal runde beregnet månedsinntekt ned hvis den er et desimaltall med færre enn fem tideler`() {
            val periodeIMnd: Long = 3
            val tremånederslønnInt = 1234561
            val tremånederslønn = BigDecimal(tremånederslønnInt)
            val månedslønnInt = tremånederslønnInt.div(3)
            val månedslønn = tremånederslønn.div(BigDecimal(periodeIMnd))

            // Verifisere at BigDecimal månedslønn er rundet ned til nærmeste heltall tilsvarende som for Int
            assertTrue(månedslønn.toInt() - månedslønnInt == 0)
        }
    }
}
