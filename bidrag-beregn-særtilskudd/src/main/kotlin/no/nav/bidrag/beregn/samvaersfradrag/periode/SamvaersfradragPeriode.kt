package no.nav.bidrag.beregn.samvaersfradrag.periode

import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.samvaersfradrag.beregning.SamvaersfradragBeregning
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragGrunnlag
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragResultat

interface SamvaersfradragPeriode {

    fun beregnPerioder(grunnlag: BeregnSamvaersfradragGrunnlag): BeregnSamvaersfradragResultat

    fun validerInput(grunnlag: BeregnSamvaersfradragGrunnlag): List<Avvik>

    companion object {
        fun getInstance(): SamvaersfradragPeriode = SamvaersfradragPeriodeImpl(SamvaersfradragBeregning.getInstance())
    }
}
