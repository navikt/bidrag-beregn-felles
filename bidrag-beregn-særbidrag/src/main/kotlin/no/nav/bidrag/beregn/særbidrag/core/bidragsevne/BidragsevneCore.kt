package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BarnIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.VoksneIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore
import java.util.Collections.emptyList

internal class BidragsevneCore(private val bidragsevnePeriode: BidragsevnePeriode = BidragsevnePeriode()) : FellesCore() {

    fun beregnBidragsevne(grunnlag: BeregnBidragsevneGrunnlagCore): BeregnBidragsevneResultatCore {
        val beregnBidragsevneGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = bidragsevnePeriode.validerInput(beregnBidragsevneGrunnlag)
        val beregnBidragsevneResultat =
            if (avvikListe.isEmpty()) {
                bidragsevnePeriode.beregnPerioder(beregnBidragsevneGrunnlag)
            } else {
                BeregnBidragsevneResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnBidragsevneResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnBidragsevneGrunnlagCore) = BeregnBidragsevneGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        inntektPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektPeriodeListe),
        barnIHusstandPeriodeListe = mapBarnIHusstandPeriodeListe(grunnlag.boforholdPeriodeListe),
        voksneIHusstandPeriodeListe = mapVoksneIHusstandPeriodeListe(grunnlag.boforholdPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnBidragsevneResultat) = BeregnBidragsevneResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe).toMutableList(),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapInntektPeriodeListe(inntektPeriodeListeCore: List<InntektPeriodeCore>) = inntektPeriodeListeCore.map {
        InntektPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            beløp = it.beløp,
        )
    }

    private fun mapBarnIHusstandPeriodeListe(boforholdPeriodeListeCore: List<BoforholdPeriodeCore>) = boforholdPeriodeListeCore.map {
        BarnIHusstandPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            antall = it.antallBarn,
        )
    }

    private fun mapVoksneIHusstandPeriodeListe(boforholdPeriodeListeCore: List<BoforholdPeriodeCore>) = boforholdPeriodeListeCore.map {
        VoksneIHusstandPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            borMedAndre = it.borMedAndreVoksne,
        )
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.map {
        ResultatPeriodeCore(
            periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            resultat = ResultatBeregningCore(
                beløp = it.resultat.beløp,
                skatt = it.resultat.skatt,
                underholdBarnEgenHusstand = it.resultat.underholdBarnEgenHusstand,
            ),
            grunnlagsreferanseListe = mapReferanseListe(it).sorted().toMutableList(),
        )
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (inntekt, antallBarnIHusstand, antallVoksneIHusstand) = resultatPeriode.grunnlag
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        if (inntekt != null) referanseListe.add(inntekt.referanse)
        referanseListe.add(antallBarnIHusstand.referanse)
        referanseListe.add(antallVoksneIHusstand.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe
        .map { it.resultat.sjablonListe }
        .flatMap { mapSjablonListe(it) }
        .distinct()
}
