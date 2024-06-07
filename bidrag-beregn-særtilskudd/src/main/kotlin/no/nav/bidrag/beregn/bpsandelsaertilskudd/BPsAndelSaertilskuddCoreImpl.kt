package no.nav.bidrag.beregn.bpsandelsaertilskudd

import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddGrunnlag
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.Inntekt
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.InntektPeriode
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.NettoSaertilskuddPeriode
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddResultatCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnedeGrunnlagCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.NettoSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.periode.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.felles.FellesCore
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.dto.PeriodeCore

class BPsAndelSaertilskuddCoreImpl(private val bPsAndelSaertilskuddPeriode: BPsAndelSaertilskuddPeriode) : FellesCore(), BPsAndelSaertilskuddCore {

    override fun beregnBPsAndelSaertilskudd(grunnlag: BeregnBPsAndelSaertilskuddGrunnlagCore): BeregnBPsAndelSaertilskuddResultatCore {
        val beregnBPsAndelSaertilskuddGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = bPsAndelSaertilskuddPeriode.validerInput(beregnBPsAndelSaertilskuddGrunnlag)
        val beregnBPsAndelSaertilskuddResultat =
            if (avvikListe.isEmpty()) {
                bPsAndelSaertilskuddPeriode.beregnPerioder(beregnBPsAndelSaertilskuddGrunnlag)
            } else {
                BeregnBPsAndelSaertilskuddResultat(emptyList())
            }
        return mapFraBusinessObject(
            avvikListe = avvikListe,
            resultat = beregnBPsAndelSaertilskuddResultat,
            grunnlag = beregnBPsAndelSaertilskuddGrunnlag,
        )
    }

    private fun mapTilBusinessObject(grunnlag: BeregnBPsAndelSaertilskuddGrunnlagCore) = BeregnBPsAndelSaertilskuddGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        nettoSaertilskuddPeriodeListe = mapNettoSaertilskuddPeriodeListe(grunnlag.nettoSaertilskuddPeriodeListe),
        inntektBPPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBPPeriodeListe),
        inntektBMPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBMPeriodeListe),
        inntektBBPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBBPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(
        avvikListe: List<Avvik>,
        resultat: BeregnBPsAndelSaertilskuddResultat,
        grunnlag: BeregnBPsAndelSaertilskuddGrunnlag,
    ) = BeregnBPsAndelSaertilskuddResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultatPeriodeListe = resultat.resultatPeriodeListe, grunnlag = grunnlag),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapNettoSaertilskuddPeriodeListe(
        nettoSaertilskuddPeriodeListeCore: List<NettoSaertilskuddPeriodeCore>,
    ): List<NettoSaertilskuddPeriode> {
        val nettoSaertilskuddPeriodeListe = mutableListOf<NettoSaertilskuddPeriode>()
        nettoSaertilskuddPeriodeListeCore.forEach {
            nettoSaertilskuddPeriodeListe.add(
                NettoSaertilskuddPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    nettoSaertilskuddBelop = it.nettoSaertilskuddBelop,
                ),
            )
        }
        return nettoSaertilskuddPeriodeListe
    }

    private fun mapInntektPeriodeListe(inntekterPeriodeListeCore: List<InntektPeriodeCore>): List<InntektPeriode> {
        val inntekterPeriodeListe = mutableListOf<InntektPeriode>()
        inntekterPeriodeListeCore.forEach {
            inntekterPeriodeListe.add(
                InntektPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = Periode(datoFom = it.periodeDatoFraTil.datoFom, datoTil = it.periodeDatoFraTil.datoTil),
                    inntektType = it.inntektType,
                    inntektBelop = it.inntektBelop,
                    deltFordel = it.deltFordel,
                    skatteklasse2 = it.skatteklasse2,
                ),
            )
        }
        return inntekterPeriodeListe
    }

    private fun mapResultatPeriode(
        resultatPeriodeListe: List<ResultatPeriode>,
        grunnlag: BeregnBPsAndelSaertilskuddGrunnlag,
    ): List<ResultatPeriodeCore> {
        val resultatPeriodeCoreListe = mutableListOf<ResultatPeriodeCore>()
        resultatPeriodeListe.forEach {
            resultatPeriodeCoreListe.add(
                ResultatPeriodeCore(
                    periode = PeriodeCore(datoFom = it.resultatDatoFraTil.datoFom, datoTil = it.resultatDatoFraTil.datoTil),
                    resultatBeregning = ResultatBeregningCore(
                        resultatAndelProsent = it.resultatBeregning.resultatAndelProsent,
                        resultatAndelBelop = it.resultatBeregning.resultatAndelBelop,
                        barnetErSelvforsorget = it.resultatBeregning.barnetErSelvforsorget,
                    ),
                    beregnedeGrunnlag = mapBeregnedeInntektGrunnlag(
                        grunnlagBeregning = it.resultatGrunnlagBeregning,
                        beregnBPsAndelSaertilskuddGrunnlag = grunnlag,
                    ),
                    grunnlagReferanseListe = mapReferanseListe(it),
                ),
            )
        }
        return resultatPeriodeCoreListe
    }

    private fun mapBeregnedeInntektGrunnlag(
        grunnlagBeregning: GrunnlagBeregning,
        beregnBPsAndelSaertilskuddGrunnlag: BeregnBPsAndelSaertilskuddGrunnlag,
    ) = BeregnedeGrunnlagCore(
        inntektBPListe = grunnlagBeregning.inntektBPListe
            .filter { inntekt -> beregnBPsAndelSaertilskuddGrunnlag.inntektBPPeriodeListe.none { it.referanse == inntekt.referanse } }
            .map {
                Inntekt(
                    referanse = it.referanse,
                    inntektType = it.inntektType,
                    inntektBelop = it.inntektBelop,
                    deltFordel = it.deltFordel,
                    skatteklasse2 = it.skatteklasse2,
                )
            },
        inntektBMListe = grunnlagBeregning.inntektBMListe
            .filter { inntekt -> beregnBPsAndelSaertilskuddGrunnlag.inntektBMPeriodeListe.none { it.referanse == inntekt.referanse } }
            .map {
                Inntekt(
                    referanse = it.referanse,
                    inntektType = it.inntektType,
                    inntektBelop = it.inntektBelop,
                    deltFordel = it.deltFordel,
                    skatteklasse2 = it.skatteklasse2,
                )
            },
        inntektBBListe = grunnlagBeregning.inntektBBListe
            .filter { inntekt -> beregnBPsAndelSaertilskuddGrunnlag.inntektBBPeriodeListe.none { it.referanse == inntekt.referanse } }
            .map {
                Inntekt(
                    referanse = it.referanse,
                    inntektType = it.inntektType,
                    inntektBelop = it.inntektBelop,
                    deltFordel = it.deltFordel,
                    skatteklasse2 = it.skatteklasse2,
                )
            },
    )

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (_, inntektBPListe, inntektBMListe, inntektBBListe) = resultatPeriode.resultatGrunnlagBeregning
        val sjablonListe = resultatPeriode.resultatBeregning.sjablonListe
        val referanseListe = mutableListOf<String>()

        inntektBPListe.forEach { referanseListe.add(it.referanse) }
        inntektBMListe.forEach { referanseListe.add(it.referanse) }
        inntektBBListe.forEach { referanseListe.add(it.referanse) }
        referanseListe.addAll(
            sjablonListe
                .map { lagSjablonReferanse(it) }
                .distinct(),
        )
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.stream()
        .map { mapSjablonListe(it.resultatBeregning.sjablonListe) }
        .flatMap { it.stream() }
        .distinct()
        .toList()
}
