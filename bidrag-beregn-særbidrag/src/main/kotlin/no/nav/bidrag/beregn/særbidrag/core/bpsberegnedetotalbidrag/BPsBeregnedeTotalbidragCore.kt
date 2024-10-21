package no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo.BeregnBPsBeregnedeTotalbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.BeregnBPsBeregnedeTotalbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.periode.BPsBeregnedeTotalbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore

internal class BPsBeregnedeTotalbidragCore(
    private val bPsBeregnedeTotalbidragPeriode: BPsBeregnedeTotalbidragPeriode = BPsBeregnedeTotalbidragPeriode(),
) : FellesCore() {

    fun beregnBPsBeregnedeTotalbidrag(grunnlag: LøpendeBidragGrunnlagCore): BeregnBPsBeregnedeTotalbidragResultatCore {
        val beregnBPsBeregnedeTotalbidragResultat =
            bPsBeregnedeTotalbidragPeriode.beregnPerioder(grunnlag)
        return mapFraBusinessObject(resultat = beregnBPsBeregnedeTotalbidragResultat)
    }

    private fun mapFraBusinessObject(resultat: BeregnBPsBeregnedeTotalbidragResultat) = BeregnBPsBeregnedeTotalbidragResultatCore(
        resultatPeriode = mapResultatPeriode(resultat.resultatPeriode),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriode),
    )

    private fun mapResultatPeriode(resultatPeriode: ResultatPeriode) = ResultatPeriodeCore(
        periode = PeriodeCore(datoFom = resultatPeriode.periode.datoFom, datoTil = resultatPeriode.periode.datoTil),
        resultat = ResultatBeregningCore(resultatPeriode.resultat.bPsBeregnedeTotalbidrag, resultatPeriode.resultat.beregnetBidragPerBarn),
        grunnlagsreferanseListe = mapReferanseListe(resultatPeriode).sorted().toMutableList(),
    )

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        referanseListe.add(resultatPeriode.grunnlag.referanse)
        referanseListe.addAll(resultatPeriode.grunnlag.grunnlagsreferanseListe)
        referanseListe.addAll(resultatPeriode.grunnlag.løpendeBidragCoreListe.map { it.referanseBarn })
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriode: ResultatPeriode) = resultatPeriode
        .resultat.sjablonListe
        .flatMap { mapSjablonListe(listOf(it)) }
        .distinct()
}
