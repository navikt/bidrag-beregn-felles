package no.nav.bidrag.beregn.bidragsevne.periode

import no.nav.bidrag.beregn.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.bidragsevne.bo.BarnIHusstand
import no.nav.bidrag.beregn.bidragsevne.bo.BarnIHustandPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneListeGrunnlag
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.bidragsevne.bo.Bostatus
import no.nav.bidrag.beregn.bidragsevne.bo.BostatusPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bidragsevne.bo.Inntekt
import no.nav.bidrag.beregn.bidragsevne.bo.InntektPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.Saerfradrag
import no.nav.bidrag.beregn.bidragsevne.bo.SaerfradragPeriode
import no.nav.bidrag.beregn.bidragsevne.bo.Skatteklasse
import no.nav.bidrag.beregn.bidragsevne.bo.SkatteklassePeriode
import no.nav.bidrag.beregn.felles.FellesPeriode
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.periode.Periodiserer
import no.nav.bidrag.beregn.felles.util.PeriodeUtil

class BidragsevnePeriodeImpl(private val bidragsevneberegning: BidragsevneBeregning) : FellesPeriode(), BidragsevnePeriode {

    override fun beregnPerioder(grunnlag: BeregnBidragsevneGrunnlag): BeregnBidragsevneResultat {
        val beregnBidragsevneListeGrunnlag = BeregnBidragsevneListeGrunnlag()

        // Juster datoer
        justerDatoerGrunnlagslister(periodeGrunnlag = grunnlag, beregnBidragsevneListeGrunnlag = beregnBidragsevneListeGrunnlag)

        // Lag bruddperioder
        lagBruddperioder(periodeGrunnlag = grunnlag, beregnBidragsevneListeGrunnlag = beregnBidragsevneListeGrunnlag)

        // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
        mergeSluttperiode(periodeListe = beregnBidragsevneListeGrunnlag.bruddPeriodeListe, datoTil = grunnlag.beregnDatoTil)

        // Foreta beregning
        beregnBidragsevnePerPeriode(beregnBidragsevneListeGrunnlag)

        return BeregnBidragsevneResultat(beregnBidragsevneListeGrunnlag.periodeResultatListe)
    }

    private fun justerDatoerGrunnlagslister(
        periodeGrunnlag: BeregnBidragsevneGrunnlag,
        beregnBidragsevneListeGrunnlag: BeregnBidragsevneListeGrunnlag,
    ) {
        // Justerer datoer på grunnlagslistene (blir gjort implisitt i xxxPeriode(it))
        beregnBidragsevneListeGrunnlag.justertInntektPeriodeListe = periodeGrunnlag.inntektPeriodeListe
            .map { InntektPeriode(it) }

        beregnBidragsevneListeGrunnlag.justertSkatteklassePeriodeListe = periodeGrunnlag.skatteklassePeriodeListe
            .map { SkatteklassePeriode(it) }

        beregnBidragsevneListeGrunnlag.justertBostatusPeriodeListe = periodeGrunnlag.bostatusPeriodeListe
            .map { BostatusPeriode(it) }

        beregnBidragsevneListeGrunnlag.justertBarnIHusstandenPeriodeListe = periodeGrunnlag.antallBarnIEgetHusholdPeriodeListe
            .map { BarnIHustandPeriode(it) }

        beregnBidragsevneListeGrunnlag.justertSaerfradragPeriodeListe = periodeGrunnlag.saerfradragPeriodeListe
            .map { SaerfradragPeriode(it) }

        beregnBidragsevneListeGrunnlag.justertSjablonPeriodeListe = periodeGrunnlag.sjablonPeriodeListe
            .map { SjablonPeriode(it) }
    }

    // Lagger bruddperioder ved å løpe gjennom alle periodelistene
    private fun lagBruddperioder(periodeGrunnlag: BeregnBidragsevneGrunnlag, beregnBidragsevneListeGrunnlag: BeregnBidragsevneListeGrunnlag) {
        // Bygger opp liste over perioder
        beregnBidragsevneListeGrunnlag.bruddPeriodeListe = Periodiserer()
            .addBruddpunkt(periodeGrunnlag.beregnDatoFra) // For å sikre bruddpunkt på start-beregning-fra-dato
            .addBruddpunkt(periodeGrunnlag.beregnDatoTil) // For å sikre bruddpunkt på start-beregning-til-dato
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertInntektPeriodeListe)
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertSkatteklassePeriodeListe)
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertBostatusPeriodeListe)
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertBarnIHusstandenPeriodeListe)
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertSaerfradragPeriodeListe)
            .addBruddpunkter(beregnBidragsevneListeGrunnlag.justertSjablonPeriodeListe)
            .finnPerioder(beregnDatoFom = periodeGrunnlag.beregnDatoFra, beregnDatoTil = periodeGrunnlag.beregnDatoTil)
            .toMutableList()
    }

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode
    private fun beregnBidragsevnePerPeriode(grunnlag: BeregnBidragsevneListeGrunnlag) {
        grunnlag.bruddPeriodeListe.forEach { beregningsperiode: Periode ->
            val inntektListe = grunnlag.justertInntektPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Inntekt(referanse = it.referanse, inntektType = it.inntektType, inntektBelop = it.inntektBelop) }

            val skatteklasse = grunnlag.justertSkatteklassePeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Skatteklasse(referanse = it.referanse, skatteklasse = it.skatteklasse) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt SKATTEKLASSE mangler data for periode: ${beregningsperiode.getPeriode()}") }

            val bostatus = grunnlag.justertBostatusPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Bostatus(referanse = it.referanse, kode = it.bostatusKode) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt BOSTATUS mangler data for periode: ${beregningsperiode.getPeriode()}") }

            val barnIHusstand = grunnlag.justertBarnIHusstandenPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { BarnIHusstand(referanse = it.referanse, antallBarn = it.antallBarn) }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "Grunnlagsobjekt BARN_I_HUSSTAND mangler data for periode: ${beregningsperiode.getPeriode()}",
                    )
                }

            val saerfradrag = grunnlag.justertSaerfradragPeriodeListe.stream()
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }
                .map { Saerfradrag(referanse = it.referanse, kode = it.saerfradragKode) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Grunnlagsobjekt SAERFRADRAG mangler data for periode: ${beregningsperiode.getPeriode()}") }

            val sjablonliste = grunnlag.justertSjablonPeriodeListe
                .filter { it.getPeriode().overlapperMed(beregningsperiode) }

            // Kaller beregningsmodulen for hver beregningsperiode
            val beregnBidragsevneGrunnlagPeriodisert =
                GrunnlagBeregning(
                    inntektListe = inntektListe,
                    skatteklasse = skatteklasse,
                    bostatus = bostatus,
                    barnIHusstand = barnIHusstand,
                    saerfradrag = saerfradrag,
                    sjablonListe = sjablonliste,
                )

            grunnlag.periodeResultatListe.add(
                ResultatPeriode(
                    resultatDatoFraTil = Periode(datoFom = beregningsperiode.datoFom, datoTil = beregningsperiode.datoTil),
                    resultatBeregning = bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert),
                    resultatGrunnlagBeregning = beregnBidragsevneGrunnlagPeriodisert,
                ),
            )
        }
    }

    // Validerer at input-verdier til bidragsevneberegning er gyldige
    override fun validerInput(grunnlag: BeregnBidragsevneGrunnlag): List<Avvik> {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(beregnDatoFom = grunnlag.beregnDatoFra, beregnDatoTil = grunnlag.beregnDatoTil).toMutableList()

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

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "skatteklassePeriodeListe",
                periodeListe = grunnlag.skatteklassePeriodeListe.map { it.getPeriode() },
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
                dataElement = "bostatusPeriodeListe",
                periodeListe = grunnlag.bostatusPeriodeListe.map { it.getPeriode() },
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
                dataElement = "antallBarnIEgetHusholdPeriodeListe",
                periodeListe = grunnlag.antallBarnIEgetHusholdPeriodeListe.map { it.getPeriode() },
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = true,
            ),
        )

        avvikListe.addAll(
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = grunnlag.beregnDatoFra,
                beregnDatoTil = grunnlag.beregnDatoTil,
                dataElement = "saerfradragPeriodeListe",
                periodeListe = grunnlag.saerfradragPeriodeListe.map { it.getPeriode() },
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
