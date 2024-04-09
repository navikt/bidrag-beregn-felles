package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.util.visningsnavnIntern
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@DisplayName("YtelserServiceTest")
class YtelserServiceTest : AbstractServiceTest() {
    @Nested
    internal inner class BeregnÅrsinntektYtelser {
        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal summere aap-inntekter fra ainntekter`() {
            val filnavnYtelserAapRequest = "src/test/resources/testfiler/eksempel_request_aap.json"
            val inntektRequest = TestUtil.byggInntektRequest(filnavnYtelserAapRequest)

            val ainntektHentetDato = LocalDate.of(2023, 12, 17)

            val ytelserService = YtelserService()

            val transformerteInntekter =
                ytelserService.beregnYtelser(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 2)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AAP
                    visningsnavn shouldBe Inntektsrapportering.AAP.visningsnavnIntern(2021)
                    sumInntekt shouldBe BigDecimal.valueOf(50000)
                    periode.fom shouldBe YearMonth.of(2021, 1)
                    periode.til shouldBe YearMonth.of(2021, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(50000)

                    with(inntektPostListe[0]) {
                        kode shouldBe "arbeidsavklaringspenger"
                        visningsnavn shouldBe "Arbeidsavklaringspenger"
                        beløp shouldBe BigDecimal.valueOf(50000)
                    }
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.AAP
                    visningsnavn shouldBe Inntektsrapportering.AAP.visningsnavnIntern(2022)
                    sumInntekt shouldBe BigDecimal.valueOf(78500.789)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(78500.789)

                    with(inntektPostListe[0]) {
                        kode shouldBe "arbeidsavklaringspenger"
                        visningsnavn shouldBe "Arbeidsavklaringspenger"
                        beløp shouldBe BigDecimal.valueOf(78500.789)
                    }
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal summere dagpenger-inntekter fra ainntekter`() {
            val filnavnYtelserDagpengerRequest = "src/test/resources/testfiler/eksempel_request_dagpenger.json"
            val inntektRequest = TestUtil.byggInntektRequest(filnavnYtelserDagpengerRequest)

            val ainntektHentetDato = LocalDate.of(2023, 12, 17)

            val ytelserService = YtelserService()

            val transformerteInntekter =
                ytelserService.beregnYtelser(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 2)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.DAGPENGER
                    visningsnavn shouldBe Inntektsrapportering.DAGPENGER.visningsnavnIntern(2021)
                    sumInntekt shouldBe BigDecimal.valueOf(50000)
                    periode.fom shouldBe YearMonth.of(2021, 1)
                    periode.til shouldBe YearMonth.of(2021, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 2
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(50000)

                    with(inntektPostListe[0]) {
                        kode shouldBe "dagpengerTilFiskerSomBareHarHyre"
                        visningsnavn shouldBe "Dagpenger til fisker som bare har hyre"
                        beløp shouldBe BigDecimal.valueOf(30000)
                    }

                    with(inntektPostListe[1]) {
                        kode shouldBe "dagpengerVedArbeidsloeshet"
                        visningsnavn shouldBe "Dagpenger ved arbeidsløshet"
                        beløp shouldBe BigDecimal.valueOf(20000)
                    }
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.DAGPENGER
                    visningsnavn shouldBe Inntektsrapportering.DAGPENGER.visningsnavnIntern(2022)
                    sumInntekt shouldBe BigDecimal.valueOf(1000)
                    periode.fom shouldBe YearMonth.of(2022, 1)
                    periode.til shouldBe YearMonth.of(2022, 12)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(1000)

                    with(inntektPostListe[0]) {
                        kode shouldBe "dagpengerVedArbeidsloeshet"
                        visningsnavn shouldBe "Dagpenger ved arbeidsløshet"
                        beløp shouldBe BigDecimal.valueOf(1000)
                    }
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal summere overgangsstønad-inntekter fra ainntekter`() {
            val filnavnYtelserOvergangsstønadRequest = "src/test/resources/testfiler/eksempel_request_overgangsstønad.json"
            val inntektRequest = TestUtil.byggInntektRequest(filnavnYtelserOvergangsstønadRequest)

            val ainntektHentetDato = LocalDate.of(2024, 4, 5)

            val ytelserServiceOvergangsstønad = YtelserServiceOvergangsstønad()

            val transformerteInntekter =
                ytelserServiceOvergangsstønad.beregnYtelser(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 2)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.OVERGANGSSTØNAD
                    visningsnavn shouldBe Inntektsrapportering.OVERGANGSSTØNAD.visningsnavnIntern(2022)
                    sumInntekt shouldBe BigDecimal.valueOf(250824)
                    periode.fom shouldBe YearMonth.of(2022, 5)
                    periode.til shouldBe YearMonth.of(2023, 4)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 1
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(250824)

                    with(inntektPostListe[0]) {
                        kode shouldBe "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"
                        visningsnavn shouldBe "Overgangsstønad til enslig mor eller far som begynte å løpe 1. april 2014 eller senere"
                        beløp shouldBe BigDecimal.valueOf(250824)
                    }
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.OVERGANGSSTØNAD
                    visningsnavn shouldBe Inntektsrapportering.OVERGANGSSTØNAD.visningsnavnIntern(2023)
                    sumInntekt shouldBe BigDecimal.valueOf(249203)
                    periode.fom shouldBe YearMonth.of(2023, 5)
                    periode.til shouldBe YearMonth.of(2024, 4)
                    gjelderBarnPersonId shouldBe ""
                    inntektPostListe.size shouldBe 2
                    inntektPostListe.sumOf { it.beløp } shouldBe BigDecimal.valueOf(249202)

                    with(inntektPostListe[0]) {
                        kode shouldBe "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"
                        visningsnavn shouldBe "Overgangsstønad til enslig mor eller far som begynte å løpe 1. april 2014 eller senere"
                        beløp shouldBe BigDecimal.valueOf(195824)
                    }

                    with(inntektPostListe[1]) {
                        kode shouldBe "overgangsstoenadTilGjenlevendeEktefelle"
                        visningsnavn shouldBe "Overgangsstønad til gjenlevende ektefelle"
                        beløp shouldBe BigDecimal.valueOf(53378)
                    }
                }
            }
        }

        @Test
        @Suppress("NonAsciiCharacters")
        fun `skal summere alle ytelser fra ainntekter`() {
            val filnavnYtelserDagpengerRequest = "src/test/resources/testfiler/eksempel_request_alle_ytelser.json"
            val inntektRequest = TestUtil.byggInntektRequest(filnavnYtelserDagpengerRequest)

            val ainntektHentetDato = LocalDate.of(2023, 12, 17)

            val ytelserService = YtelserService()

            val transformerteInntekter = ytelserService.beregnYtelser(inntektRequest.ainntektsposter, ainntektHentetDato)

            TestUtil.printJson(transformerteInntekter)

            assertSoftly {
                assertNotNull(transformerteInntekter)
                assertTrue(transformerteInntekter.isNotEmpty())
                assertTrue(transformerteInntekter.size == 7)

                with(transformerteInntekter[0]) {
                    inntektRapportering shouldBe Inntektsrapportering.AAP
                    sumInntekt shouldBe BigDecimal.valueOf(1000)
                }

                with(transformerteInntekter[1]) {
                    inntektRapportering shouldBe Inntektsrapportering.DAGPENGER
                    sumInntekt shouldBe BigDecimal.valueOf(2000)
                }

                with(transformerteInntekter[2]) {
                    inntektRapportering shouldBe Inntektsrapportering.FORELDREPENGER
                    sumInntekt shouldBe BigDecimal.valueOf(1000)
                }

                with(transformerteInntekter[3]) {
                    inntektRapportering shouldBe Inntektsrapportering.INTRODUKSJONSSTØNAD
                    sumInntekt shouldBe BigDecimal.valueOf(1000)
                }

                with(transformerteInntekter[4]) {
                    inntektRapportering shouldBe Inntektsrapportering.KVALIFISERINGSSTØNAD
                    sumInntekt shouldBe BigDecimal.valueOf(1000)
                }

                with(transformerteInntekter[5]) {
                    inntektRapportering shouldBe Inntektsrapportering.PENSJON
                    sumInntekt shouldBe BigDecimal.valueOf(23000)
                }

                with(transformerteInntekter[6]) {
                    inntektRapportering shouldBe Inntektsrapportering.SYKEPENGER
                    sumInntekt shouldBe BigDecimal.valueOf(3000)
                }
            }
        }
    }
}
