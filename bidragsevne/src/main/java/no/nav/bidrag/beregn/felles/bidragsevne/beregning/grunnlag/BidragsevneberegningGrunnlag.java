package no.nav.bidrag.beregn.felles.bidragsevne.beregning.grunnlag;

import java.util.ArrayList;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.SjablonPeriode;

public class BidragsevneberegningGrunnlag {
    public BidragsevneberegningGrunnlag() {
    }

    private Double inntekt;
    private Integer antallEgneBarnIHusstand;
    private ArrayList<SjablonPeriode> sjabloner;
    public BidragsevneberegningGrunnlag(Double inntekt, Boolean borAlene, Integer antallEgneBarnIHusstand, ArrayList<SjablonPeriode> sjabloner) {
        this.inntekt = inntekt;
        this.antallEgneBarnIHusstand = antallEgneBarnIHusstand;
        this.sjabloner = sjabloner;
    }

    public Double getInntekt() {
        return inntekt;
    }

    public void setInntekt(Double inntekt) {
        this.inntekt = inntekt;
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
