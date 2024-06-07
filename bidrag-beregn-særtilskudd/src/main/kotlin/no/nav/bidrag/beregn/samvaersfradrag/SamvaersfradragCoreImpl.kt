package no.nav.bidrag.beregn.samvaersfradrag

import no.nav.bidrag.beregn.felles.FellesCore
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragGrunnlag
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragResultat
import no.nav.bidrag.beregn.samvaersfradrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.samvaersfradrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.samvaersfradrag.bo.SamvaersfradragGrunnlagPeriode
import no.nav.bidrag.beregn.samvaersfradrag.dto.BeregnSamvaersfradragGrunnlagCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.BeregnSamvaersfradragResultatCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.SamvaersklassePeriodeCore
import no.nav.bidrag.beregn.samvaersfradrag.periode.SamvaersfradragPeriode

class SamvaersfradragCoreImpl(private val samvaersfradragPeriode: SamvaersfradragPeriode) : FellesCore(), SamvaersfradragCore {
    override fun beregnSamvaersfradrag(grunnlag: BeregnSamvaersfradragGrunnlagCore): BeregnSamvaersfradragResultatCore {
        val beregnSamvaersfradragGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = samvaersfradragPeriode.validerInput(beregnSamvaersfradragGrunnlag)
        val beregnSamvaersfradragResultat =
            if (avvikListe.isEmpty()) {
                samvaersfradragPeriode.beregnPerioder(beregnSamvaersfradragGrunnlag)
            } else {
                BeregnSamvaersfradragResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnSamvaersfradragResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnSamvaersfradragGrunnlagCore) = BeregnSamvaersfradragGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        samvaersfradragGrunnlagPeriodeListe = mapSamvaersklassePeriodeListe(grunnlag.samvaersklassePeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnSamvaersfradragResultat) = BeregnSamvaersfradragResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapSamvaersklassePeriodeListe(samvaersklassePeriodeListeCore: List<SamvaersklassePeriodeCore>): List<SamvaersfradragGrunnlagPeriode> {
        val samvaersklassePeriodeListe = mutableListOf<SamvaersfradragGrunnlagPeriode>()
        samvaersklassePeriodeListeCore.forEach {
            samvaersklassePeriodeListe.add(
                SamvaersfradragGrunnlagPeriode(
                    referanse = it.referanse,
                    samvaersfradragDatoFraTil = Periode(
                        datoFom = it.samvaersklassePeriodeDatoFraTil.datoFom,
                        datoTil = it.samvaersklassePeriodeDatoFraTil.datoTil,
                    ),
                    barnPersonId = it.barnPersonId,
                    barnFodselsdato = it.barnFodselsdato,
                    samvaersklasse = it.samvaersklasse,
                ),
            )
        }
        return samvaersklassePeriodeListe
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>): List<ResultatPeriodeCore> {
        val resultatPeriodeCoreListe = mutableListOf<ResultatPeriodeCore>()
        resultatPeriodeListe.forEach {
            resultatPeriodeCoreListe.add(
                ResultatPeriodeCore(
                    periode = PeriodeCore(datoFom = it.resultatDatoFraTil.datoFom, datoTil = it.resultatDatoFraTil.datoTil),
                    resultatBeregningListe = mapResultatBeregning(it.resultatBeregningListe),
                    grunnlagReferanseListe = mapReferanseListe(it),
                ),
            )
        }
        return resultatPeriodeCoreListe
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (samvaersfradragGrunnlagPerBarnListe) = resultatPeriode.resultatGrunnlag
        val sjablonListe = mutableListOf<SjablonPeriodeNavnVerdi>()
        val referanseListe = mutableListOf<String>()
        resultatPeriode.resultatBeregningListe.forEach { sjablonListe.addAll(it.sjablonListe) }
        samvaersfradragGrunnlagPerBarnListe.forEach { referanseListe.add(it.referanse) }
        referanseListe.addAll(
            sjablonListe
                .map { lagSjablonReferanse(it) }
                .distinct(),
        )
        return referanseListe.sorted()
    }

    private fun mapResultatBeregning(resultatBeregningListe: List<ResultatBeregning>): List<ResultatBeregningCore> {
        val resultatBeregningCoreListe = mutableListOf<ResultatBeregningCore>()
        resultatBeregningListe.forEach {
            resultatBeregningCoreListe.add(
                ResultatBeregningCore(barnPersonId = it.barnPersonId, belop = it.belop),
            )
        }
        return resultatBeregningCoreListe
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.stream()
        .map { it.resultatBeregningListe }
        .flatMap { it.stream() }
        .map { mapSjablonListe(it.sjablonListe) }
        .flatMap { it.stream() }
        .distinct()
        .toList()
}
