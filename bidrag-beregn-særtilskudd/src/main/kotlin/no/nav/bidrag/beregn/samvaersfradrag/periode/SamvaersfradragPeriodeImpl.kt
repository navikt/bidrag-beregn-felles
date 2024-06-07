package no.nav.bidrag.beregn.samvaersfradrag.periode

import no.nav.bidrag.beregn.felles.FellesPeriode
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.periode.Periodiserer
import no.nav.bidrag.beregn.felles.util.PeriodeUtil
import no.nav.bidrag.beregn.samvaersfradrag.beregning.SamvaersfradragBeregning
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragGrunnlag
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragListeGrunnlag
import no.nav.bidrag.beregn.samvaersfradrag.bo.BeregnSamvaersfradragResultat
import no.nav.bidrag.beregn.samvaersfradrag.bo.GrunnlagBeregningPeriodisert
import no.nav.bidrag.beregn.samvaersfradrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.samvaersfradrag.bo.SamvaersfradragGrunnlagPerBarn
import no.nav.bidrag.beregn.samvaersfradrag.bo.SamvaersfradragGrunnlagPeriode
import java.time.Period

class SamvaersfradragPeriodeImpl(private val samvaersfradragBeregning: SamvaersfradragBeregning) : FellesPeriode(), SamvaersfradragPeriode {

    override fun beregnPerioder(grunnlag: BeregnSamvaersfradragGrunnlag): BeregnSamvaersfradragResultat {
        val beregnSamvaersfradragListeGrunnlag = BeregnSamvaersfradragListeGrunnlag()

        // Juster datoer
        justerDatoerGrunnlagslister(periodeGrunnlag = grunnlag, beregnSamvaersfradragListeGrunnlag = beregnSamvaersfradragListeGrunnlag)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, beregnSamvaersfradragListeGrunnlag = beregnSamvaersfradragListeGrunnlag)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = beregnSamvaersfradragListeGrunnlag.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnSamvaersfradragPerPeriode(beregnSamvaersfradragListeGrunnlag)

        return BeregnSamvaersfradragResultat(beregnSamvaersfradragListeGrunnlag.periodeResultatListe)
    }

    private fun justerDatoerGrunnlagslister(
        periodeGrunnlag: BeregnSamvaersfradragGrunnlag,
        beregnSamvaersfradragListeGrunnlag: BeregnSamvaersfradragListeGrunnlag,
    ) {
        // Justerer datoer på grunnlagslistene (blir gjort implisitt i xxxPeriode::new)
        beregnSamvaersfradragListeGrunnlag.justertSamvaersfradragPeriodeListe = periodeGrunnlag.samvaersfradragGrunnlagPeriodeListe
            .map { SamvaersfradragGrunnlagPeriode(it) }

        beregnSamvaersfradragListeGrunnlag.justertSjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe
            .map { SjablonPeriode(it) }
    }

    // Lagger bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(
        periodeGrunnlag: BeregnSamvaersfradragGrunnlag,
        beregnSamvaersfradragListeGrunnlag: BeregnSamvaersfradragListeGrunnlag,
    ) {
        // Bygger opp liste over perioder
        beregnSamvaersfradragListeGrunnlag.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .addBruddpunkter(beregnSamvaersfradragListeGrunnlag.justertSamvaersfradragPeriodeListe)
            .addBruddpunkter(beregnSamvaersfradragListeGrunnlag.justertSjablonPeriodeListe)
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnSamvaersfradragPerPeriode(grunnlag: BeregnSamvaersfradragListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val samvaersfradragGrunnnlagPerBarnliste = grunnlag.justertSamvaersfradragPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    SamvaersfradragGrunnlagPerBarn(
                        referanse = it.referanse,
                        barnPersonId = it.barnPersonId,
                        barnAlder = Period.between(it.barnFodselsdato, beregningsperiode.datoFom).years,
                        samvaersklasse = it.samvaersklasse,
                    )
                }

            val sjablonliste = grunnlag.justertSjablonPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnSamvaersfradragGrunnlagPeriodisert =
                GrunnlagBeregningPeriodisert(
                    samvaersfradragGrunnlagPerBarnListe = samvaersfradragGrunnnlagPerBarnliste,
                    sjablonListe = sjablonliste,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    resultatDatoFraTil = beregningsperiode,
                    resultatBeregningListe = samvaersfradragBeregning.beregn(beregnSamvaersfradragGrunnlagPeriodisert),
                    resultatGrunnlag = beregnSamvaersfradragGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til samvaersfradragberegning er gyldige
    override fun validerInput(grunnlag: BeregnSamvaersfradragGrunnlag): List<Avvik> {
        val avvikListe = PeriodeUtil.validerBeregnPeriodeInput(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil).toMutableList()

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "samvaersfradragGrunnlagPeriodeListe",
                periodeListe = grunnlag.samvaersfradragGrunnlagPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = true,
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
