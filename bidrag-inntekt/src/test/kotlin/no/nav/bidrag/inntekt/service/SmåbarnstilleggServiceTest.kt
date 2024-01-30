package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class SmåbarnstilleggServiceTest : AbstractServiceTest() {

    @Test
    fun `skal returnere summert årsbeløp for småbarnstillegg`() {
        val småbarnstilleggService = SmåbarnstilleggService()

        val småbarnstillegg = TestUtil.byggSmåbarnstillegg()
        val beregnetSmåbarnstillegg = småbarnstilleggService.beregnSmåbarnstillegg(småbarnstillegg)

        assertSoftly {
            assertNotNull(beregnetSmåbarnstillegg)
            beregnetSmåbarnstillegg.size shouldBe 3

            with(beregnetSmåbarnstillegg[0]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2021-11")
                periode.til shouldBe YearMonth.parse("2022-03")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetSmåbarnstillegg[1]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2022-06")
                periode.til shouldBe YearMonth.parse("2022-07")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetSmåbarnstillegg[2]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2022-10")
                periode.til.shouldBeNull()
                inntektPostListe.size shouldBe 0
            }
        }
    }
}
