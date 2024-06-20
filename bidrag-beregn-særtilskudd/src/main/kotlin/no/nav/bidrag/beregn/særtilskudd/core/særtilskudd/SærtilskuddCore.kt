package no.nav.bidrag.beregn.særtilskudd.core.særtilskudd

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.felles.FellesCore
import no.nav.bidrag.beregn.core.særtilskudd.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.core.særtilskudd.bo.BeregnSaertilskuddResultat
import no.nav.bidrag.beregn.core.særtilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.core.særtilskudd.dto.BPsAndelSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.BeregnSaertilskuddResultatCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.LopendeBidragPeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.dto.SamvaersfradragPeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.periode.SaertilskuddPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode

class SærtilskuddCore(private val saertilskuddPeriode: SaertilskuddPeriode = SaertilskuddPeriode()) : FellesCore() {
    fun beregnSaertilskudd(grunnlag: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore {
        val beregnSaertilskuddGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = saertilskuddPeriode.validerInput(beregnSaertilskuddGrunnlag)
        val beregnSaertilskuddResultat =
            if (avvikListe.isEmpty()) {
                saertilskuddPeriode.beregnPerioder(beregnSaertilskuddGrunnlag)
            } else {
                BeregnSaertilskuddResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnSaertilskuddResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnSaertilskuddGrunnlagCore) = BeregnSaertilskuddGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        soknadsbarnPersonId = grunnlag.soknadsbarnPersonId,
        bidragsevnePeriodeListe = mapBidragsevnePeriodeListe(grunnlag.bidragsevnePeriodeListe),
        bPsAndelSaertilskuddPeriodeListe = mapBPsAndelSaertilskuddPeriodeListe(grunnlag.bPsAndelSaertilskuddPeriodeListe),
        lopendeBidragPeriodeListe = mapLopendeBidragPeriodeListe(grunnlag.lopendeBidragPeriodeListe),
        samvaersfradragGrunnlagPeriodeListe = mapSamvaersfradragPeriodeListe(grunnlag.samvaersfradragPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnSaertilskuddResultat) = BeregnSaertilskuddResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapBidragsevnePeriodeListe(
        bidragsevnePeriodeListeCore: List<BidragsevnePeriodeCore>,
    ): List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode> {
        val bidragsevnePeriodeListe = mutableListOf<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode>()
        bidragsevnePeriodeListeCore.forEach {
            bidragsevnePeriodeListe.add(
                no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    bidragsevneBelop = it.bidragsevneBelop,
                ),
            )
        }
        return bidragsevnePeriodeListe
    }

    private fun mapBPsAndelSaertilskuddPeriodeListe(
        bPsAndelSaertilskuddPeriodeListeCore: List<BPsAndelSaertilskuddPeriodeCore>,
    ): List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode> {
        val bPsAndelSaertilskuddPeriodeListe = mutableListOf<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode>()
        bPsAndelSaertilskuddPeriodeListeCore.forEach {
            bPsAndelSaertilskuddPeriodeListe.add(
                no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    bPsAndelSaertilskuddProsent = it.bPsAndelSaertilskuddProsent,
                    bPsAndelSaertilskuddBelop = it.bPsAndelSaertilskuddBelop,
                    barnetErSelvforsorget = it.barnetErSelvforsorget,
                ),
            )
        }
        return bPsAndelSaertilskuddPeriodeListe
    }

    private fun mapLopendeBidragPeriodeListe(
        lopendeBidragPeriodeListeCore: List<LopendeBidragPeriodeCore>,
    ): List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode> {
        val lopendeBidragPeriodeListe = mutableListOf<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode>()
        lopendeBidragPeriodeListeCore.forEach {
            lopendeBidragPeriodeListe.add(
                no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    barnPersonId = it.barnPersonId,
                    lopendeBidragBelop = it.lopendeBidragBelop,
                    opprinneligBPsAndelUnderholdskostnadBelop = it.opprinneligBPsAndelUnderholdskostnadBelop,
                    opprinneligBidragBelop = it.opprinneligBidragBelop,
                    opprinneligSamvaersfradragBelop = it.opprinneligSamvaersfradragBelop,
                ),
            )
        }
        return lopendeBidragPeriodeListe
    }

    private fun mapSamvaersfradragPeriodeListe(
        samvaersfradragPeriodeCoreListe: List<SamvaersfradragPeriodeCore>,
    ): List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode> {
        val samvaersfradragPeriodeListe = mutableListOf<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode>()
        samvaersfradragPeriodeCoreListe.forEach {
            samvaersfradragPeriodeListe.add(
                no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode(
                    referanse = it.referanse,
                    barnPersonId = it.barnPersonId,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    samvaersfradragBelop = it.samvaersfradragBelop,
                ),
            )
        }
        return samvaersfradragPeriodeListe
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>): List<ResultatPeriodeCore> {
        val resultatPeriodeCoreListe = mutableListOf<ResultatPeriodeCore>()
        resultatPeriodeListe.forEach {
            resultatPeriodeCoreListe.add(
                ResultatPeriodeCore(
                    periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    soknadsbarnPersonId = it.soknadsbarnPersonId,
                    resultatBeregning = ResultatBeregningCore(belop = it.resultat.resultatBelop, kode = it.resultat.resultatkode.toString()),
                    grunnlagReferanseListe = mapReferanseListe(it),
                ),
            )
        }
        return resultatPeriodeCoreListe
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (bidragsevne, bPsAndelSaertilskudd, lopendeBidragListe, samvaersfradragGrunnlagListe) = resultatPeriode.grunnlag
        val referanseListe = mutableListOf<String>()
        referanseListe.add(bidragsevne.referanse)
        referanseListe.add(bPsAndelSaertilskudd.referanse)
        samvaersfradragGrunnlagListe.forEach {
            referanseListe.add(it.referanse)
        }
        lopendeBidragListe.forEach {
            referanseListe.add(it.referanse)
        }
        return referanseListe.sorted()
    }
}
