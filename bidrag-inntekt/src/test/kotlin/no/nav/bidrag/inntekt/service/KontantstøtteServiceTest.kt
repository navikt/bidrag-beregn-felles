package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

@DisplayName("KontantstøtteServiceTest")
class KontantstøtteServiceTest : AbstractServiceTest() {
    @Test
    fun `skal returnere kontantstøtte for alle perioder i input`() {
        val kontantstøtteService = KontantstøtteService()

        val kontantstøtte = TestUtil.byggKontantstøtte()
        val beregnetKontantstøtte = kontantstøtteService.beregnKontantstøtte(kontantstøtte)

        assertSoftly {
            assertNotNull(beregnetKontantstøtte)
            beregnetKontantstøtte.size shouldBe 5

            with(beregnetKontantstøtte[0]) {
                inntektRapportering shouldBe Inntektsrapportering.KONTANTSTØTTE
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2021-11")
                periode.til shouldBe YearMonth.parse("2022-06")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetKontantstøtte[1]) {
                inntektRapportering shouldBe Inntektsrapportering.KONTANTSTØTTE
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2022-10")
                periode.til shouldBe YearMonth.parse("2023-01")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetKontantstøtte[2]) {
                inntektRapportering shouldBe Inntektsrapportering.KONTANTSTØTTE
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2023-05")
                periode.til shouldBe YearMonth.parse("2023-07")
                gjelderBarnPersonId shouldBe "12345678901"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetKontantstøtte[3]) {
                inntektRapportering shouldBe Inntektsrapportering.KONTANTSTØTTE
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2022-09")
                periode.til shouldBe YearMonth.parse("2022-12")
                gjelderBarnPersonId shouldBe "98765432109"
                inntektPostListe.size shouldBe 0
            }

            with(beregnetKontantstøtte[4]) {
                inntektRapportering shouldBe Inntektsrapportering.KONTANTSTØTTE
                sumInntekt shouldBe BigDecimal.valueOf(90000)
                periode.fom shouldBe YearMonth.parse("2023-05")
                periode.til.shouldBeNull()
                gjelderBarnPersonId shouldBe "98765432109"
                inntektPostListe.size shouldBe 0
            }
        }
    }
}
