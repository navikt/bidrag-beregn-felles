package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.UtgiftPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.InntektPeriodeCore

internal class BPsAndelSærbidragCore(
    private val bPsAndelSærbidragPeriode: BPsAndelSærbidragPeriode = BPsAndelSærbidragPeriode(),
) : FellesCore() {

    fun beregnBPsAndelSaertilskudd(grunnlag: BeregnBPsAndelSærtilskuddGrunnlagCore): BeregnBPsAndelSærtilskuddResultatCore {
        val beregnBPsAndelSaertilskuddGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = bPsAndelSærbidragPeriode.validerInput(beregnBPsAndelSaertilskuddGrunnlag)
        val beregnBPsAndelSaertilskuddResultat =
            if (avvikListe.isEmpty()) {
                bPsAndelSærbidragPeriode.beregnPerioder(beregnBPsAndelSaertilskuddGrunnlag)
            } else {
                BeregnBPsAndelSaertilskuddResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnBPsAndelSaertilskuddResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnBPsAndelSærtilskuddGrunnlagCore) = BeregnBPsAndelSaertilskuddGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        utgiftPeriodeListe = mapUtgiftPeriodeListe(grunnlag.utgiftPeriodeListe),
        inntektBPPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBPPeriodeListe),
        inntektBMPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBMPeriodeListe),
        inntektSBPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBBPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnBPsAndelSaertilskuddResultat) = BeregnBPsAndelSærtilskuddResultatCore(
        resultatPeriodeListe = mapResultatPeriode(resultat.resultatPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.resultatPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapUtgiftPeriodeListe(utgiftPeriodeListeCore: List<UtgiftPeriodeCore>) = utgiftPeriodeListeCore.map {
        UtgiftPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            beløp = it.beløp,
        )
    }

    private fun mapInntektPeriodeListe(inntekterPeriodeListeCore: List<InntektPeriodeCore>) = inntekterPeriodeListeCore.map {
        InntektPeriode(
            referanse = it.referanse,
            periode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            type = " ",
            beløp = it.beløp,
        )
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.map {
        ResultatPeriodeCore(
            periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            resultat = ResultatBeregningCore(
                resultatAndelProsent = it.resultat.resultatAndelProsent,
                resultatAndelBeløp = it.resultat.resultatAndelBelop,
                barnetErSelvforsørget = it.resultat.barnetErSelvforsorget,
            ),
            grunnlagsreferanseListe = mapReferanseListe(it),
        )
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (utgiftsbeløp, inntektBPListe, inntektBMListe, inntektBBListe) = resultatPeriode.grunnlag
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        referanseListe.add(utgiftsbeløp.referanse)
        inntektBPListe.forEach { referanseListe.add(it.referanse) }
        inntektBMListe.forEach { referanseListe.add(it.referanse) }
        inntektBBListe.forEach { referanseListe.add(it.referanse) }
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe
        .map { it.resultat.sjablonListe }
        .flatMap { mapSjablonListe(it) }
        .distinct()
}
