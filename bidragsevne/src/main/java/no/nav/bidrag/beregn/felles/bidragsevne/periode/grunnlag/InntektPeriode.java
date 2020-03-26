package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

public class InntektPeriode implements PeriodisertGrunnlag {

    private final Periode datoFraTil;
    private final Integer skatteklasse;
    private final Double inntektBelop;

    public InntektPeriode(Periode datoFraTil, Integer skatteklasse, Double inntektBelop) {
        this.datoFraTil = datoFraTil;
        this.skatteklasse = skatteklasse;
        this.inntektBelop = inntektBelop;
    }

    public Periode getDatoFraTil() {
        return datoFraTil;
    }

    public Integer getSkatteklasse() { return skatteklasse; }

    public Double getInntektBelop() {
        return inntektBelop;
    }
}
