package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

public class AntallBarnIEgetHusholdPeriode implements PeriodisertGrunnlag {
  private final Periode datoFraTil;
  private final Integer antallBarn;

  public AntallBarnIEgetHusholdPeriode(
      Periode datoFraTil, Integer antallBarn) {
    this.datoFraTil = datoFraTil;
    this.antallBarn = antallBarn;
  }

  @Override
  public Periode getDatoFraTil() {
    return datoFraTil;
  }

  public Integer getAntallBarn() {
    return antallBarn;
  }
}
