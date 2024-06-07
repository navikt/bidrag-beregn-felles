package no.nav.bidrag.beregn.bpsandelsaertilskudd

import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddResultatCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.periode.BPsAndelSaertilskuddPeriode

fun interface BPsAndelSaertilskuddCore {
    fun beregnBPsAndelSaertilskudd(grunnlag: BeregnBPsAndelSaertilskuddGrunnlagCore): BeregnBPsAndelSaertilskuddResultatCore

    companion object {
        fun getInstance(): BPsAndelSaertilskuddCore = BPsAndelSaertilskuddCoreImpl(BPsAndelSaertilskuddPeriode.getInstance())
    }
}
