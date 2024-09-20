package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo.BeregnSumLøpendeBidragResultat
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.BeregnSumLøpendeBidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.periode.SumLøpendeBidragPeriode

internal class SumLøpendeBidragCore(private val sumLøpendeBidragPeriode: SumLøpendeBidragPeriode = SumLøpendeBidragPeriode()) : FellesCore() {

    fun beregnSumLøpendeBidrag(grunnlag: LøpendeBidragGrunnlagCore): BeregnSumLøpendeBidragResultatCore {
//        val beregnSumLøpendeBidragGrunnlag = mapTilBusinessObject(grunnlag)
//        val avvikListe = sumLøpendeBidragPeriode.validerInput(grunnlag)
        val beregnSumLøpendeBidragResultat =
//            if (avvikListe.isEmpty()) {
            sumLøpendeBidragPeriode.beregnPerioder(grunnlag)
//            } else {
//                BeregnSumLøpendeBidragResultat(emptyList())
//            }
        return mapFraBusinessObject(resultat = beregnSumLøpendeBidragResultat)
//        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnSumLøpendeBidragResultat)
    }

//    private fun mapTilBusinessObject(grunnlag: BeregnSumLøpendeBidragGrunnlagCore) = GrunnlagBeregning(
//        beregnDatoFra = grunnlag.beregnDatoFra,
//        beregnDatoTil = grunnlag.beregnDatoTil,
//        løpendeBidragCoreListe = grunnlag.løpendeBidragCoreListe,
//        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
//    )

    private fun mapFraBusinessObject(resultat: BeregnSumLøpendeBidragResultat) = BeregnSumLøpendeBidragResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe).toMutableList(),
//        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.map {
        ResultatPeriodeCore(
            periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            resultat = ResultatBeregningCore(it.resultat.sum),
            grunnlagsreferanseListe = mapReferanseListe(it).sorted().toMutableList(),
        )
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        referanseListe.add(resultatPeriode.grunnlag.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe
        .map { it.resultat.sjablonListe }
        .flatMap { mapSjablonListe(it) }
        .distinct()
}
