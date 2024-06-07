package no.nav.bidrag.beregn.bpsandelsaertilskudd.periode

import no.nav.bidrag.beregn.bpsandelsaertilskudd.beregning.BPsAndelSaertilskuddBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddGrunnlag
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddListeGrunnlag
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.Inntekt
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.InntektPeriode
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.NettoSaertilskuddPeriode
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.felles.FellesPeriode
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.inntekt.InntektPeriodeGrunnlagUtenInntektType
import no.nav.bidrag.beregn.felles.periode.Periodiserer
import no.nav.bidrag.beregn.felles.util.InntektUtil.behandlUtvidetBarnetrygd
import no.nav.bidrag.beregn.felles.util.PeriodeUtil
import java.time.LocalDate

class BPsAndelSaertilskuddPeriodeImpl(private val bPsAndelSaertilskuddBeregning: BPsAndelSaertilskuddBeregning) :
    FellesPeriode(), BPsAndelSaertilskuddPeriode {

    override fun beregnPerioder(grunnlag: BeregnBPsAndelSaertilskuddGrunnlag): BeregnBPsAndelSaertilskuddResultat {
        val beregnBPsAndelSaertilskuddListeGrunnlag = BeregnBPsAndelSaertilskuddListeGrunnlag()

        // Juster datoer
        justerDatoerGrunnlagslister(periodeGrunnlag = grunnlag, beregnBPsAndelSaertilskuddListeGrunnlag = beregnBPsAndelSaertilskuddListeGrunnlag)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, beregnBPsAndelSaertilskuddListeGrunnlag = beregnBPsAndelSaertilskuddListeGrunnlag)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = beregnBPsAndelSaertilskuddListeGrunnlag.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnBPsAndelSaertilskuddPerPeriode(beregnBPsAndelSaertilskuddListeGrunnlag)

        return BeregnBPsAndelSaertilskuddResultat(beregnBPsAndelSaertilskuddListeGrunnlag.periodeResultatListe)
    }

    private fun justerDatoerGrunnlagslister(
        periodeGrunnlag: BeregnBPsAndelSaertilskuddGrunnlag,
        beregnBPsAndelSaertilskuddListeGrunnlag: BeregnBPsAndelSaertilskuddListeGrunnlag,
    ) {
        // Justerer datoer på grunnlagslistene (blir gjort implisitt i xxxPeriode(it))
        beregnBPsAndelSaertilskuddListeGrunnlag.justertSjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe
            .map { SjablonPeriode(it) }

        beregnBPsAndelSaertilskuddListeGrunnlag.justertNettoSaertilskuddPeriodeListe = periodeGrunnlag.nettoSaertilskuddPeriodeListe
            .map { NettoSaertilskuddPeriode(it) }

        beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBPPeriodeListe = periodeGrunnlag.inntektBPPeriodeListe
            .map { InntektPeriode(it) }

        beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBMPeriodeListe = behandlUtvidetBarnetrygd(
            inntektPeriodeListe = periodeGrunnlag.inntektBMPeriodeListe,
            sjablonPeriodeListe = beregnBPsAndelSaertilskuddListeGrunnlag.justertSjablonPeriodeListe,
        )
            .map { InntektPeriode(it) }

        beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBBPeriodeListe = periodeGrunnlag.inntektBBPeriodeListe
            .map { InntektPeriode(it) }
    }

    // Lagger bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(
        periodeGrunnlag: BeregnBPsAndelSaertilskuddGrunnlag,
        beregnBPsAndelSaertilskuddListeGrunnlag: BeregnBPsAndelSaertilskuddListeGrunnlag,
    ) {
        // Regler for beregning av BPs andel ble endret fra 01.01.2009, alle perioder etter da skal beregnes på ny måte.
        // Det må derfor legges til brudd på denne datoen
        val datoRegelendringer = listOf(Periode(datoFom = LocalDate.parse("2009-01-01"), datoTil = LocalDate.parse("2009-01-01")))

        // Bygger opp liste over perioder
        beregnBPsAndelSaertilskuddListeGrunnlag.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .addBruddpunkter(beregnBPsAndelSaertilskuddListeGrunnlag.justertNettoSaertilskuddPeriodeListe)
            .addBruddpunkter(beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBPPeriodeListe)
            .addBruddpunkter(beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBMPeriodeListe)
            .addBruddpunkter(beregnBPsAndelSaertilskuddListeGrunnlag.justertInntektBBPeriodeListe)
            .addBruddpunkter(beregnBPsAndelSaertilskuddListeGrunnlag.justertSjablonPeriodeListe)
            .addBruddpunkter(datoRegelendringer)
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnBPsAndelSaertilskuddPerPeriode(grunnlag: BeregnBPsAndelSaertilskuddListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val nettoSaertilskuddBelop = grunnlag.justertNettoSaertilskuddPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { it.nettoSaertilskuddBelop }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt NETTO_SAERTILSKUDD mangler data for periode: $beregningsperiode") }

            val inntektBPListe = grunnlag.justertInntektBPPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    Inntekt(
                        referanse = it.referanse,
                        inntektType = it.inntektType,
                        inntektBelop = it.inntektBelop,
                        deltFordel = false,
                        skatteklasse2 = false,
                    )
                }

            val inntektBMListe = grunnlag.justertInntektBMPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    Inntekt(
                        referanse = it.referanse,
                        inntektType = it.inntektType,
                        inntektBelop = it.inntektBelop,
                        deltFordel = it.deltFordel,
                        skatteklasse2 = it.skatteklasse2,
                    )
                }

            val inntektBBListe = grunnlag.justertInntektBBPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    Inntekt(
                        referanse = it.referanse,
                        inntektType = it.inntektType,
                        inntektBelop = it.inntektBelop,
                        deltFordel = false,
                        skatteklasse2 = false,
                    )
                }

            val sjablonliste = grunnlag.justertSjablonPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnBPsAndelSaertilskuddGrunnlagPeriodisert =
                GrunnlagBeregning(
                    nettoSaertilskuddBelop = nettoSaertilskuddBelop,
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektBBListe = inntektBBListe,
                    sjablonListe = sjablonliste,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    resultatDatoFraTil = beregningsperiode,
                    resultatBeregning = bPsAndelSaertilskuddBeregning.beregn(beregnBPsAndelSaertilskuddGrunnlagPeriodisert),
                    resultatGrunnlagBeregning = beregnBPsAndelSaertilskuddGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Sjekker om det skal legges til inntekt for fordel særfradrag enslig forsørger og skatteklasse 2 (kun BM)
    private fun behandlUtvidetBarnetrygd(inntektPeriodeListe: List<InntektPeriode>, sjablonPeriodeListe: List<SjablonPeriode>): List<InntektPeriode> {
        if (inntektPeriodeListe.isEmpty()) {
            return inntektPeriodeListe
        }

        val justertInntektPeriodeListe = behandlUtvidetBarnetrygd(
            inntektPeriodeGrunnlagListe = inntektPeriodeListe
                .map {
                    InntektPeriodeGrunnlagUtenInntektType(
                        referanse = it.referanse,
                        inntektPeriode = it.getPeriode(),
                        type = it.inntektType,
                        belop = it.inntektBelop,
                        deltFordel = it.deltFordel,
                        skatteklasse2 = it.skatteklasse2,
                    )
                },
            sjablonPeriodeListe = sjablonPeriodeListe,
        )

        return justertInntektPeriodeListe
            .map {
                InntektPeriode(
                    referanse = it.referanse,
                    periodeDatoFraTil = it.getPeriode(),
                    inntektType = it.type,
                    inntektBelop = it.belop,
                    deltFordel = it.deltFordel,
                    skatteklasse2 = it.skatteklasse2,
                )
            }
            .sortedBy { it.periodeDatoFraTil.datoFom }
    }

    // Validerer at input-verdier til BPsAndelSaertilskuddsberegning er gyldige
    override fun validerInput(grunnlag: BeregnBPsAndelSaertilskuddGrunnlag): List<Avvik> {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(beregnDatoFom = grunnlag.beregnDatoFra, beregnDatoTil = grunnlag.beregnDatoTil).toMutableList()

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "nettoSaertilskuddPeriodeListe",
                periodeListe = grunnlag.nettoSaertilskuddPeriodeListe.map { it.getPeriode() },
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
                dataElement = "inntektBBPeriodeListe",
                periodeListe = grunnlag.inntektBBPeriodeListe.map { it.getPeriode() },
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
