package no.nav.bidrag.beregn.bidragsevne

import no.nav.bidrag.beregn.bidragsevne.bo.BarnIHustandPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.bidragsevne.bo.BostatusPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.InntektPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.SaerfradragPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.SkatteklassePeriode
import no.nav.bidrag.beregn.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.bidragsevne.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.bidragsevne.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.bidragsevne.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.bidragsevne.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.bidragsevne.dto.SaerfradragPeriodeCore
import no.nav.bidrag.beregn.bidragsevne.dto.SkatteklassePeriodeCore
import no.nav.bidrag.beregn.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.beregn.felles.FellesCore
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.domene.enums.beregning.Særfradragskode
import no.nav.bidrag.domene.enums.person.Bostatuskode

class BidragsevneCoreImpl(private val bidragsevnePeriode: BidragsevnePeriode) : FellesCore(), BidragsevneCore {

    override fun beregnBidragsevne(grunnlag: BeregnBidragsevneGrunnlagCore): BeregnBidragsevneResultatCore {
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
        skatteklassePeriodeListe = mapSkatteklassePeriodeListe(grunnlag.skatteklassePeriodeListe),
        bostatusPeriodeListe = mapBostatusPeriodeListe(grunnlag.bostatusPeriodeListe),
        antallBarnIEgetHusholdPeriodeListe = mapAntallBarnIEgetHusholdPeriodeListe(grunnlag.antallBarnIEgetHusholdPeriodeListe),
        saerfradragPeriodeListe = mapSaerfradragPeriodeListe(grunnlag.saerfradragPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnBidragsevneResultat) = BeregnBidragsevneResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapInntektPeriodeListe(inntektPeriodeListeCore: List<InntektPeriodeCore>): List<InntektPeriode> {
        val inntektPeriodeListe = mutableListOf<InntektPeriode>()
        inntektPeriodeListeCore.forEach {
            inntektPeriodeListe.add(
                InntektPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    inntektType = it.inntektType,
                    inntektBelop = it.inntektBelop,
                ),
            )
        }
        return inntektPeriodeListe
    }

    private fun mapSkatteklassePeriodeListe(skatteklassePeriodeListeCore: List<SkatteklassePeriodeCore>): List<SkatteklassePeriode> {
        val skatteklassePeriodeListe = mutableListOf<SkatteklassePeriode>()
        skatteklassePeriodeListeCore.forEach {
            skatteklassePeriodeListe.add(
                SkatteklassePeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    skatteklasse = it.skatteklasse,
                ),
            )
        }
        return skatteklassePeriodeListe
    }

    private fun mapBostatusPeriodeListe(bostatusPeriodeListeCore: List<BostatusPeriodeCore>): List<BostatusPeriode> {
        val bostatusPeriodeListe = mutableListOf<BostatusPeriode>()
        bostatusPeriodeListeCore.forEach {
            bostatusPeriodeListe.add(
                BostatusPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    bostatusKode = Bostatuskode.valueOf(it.bostatusKode),
                ),
            )
        }
        return bostatusPeriodeListe
    }

    private fun mapAntallBarnIEgetHusholdPeriodeListe(
        antallBarnIEgetHusholdPeriodeListeCore: List<AntallBarnIEgetHusholdPeriodeCore>,
    ): List<BarnIHustandPeriode> {
        val antallBarnIEgetHusholdPeriodeListe = mutableListOf<BarnIHustandPeriode>()
        antallBarnIEgetHusholdPeriodeListeCore.forEach {
            antallBarnIEgetHusholdPeriodeListe.add(
                BarnIHustandPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    antallBarn = it.antallBarn,
                ),
            )
        }
        return antallBarnIEgetHusholdPeriodeListe
    }

    private fun mapSaerfradragPeriodeListe(saerfradragPeriodeListeCore: List<SaerfradragPeriodeCore>): List<SaerfradragPeriode> {
        val saerfradragPeriodeListe = mutableListOf<SaerfradragPeriode>()
        saerfradragPeriodeListeCore.forEach {
            saerfradragPeriodeListe.add(
                SaerfradragPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    saerfradragKode = Særfradragskode.valueOf(it.saerfradragKode),
                ),
            )
        }
        return saerfradragPeriodeListe
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>): List<ResultatPeriodeCore> {
        val resultatPeriodeCoreListe = mutableListOf<ResultatPeriodeCore>()
        resultatPeriodeListe.forEach {
            resultatPeriodeCoreListe.add(
                ResultatPeriodeCore(
                    periode = PeriodeCore(datoFom = it.resultatDatoFraTil.datoFom, datoTil = it.resultatDatoFraTil.datoTil),
                    resultatBeregning = ResultatBeregningCore(it.resultatBeregning.belop),
                    grunnlagReferanseListe = mapReferanseListe(it),
                ),
            )
        }
        return resultatPeriodeCoreListe
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (inntektListe, skatteklasse, bostatus, barnIHusstand, saerfradrag) = resultatPeriode.resultatGrunnlagBeregning
        val sjablonListe = resultatPeriode.resultatBeregning.sjablonListe
        val referanseListe = mutableListOf<String>()
        inntektListe.forEach {
            referanseListe.add(it.referanse)
        }
        referanseListe.add(skatteklasse.referanse)
        referanseListe.add(bostatus.referanse)
        referanseListe.add(barnIHusstand.referanse)
        referanseListe.add(saerfradrag.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.stream()
        .map { mapSjablonListe(it.resultatBeregning.sjablonListe) }
        .flatMap { it.stream() }
        .distinct()
        .toList()
}
