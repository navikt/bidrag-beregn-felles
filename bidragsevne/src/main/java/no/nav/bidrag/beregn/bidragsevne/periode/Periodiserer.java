package no.nav.bidrag.beregn.bidragsevne.periode;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Periodiserer {

  private final Set<LocalDate> bruddpunkter = new HashSet<>();
  private boolean aapenSluttdato = false;

  public Periodiserer addBruddpunkt(LocalDate dato) {
    bruddpunkter.add(dato);
    return this;
  }

  private Periodiserer addBruddpunkter(Periode periode) {
    addBruddpunkt(periode.getDatoFra());
    if (periode.getDatoTil() == null) {
      aapenSluttdato = true;
    } else {
      addBruddpunkt(periode.getDatoTil());
    }
    return this;
  }

  public Periodiserer addBruddpunkter(PeriodisertGrunnlag grunnlag) {
    addBruddpunkter(grunnlag.getDatoFraTil());
    return this;
  }

  Periodiserer addBruddpunkter(Iterable<? extends PeriodisertGrunnlag> grunnlagListe) {
    for (PeriodisertGrunnlag grunnlag : grunnlagListe) {
      addBruddpunkter(grunnlag);
    }
    return this;
  }

  public List<Periode> finnPerioder(LocalDate beregnDatoFra, LocalDate beregnDatoTil) {
    var sortertBruddpunktListe = bruddpunkter.stream().filter((dato) -> dato.isAfter(beregnDatoFra.minusDays(1)))
        .filter((dato) -> dato.isBefore(beregnDatoTil.plusDays(1))).sorted().collect(toList());

    List<Periode> perioder = new ArrayList<>();
    Iterator<LocalDate> bruddpunktIt = sortertBruddpunktListe.iterator();
    if (bruddpunktIt.hasNext()) {
      LocalDate start = bruddpunktIt.next();
      while (bruddpunktIt.hasNext()) {
        LocalDate end = bruddpunktIt.next();
        perioder.add(new Periode(start, end));
        start = end;
      }
      if (aapenSluttdato) {
        perioder.add(new Periode(start, null));
      }
    }
    return perioder;
  }
}
