package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.BidragsevnePeriodeGrunnlag;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.BidragsevnePeriodeResultat;

public interface BidragsevnePeriode {
    BidragsevnePeriodeResultat beregnPerioder(BidragsevnePeriodeGrunnlag grunnlag);

    static BidragsevnePeriode getInstance() {
        return new BidragsevnePeriodeImpl();
    }
}
