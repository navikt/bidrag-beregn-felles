package no.nav.bidrag.beregn.core.periode

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PeriodisererTest {
    @Test
    fun testPeriodiseringKunEnDato() {
        val perioder =
            Periodiserer()
                .addBruddpunkt(LocalDate.parse("2019-01-01"))
                .finnPerioder(beregnDatoFom = LocalDate.parse("2000-01-01"), beregnDatoTil = LocalDate.parse("2100-01-01"))

        assertAll(
            { assertThat(perioder).isNotNull() },
            { assertThat(perioder.size).isEqualTo(0) },
        )
    }

    @Test
    fun testPeriodiseringMedToDatoer() {
        val perioder =
            Periodiserer()
                .addBruddpunkt(LocalDate.parse("2019-01-01"))
                .addBruddpunkt(LocalDate.parse("2019-03-01"))
                .finnPerioder(beregnDatoFom = LocalDate.parse("2000-01-01"), beregnDatoTil = LocalDate.parse("2100-01-01"))

        assertAll(
            { assertThat(perioder).isNotNull() },
            { assertThat(perioder.size).isEqualTo(1) },
            { assertThat(perioder[0].datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(perioder[0].datoTil).isEqualTo(LocalDate.parse("2019-03-01")) },
        )
    }
}
