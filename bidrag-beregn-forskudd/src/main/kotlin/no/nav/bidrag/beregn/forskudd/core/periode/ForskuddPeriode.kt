package no.nav.bidrag.beregn.forskudd.core.periode

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.beregn.core.util.PeriodeUtil
import no.nav.bidrag.beregn.forskudd.core.ForskuddBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Alder
import no.nav.bidrag.beregn.forskudd.core.bo.AlderPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.BarnIHusstanden
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddGrunnlag
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddResultat
import no.nav.bidrag.beregn.forskudd.core.bo.Bostatus
import no.nav.bidrag.beregn.forskudd.core.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.GrunnlagTilBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Inntekt
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.Sivilstand
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.firstDayOfNextMonth

internal class ForskuddPeriode(private val forskuddBeregning: ForskuddBeregning = ForskuddBeregning()) {
    fun beregnPerioder(grunnlag: BeregnForskuddGrunnlag): BeregnForskuddResultat {
        val grunnlagTilBeregning = GrunnlagTilBeregning()

        // Lag grunnlag til beregning
        lagGrunnlagTilBeregning(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, grunnlagTilBeregning = grunnlagTilBeregning)

        // Foreta beregning
        beregnForskuddPerPeriode(grunnlagTilBeregning)

        return BeregnForskuddResultat(grunnlagTilBeregning.periodeResultatListe)
    }

    // Lager grunnlag til beregning
    private fun lagGrunnlagTilBeregning(periodeGrunnlag: BeregnForskuddGrunnlag, grunnlagTilBeregning: GrunnlagTilBeregning) {
        grunnlagTilBeregning.inntektPeriodeListe = periodeGrunnlag.inntektPeriodeListe.map { it }
        grunnlagTilBeregning.sivilstandPeriodeListe = periodeGrunnlag.sivilstandPeriodeListe.map { it }
        grunnlagTilBeregning.barnIHusstandenPeriodeListe = periodeGrunnlag.barnIHusstandenPeriodeListe.map { it }
        grunnlagTilBeregning.bostatusPeriodeListe = periodeGrunnlag.bostatusPeriodeListe.map { it }

        grunnlagTilBeregning.alderPeriodeListe =
            settBarnAlderPerioder(
                fødselsdato = periodeGrunnlag.søknadsbarn.fødselsdato,
                beregnDatoFra = periodeGrunnlag.beregnDatoFra,
                beregnDatoTil = periodeGrunnlag.beregnDatoTil,
            )
                .map { AlderPeriode(referanse = periodeGrunnlag.søknadsbarn.referanse, alderPeriode = it.alderPeriode, alder = it.alder) }

        grunnlagTilBeregning.sjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe.map { it }
    }

    // Lager bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(periodeGrunnlag: BeregnForskuddGrunnlag, grunnlagTilBeregning: GrunnlagTilBeregning) {
        // Bygger opp liste over perioder, basert på alle typer inputparametre
        grunnlagTilBeregning.bruddPeriodeListe =
            Periodiserer()
                .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start beregning fra-dato
                .addBruddpunkter(grunnlagTilBeregning.inntektPeriodeListe)
                .addBruddpunkter(grunnlagTilBeregning.sivilstandPeriodeListe)
                .addBruddpunkter(grunnlagTilBeregning.barnIHusstandenPeriodeListe)
                .addBruddpunkter(grunnlagTilBeregning.bostatusPeriodeListe)
                .addBruddpunkter(grunnlagTilBeregning.alderPeriodeListe)
                .addBruddpunkter(grunnlagTilBeregning.sjablonPeriodeListe)
                .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start beregning til-dato
                .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
                .toMutableList()

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        val bruddPeriodeListeAntallElementer = grunnlagTilBeregning.bruddPeriodeListe.size
        if (bruddPeriodeListeAntallElementer > 1) {
            val nestSisteTilDato = grunnlagTilBeregning.bruddPeriodeListe[bruddPeriodeListeAntallElementer - 2].datoTil
            val sisteTilDato = grunnlagTilBeregning.bruddPeriodeListe[bruddPeriodeListeAntallElementer - 1].datoTil
            if (periodeGrunnlag.beregnDatoTil == nestSisteTilDato && null == sisteTilDato) {
                val nyPeriode =
                    Periode(
                        datoFom = grunnlagTilBeregning.bruddPeriodeListe[bruddPeriodeListeAntallElementer - 2].datoFom,
                        datoTil = null,
                    )
                grunnlagTilBeregning.bruddPeriodeListe.removeAt(bruddPeriodeListeAntallElementer - 1)
                grunnlagTilBeregning.bruddPeriodeListe.removeAt(bruddPeriodeListeAntallElementer - 2)
                grunnlagTilBeregning.bruddPeriodeListe.add(nyPeriode)
            }
        }
    }

    // Løper gjennom alle bruddperioder og foretar beregning
    private fun beregnForskuddPerPeriode(grunnlagTilBeregning: GrunnlagTilBeregning) {
        // Løper gjennom periodene og finner matchende verdi for hver kategori
        // Kaller beregningsmodulen for hver beregningsperiode

        grunnlagTilBeregning.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val inntektListe =
                grunnlagTilBeregning.inntektPeriodeListe
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { Inntekt(referanse = it.referanse, type = it.type, beløp = it.beløp) }

            val sivilstand =
                grunnlagTilBeregning.sivilstandPeriodeListe.stream()
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { Sivilstand(referanse = it.referanse, kode = it.kode) }
                    .findFirst()
                    .orElseThrow {
                        IllegalArgumentException(
                            "Grunnlagsobjekt SIVILSTAND mangler data for periode: ${beregningsperiode.getPeriode()}",
                        )
                    }

            val alder =
                grunnlagTilBeregning.alderPeriodeListe.stream()
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { Alder(referanse = it.referanse, alder = it.alder) }
                    .findFirst()
                    .orElseThrow {
                        IllegalArgumentException(
                            "Ikke mulig å beregne søknadsbarnets alder for periode: ${beregningsperiode.getPeriode()}",
                        )
                    }

            val bostatus =
                grunnlagTilBeregning.bostatusPeriodeListe.stream()
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { Bostatus(referanse = it.referanse, kode = it.kode) }
                    .findFirst()
                    .orElseThrow {
                        IllegalArgumentException(
                            "Grunnlagsobjekt BOSTATUS mangler data for periode: ${beregningsperiode.getPeriode()}",
                        )
                    }

            val barnIHusstandenListe =
                grunnlagTilBeregning.barnIHusstandenPeriodeListe
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                    .map { BarnIHusstanden(referanse = it.referanse, antall = it.antall) }

            val sjablonListe =
                grunnlagTilBeregning.sjablonPeriodeListe
                    .filter { it.getPeriode().overlapperMed(beregningsperiode) }

            val grunnlagBeregning =
                GrunnlagBeregning(
                    inntektListe = inntektListe,
                    sivilstand = sivilstand,
                    barnIHusstandenListe = barnIHusstandenListe,
                    søknadsbarnAlder = alder,
                    søknadsbarnBostatus = bostatus,
                    sjablonListe = sjablonListe,
                )

            grunnlagTilBeregning.periodeResultatListe
                .add(
                    ResultatPeriode(
                        periode = beregningsperiode,
                        resultat = forskuddBeregning.beregn(grunnlagBeregning),
                        grunnlag = grunnlagBeregning,
                    ),
                )
        }
    }

    // Deler opp i aldersperioder med utgangspunkt i fødselsdato
    private fun settBarnAlderPerioder(fødselsdato: LocalDate, beregnDatoFra: LocalDate, beregnDatoTil: LocalDate): List<AlderPeriode> {
        val bruddAlderListe = ArrayList<AlderPeriode>()
        val barn11ÅrDato = fødselsdato.plusYears(11).with(firstDayOfMonth())
        val barn18ÅrDato = fødselsdato.plusYears(18).with(firstDayOfNextMonth())

        var alderStartPeriode = 0
        if (!barn11ÅrDato.isAfter(beregnDatoFra)) {
            alderStartPeriode = if (!barn18ÅrDato.isAfter(beregnDatoFra)) 18 else 11
        }

        // Barn fyller 11 år i perioden
        val barn11ÅrIPerioden = barn11ÅrDato.isAfter(beregnDatoFra.minusDays(1)) && barn11ÅrDato.isBefore(beregnDatoTil.plusDays(1))

        // Barn fyller 18 år i perioden
        val barn18ÅrIPerioden = barn18ÅrDato.isAfter(beregnDatoFra.minusDays(1)) && barn18ÅrDato.isBefore(beregnDatoTil.plusDays(1))
        if (barn11ÅrIPerioden) {
            bruddAlderListe.add(
                AlderPeriode(
                    referanse = "",
                    alderPeriode = Periode(datoFom = beregnDatoFra.with(firstDayOfMonth()), datoTil = barn11ÅrDato.with(firstDayOfMonth())),
                    alder = 0,
                ),
            )
            if (barn18ÅrIPerioden) {
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = barn11ÅrDato.with(firstDayOfMonth()), datoTil = barn18ÅrDato.with(firstDayOfMonth())),
                        alder = 11,
                    ),
                )
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = barn18ÅrDato.with(firstDayOfMonth()), datoTil = null),
                        alder = 18,
                    ),
                )
            } else {
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = barn11ÅrDato.with(firstDayOfMonth()), datoTil = null),
                        alder = 11,
                    ),
                )
            }
        } else {
            if (barn18ÅrIPerioden) {
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = beregnDatoFra.with(firstDayOfMonth()), datoTil = barn18ÅrDato.with(firstDayOfMonth())),
                        alder = 11,
                    ),
                )
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = barn18ÅrDato.with(firstDayOfMonth()), datoTil = null),
                        alder = 18,
                    ),
                )
            } else {
                bruddAlderListe.add(
                    AlderPeriode(
                        referanse = "",
                        alderPeriode = Periode(datoFom = beregnDatoFra.with(firstDayOfMonth()), datoTil = null),
                        alder = alderStartPeriode,
                    ),
                )
            }
        }
        return bruddAlderListe
    }

    // Validerer at input-verdier til forskuddsberegning er gyldige
    fun validerInput(grunnlag: BeregnForskuddGrunnlag): List<Avvik> {
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
                dataElement = "bidragMottakerInntektPeriodeListe",
                periodeListe = grunnlag.inntektPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = false,
            ),
        )

        // Sjekk perioder for sivilstand
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "bidragMottakerSivilstandPeriodeListe",
                periodeListe = grunnlag.sivilstandPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        // Sjekk perioder for bostatus
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "søknadsbarnBostatusPeriodeListe",
                periodeListe = grunnlag.bostatusPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        // Sjekk perioder for barn
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "bidragMottakerBarnPeriodeListe",
                periodeListe = grunnlag.barnIHusstandenPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = false,
            ),
        )

        // Sjekk perioder for sjablonliste
        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "sjablonPeriodeListe",
                periodeListe = grunnlag.sjablonPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        return avvikListe
    }
}
