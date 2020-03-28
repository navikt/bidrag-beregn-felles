package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.grunnlag.BidragsevneberegningGrunnlag;

public interface Bidragsevneberegning {
    ResultatBeregning beregn(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);

    Double beregnMinstefradrag(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);

    Double beregnSkattetrinnBelop(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);

    static Bidragsevneberegning getInstance(){
        return new BidragsevneberegningImpl();
    }


}
