package no.nav.bidrag.beregn.bidragsevne.beregning;

import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.bidragsevne.bo.ResultatBeregning;

public interface Bidragsevneberegning {
    ResultatBeregning beregn(
        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert);

    Double beregnMinstefradrag(
        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert);

    Double beregnSkattetrinnBelop(
        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert);

    static Bidragsevneberegning getInstance(){
        return new BidragsevneberegningImpl();
    }


}
