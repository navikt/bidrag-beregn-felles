package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

@DisplayName("BarnetilleggPensjonServiceTest")
class BarnetilleggPensjonServiceTest : AbstractServiceTest() {
    @Test
    fun `skal returnere barnetillegg pensjon for alle perioder i input`() {
        val barnetilleggPensjonService = BarnetilleggPensjonService()

        val barnetilleggPensjon = TestUtil.byggBarnetilleggPensjon()
        val beregnetBarnetilleggPensjon = barnetilleggPensjonService.beregnBarnetilleggPensjon(barnetilleggPensjon)

        assertSoftly {
            assertNotNull(beregnetBarnetilleggPensjon)
            beregnetBarnetilleggPensjon.size shouldBe 5

            with(beregnetBarnetilleggPensjon[0]) {
                inntektRapportering shouldBe Inntektsrapportering.BARNETILLEGG
                visningsnavn shouldBe Inntektsrapportering.BARNETILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2021-11")
                periode.til shouldBe YearMonth.parse("2022-06")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetBarnetilleggPensjon[1]) {
                inntektRapportering shouldBe Inntektsrapportering.BARNETILLEGG
                visningsnavn shouldBe Inntektsrapportering.BARNETILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2022-10")
                periode.til shouldBe YearMonth.parse("2023-01")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetBarnetilleggPensjon[2]) {
                inntektRapportering shouldBe Inntektsrapportering.BARNETILLEGG
                visningsnavn shouldBe Inntektsrapportering.BARNETILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2023-05")
                periode.til shouldBe YearMonth.parse("2023-07")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetBarnetilleggPensjon[3]) {
                inntektRapportering shouldBe Inntektsrapportering.BARNETILLEGG
                visningsnavn shouldBe Inntektsrapportering.BARNETILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2022-09")
                periode.til shouldBe YearMonth.parse("2022-12")
                gjelderBarnPersonId shouldBe "98765432109"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetBarnetilleggPensjon[4]) {
                inntektRapportering shouldBe Inntektsrapportering.BARNETILLEGG
                visningsnavn shouldBe Inntektsrapportering.BARNETILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2023-05")
                periode.til.shouldBeNull()
                gjelderBarnPersonId shouldBe "98765432109"
                inntektPostListe.size shouldBe 0
            }
        }
    }
}
