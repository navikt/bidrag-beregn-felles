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
//        val beregningGrunnlag =
//            GrunnlagBeregning(
//                beregnDatoFra = grunnlag.beregnDatoFra,
//                beregnDatoTil = grunnlag.beregnDatoTil,
//                løpendeBidragCoreListe = grunnlag.løpendeBidragCoreListe,
//                sjablonPeriodeListe = sjablonliste,
//            )

        val resultatListe = listOf(
            ResultatPeriode(
                periode = Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil),
                resultat = sumLøpendeBidragBeregning.beregn(grunnlag),
                grunnlag = grunnlag,
            ),
        )

        return BeregnSumLøpendeBidragResultat(resultatListe)
    }

    // Validerer at input-verdier til beregning av sum løpende bidrag er gyldige
    // Setter alle valideringer til false ettersom det bare er en periode
//    fun validerInput(grunnlag: LøpendeBidragGrunnlagCore): List<Avvik> {
//        // Sjekk beregn dato fra/til
//        val avvikListe =
//            PeriodeUtil.validerBeregnPeriodeInput(
//                beregnDatoFom = grunnlag.beregnDatoFra,
//                beregnDatoTil = grunnlag.beregnDatoTil,
//            ).toMutableList()
//
//        return avvikListe
//    }
}
