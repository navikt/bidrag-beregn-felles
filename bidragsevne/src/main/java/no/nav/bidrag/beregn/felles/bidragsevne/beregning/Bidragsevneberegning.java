package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;

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
