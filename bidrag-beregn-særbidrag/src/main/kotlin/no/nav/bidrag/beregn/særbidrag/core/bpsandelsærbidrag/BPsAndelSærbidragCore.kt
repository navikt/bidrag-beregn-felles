package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.UtgiftPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesCore

internal class BPsAndelSærbidragCore(
    private val bPsAndelSærbidragPeriode: BPsAndelSærbidragPeriode = BPsAndelSærbidragPeriode(),
) : FellesCore() {

    fun beregnBPsAndelSærbidrag(grunnlag: BeregnBPsAndelSærbidragGrunnlagCore): BeregnBPsAndelSærbidragResultatCore {
        val beregnBPsAndelSærbidragGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = bPsAndelSærbidragPeriode.validerInput(beregnBPsAndelSærbidragGrunnlag)
        val beregnBPsAndelSærbidragResultat =
            if (avvikListe.isEmpty()) {
                bPsAndelSærbidragPeriode.beregnPerioder(beregnBPsAndelSærbidragGrunnlag)
            } else {
                BeregnBPsAndelSærbidragResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnBPsAndelSærbidragResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnBPsAndelSærbidragGrunnlagCore) = BeregnBPsAndelSærbidragGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        utgiftPeriodeListe = mapUtgiftPeriodeListe(grunnlag.utgiftPeriodeListe),
        inntektBPPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBPPeriodeListe),
        inntektBMPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektBMPeriodeListe),
        inntektSBPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektSBPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnBPsAndelSærbidragResultat) = BeregnBPsAndelSærbidragResultatCore(
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
            beløp = it.beløp,
        )
    }

    private fun mapResultatPeriode(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe.map {
        ResultatPeriodeCore(
            periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
            resultat = ResultatBeregningCore(
                resultatAndelFaktor = it.resultat.resultatAndelFaktor,
                resultatAndelBeløp = it.resultat.resultatAndelBeløp,
                barnetErSelvforsørget = it.resultat.barnetErSelvforsørget,
            ),
            grunnlagsreferanseListe = mapReferanseListe(it).sorted().toMutableList(),
        )
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (utgiftsbeløp, inntektBP, inntektBM, inntektSB) = resultatPeriode.grunnlag
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        referanseListe.add(utgiftsbeløp.referanse)
        if (inntektBP != null) referanseListe.add(inntektBP.referanse)
        if (inntektBM != null) referanseListe.add(inntektBM.referanse)
        if (inntektSB != null) referanseListe.add(inntektSB.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.sorted()
    }

    private fun mapSjablonGrunnlagListe(resultatPeriodeListe: List<ResultatPeriode>) = resultatPeriodeListe
        .map { it.resultat.sjablonListe }
        .flatMap { mapSjablonListe(it) }
        .distinct()
}
