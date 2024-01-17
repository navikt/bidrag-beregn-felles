package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.bigdecimal.shouldBeZero
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.util.visningsnavn
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
        fun `skal transformere årsinntekter når dagens dato er 2023-01-01`() {
            val ainntektHentetDato = LocalDate.of(2023, 1, 1)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 3)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    visningsnavn shouldBe "${Inntektsrapportering.AINNTEKT.visningsnavn.intern} 2021"
                    sumInntekt shouldBe BigDecimal.valueOf(4000)
                    periode.fom shouldBe YearMonth.of(2021, 1)
                    periode.til shouldBe YearMonth.of(2021, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 4000
                    inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"
                    inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"
                    inntektPostListe[0].beløp.toInt() shouldBe 4000
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(393000.789)
                    periode.fom shouldBe YearMonth.of(2021, 12)
                    periode.til shouldBe YearMonth.of(2022, 11)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 393000
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(660000)
                    periode.fom shouldBe YearMonth.of(2022, 9)
                    periode.til shouldBe YearMonth.of(2022, 11)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 660000
                    inntektPostListe[0].kode shouldBe "fastloenn"
                    inntektPostListe[0].visningsnavn shouldBe "Fastlønn"
                    inntektPostListe[0].beløp.toInt() shouldBe 660000
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når dagens dato er 2023-01-10`() {
            val ainntektHentetDato = LocalDate.of(2023, 1, 10)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldNotBeEmpty()
                transformerteInntekter.size shouldBe 4

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    visningsnavn shouldBe "${Inntektsrapportering.AINNTEKT.visningsnavn.intern} 2021"
                    sumInntekt shouldBe BigDecimal.valueOf(4000)
                    periode.fom shouldBe YearMonth.of(2021, 1)
                    periode.til shouldBe YearMonth.of(2021, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 4000
                    inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"
                    inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"
                    inntektPostListe[0].beløp.toInt() shouldBe 4000
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    visningsnavn shouldBe "${Inntektsrapportering.AINNTEKT.visningsnavn.intern} 2022"
                    sumInntekt shouldBe BigDecimal.valueOf(446000.789)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 446000
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(446000.789)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 446000
                }

                with(transformerteInntekter[3]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(660000)
                    periode.fom shouldBe YearMonth.of(2022, 10)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 660000
                    inntektPostListe[0].kode shouldBe "fastloenn"
                    inntektPostListe[0].visningsnavn shouldBe "Fastlønn"
                    inntektPostListe[0].beløp.toInt() shouldBe 660000
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal transformere årsinntekter når dagens dato er 2023-09-01`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)

            val ainntektService = AinntektService()

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldNotBeEmpty()
                transformerteInntekter.size shouldBe 4

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    visningsnavn shouldBe "${Inntektsrapportering.AINNTEKT.visningsnavn.intern} 2021"
                    sumInntekt shouldBe BigDecimal.valueOf(4000)
                    periode.fom shouldBe YearMonth.of(2021, 1)
                    periode.til shouldBe YearMonth.of(2021, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 4000
                    inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"
                    inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"
                    inntektPostListe[0].beløp.toInt() shouldBe 4000
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT
                    visningsnavn shouldBe "${Inntektsrapportering.AINNTEKT.visningsnavn.intern} 2022"
                    sumInntekt shouldBe BigDecimal.valueOf(446000.789)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 446000
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_12MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(743001.119)
                    periode.fom shouldBe YearMonth.of(2022, 8)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 4
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(743001)
                }

                with(transformerteInntekter[3]) {
                    inntektRapportering shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                    visningsnavn shouldBe Inntektsrapportering.AINNTEKT_BEREGNET_3MND.visningsnavn.intern
                    sumInntekt shouldBe BigDecimal.valueOf(880000)
                    periode.fom shouldBe YearMonth.of(2023, 5)
                    periode.til shouldBe YearMonth.of(2023, 7)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 3
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBe 880000
                }
            }
        }

        @Test
        fun `summert årsinntekt skal bli null dersom periode for månedsinntekter er null`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val ainntektService = AinntektService()

            val ainntektspost = inntektRequest.ainntektsposter[0]
            val ainntektspostMedOpptjeningsperiodeTilLikFra =
                ainntektspost.copy(opptjeningsperiodeTil = ainntektspost.opptjeningsperiodeFra)

            val transformerteInntekter =
                ainntektService.beregnAarsinntekt(listOf(ainntektspostMedOpptjeningsperiodeTilLikFra), ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.size.shouldBe(2)
                transformerteInntekter[0].sumInntekt.shouldBeZero()
                transformerteInntekter[1].sumInntekt.shouldBeZero()
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
                ainntektService.beregnMaanedsinntekt(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.size shouldBeExactly 20

                // Assertions for total sum of income in each year
                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2021 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 4000

                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2022 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 446000

                transformerteInntekter
                    .filter { it.gjelderÅrMåned.year == 2023 }
                    .sumOf { it.sumInntekt.toInt() } shouldBeExactly 468000

                // Assertions for the first element
                with(transformerteInntekter[0]) {
                    gjelderÅrMåned shouldBe YearMonth.of(2021, 11)
                    sumInntekt shouldBe BigDecimal.valueOf(2000)
                    inntektPostListe.size shouldBeExactly 1
                    inntektPostListe.sumOf { it.beløp.toInt() } shouldBeExactly 2000
                    inntektPostListe[0].kode shouldBe "overtidsgodtgjoerelse"
                    inntektPostListe[0].visningsnavn shouldBe "Overtidsgodtgjørelse"
                    inntektPostListe[0].beløp.toInt() shouldBeExactly 2000
                }
            }
        }

        @Test
        fun `skal ikke transformere noen månedsinntekter dersom perioden er på null måneder`() {
            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val ainntektService = AinntektService()

            val ainntektspost = inntektRequest.ainntektsposter[0]
            val ainntektspostMedOpptjeningsperiodeTilLikFra =
                ainntektspost.copy(opptjeningsperiodeTil = ainntektspost.opptjeningsperiodeFra)

            val transformerteInntekter =
                ainntektService.beregnMaanedsinntekt(listOf(ainntektspostMedOpptjeningsperiodeTilLikFra), ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.shouldBeEmpty()
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

            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val ainntektService = AinntektService()

            val ainntektspost = inntektRequest.ainntektsposter[0]
            val ainntektspostMedOpptjeningsperiodeTilLikFra =
                ainntektspost.copy(
                    opptjeningsperiodeTil = ainntektspost.opptjeningsperiodeFra?.plusMonths(periodeIMnd),
                    beløp = tremånederslønn,
                )

            val transformerteInntekter =
                ainntektService.beregnMaanedsinntekt(listOf(ainntektspostMedOpptjeningsperiodeTilLikFra), ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.size shouldBe periodeIMnd.toInt()

                for (i in 0 until periodeIMnd.toInt()) {
                    with(transformerteInntekter[i]) {
                        sumInntekt shouldBe månedslønn
                        inntektPostListe.size shouldBe 1
                        inntektPostListe[0].beløp shouldBe månedslønn
                    }
                }
            }
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

            val ainntektHentetDato = LocalDate.of(2023, 9, 1)
            val ainntektService = AinntektService()

            val ainntektspost = inntektRequest.ainntektsposter[0]
            val ainntektspostMedOpptjeningsperiodeTilLikFra =
                ainntektspost.copy(
                    opptjeningsperiodeTil = ainntektspost.opptjeningsperiodeFra?.plusMonths(periodeIMnd),
                    beløp = tremånederslønn,
                )

            val transformerteInntekter =
                ainntektService.beregnMaanedsinntekt(listOf(ainntektspostMedOpptjeningsperiodeTilLikFra), ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                transformerteInntekter.shouldNotBeNull()
                transformerteInntekter.size shouldBe periodeIMnd.toInt()

                for (i in 0 until periodeIMnd.toInt()) {
                    with(transformerteInntekter[i]) {
                        sumInntekt shouldBe månedslønn
                        inntektPostListe.size shouldBe 1
                        inntektPostListe[0].beløp shouldBe månedslønn
                    }
                }
            }
        }
    }
}
