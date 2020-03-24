package no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.resultat.BidragsevneBeregningResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.Periode;

public class PeriodeResultat {

  private final Periode datoFraTil;
  private final BidragsevneBeregningResultat bidragsevneBeregningResultat;

  public PeriodeResultat(Periode datoFraTil, BidragsevneBeregningResultat bidragsevneBeregningResultat) {
    this.datoFraTil = datoFraTil;
    this.bidragsevneBeregningResultat = bidragsevneBeregningResultat;
  }

  public Periode getDatoFraTil() {
    return datoFraTil;
  }

  public BidragsevneBeregningResultat getBidragsevneBeregningResultat() {
    return bidragsevneBeregningResultat;
  }
}
