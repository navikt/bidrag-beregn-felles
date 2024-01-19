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

class UtvidetBarnetrygdOgSmåbarnstilleggServiceTest : AbstractServiceTest() {
    @Test
    fun `skal returnere summert årsbeløp for hhv utvidet barnetrygd og småbarnstillegg`() {
        val utvidetBarnetrygdOgSmåbarnstilleggService = UtvidetBarnetrygdOgSmåbarnstilleggService()

        val ubst = TestUtil.byggUtvidetBarnetrygdOgSmåbarnstillegg()
        val beregnetUtvidetBarnetrygdOgSmåbarnstillegg =
            utvidetBarnetrygdOgSmåbarnstilleggService.beregnUtvidetBarnetrygdOgSmåbarnstillegg(ubst)

        assertSoftly {
            assertNotNull(beregnetUtvidetBarnetrygdOgSmåbarnstillegg)
            beregnetUtvidetBarnetrygdOgSmåbarnstillegg.size shouldBe 5

            with(beregnetUtvidetBarnetrygdOgSmåbarnstillegg[0]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2021-11")
                periode.til shouldBe YearMonth.parse("2022-03")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetUtvidetBarnetrygdOgSmåbarnstillegg[1]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2022-06")
                periode.til shouldBe YearMonth.parse("2022-07")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetUtvidetBarnetrygdOgSmåbarnstillegg[2]) {
                inntektRapportering shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
                visningsnavn shouldBe Inntektsrapportering.SMÅBARNSTILLEGG.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(7920)
                periode.fom shouldBe YearMonth.parse("2022-10")
                periode.til.shouldBeNull()
                inntektPostListe.size shouldBe 0
            }

            with(beregnetUtvidetBarnetrygdOgSmåbarnstillegg[3]) {
                inntektRapportering shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD
                visningsnavn shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(12648)
                periode.fom shouldBe YearMonth.parse("2019-01")
                periode.til shouldBe YearMonth.parse("2019-09")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetUtvidetBarnetrygdOgSmåbarnstillegg[4]) {
                inntektRapportering shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD
                visningsnavn shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD.visningsnavn.intern
                sumInntekt shouldBe BigDecimal.valueOf(12648)
                periode.fom shouldBe YearMonth.parse("2020-11")
                periode.til shouldBe YearMonth.parse("2022-09")
                inntektPostListe.size shouldBe 0
            }
        }
    }
}
