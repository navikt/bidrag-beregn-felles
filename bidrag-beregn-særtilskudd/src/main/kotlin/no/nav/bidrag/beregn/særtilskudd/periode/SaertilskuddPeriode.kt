package no.nav.bidrag.beregn.særtilskudd.periode

import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.særtilskudd.beregning.SaertilskuddBeregning
import no.nav.bidrag.beregn.særtilskudd.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særtilskudd.bo.BeregnSaertilskuddResultat

interface SaertilskuddPeriode {

    fun beregnPerioder(grunnlag: BeregnSaertilskuddGrunnlag): BeregnSaertilskuddResultat

    fun validerInput(grunnlag: BeregnSaertilskuddGrunnlag): List<Avvik>

    companion object {
        fun getInstance(): SaertilskuddPeriode = SaertilskuddPeriodeImpl(SaertilskuddBeregning.getInstance())
    }
}
