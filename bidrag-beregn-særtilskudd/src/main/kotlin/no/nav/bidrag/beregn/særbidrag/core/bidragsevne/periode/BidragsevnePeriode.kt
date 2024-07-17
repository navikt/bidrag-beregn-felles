package no.nav.bidrag.beregn.særbidrag.core.bidragsevne.periode

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.beregn.core.util.PeriodeUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.AntallBarnIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneListeGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesPeriode

class BidragsevnePeriode(private val bidragsevneberegning: BidragsevneBeregning = BidragsevneBeregning()) : FellesPeriode() {

    fun beregnPerioder(grunnlag: BeregnBidragsevneGrunnlag): BeregnBidragsevneResultat {
        val grunnlagTilBeregning = BeregnBidragsevneListeGrunnlag()

        // Lag grunnlag til beregning
        lagGrunnlagTilBeregning(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = grunnlagTilBeregning.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil, åpenSluttperiode = false)

        // Foreta beregning
        beregnBidragsevnePerPeriode(grunnlagTilBeregning)

        return BeregnBidragsevneResultat(grunnlagTilBeregning.periodeResultatListe)
    }

    // Lager grunnlag til beregning
    private fun lagGrunnlagTilBeregning(periodeGrunnlag: BeregnBidragsevneGrunnlag, grunnlagTilBeregning: BeregnBidragsevneListeGrunnlag) {
        grunnlagTilBeregning.inntektPeriodeListe = periodeGrunnlag.inntektPeriodeListe.map { it }
        grunnlagTilBeregning.barnIHusstandPeriodeListe = periodeGrunnlag.barnIHusstandPeriodeListe.map { it }
        grunnlagTilBeregning.voksneIHusstandPeriodeListe = periodeGrunnlag.voksneIHusstandPeriodeListe.map { it }
        grunnlagTilBeregning.sjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe.map { it }
    }

    // Lager bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(periodeGrunnlag: BeregnBidragsevneGrunnlag, grunnlagTilBeregning: BeregnBidragsevneListeGrunnlag) {
        // Bygger opp liste over perioder
        grunnlagTilBeregning.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkter(grunnlagTilBeregning.inntektPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.barnIHusstandPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.voksneIHusstandPeriodeListe)
            .addBruddpunkter(grunnlagTilBeregning.sjablonPeriodeListe)
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnBidragsevnePerPeriode(grunnlagTilBeregning: BeregnBidragsevneListeGrunnlag) {
        grunnlagTilBeregning.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val inntekt =
                grunnlagTilBeregning.inntektPeriodeListe
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { Inntekt(referanse = it.referanse, inntektBeløp = it.beløp) }
                    .firstOrNull()

            val antallBarnIHusstand =
                grunnlagTilBeregning.barnIHusstandPeriodeListe.stream()
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { AntallBarnIHusstand(referanse = it.referanse, antallBarn = it.antall) }
                    .findFirst()
                    .orElseThrow {
                        IllegalArgumentException("Antall barn i husstand mangler data for periode: ${beregningsperiode.getPeriode()}")
                    }

            val bostatusVoksneIHusstand =
                grunnlagTilBeregning.voksneIHusstandPeriodeListe.stream()
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { BostatusVoksneIHusstand(referanse = it.referanse, borMedAndre = it.borMedAndre) }
                    .findFirst()
                    .orElseThrow {
                        IllegalArgumentException("Bostatus voksne i husstand mangler data for periode: ${beregningsperiode.getPeriode()}")
                    }

            val sjablonliste = grunnlagTilBeregning.sjablonPeriodeListe.filter { it.getPeriode().overlapperMed(beregningsperiode) }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnBidragsevneGrunnlagPeriodisert =
                GrunnlagBeregning(
                    inntekt = inntekt,
                    antallBarnIHusstand = antallBarnIHusstand,
                    bostatusVoksneIHusstand = bostatusVoksneIHusstand,
                    sjablonListe = sjablonliste,
                )

            grunnlagTilBeregning.periodeResultatListe.add(
                ResultatPeriode(
                    periode = beregningsperiode,
                    resultat = bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert),
                    grunnlag = beregnBidragsevneGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til bidragsevneberegning er gyldige
    fun validerInput(grunnlag: BeregnBidragsevneGrunnlag): List<Avvik> {
        // Sjekk beregn dato fra/til
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
            ).toMutableList()

        // Sjekk perioder for inntekt
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "inntektPeriodeListe",
                periodeListe = grunnlag.inntektPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        // Sjekk perioder for antall barn i husstand
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "barnIHusstandPeriodeListe",
                periodeListe = grunnlag.barnIHusstandPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        // Sjekk perioder for bostatus voksne i husstand
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "voksneIHusstandPeriodeListe",
                periodeListe = grunnlag.voksneIHusstandPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        // Sjekk perioder for sjabloner
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
