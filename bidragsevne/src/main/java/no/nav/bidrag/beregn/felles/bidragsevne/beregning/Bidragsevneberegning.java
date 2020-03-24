package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.grunnlag.BidragsevneberegningGrunnlag;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.resultat.BidragsevneBeregningResultat;

public interface Bidragsevneberegning {
    BidragsevneBeregningResultat beregn(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);

    Double beregnMinstefradrag(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);

    Double beregnSkattetrinnBelop(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag);


}
