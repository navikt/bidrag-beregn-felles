package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.periode

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesPeriode
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.beregning.SumLøpendeBidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo.BeregnSumLøpendeBidragResultat
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore

class SumLøpendeBidragPeriode(private val sumLøpendeBidragBeregning: SumLøpendeBidragBeregning = SumLøpendeBidragBeregning()) : FellesPeriode() {

    fun beregnPerioder(grunnlag: LøpendeBidragGrunnlagCore): BeregnSumLøpendeBidragResultat {
        // Kaller beregningsmodulen for å beregne resultat
        val resultat =
            ResultatPeriode(
                periode = Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil),
                resultat = sumLøpendeBidragBeregning.beregn(grunnlag),
                grunnlag = grunnlag,
            )

        return BeregnSumLøpendeBidragResultat(resultat)
    }
}
