package no.nav.bidrag.beregn.bpsandelsaertilskudd.periode

import no.nav.bidrag.beregn.bpsandelsaertilskudd.beregning.BPsAndelSaertilskuddBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddGrunnlag
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.felles.bo.Avvik

interface BPsAndelSaertilskuddPeriode {

    fun beregnPerioder(grunnlag: BeregnBPsAndelSaertilskuddGrunnlag): BeregnBPsAndelSaertilskuddResultat

    fun validerInput(grunnlag: BeregnBPsAndelSaertilskuddGrunnlag): List<Avvik>

    companion object {
        fun getInstance(): BPsAndelSaertilskuddPeriode = BPsAndelSaertilskuddPeriodeImpl(BPsAndelSaertilskuddBeregning.getInstance())
    }
}
