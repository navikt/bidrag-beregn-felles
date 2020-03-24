package no.nav.bidrag.beregn.felles.bidragsevne.beregning.resultat;

public class BidragsevneBeregningResultat {
    private Double evne;

    public BidragsevneBeregningResultat(Double evne) {

        this.evne = evne;
    }

    public Double getEvne() {
        return evne;
    }

    public void setEvne(Double evne){
        this.evne = evne;
    }

    public boolean kanMergesMed(BidragsevneBeregningResultat bidragsevneresultatforrige) {
        return (this.getEvne().equals(bidragsevneresultatforrige.getEvne()));
    }
}
