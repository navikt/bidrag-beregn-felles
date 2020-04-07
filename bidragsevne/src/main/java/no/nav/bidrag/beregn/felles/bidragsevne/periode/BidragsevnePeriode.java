package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;

public interface BidragsevnePeriode {
    BeregnBidragsevneResultat beregnPerioder(
        BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt);

    static BidragsevnePeriode getInstance() {
        return new BidragsevnePeriodeImpl();
    }
}
