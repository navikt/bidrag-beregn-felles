package no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode;

public class PeriodeResultat {

  private final Periode datoFraTil;
  private final ResultatBeregning resultatBeregning;

  public PeriodeResultat(Periode datoFraTil, ResultatBeregning resultatBeregning) {
    this.datoFraTil = datoFraTil;
    this.resultatBeregning = resultatBeregning;
  }

  public Periode getDatoFraTil() {
    return datoFraTil;
  }

  public ResultatBeregning getResultatBeregning() {
    return resultatBeregning;
  }
}
