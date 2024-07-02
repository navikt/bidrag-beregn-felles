package no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.periode

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.beregn.core.util.PeriodeUtil
import no.nav.bidrag.beregn.særtilskudd.core.felles.FellesPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.beregning.SaertilskuddBeregning
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskudd
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BeregnSaertilskuddListeGrunnlag
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BeregnSaertilskuddResultat
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.Bidragsevne
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidrag
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlag

class SaertilskuddPeriode(private val saertilskuddberegning: SaertilskuddBeregning = SaertilskuddBeregning()) : FellesPeriode() {

    fun beregnPerioder(grunnlag: BeregnSaertilskuddGrunnlag): BeregnSaertilskuddResultat {
        val beregnSaertilskuddListeGrunnlag = BeregnSaertilskuddListeGrunnlag()

        // Juster datoer
        justerDatoerGrunnlagslister(periodeGrunnlag = grunnlag, beregnSaertilskuddListeGrunnlag = beregnSaertilskuddListeGrunnlag)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, beregnSaertilskuddListeGrunnlag = beregnSaertilskuddListeGrunnlag)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = beregnSaertilskuddListeGrunnlag.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnSaertilskuddPerPeriode(periodeGrunnlag = grunnlag, grunnlag = beregnSaertilskuddListeGrunnlag)

        return BeregnSaertilskuddResultat(beregnSaertilskuddListeGrunnlag.periodeResultatListe)
    }

    private fun justerDatoerGrunnlagslister(
        periodeGrunnlag: BeregnSaertilskuddGrunnlag,
        beregnSaertilskuddListeGrunnlag: BeregnSaertilskuddListeGrunnlag,
    ) {
        // Justerer datoer på grunnlagslistene (blir gjort implisitt i xxxPeriode(it))
        beregnSaertilskuddListeGrunnlag.justertBidragsevnePeriodeListe = periodeGrunnlag.bidragsevnePeriodeListe
            .map { no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode(it) }

        beregnSaertilskuddListeGrunnlag.justertBPsAndelSaertilskuddPeriodeListe = periodeGrunnlag.bPsAndelSaertilskuddPeriodeListe
            .map { no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode(it) }

        beregnSaertilskuddListeGrunnlag.justertLopendeBidragPeriodeListe = periodeGrunnlag.lopendeBidragPeriodeListe
            .map { no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode(it) }

        beregnSaertilskuddListeGrunnlag.justertSamvaersfradragPeriodeListe = periodeGrunnlag.samvaersfradragGrunnlagPeriodeListe
            .map { no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode(it) }
    }

    // Lagger bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(periodeGrunnlag: BeregnSaertilskuddGrunnlag, beregnSaertilskuddListeGrunnlag: BeregnSaertilskuddListeGrunnlag) {
        // Bygger opp liste over perioder
        beregnSaertilskuddListeGrunnlag.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .addBruddpunkter(beregnSaertilskuddListeGrunnlag.justertBidragsevnePeriodeListe)
            .addBruddpunkter(beregnSaertilskuddListeGrunnlag.justertBPsAndelSaertilskuddPeriodeListe)
            .addBruddpunkter(beregnSaertilskuddListeGrunnlag.justertLopendeBidragPeriodeListe)
            .addBruddpunkter(beregnSaertilskuddListeGrunnlag.justertSamvaersfradragPeriodeListe)
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnSaertilskuddPerPeriode(periodeGrunnlag: BeregnSaertilskuddGrunnlag, grunnlag: BeregnSaertilskuddListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val bidragsevne = grunnlag.justertBidragsevnePeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Bidragsevne(referanse = it.referanse, bidragsevneBelop = it.bidragsevneBelop) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt BIDRAGSEVNE mangler data for periode: ${beregningsperiode.getPeriode()}") }

            val bPsAndelSaertilskudd = grunnlag.justertBPsAndelSaertilskuddPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    BPsAndelSaertilskudd(
                        referanse = it.referanse,
                        bPsAndelSaertilskuddProsent = it.bPsAndelSaertilskuddProsent,
                        bPsAndelSaertilskuddBelop = it.bPsAndelSaertilskuddBelop,
                        barnetErSelvforsorget = it.barnetErSelvforsorget,
                    )
                }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "Grunnlagsobjekt BP_ANDEL_SAERTILSKUDD mangler data for periode: ${beregningsperiode.getPeriode()}",
                    )
                }

            val lopendeBidragListe = grunnlag.justertLopendeBidragPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    LopendeBidrag(
                        referanse = it.referanse,
                        barnPersonId = it.barnPersonId,
                        lopendeBidragBelop = it.lopendeBidragBelop,
                        opprinneligBPsAndelUnderholdskostnadBelop = it.opprinneligBPsAndelUnderholdskostnadBelop,
                        opprinneligBidragBelop = it.opprinneligBidragBelop,
                        opprinneligSamvaersfradragBelop = it.opprinneligSamvaersfradragBelop,
                    )
                }

            val samvaersfradragGrunnlagListe = grunnlag.justertSamvaersfradragPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map {
                    SamvaersfradragGrunnlag(
                        referanse = it.referanse,
                        barnPersonId = it.barnPersonId,
                        samvaersfradragBelop = it.samvaersfradragBelop,
                    )
                }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnSaertilskuddGrunnlagPeriodisert =
                GrunnlagBeregning(
                    bidragsevne = bidragsevne,
                    bPsAndelSaertilskudd = bPsAndelSaertilskudd,
                    lopendeBidragListe = lopendeBidragListe,
                    samvaersfradragGrunnlagListe = samvaersfradragGrunnlagListe,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    periode = beregningsperiode,
                    soknadsbarnPersonId = periodeGrunnlag.soknadsbarnPersonId,
                    resultat = saertilskuddberegning.beregn(beregnSaertilskuddGrunnlagPeriodisert),
                    grunnlag = beregnSaertilskuddGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til særtilskuddberegning er gyldige
    fun validerInput(grunnlag: BeregnSaertilskuddGrunnlag): List<Avvik> {
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
                dataElement = "bPsAndelSaertilskuddPeriodeListe",
                periodeListe = grunnlag.bPsAndelSaertilskuddPeriodeListe.map { it.getPeriode() },
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
                dataElement = "lopendeBidragPeriodeListe",
                periodeListe = grunnlag.lopendeBidragPeriodeListe.map { it.getPeriode() },
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
                dataElement = "samvaersfradragGrunnlagPeriodeListe",
                periodeListe = grunnlag.samvaersfradragGrunnlagPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        return avvikListe
    }
}
