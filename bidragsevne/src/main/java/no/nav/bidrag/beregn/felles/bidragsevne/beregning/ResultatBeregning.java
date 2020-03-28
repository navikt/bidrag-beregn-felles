package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

public class ResultatBeregning {
    private Double evne;

    public ResultatBeregning(Double evne) {
        this.evne = evne;
    }

    public Double getEvne() {
        return evne;
    }

    public void setEvne(Double evne){
        this.evne = evne;
    }

    public boolean kanMergesMed(ResultatBeregning bidragsevneresultatforrige) {
        return (this.getEvne().equals(bidragsevneresultatforrige.getEvne()));
    }
}
