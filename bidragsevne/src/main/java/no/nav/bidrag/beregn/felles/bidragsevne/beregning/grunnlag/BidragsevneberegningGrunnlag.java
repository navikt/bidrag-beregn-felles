package no.nav.bidrag.beregn.felles.bidragsevne.beregning.grunnlag;

import java.util.ArrayList;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.SjablonPeriode;

public class BidragsevneberegningGrunnlag {
    public BidragsevneberegningGrunnlag() {
    }

    private Double inntektBelop;
    private Integer skatteklasse;
    private Boolean borAlene ;
    private Integer antallEgneBarnIHusstand;
    private ArrayList<SjablonPeriode> sjabloner;

    public BidragsevneberegningGrunnlag(Double inntektBelop, Integer skatteklasse,
        Boolean borAlene, Integer antallEgneBarnIHusstand,
        ArrayList<SjablonPeriode> sjabloner) {
        this.inntektBelop = inntektBelop;
        this.skatteklasse = skatteklasse;
        this.borAlene = borAlene;
        this.antallEgneBarnIHusstand = antallEgneBarnIHusstand;
        this.sjabloner = sjabloner;
    }

    public Double getInntektBelop() {
        return inntektBelop;
    }

    public void setInntektBelop(Double inntektBelop) {
        this.inntektBelop = inntektBelop;
    }

    public Integer getSkatteklasse() {
        return skatteklasse;
    }

    public void setSkatteklasse(Integer skatteklasse) {
        this.skatteklasse = skatteklasse;
    }

    public Boolean getBorAlene() {
        return borAlene;
    }

    public void setBorAlene(Boolean borAlene) {
        this.borAlene = borAlene;
    }

    public Integer getAntallEgneBarnIHusstand() {
        return antallEgneBarnIHusstand;
    }

    public void setAntallEgneBarnIHusstand(Integer antallEgneBarnIHusstand) {
        this.antallEgneBarnIHusstand = antallEgneBarnIHusstand;
    }

    public ArrayList<SjablonPeriode> getSjabloner() {
        return sjabloner;
    }

    public void setSjabloner(ArrayList<SjablonPeriode> sjabloner) {
        this.sjabloner = sjabloner;
    }

    public SjablonPeriode hentSjablon(String sjablonnavn) {
     /*   System.out.println("blir kall med: " + sjablonnavn);
        for (Sjablon sjablon : sjabloner) {
            if (sjablon.getSjablonnavn().equals(sjablonnavn)) {
                return sjablon;
            }
        }
        return null;
    }*/

        SjablonPeriode sjablonPeriode = sjabloner.stream()
                .filter(sjabloner -> sjablonnavn.equals(sjabloner.getSjablonnavn()))
                .findAny()
                .orElse(null);
        return sjablonPeriode;

    }

}
