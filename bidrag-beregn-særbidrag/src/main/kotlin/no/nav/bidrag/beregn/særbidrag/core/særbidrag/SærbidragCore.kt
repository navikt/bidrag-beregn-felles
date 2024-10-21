package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsBeregnedeTotalbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBpPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsBeregnedeTotalbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BetaltAvBpPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import java.util.Collections.emptyList

internal class SærbidragCore(private val særbidragPeriode: SærbidragPeriode = SærbidragPeriode()) : FellesCore() {

    fun beregnSærbidrag(grunnlag: BeregnSærbidragGrunnlagCore): BeregnSærbidragResultatCore {
        val beregnSærbidragGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = særbidragPeriode.validerInput(beregnSærbidragGrunnlag)
        val beregnSærbidragResultat =
            if (avvikListe.isEmpty()) {
                særbidragPeriode.beregnPerioder(beregnSærbidragGrunnlag)
            } else {
                BeregnSærbidragResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnSærbidragResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnSærbidragGrunnlagCore) = BeregnSærbidragGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        søknadsbarnPersonId = grunnlag.søknadsbarnPersonId,
        betaltAvBpPeriodeListe = mapBetaltAvBpPeriodeListe(grunnlag.betaltAvBpPeriodeListe),
        bidragsevnePeriodeListe = mapBidragsevnePeriodeListe(grunnlag.bidragsevnePeriodeListe),
        bPsBeregnedeTotalbidrag = mapBPsBeregnedeTotalbidrag(grunnlag.bPsBeregnedeTotalbidragPeriodeCore),
        bPsAndelSærbidragPeriodeListe = mapBPsAndelSærbidragPeriodeListe(grunnlag.bPsAndelSærbidragPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnSærbidragResultat) = BeregnSærbidragResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapBetaltAvBpPeriodeListe(betaltAvBpPeriodeListeCore: List<BetaltAvBpPeriodeCore>) = betaltAvBpPeriodeListeCore.map {
        BetaltAvBpPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            beløp = it.beløp,
        )
    }

    private fun mapBidragsevnePeriodeListe(bidragsevnePeriodeListeCore: List<BidragsevnePeriodeCore>) = bidragsevnePeriodeListeCore.map {
        BidragsevnePeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            beløp = it.beløp,
            skatt = it.skatt,
            underholdBarnEgenHusstand = it.underholdBarnEgenHusstand,
        )
    }

    private fun mapBPsBeregnedeTotalbidrag(BPsBeregnedeTotalbidragPeriodeCore: BPsBeregnedeTotalbidragPeriodeCore) = BPsBeregnedeTotalbidragPeriode(
        referanse = BPsBeregnedeTotalbidragPeriodeCore.referanse,
        periode = Periode(
            datoFom = BPsBeregnedeTotalbidragPeriodeCore.periode.datoFom,
            datoTil = BPsBeregnedeTotalbidragPeriodeCore.periode.datoTil,
        ),
        bPsBeregnedeTotalbidrag = BPsBeregnedeTotalbidragPeriodeCore.bPsBeregnedeTotalbidrag,
    )
}

private fun mapBPsAndelSærbidragPeriodeListe(bPsAndelSærbidragPeriodeListeCore: List<BPsAndelSærbidragPeriodeCore>) =
    bPsAndelSærbidragPeriodeListeCore.map {
        BPsAndelSærbidragPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            endeligAndelFaktor = it.endeligAndelFaktor,
            andelBeløp = it.andelBeløp,
            beregnetAndelFaktor = it.beregnetAndelFaktor,
            barnEndeligInntekt = it.barnEndeligInntekt,
            barnetErSelvforsørget = it.barnetErSelvforsørget,
        )
    }

private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.map {
    ResultatPeriodeCore(
        periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
        søknadsbarnPersonId = it.søknadsbarnPersonId,
        resultat = ResultatBeregningCore(
            beregnetBeløp = it.resultat.beregnetBeløp,
            resultatKode = it.resultat.resultatKode,
            resultatBeløp = it.resultat.resultatBeløp,
        ),
        grunnlagsreferanseListe = mapReferanseListe(it).sorted().toMutableList(),
    )
}

private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
    val (_, bidragsevne, bPsBeregnedeTotalbidrag, bPsAndelSærbidrag) = resultatPeriode.grunnlag
    val referanseListe = mutableListOf<String>()
    // Ikke nødvendig
    //    referanseListe.add(beløpBetaltAvBp.referanse)
    referanseListe.add(bidragsevne.referanse)
    referanseListe.add(bPsBeregnedeTotalbidrag.referanse)
    referanseListe.add(bPsAndelSærbidrag.referanse)
    return referanseListe.sorted()
}
