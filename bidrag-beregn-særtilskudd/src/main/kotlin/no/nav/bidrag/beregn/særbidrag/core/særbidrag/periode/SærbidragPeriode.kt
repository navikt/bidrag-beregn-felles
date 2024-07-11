package no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.beregn.core.util.PeriodeUtil
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragListeGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode

class SærbidragPeriode(private val særbidragBeregning: SærbidragBeregning = SærbidragBeregning()) : FellesPeriode() {

    fun beregnPerioder(grunnlag: BeregnSærbidragGrunnlag): BeregnSærbidragResultat {
        val grunnlagTilBeregning = BeregnSærbidragListeGrunnlag()

        // Lag grunnlag til beregning
        lagGrunnlagTilBeregning(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, beregnSærbidragListeGrunnlag = grunnlagTilBeregning)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = grunnlagTilBeregning.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnSærbidragPerPeriode(søknadsbarnPersonId = grunnlag.søknadsbarnPersonId, grunnlag = grunnlagTilBeregning)

        return BeregnSærbidragResultat(grunnlagTilBeregning.periodeResultatListe)
    }

    // Lager grunnlag til beregning
    private fun lagGrunnlagTilBeregning(
        periodeGrunnlag: BeregnSærbidragGrunnlag,
        grunnlagTilBeregning: BeregnSærbidragListeGrunnlag
    ) {
        grunnlagTilBeregning.bidragsevnePeriodeListe = periodeGrunnlag.bidragsevnePeriodeListe.map { it }
        grunnlagTilBeregning.bPsAndelSærbidragPeriodeListe = periodeGrunnlag.bPsAndelSærbidragPeriodeListe.map { it }
    }

    // Lagger bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(periodeGrunnlag: BeregnSærbidragGrunnlag, beregnSærbidragListeGrunnlag: BeregnSærbidragListeGrunnlag) {
        // Bygger opp liste over perioder
        beregnSærbidragListeGrunnlag.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkter(beregnSærbidragListeGrunnlag.bidragsevnePeriodeListe)
            .addBruddpunkter(beregnSærbidragListeGrunnlag.bPsAndelSærbidragPeriodeListe)
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnSærbidragPerPeriode(søknadsbarnPersonId: String, grunnlag: BeregnSærbidragListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val bidragsevne = grunnlag.bidragsevnePeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Bidragsevne(referanse = it.referanse, beløp = it.beløp) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt BIDRAGSEVNE mangler data for periode: ${beregningsperiode.getPeriode()}") }

            val bPsAndelSærbidrag = grunnlag.bPsAndelSærbidragPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    BPsAndelSærbidrag(
                        referanse = it.referanse,
                        andelProsent = it.andelProsent,
                        andelBeløp = it.andelBeløp,
                        barnetErSelvforsørget = it.barnetErSelvforsørget,
                    )
                }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "Grunnlagsobjekt BP_ANDEL_SÆRBIDRAG mangler data for periode: ${beregningsperiode.getPeriode()}",
                    )
                }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnSærbidragGrunnlagPeriodisert =
                GrunnlagBeregning(
                    bidragsevne = bidragsevne,
                    bPsAndelSærbidrag = bPsAndelSærbidrag,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    periode = beregningsperiode,
                    søknadsbarnPersonId = søknadsbarnPersonId,
                    resultat = særbidragBeregning.beregn(beregnSærbidragGrunnlagPeriodisert),
                    grunnlag = beregnSærbidragGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til særbidragberegning er gyldige
    fun validerInput(grunnlag: BeregnSærbidragGrunnlag): List<Avvik> {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(beregnDatoFom = grunnlag.beregnDatoFra, beregnDatoTil = grunnlag.beregnDatoTil).toMutableList()

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "bidragsevnePeriodeListe",
                periodeListe = grunnlag.bidragsevnePeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "bPsAndelSærbidragPeriodeListe",
                periodeListe = grunnlag.bPsAndelSærbidragPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        return avvikListe
    }
}
