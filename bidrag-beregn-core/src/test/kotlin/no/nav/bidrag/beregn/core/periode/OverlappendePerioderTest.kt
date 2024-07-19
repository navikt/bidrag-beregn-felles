package no.nav.bidrag.beregn.core.periode

import no.nav.bidrag.beregn.core.bo.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("OverlappendePerioderTest")
internal class OverlappendePerioderTest {
    @Test
    fun testStartDatoLikSluttDatoLikSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoFørSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoLikSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoFørSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFoerSluttDatoFørSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2009-12-01"), datoTil = LocalDate.parse("2010-03-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoEtterSkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = LocalDate.parse("2010-05-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testHelePeriodenFørSkalIkkeOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2009-01-01"), datoTil = LocalDate.parse("2009-04-01")))
        assertThat(overlappendePerioder).isFalse()
    }

    @Test
    fun testHelePeriodenEtterSkalIkkeOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2011-01-01"), datoTil = LocalDate.parse("2011-04-01")))
        assertThat(overlappendePerioder).isFalse()
    }

    @Test
    fun testStartDatoLikSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoLikSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFørSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2009-12-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFørSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2009-12-01"), datoTil = LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoFørSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2009-12-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull1SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = LocalDate.parse("2010-04-01"))
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull2SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = LocalDate.parse("2010-04-01")))
        assertThat(overlappendePerioder).isTrue()
    }

    @Test
    fun testStartDatoEtterSluttDatoNull3SkalOverlappe() {
        val overlappendePerioder =
            Periode(datoFom = LocalDate.parse("2010-01-01"), datoTil = null)
                .overlapperMed(Periode(datoFom = LocalDate.parse("2010-02-01"), datoTil = null))
        assertThat(overlappendePerioder).isTrue()
    }
}
