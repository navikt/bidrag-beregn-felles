package no.nav.bidrag.beregn.bidragsevne.periode

import no.nav.bidrag.beregn.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.felles.bo.Avvik

interface BidragsevnePeriode {

    fun beregnPerioder(grunnlag: BeregnBidragsevneGrunnlag): BeregnBidragsevneResultat

    fun validerInput(grunnlag: BeregnBidragsevneGrunnlag): List<Avvik>

    companion object {
        fun getInstance(): BidragsevnePeriode = BidragsevnePeriodeImpl(BidragsevneBeregning.getInstance())
    }
}
