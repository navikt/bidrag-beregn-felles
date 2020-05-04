package no.nav.bidrag.beregn.felles.bidragsevne;


import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneResultatCore;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;

public interface BidragsevneCore {

  BeregnBidragsevneResultatCore beregnBidragsevne(
      BeregnBidragsevneGrunnlagAltCore beregnBidragsevneGrunnlagAltCore);

  static BidragsevneCore getInstance() { return new BidragsevneCoreImpl(BidragsevnePeriode.getInstance());
  }
}

