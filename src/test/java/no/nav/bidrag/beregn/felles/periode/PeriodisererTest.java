package no.nav.bidrag.beregn.felles.periode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import no.nav.bidrag.beregn.felles.bo.Periode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PeriodisererTest")
class PeriodisererTest {

  @Test
  void testPeriodiseringKunEnDato() {
    List<Periode> perioder = new Periodiserer()
        .addBruddpunkt(LocalDate.parse("2019-01-01"))
        .finnPerioder(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01"));

    assertAll(
        () -> assertThat(perioder).isNotNull(),
        () -> assertThat(perioder.size()).isEqualTo(0)
    );
  }

  @Test
  void testPeriodiseringMedToDatoer() {
    List<Periode> perioder = new Periodiserer()
        .addBruddpunkt(LocalDate.parse("2019-01-01"))
        .addBruddpunkt(LocalDate.parse("2019-03-01"))
        .finnPerioder(LocalDate.parse("2000-01-01"), LocalDate.parse("2100-01-01"));

    assertAll(
        () -> assertThat(perioder).isNotNull(),
        () -> assertThat(perioder.size()).isEqualTo(1),
        () -> assertThat(perioder.get(0).getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(perioder.get(0).getDatoTil()).isEqualTo(LocalDate.parse("2019-03-01"))
    );
  }
}
