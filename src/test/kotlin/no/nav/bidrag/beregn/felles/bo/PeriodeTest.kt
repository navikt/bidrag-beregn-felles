package no.nav.bidrag.beregn.felles.bo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate

@DisplayName("Periode")
internal class PeriodeTest {

    @Test
    fun testStartDatoLikSluttDatoLikSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoFoerSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoLikSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoFoerSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFoerSluttDatoFoerSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2009-12-01"), LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoEtterSkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-05-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testHelePeriodenFoerSkalIkkeOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2009-01-01"), LocalDate.parse("2009-04-01")))
        assertThat(overlappendePerioder).isFalse()
    }

    @Test
    fun testHelePeriodenEtterSkalIkkeOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2011-01-01"), LocalDate.parse("2011-04-01")))
        assertThat(overlappendePerioder).isFalse()
    }

    @Test
    fun testStartDatoLikSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-01-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2010-01-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFoerSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2009-12-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFoerSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2009-12-01"), LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFoerSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2009-12-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder = Periode(LocalDate.parse("2010-01-01"), null)
                .overlapperMed(Periode(LocalDate.parse("2010-02-01"), null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun `skal instanisere periode med justerte datoer`() {
        val periode = Periode(LocalDate.now(), LocalDate.now().plusMonths(1))
        val justertPeriode = Periode(periode)

        assertAll(
                { assertThat(justertPeriode.datoFra).`as`("datoFra").isEqualTo(LocalDate.now().plusMonths(1).withDayOfMonth(1)) },
                { assertThat(justertPeriode.datoTil).`as`("datoTil").isEqualTo(LocalDate.now().plusMonths(2).withDayOfMonth(1)) }
        )
    }
}