package no.nav.bidrag.beregn.felles.periode

import no.nav.bidrag.beregn.felles.bo.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate

@DisplayName("Periodiserer")
internal class PeriodisererTest {
    private val periodiserer = Periodiserer()

    @Test
    fun testPeriodiseringKunEnDato() {
        val perioder = periodiserer
                .addBruddpunkt(LocalDate.parse("2019-01-01"))
                .finnPerioder(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01"))

        assertThat(perioder.size).isEqualTo(0)
    }

    @Test
    fun testPeriodiseringMedToDatoer() {
        val perioder = periodiserer
                .addBruddpunkt(LocalDate.parse("2019-01-01"))
                .addBruddpunkt(LocalDate.parse("2019-03-01"))
                .finnPerioder(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01"))

        assertAll(
                { assertThat(perioder.size).isEqualTo(1) },
                { assertThat(perioder[0].datoFra).isEqualTo(LocalDate.parse("2019-01-01")) },
                { assertThat(perioder[0].datoTil).isEqualTo(LocalDate.parse("2019-03-01")) }
        )
    }

    @Test
    fun `skal legge til brekkpunkt for periodens fradato og forvente aapen sluttdato`() {
        val firstDayThisMonth = LocalDate.now().withDayOfMonth(1)

        periodiserer.addBruddpunkter(Periode(firstDayThisMonth, null))

        assertAll(
                { assertThat(periodiserer.bruddpunkter).`as`("antall bruddpunkter").hasSize(1) },
                { assertThat(periodiserer.bruddpunkter.iterator().next()).`as`("bruddpunkt").isEqualTo(firstDayThisMonth) },
                { assertThat(periodiserer.aapenSluttdato).`as`("åpen sluttdato").isTrue() }
        )
    }

    @Test
    fun `skal legge til 2 brekkpunkt for perioden`() {
        val firstDayThisMonth = LocalDate.now().withDayOfMonth(1)
        val firstDayNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        periodiserer.addBruddpunkter(Periode(firstDayThisMonth, firstDayNextMonth))

        assertAll(
                { assertThat(periodiserer.bruddpunkter).`as`("antall bruddpunkter").hasSize(2) },
                { assertThat(periodiserer.aapenSluttdato).`as`("åpen sluttdato").isFalse() }
        )
    }

    @Test
    fun `skal legge til 2 brekkpunkt for 2 perioder`() {
        val firstDayThisMonth = LocalDate.now().withDayOfMonth(1)
        val firstDayNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        periodiserer.addBruddpunkter(listOf(Periode(firstDayThisMonth, firstDayNextMonth), Periode(firstDayNextMonth, null)))

        assertAll(
                { assertThat(periodiserer.bruddpunkter).`as`("antall bruddpunkter").hasSize(2) },
                { assertThat(periodiserer.aapenSluttdato).`as`("åpen sluttdato").isTrue() }
        )
    }

    @Test
    fun `skal legge til 2 brudd, for foedselsdatoen ved 11 og 18 aar`() {
        val nittenAar = LocalDate.now().minusYears(19)
        val fra10aarSiden = LocalDate.now().minusYears(10)
        val forsteDenneManeden = LocalDate.now().withDayOfMonth(1)

        periodiserer.addBruddpunkter(nittenAar, fra10aarSiden, forsteDenneManeden)

        assertAll(
                { assertThat(periodiserer.aapenSluttdato).`as`("åpen sluttdato").isFalse() },
                {
                    assertThat(periodiserer.bruddpunkter).`as`("brekkpunkter").isEqualTo(setOf(
                            LocalDate.now().minusYears(8).withDayOfMonth(1),
                            LocalDate.now().minusYears(1).plusMonths(1).withDayOfMonth(1)
                    ))
                }
        )
    }

    @Test
    fun `skal lage en perioder fra 2 brudd, for foedselsdatoen ved 11 og 18 aar`() {
        val nittenAar = LocalDate.now().minusYears(19)
        val fra10aarSiden = LocalDate.now().minusYears(10)
        val forsteDenneManeden = LocalDate.now().withDayOfMonth(1)
        val elleveAar = LocalDate.now().minusYears(8).withDayOfMonth(1)
        val attenAar = LocalDate.now().minusYears(1).plusMonths(1).withDayOfMonth(1)

        periodiserer.addBruddpunkter(nittenAar, fra10aarSiden, forsteDenneManeden)
        val perioder = periodiserer.finnPerioder(fra10aarSiden, LocalDate.now().withDayOfMonth(1))

        assertThat(perioder).`as`("perioden fra 11 til 18 år").isEqualTo(listOf(Periode(elleveAar, attenAar)))
    }
}
