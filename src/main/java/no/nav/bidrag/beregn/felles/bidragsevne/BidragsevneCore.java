package no.nav.bidrag.beregn.felles.bidragsevne;


import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneResultatCore;

public interface BidragsevneCore {

  BeregnBidragsevneResultatCore beregnBidragsevne(BeregnBidragsevneGrunnlagAltCore grunnlag);

  static BidragsevneCore getInstance() {
    return new BidragsevneCoreImpl();
  }
}

