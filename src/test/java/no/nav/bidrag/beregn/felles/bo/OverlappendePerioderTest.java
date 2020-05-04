package no.nav.bidrag.beregn.felles.bo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OverlappendePerioderTest")
class OverlappendePerioderTest {

  @Test
  void testStartDatoLikSluttDatoLikSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoLikSluttDatoFoerSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-03-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoLikSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-04-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoFoerSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-03-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoFoerSluttDatoFoerSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2009-12-01"), LocalDate.parse("2010-03-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoEtterSkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-05-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testHelePeriodenFoerSkalIkkeOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2009-01-01"), LocalDate.parse("2009-04-01")));
    assertThat(overlappendePerioder).isFalse();
  }

  @Test
  void testHelePeriodenEtterSkalIkkeOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2011-01-01"), LocalDate.parse("2011-04-01")));
    assertThat(overlappendePerioder).isFalse();
  }

  @Test
  void testStartDatoLikSluttDatoNull1SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-01-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoLikSluttDatoNull2SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoLikSluttDatoNull3SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2010-01-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoFoerSluttDatoNull1SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2009-12-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoFoerSluttDatoNull2SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2009-12-01"), LocalDate.parse("2010-04-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoFoerSluttDatoNull3SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2009-12-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoNull1SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-04-01"))
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoNull2SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), LocalDate.parse("2010-04-01")));
    assertThat(overlappendePerioder).isTrue();
  }

  @Test
  void testStartDatoEtterSluttDatoNull3SkalOverlappe() {
    Boolean overlappendePerioder = new Periode(LocalDate.parse("2010-01-01"), null)
        .overlapperMed(new Periode(LocalDate.parse("2010-02-01"), null));
    assertThat(overlappendePerioder).isTrue();
  }
}
