package no.nav.bidrag.inntekt.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.TestUtil
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class UtvidetBarnetrygdServiceTest : AbstractServiceTest() {
    @Test
    fun `skal returnere summert årsbeløp for utvidet barnetrygd`() {
        val utvidetBarnetrygdService = UtvidetBarnetrygdService()

        val utvidetBarnetrygd = TestUtil.byggUtvidetBarnetrygd()
        val beregnetUtvidetBarnetrygd = utvidetBarnetrygdService.beregnUtvidetBarnetrygd(utvidetBarnetrygd)

        assertSoftly {
            assertNotNull(beregnetUtvidetBarnetrygd)
            beregnetUtvidetBarnetrygd.size shouldBe 2

            with(beregnetUtvidetBarnetrygd[0]) {
                inntektRapportering shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD
                sumInntekt shouldBe BigDecimal.valueOf(12648)
                periode.fom shouldBe YearMonth.parse("2019-01")
                periode.til shouldBe YearMonth.parse("2019-09")
                inntektPostListe.size shouldBe 0
            }

            with(beregnetUtvidetBarnetrygd[1]) {
                inntektRapportering shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD
                sumInntekt shouldBe BigDecimal.valueOf(12648)
                periode.fom shouldBe YearMonth.parse("2020-11")
                periode.til shouldBe YearMonth.parse("2022-09")
                inntektPostListe.size shouldBe 0
            }
        }
    }
}
