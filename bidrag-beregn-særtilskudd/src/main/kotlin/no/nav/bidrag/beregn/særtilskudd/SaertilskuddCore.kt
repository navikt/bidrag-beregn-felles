package no.nav.bidrag.beregn.særtilskudd

import no.nav.bidrag.beregn.særtilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.dto.BeregnSaertilskuddResultatCore
import no.nav.bidrag.beregn.særtilskudd.periode.SaertilskuddPeriode

fun interface SaertilskuddCore {
    fun beregnSaertilskudd(grunnlag: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore

    companion object {
        fun getInstance(): SaertilskuddCore = SaertilskuddCoreImpl(SaertilskuddPeriode.getInstance())
    }
}
