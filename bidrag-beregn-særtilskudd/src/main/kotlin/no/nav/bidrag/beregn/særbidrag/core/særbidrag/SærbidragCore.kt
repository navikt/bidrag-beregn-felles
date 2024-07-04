package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSaertilskuddResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.LopendeBidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.SamvaersfradragGrunnlagPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSaertilskuddResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.LopendeBidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.SamvaersfradragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode

class SærbidragCore(private val særbidragPeriode: SærbidragPeriode = SærbidragPeriode()) : FellesCore() {
    fun beregnSaertilskudd(grunnlag: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore {
        val beregnSaertilskuddGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = særbidragPeriode.validerInput(beregnSaertilskuddGrunnlag)
        val beregnSaertilskuddResultat =
            if (avvikListe.isEmpty()) {
                særbidragPeriode.beregnPerioder(beregnSaertilskuddGrunnlag)
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
                    grunnlagsreferanseListe = mapReferanseListe(it),
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
