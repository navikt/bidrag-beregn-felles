package no.nav.bidrag.beregn.bpsandelsaertilskudd.beregning

import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.ResultatBeregning

fun interface BPsAndelSaertilskuddBeregning {
    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning

    companion object {
        fun getInstance(): BPsAndelSaertilskuddBeregning = BPsAndelSaertilskuddBeregningImpl()
    }
}
