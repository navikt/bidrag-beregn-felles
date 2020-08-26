package no.nav.bidrag.beregn.felles;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.enums.AvvikType;

public class PeriodeUtil {

  // Validerer at datoer er gyldige
  public static List<Avvik> validerInputDatoer(LocalDate beregnDatoFra, LocalDate beregnDatoTil, String dataElement, List<Periode> periodeListe,
      boolean sjekkOverlapp, boolean sjekkOpphold, boolean sjekkNull, boolean sjekkBeregnPeriode) {
    var indeks = 0;
    Periode forrigePeriode = null;

    // Sjekk beregn dato fra/til
    var avvikListe = new ArrayList<>(PeriodeUtil.validerBeregnPeriodeInput(beregnDatoFra, beregnDatoTil));

    // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
    if (sjekkBeregnPeriode) {
      avvikListe.addAll(sjekkBeregnPeriode(beregnDatoFra, beregnDatoTil, dataElement, periodeListe));
    }

    for (Periode dennePeriode : periodeListe) {
      indeks++;

      //Sjekk om perioder overlapper
      if (sjekkOverlapp) {
        if (dennePeriode.overlapper(forrigePeriode)) {
          var feilmelding = "Overlappende perioder i " + dataElement + ": periodeDatoTil=" + forrigePeriode.getDatoTil() + ", periodeDatoFra=" +
              dennePeriode.getDatoFra();
          avvikListe.add(new Avvik(feilmelding, AvvikType.PERIODER_OVERLAPPER));
        }
      }

      //Sjekk om det er opphold mellom perioder
      if (sjekkOpphold) {
        if (dennePeriode.harOpphold(forrigePeriode)) {
          var feilmelding = "Opphold mellom perioder i " + dataElement + ": periodeDatoTil=" + forrigePeriode.getDatoTil() + ", periodeDatoFra=" +
              dennePeriode.getDatoFra();
          avvikListe.add(new Avvik(feilmelding, AvvikType.PERIODER_HAR_OPPHOLD));
        }
      }

      //Sjekk om dato er null
      if (sjekkNull) {
        if ((indeks != periodeListe.size()) && (dennePeriode.getDatoTil() == null)) {
          var feilmelding = "periodeDatoTil kan ikke være null i " + dataElement + ": periodeDatoFra=" + dennePeriode.getDatoFra() +
              ", periodeDatoTil=" + dennePeriode.getDatoTil();
          avvikListe.add(new Avvik(feilmelding, AvvikType.NULL_VERDI_I_DATO));
        }
        if ((indeks != 1) && (dennePeriode.getDatoFra() == null)) {
          var feilmelding = "periodeDatoFra kan ikke være null i " + dataElement + ": periodeDatoFra=" + dennePeriode.getDatoFra() +
              ", periodeDatoTil=" + dennePeriode.getDatoTil();
          avvikListe.add(new Avvik(feilmelding, AvvikType.NULL_VERDI_I_DATO));
        }
      }

      //Sjekk om dato fra er etter dato til
      if (!(dennePeriode.datoTilErEtterDatoFra())) {
        var feilmelding = "periodeDatoTil må være etter periodeDatoFra i " + dataElement + ": periodeDatoFra=" + dennePeriode.getDatoFra() +
            ", periodeDatoTil=" + dennePeriode.getDatoTil();
        avvikListe.add(new Avvik(feilmelding, AvvikType.DATO_FRA_ETTER_DATO_TIL));
      }

      forrigePeriode = new Periode(dennePeriode.getDatoFra(), dennePeriode.getDatoTil());
    }

    return avvikListe;
  }

  // Validerer at beregningsperiode fra/til er gyldig
  private static List<Avvik> validerBeregnPeriodeInput(LocalDate beregnDatoFra, LocalDate beregnDatoTil) {
    var avvikListe = new ArrayList<Avvik>();

    if (beregnDatoFra == null) {
      avvikListe.add(new Avvik("beregnDatoFra kan ikke være null", AvvikType.NULL_VERDI_I_DATO));
    }
    if (beregnDatoTil == null) {
      avvikListe.add(new Avvik("beregnDatoTil kan ikke være null", AvvikType.NULL_VERDI_I_DATO));
    }
    if (!new Periode(beregnDatoFra, beregnDatoTil).datoTilErEtterDatoFra()) {
      avvikListe.add(new Avvik("beregnDatoTil må være etter beregnDatoFra", AvvikType.DATO_FRA_ETTER_DATO_TIL));
    }

    return avvikListe;
  }

  // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
  private static List<Avvik> sjekkBeregnPeriode(LocalDate beregnDatoFra, LocalDate beregnDatoTil, String dataElement, List<Periode> periodeListe) {
    var avvikListe = new ArrayList<Avvik>();

    //Sjekk at første dato i periodelisten ikke er etter start-dato i perioden det skal beregnes for
    var startDatoIPeriodeListe = periodeListe.stream().findFirst().get().getDatoFra();

    if (startDatoIPeriodeListe.isAfter(beregnDatoFra)) {
      var feilmelding = "Første dato i " + dataElement + " (" + startDatoIPeriodeListe + ") " + "er etter beregnDatoFra (" + beregnDatoFra + ")";
      avvikListe.add(new Avvik(feilmelding, AvvikType.PERIODE_MANGLER_DATA));
    }

    //Sjekk at siste dato i periodelisten ikke er før slutt-dato i perioden det skal beregnes for
    var sluttDatoPeriodeListe = periodeListe.stream().map(Periode::getDatoTil).sorted(nullsLast(naturalOrder())).collect(toList());
    var sluttDatoIPeriodeListe = sluttDatoPeriodeListe.get(sluttDatoPeriodeListe.size() - 1);

    if ((sluttDatoIPeriodeListe != null) && (sluttDatoIPeriodeListe.isBefore(beregnDatoTil))) {
      var feilmelding = "Siste dato i " + dataElement + " (" + sluttDatoIPeriodeListe + ") " + "er før beregnDatoTil (" + beregnDatoTil + ")";
      avvikListe.add(new Avvik(feilmelding, AvvikType.PERIODE_MANGLER_DATA));
    }

    return avvikListe;
  }
}
