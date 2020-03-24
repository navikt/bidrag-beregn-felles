package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

public class SjablonPeriodeVerdi implements PeriodisertGrunnlag {

  private final Periode datoFraTil;
  private final Double sjablonVerdi1;
  private final Double sjablonVerdi2;

  public SjablonPeriodeVerdi(Periode datoFraTil, Double sjablonVerdi1, Double sjablonVerdi2) {
    this.datoFraTil = datoFraTil;
    this.sjablonVerdi1 = sjablonVerdi1;
    this.sjablonVerdi2 = sjablonVerdi2;
  }

  public Periode getDatoFraTil() {
    return datoFraTil;
  }

  public Double getSjablonVerdi1() { return sjablonVerdi1; }

  public Double getSjablonVerdi2() { return sjablonVerdi2; }
}