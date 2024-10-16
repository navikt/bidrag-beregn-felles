package no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.periode

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.beregning.BPsBeregnedeTotalbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo.BeregnBPsBeregnedeTotalbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesPeriode

class BPsBeregnedeTotalbidragPeriode(
    private val bPsBeregnedeTotalbidragBeregning: BPsBeregnedeTotalbidragBeregning = BPsBeregnedeTotalbidragBeregning(),
) : FellesPeriode() {

    fun beregnPerioder(grunnlag: LøpendeBidragGrunnlagCore): BeregnBPsBeregnedeTotalbidragResultat {
        // Kaller beregningsmodulen for å beregne resultat
        val resultat =
            ResultatPeriode(
                periode = Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil),
                resultat = bPsBeregnedeTotalbidragBeregning.beregn(grunnlag),
                grunnlag = grunnlag,
            )

        return BeregnBPsBeregnedeTotalbidragResultat(resultat)
    }
}
