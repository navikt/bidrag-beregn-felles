package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.PeriodisertGrunnlag;

public class SjablonPeriode implements PeriodisertGrunnlag {

    private final Periode datoFraTil;
    private final String sjablonnavn;
    private final Double sjablonVerdi1;
    private final Double sjablonVerdi2;

    public SjablonPeriode(Periode datoFraTil, String sjablonnavn, Double sjablonVerdi1, Double sjablonVerdi2) {
        this.datoFraTil = datoFraTil;
        this.sjablonnavn = sjablonnavn;
        this.sjablonVerdi1 = sjablonVerdi1;
        this.sjablonVerdi2 = sjablonVerdi2;
    }

    public Periode getDatoFraTil() {
        return datoFraTil;
    }

    public String getSjablonnavn() {
        return sjablonnavn;
    }

    public Double getSjablonVerdi1() {
        return sjablonVerdi1;
    }

    public Double getSjablonVerdi2() {
        return sjablonVerdi2;
    }

}
