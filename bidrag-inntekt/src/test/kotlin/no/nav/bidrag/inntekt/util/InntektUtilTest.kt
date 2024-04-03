package no.nav.bidrag.inntekt.util

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.ints.shouldBeExactly
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InntektUtilTest {

    @Test
    fun `Skal finne riktig cut-off dag`() {
        assertSoftly {
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-01-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-02-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-03-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-04-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-05-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-06-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-07-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-08-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-09-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-10-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-11-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2021-12-01")) shouldBeExactly 6

            InntektUtil.finnCutOffDag(LocalDate.parse("2022-01-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-02-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-03-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-04-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-05-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-06-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-07-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-08-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-09-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-10-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-11-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2022-12-01")) shouldBeExactly 5

            InntektUtil.finnCutOffDag(LocalDate.parse("2023-01-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-02-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-03-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-04-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-05-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-06-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-07-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-08-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-09-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-10-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-11-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2023-12-01")) shouldBeExactly 5

            InntektUtil.finnCutOffDag(LocalDate.parse("2024-01-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-02-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-03-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-04-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-05-01")) shouldBeExactly 6
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-06-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-07-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-08-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-09-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-10-01")) shouldBeExactly 7
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-11-01")) shouldBeExactly 5
            InntektUtil.finnCutOffDag(LocalDate.parse("2024-12-01")) shouldBeExactly 5
        }
    }
}
