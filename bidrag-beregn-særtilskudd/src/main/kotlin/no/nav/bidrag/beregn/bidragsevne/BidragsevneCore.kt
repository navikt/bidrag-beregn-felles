package no.nav.bidrag.beregn.bidragsevne

import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.bidragsevne.periode.BidragsevnePeriode

fun interface BidragsevneCore {
    fun beregnBidragsevne(grunnlag: BeregnBidragsevneGrunnlagCore): BeregnBidragsevneResultatCore

    companion object {
        fun getInstance(): BidragsevneCore = BidragsevneCoreImpl(BidragsevnePeriode.getInstance())
    }
}
