package no.nav.bidrag.beregn.core

import no.nav.bidrag.beregn.felles.FellesCore
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.særtilskudd.bo.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.særtilskudd.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særtilskudd.bo.BeregnSaertilskuddResultat
import no.nav.bidrag.beregn.særtilskudd.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særtilskudd.bo.LopendeBidragPeriode
import no.nav.bidrag.beregn.særtilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.bo.SamvaersfradragGrunnlagPeriode
import no.nav.bidrag.beregn.særtilskudd.dto.BPsAndelSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.dto.BeregnSaertilskuddResultatCore
import no.nav.bidrag.beregn.særtilskudd.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særtilskudd.dto.LopendeBidragPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særtilskudd.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.dto.SamvaersfradragPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.periode.SaertilskuddPeriode

class SærtilskuddCore(private val saertilskuddPeriode: SaertilskuddPeriode) : FellesCore(), SaertilskuddCore {
    override fun beregnSaertilskudd(grunnlag: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore {
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

    private fun mapBidragsevnePeriodeListe(bidragsevnePeriodeListeCore: List<BidragsevnePeriodeCore>): List<BidragsevnePeriode> {
        val bidragsevnePeriodeListe = mutableListOf<BidragsevnePeriode>()
        bidragsevnePeriodeListeCore.forEach {
            bidragsevnePeriodeListe.add(
                BidragsevnePeriode(
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
    ): List<BPsAndelSaertilskuddPeriode> {
        val bPsAndelSaertilskuddPeriodeListe = mutableListOf<BPsAndelSaertilskuddPeriode>()
        bPsAndelSaertilskuddPeriodeListeCore.forEach {
            bPsAndelSaertilskuddPeriodeListe.add(
                BPsAndelSaertilskuddPeriode(
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

    private fun mapLopendeBidragPeriodeListe(lopendeBidragPeriodeListeCore: List<LopendeBidragPeriodeCore>): List<LopendeBidragPeriode> {
        val lopendeBidragPeriodeListe = mutableListOf<LopendeBidragPeriode>()
        lopendeBidragPeriodeListeCore.forEach {
            lopendeBidragPeriodeListe.add(
                LopendeBidragPeriode(
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
    ): List<SamvaersfradragGrunnlagPeriode> {
        val samvaersfradragPeriodeListe = mutableListOf<SamvaersfradragGrunnlagPeriode>()
        samvaersfradragPeriodeCoreListe.forEach {
            samvaersfradragPeriodeListe.add(
                SamvaersfradragGrunnlagPeriode(
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
