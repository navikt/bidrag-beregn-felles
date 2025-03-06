package no.nav.bidrag.beregn.core.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PeriodeUtilKtTest {

    @Test
    fun `skal sette periode til for opphørsdato`() {
        justerPeriodeTilOpphørsdato(YearMonth.now()) shouldBe YearMonth.now()
        justerPeriodeTilOpphørsdato(YearMonth.now().minusMonths(1)) shouldBe YearMonth.now().minusMonths(1)
        justerPeriodeTilOpphørsdato(YearMonth.now().minusYears(1)) shouldBe YearMonth.now().minusYears(1)
        justerPeriodeTilOpphørsdato(YearMonth.now().plusMonths(1)) shouldBe null
    }

    @Test
    fun `skal sette periode tom for opphørsdato`() {
        justerPeriodeTomOpphørsdato(YearMonth.now().atDay(1)) shouldBe YearMonth.now().minusMonths(1).atEndOfMonth()
        justerPeriodeTomOpphørsdato(YearMonth.now().minusMonths(1).atDay(1)) shouldBe YearMonth.now().minusMonths(2).atEndOfMonth()
        justerPeriodeTomOpphørsdato(YearMonth.now().plusMonths(1).atDay(1)) shouldBe null
    }
}
