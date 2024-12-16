package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.bo.BarnBM
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

class BeregnNettoTilsynsutgiftServiceTest {
    fun barnUnderTolvÅr(barnBMListe: List<BarnBM>, fom: YearMonth): List<BarnBM> = barnBMListe.filter {
        it.fødselsdato.let { fødselsdato ->
            Period.between(
                fødselsdato,
                LocalDate.of(fom.year, 7, 1),
            ).years < 12
        }
    }

    @Test
    fun `test under 12 år`() {
        val barnList = listOf(
            BarnBM("", LocalDate.now().minusYears(12).withMonth(6).withDayOfMonth(12)),
            BarnBM("", LocalDate.now().minusYears(12).withMonth(8).withDayOfMonth(12)),
        )

        barnUnderTolvÅr(barnList, YearMonth.now()).let {
            assertEquals(2, it.size)
        }
    }
}
