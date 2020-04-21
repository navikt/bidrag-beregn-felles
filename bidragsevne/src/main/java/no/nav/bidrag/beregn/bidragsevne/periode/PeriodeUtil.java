package no.nav.bidrag.beregn.bidragsevne.periode;

import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;

import java.time.LocalDate;

public class PeriodeUtil {

  // Juster periode
  static Periode justerPeriode(Periode periode) {
    return new Periode(justerDato(periode.getDatoFra()), justerDato(periode.getDatoTil()));
  }

  // Juster dato til den første i neste måned (hvis ikke dato er den første i inneværende måned)
  private static LocalDate justerDato(LocalDate dato) {
    return (dato == null || dato.getDayOfMonth() == 1) ? dato : dato.with(firstDayOfNextMonth());
  }
}

