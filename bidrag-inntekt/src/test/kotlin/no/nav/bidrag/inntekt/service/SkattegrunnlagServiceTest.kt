package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

@DisplayName("SkattegrunnlagServiceTest")
class SkattegrunnlagServiceTest : AbstractServiceTest() {
    private val skattegrunnlagService: SkattegrunnlagService = SkattegrunnlagService()

    @Test
    fun `skal returnere Kapsinntekter`() {
        val skattegrunnlagDto = TestUtil.byggSkattegrunnlagDto()
        val beregnedeKapsinntekter =
            skattegrunnlagService.beregnSkattegrunnlag(skattegrunnlagDto, Inntektsrapportering.KAPITALINNTEKT)

        assertSoftly {
            assertNotNull(beregnedeKapsinntekter)
            beregnedeKapsinntekter.size shouldBe 2

            with(beregnedeKapsinntekter[0]) {
                periode.fom shouldBe YearMonth.parse("2021-01")
                periode.til shouldBe YearMonth.parse("2021-12")
                inntektRapportering shouldBe Inntektsrapportering.KAPITALINNTEKT
                sumInntekt shouldBe BigDecimal.valueOf(1700)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4

                with(inntektPostListe[0]) {
                    kode shouldBe "andelIFellesTapVedSalgAvAndelISDF"
                    visningsnavn shouldBe "Andel i felles tap ved salg av andel i SDF"
                    beløp shouldBe BigDecimal.valueOf(1000)
                }

                with(inntektPostListe[1]) {
                    kode shouldBe "andreFradragsberettigedeKostnader"
                    visningsnavn shouldBe "Andre fradragsberettigede kostnader"
                    beløp shouldBe BigDecimal.valueOf(500)
                }

                with(inntektPostListe[2]) {
                    kode shouldBe "annenSkattepliktigKapitalinntektFraAnnetFinansprodukt"
                    visningsnavn shouldBe "Annen skattepliktig kapitalinntekt fra annet finansprodukt"
                    beløp shouldBe BigDecimal.valueOf(1500)
                }

                with(inntektPostListe[3]) {
                    kode shouldBe "samledeOpptjenteRenterIUtenlandskeBanker"
                    visningsnavn shouldBe "Samlede opptjente renter i utenlandske banker"
                    beløp shouldBe BigDecimal.valueOf(1700)
                }
            }

            with(beregnedeKapsinntekter[1]) {
                periode.fom shouldBe YearMonth.parse("2022-01")
                periode.til shouldBe YearMonth.parse("2022-12")
                inntektRapportering shouldBe Inntektsrapportering.KAPITALINNTEKT
                sumInntekt shouldBe BigDecimal.valueOf(1700)
                gjelderBarnPersonId shouldBe ""
                inntektPostListe.size shouldBe 4

                with(inntektPostListe[0]) {
                    kode shouldBe "andelIFellesTapVedSalgAvAndelISDF"
                    visningsnavn shouldBe "Andel i felles tap ved salg av andel i SDF"
                    beløp shouldBe BigDecimal.valueOf(1000)
                }

                with(inntektPostListe[1]) {
                    kode shouldBe "andreFradragsberettigedeKostnader"
                    visningsnavn shouldBe "Andre fradragsberettigede kostnader"
                    beløp shouldBe BigDecimal.valueOf(500)
                }

                with(inntektPostListe[2]) {
                    kode shouldBe "annenSkattepliktigKapitalinntektFraAnnetFinansprodukt"
                    visningsnavn shouldBe "Annen skattepliktig kapitalinntekt fra annet finansprodukt"
                    beløp shouldBe BigDecimal.valueOf(1500)
                }

                with(inntektPostListe[3]) {
                    kode shouldBe "samledeOpptjenteRenterIUtenlandskeBanker"
                    visningsnavn shouldBe "Samlede opptjente renter i utenlandske banker"
                    beløp shouldBe BigDecimal.valueOf(1700)
                }
            }
        }
    }

    @Test
    fun `skal returnere Ligsinntekter`() {
        val skattegrunnlagDto = TestUtil.byggSkattegrunnlagDto()
        val beregnedeLigsinntekter =
            skattegrunnlagService.beregnSkattegrunnlag(skattegrunnlagDto, Inntektsrapportering.LIGNINGSINNTEKT)

        assertSoftly {
            with(beregnedeLigsinntekter[0]) {
                periode.fom shouldBe YearMonth.parse("2021-01")
                inntektRapportering shouldBe Inntektsrapportering.LIGNINGSINNTEKT
                sumInntekt shouldBe BigDecimal.valueOf(1000)
                inntektPostListe.size shouldBe 4

                with(inntektPostListe[0]) {
                    kode shouldBe "alderspensjonFraIPAOgIPS"
                    visningsnavn shouldBe "Alderspensjon fra IPA og IPS"
                    beløp shouldBe BigDecimal.valueOf(100)
                }

                with(inntektPostListe[1]) {
                    kode shouldBe "annenArbeidsinntekt"
                    visningsnavn shouldBe "Annen arbeidsinntekt"
                    beløp shouldBe BigDecimal.valueOf(200)
                }

                with(inntektPostListe[2]) {
                    kode shouldBe "annenPensjonFoerAlderspensjon"
                    visningsnavn shouldBe "Annen pensjon før alderspensjon"
                    beløp shouldBe BigDecimal.valueOf(300)
                }

                with(inntektPostListe[3]) {
                    kode shouldBe "arbeidsavklaringspenger"
                    visningsnavn shouldBe "Arbeidsavklaringspenger"
                    beløp shouldBe BigDecimal.valueOf(400)
                }
            }
        }
    }
}
