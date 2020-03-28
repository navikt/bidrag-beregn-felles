package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;


import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.PeriodisertGrunnlag;

public class BostatusPeriode implements PeriodisertGrunnlag {

  private final Periode datoFraTil;
  private final Boolean borAlene ;

  public BostatusPeriode(Periode datoFraTil, Boolean borAlene) {
    this.datoFraTil = datoFraTil;
    this.borAlene = borAlene;
  }

  @Override
  public Periode getDatoFraTil() {
    return datoFraTil;
  }

  public Boolean getBorAlene() {
    return borAlene;
  }
}
