package no.nav.bidrag.beregn.bidragsevne;


import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore;

public interface BidragsevneCore {

  BeregnBidragsevneResultatCore beregnBidragsevne(BeregnBidragsevneGrunnlagAltCore grunnlag);

  static BidragsevneCore getInstance() {
    return new BidragsevneCoreImpl();
  }
}

