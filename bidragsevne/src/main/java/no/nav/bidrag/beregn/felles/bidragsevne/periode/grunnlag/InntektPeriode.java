package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

public class InntektPeriode implements PeriodisertGrunnlag {

    private final Periode datoFraTil;
    private final Integer skatteklasse;
    private final Double belop;

    public InntektPeriode(Periode datoFraTil, Integer skatteklasse, Double belop) {
        this.datoFraTil = datoFraTil;
        this.skatteklasse = skatteklasse;
        this.belop = belop;
    }

    public Periode getDatoFraTil() {
        return datoFraTil;
    }

    public Integer getSkatteklasse() { return skatteklasse; }

    public Double getBelop() {
        return belop;
    }
}
