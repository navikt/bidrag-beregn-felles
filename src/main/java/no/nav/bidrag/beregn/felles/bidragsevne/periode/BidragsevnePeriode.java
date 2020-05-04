package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.Bidragsevneberegning;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Avvik;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;

public interface BidragsevnePeriode {
    BeregnBidragsevneResultat beregnPerioder(
        BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt);
    List<Avvik> validerInput(BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt);
    static BidragsevnePeriode getInstance() {
        return new BidragsevnePeriodeImpl(Bidragsevneberegning.getInstance());
    }
}
