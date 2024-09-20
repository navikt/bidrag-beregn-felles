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
        resultatPeriode = mapResultatPeriode(resultat.resultatPeriode),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriode),
//        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapResultatPeriode(resultatPeriode: ResultatPeriode) = ResultatPeriodeCore(
        periode = PeriodeCore(datoFom = resultatPeriode.periode.datoFom, datoTil = resultatPeriode.periode.datoTil),
        resultat = ResultatBeregningCore(resultatPeriode.resultat.sum),
        grunnlagsreferanseListe = mapReferanseListe(resultatPeriode).sorted().toMutableList(),
    )

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        referanseListe.add(resultatPeriode.grunnlag.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriode: ResultatPeriode) = resultatPeriode
        .resultat.sjablonListe
        .flatMap { mapSjablonListe(listOf(it)) }
        .distinct()
}
