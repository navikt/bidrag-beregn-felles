package no.nav.bidrag.beregn.bidragsevne.periode;

import java.util.List;
import no.nav.bidrag.beregn.bidragsevne.bo.Avvik;
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneResultat;

public interface BidragsevnePeriode {
    BeregnBidragsevneResultat beregnPerioder(
        BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt);
    List<Avvik> validerInput(BeregnBidragsevneGrunnlagAlt grunnlag);
    static BidragsevnePeriode getInstance() {
        return new BidragsevnePeriodeImpl();
    }
}
