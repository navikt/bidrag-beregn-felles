package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.beregn.core.util.PeriodeUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning.BPsAndelSærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragListeGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Utgift
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesPeriode
import java.time.LocalDate

class BPsAndelSærbidragPeriode(private val bPsAndelSærbidragBeregning: BPsAndelSærbidragBeregning = BPsAndelSærbidragBeregning()) :
    FellesPeriode() {

    fun beregnPerioder(grunnlag: BeregnBPsAndelSærbidragGrunnlag): BeregnBPsAndelSærbidragResultat {
        val grunnlagTilBeregning = BeregnBPsAndelSærbidragListeGrunnlag()

        // Lag grunnlag til beregning
        lagGrunnlagTilBeregning(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = grunnlagTilBeregning.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnBPsAndelSærbidragPerPeriode(grunnlagTilBeregning)

        return BeregnBPsAndelSærbidragResultat(grunnlagTilBeregning.periodeResultatListe)
    }

    // Lager grunnlag til beregning
    private fun lagGrunnlagTilBeregning(
        periodeGrunnlag: BeregnBPsAndelSærbidragGrunnlag,
        grunnlagTilBeregning: BeregnBPsAndelSærbidragListeGrunnlag
    ) {
        grunnlagTilBeregning.inntektBPPeriodeListe = periodeGrunnlag.inntektBPPeriodeListe.map { it }
        grunnlagTilBeregning.inntektBMPeriodeListe = periodeGrunnlag.inntektBMPeriodeListe.map { it }
        grunnlagTilBeregning.inntektSBPeriodeListe = periodeGrunnlag.inntektSBPeriodeListe.map { it }
        grunnlagTilBeregning.utgiftPeriodeListe = periodeGrunnlag.utgiftPeriodeListe.map { it }
        grunnlagTilBeregning.sjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe.map { it }
    }

    // Lager bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(
        periodeGrunnlag: BeregnBPsAndelSærbidragGrunnlag,
        grunnlagTilBeregning: BeregnBPsAndelSærbidragListeGrunnlag,
    ) {
        // Regler for beregning av BPs andel ble endret fra 01.01.2009, alle perioder etter da skal beregnes på ny måte.
        // Det må derfor legges til brudd på denne datoen
        val datoRegelendringer = listOf(Periode(datoFom = LocalDate.parse("2009-01-01"), datoTil = LocalDate.parse("2009-01-01")))

        // Bygger opp liste over perioder
        grunnlagTilBeregning.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkter(grunnlagTilBeregning.utgiftPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.inntektBPPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.inntektBMPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.inntektSBPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.sjablonPeriodeListe)
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .addBruddpunkter(datoRegelendringer)
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnBPsAndelSærbidragPerPeriode(grunnlag: BeregnBPsAndelSærbidragListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val inntektBPListe = grunnlag.inntektBPPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Inntekt(referanse = it.referanse, inntektType = it.type, inntektBeløp = it.beløp) }

            val inntektBMListe = grunnlag.inntektBMPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Inntekt(referanse = it.referanse, inntektType = it.type, inntektBeløp = it.beløp) }

            val inntektSBListe = grunnlag.inntektSBPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Inntekt(referanse = it.referanse, inntektType = it.type, inntektBeløp = it.beløp) }

            val utgiftsbeløp = grunnlag.utgiftPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Utgift(referanse = it.referanse, beløp = it.beløp) }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException("Grunnlagsobjekt DELBEREGNING_UTGIFT mangler data for periode: ${beregningsperiode.getPeriode()}")
                }

            val sjablonliste = grunnlag.sjablonPeriodeListe.filter { it.getPeriode().overlapperMed(beregningsperiode) }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnBPsAndelSærbidragGrunnlagPeriodisert =
                GrunnlagBeregning(
                    utgift = utgiftsbeløp,
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektSBListe = inntektSBListe,
                    sjablonListe = sjablonliste,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    periode = beregningsperiode,
                    resultat = bPsAndelSærbidragBeregning.beregn(beregnBPsAndelSærbidragGrunnlagPeriodisert),
                    grunnlag = beregnBPsAndelSærbidragGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til BPsAndelSærbidragsberegning er gyldige
    fun validerInput(grunnlag: BeregnBPsAndelSærbidragGrunnlag): List<Avvik> {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil
            ).toMutableList()

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "utgiftPeriodeListe",
                periodeListe = grunnlag.utgiftPeriodeListe.map { it.getPeriode() },
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
                dataElement = "inntektBPPeriodeListe",
                periodeListe = grunnlag.inntektBPPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "inntektBMPeriodeListe",
                periodeListe = grunnlag.inntektBMPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "inntektSBPeriodeListe",
                periodeListe = grunnlag.inntektSBPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "sjablonPeriodeListe",
                periodeListe = grunnlag.sjablonPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = false,
            ),
        )

        return avvikListe
    }
}
