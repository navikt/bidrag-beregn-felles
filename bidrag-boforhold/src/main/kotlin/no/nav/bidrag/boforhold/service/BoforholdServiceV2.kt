package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.dto.BoforholdRequest
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class BoforholdServiceV2() {
    fun beregnEgneBarn(virkningstidspunkt: LocalDate, boforholdGrunnlagListe: List<BoforholdRequest>): List<BoforholdResponse> {
        secureLogger.info { "Beregner bostatus for BMs egne barn V2. Input: $virkningstidspunkt $boforholdGrunnlagListe" }

        val resultat = mutableListOf<BoforholdResponse>()
        boforholdGrunnlagListe
            .filter { relatertPerson ->
                relatertPerson.erBarnAvBmBp || relatertPerson.bostatusListe.any { it.kilde == Kilde.MANUELL }
            }
            .sortedWith(
                compareBy { it.relatertPersonPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderForBarn(virkningstidspunkt, barn))
            }

        secureLogger.info { "Resultat av beregning bostatus for BMs egne barn V2: $resultat" }

        return resultat
    }

    private fun beregnPerioderForBarn(virkningstidspunkt: LocalDate, boforholdRequest: BoforholdRequest): List<BoforholdResponse> {
        // Bruker fødselsdato som startdato for beregning hvis barnet er født etter virkningstidspunkt
        val startdatoBeregning = if (virkningstidspunkt.isBefore(boforholdRequest.fødselsdato)) {
            boforholdRequest.fødselsdato.withDayOfMonth(1)
        } else {
            virkningstidspunkt
        }

        // Filterer først bort alle perioder som avsluttes før startdatoBeregning
        val bostatuslisteRelevantePerioder = boforholdRequest.bostatusListe
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }

        // Justerer offentlige perioder slik at de starter på første dag i måneden og slutter på siste dag i måneden
        val justerteOffentligePerioder = bostatuslisteRelevantePerioder
            .filter { it.kilde == Kilde.OFFENTLIG }
            .map {
                BoforholdResponse(
                    relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                    periodeFom = if (it.periodeFom == null) startdatoBeregning else it.periodeFom.withDayOfMonth(1),
                    periodeTom = it.periodeTom?.plusMonths(1)?.withDayOfMonth(1)?.minusDays(1),
                    bostatus = it.bostatus ?: Bostatuskode.MED_FORELDER,
                    fødselsdato = boforholdRequest.fødselsdato,
                    kilde = Kilde.OFFENTLIG,
                )
            }

        val manuelleOpplysninger = bostatuslisteRelevantePerioder
            .filter { it.kilde == Kilde.MANUELL }.sortedBy { it.periodeFom }.map {
                BoforholdResponse(
                    relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                    periodeFom = if (it.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else it.periodeFom,
                    periodeTom = it.periodeTom,
                    bostatus = it.bostatus!!,
                    fødselsdato = boforholdRequest.fødselsdato,
                    kilde = it.kilde,
                )
            }

        // Leser gjennom manuelle perioder og slår sammen sammenhengende perioder med lik Bostatuskode. Hvis det er flere perioder med
        // periodeTom = null så slås disse sammen til én periode hvis de har lik Bostatuskode. Hvis ikke så settes periodeTom lik neste periodes
        // periodeFom minus én dag.
        val justerteManuellePerioder = slåSammenSammenhengendePerioderMedLikBostatuskode(manuelleOpplysninger)

        // Finner 18-årsdagen til barnet, settes lik første dag i måneden etter 18-årsdagen
        val attenårFraDato = beregnetAttenÅrFraDato(boforholdRequest.fødselsdato)

        // Behandler først barn uten husstandsmedlemskap i offentlige opplysninger. Genererer offentlige perioder som dekker perioden fra
        // startdatoBeregning til dagens dato. Hvis barnet fyller 18 år i perioden genereres to perioder, én før og én etter 18-årsdagen.
        val genererteOffentligePerioder = mutableListOf<BoforholdResponse>()
        if (justerteOffentligePerioder.isEmpty()) {
            if (personenHarFylt18År(boforholdRequest.fødselsdato, startdatoBeregning)) {
                genererteOffentligePerioder.add(
                    BoforholdResponse(
                        relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                        periodeFom = startdatoBeregning,
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        fødselsdato = boforholdRequest.fødselsdato,
                        kilde = Kilde.OFFENTLIG,
                    ),
                )
            } else {
                if (personenHarFylt18År(boforholdRequest.fødselsdato, LocalDate.now())) {
                    // Barnet fyller 18 år mellom startdatoBeregning og dagens dato, og det må lages to perioder, én før og én etter 18årsdagen
                    genererteOffentligePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                            periodeFom = startdatoBeregning,
                            periodeTom = attenårFraDato.minusDays(1),
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = boforholdRequest.fødselsdato,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    )
                    genererteOffentligePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                            periodeFom = attenårFraDato,
                            periodeTom = null,
                            bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                            fødselsdato = boforholdRequest.fødselsdato,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    )
                } else {
                    genererteOffentligePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdRequest.relatertPersonPersonId,
                            periodeFom = startdatoBeregning,
                            periodeTom = null,
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = boforholdRequest.fødselsdato,
                            kilde = Kilde.OFFENTLIG,

                        ),
                    )
                }
            }
            return slåSammenManuelleOgOffentligePerioder(manuelleOpplysninger, genererteOffentligePerioder)
        } else {
            // Det finnes offentlige perioder og disse behandles under.

            // Fyller ut perioder der det ikke finnes informasjon om barnet i offentlige opplysninger
            val komplettTidslinjeListe = fyllUtMedPerioderBarnetIkkeBorIHusstanden(startdatoBeregning, justerteOffentligePerioder)

            // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
            val offentligePerioderJustertMotAttenårsdag =
                justerMotAttenårsdag(
                    attenårFraDato,
                    komplettTidslinjeListe,
                )

            val sammenslåtteManuelleOgOffentligePerioder =
                slåSammenManuelleOgOffentligePerioder(justerteManuellePerioder, offentligePerioderJustertMotAttenårsdag)

            // Slår sammen sammenhengende perioder med lik Bostatuskode og setter kilde = Manuell
            return slåSammenSammenhengendePerioderMedLikBostatuskode(sammenslåtteManuelleOgOffentligePerioder)
        }
    }

    private fun slåSammenSammenhengendePerioderMedLikBostatuskode(liste: List<BoforholdResponse>): List<BoforholdResponse> {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        var kilde: Kilde? = null
        val sammenslåttListe = mutableListOf<BoforholdResponse>()

        for (indeks in liste.indices) {
            if (indeks < liste.size - 1) {
                // Hvis et element som ikke er siste element i listen har periodeTom = null så endres denne til neste periodes periodeFom minus én dag
                periodeTom = if (liste[indeks].periodeTom == null) {
                    liste[indeks + 1].periodeFom.minusDays(1)
                } else {
                    liste[indeks].periodeTom
                }

                if (liste[indeks + 1].periodeFom.isBefore(periodeTom!!.plusDays(2)) &&
                    liste[indeks + 1].bostatus == liste[indeks].bostatus
                ) {
                    // perioden overlapper og skal slås sammen
                    if (periodeFom == null) {
                        periodeFom = liste[indeks].periodeFom
                    }
                    if (liste[indeks].kilde == Kilde.MANUELL) {
                        kilde = Kilde.MANUELL
                    }
                } else {
                    // Neste periode overlapper ikke og det skal lages ny forekomst i sammenslåttListe. PeriodeTom overstyres siden det er flere
                    // perioder i listen. Kun siste element skal kunne ha periodeTom = null.
                    if (periodeFom != null) {
                        // Det er overlappende perioder og periodeFom settes lik periodeFom for første overlappende periode.
                        sammenslåttListe.add(
                            BoforholdResponse(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = periodeFom,
                                periodeTom = liste[indeks].periodeTom ?: periodeTom,
                                bostatus = liste[indeks].bostatus,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = kilde ?: liste[indeks].kilde,

                            ),
                        )
                        periodeFom = null
                        kilde = null
                    } else {
                        sammenslåttListe.add(
                            BoforholdResponse(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom ?: periodeTom,
                                bostatus = liste[indeks].bostatus,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                    }
                }
            } else {
                // Siste forekomst
                sammenslåttListe.add(
                    BoforholdResponse(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = periodeFom ?: liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                        fødselsdato = liste[indeks].fødselsdato,
                        kilde = liste[indeks].kilde,

                    ),
                )
            }
        }
        return sammenslåttListe
    }

    private fun fyllUtMedPerioderBarnetIkkeBorIHusstanden(startdatoBeregning: LocalDate, liste: List<BoforholdResponse>): List<BoforholdResponse> {
        val sammenhengendePerioderListe = mutableListOf<BoforholdResponse>()

        for (indeks in liste.indices) {
//            Sjekker første forekomst og danner periode mellom startdatoBeregning og første forekomst hvis det er opphold
            if (indeks == 0) {
                if (liste[indeks].periodeFom.isAfter(startdatoBeregning)) {
                    sammenhengendePerioderListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = startdatoBeregning,
                            periodeTom = liste[indeks].periodeFom.minusDays(1),
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = liste[indeks].fødselsdato,
                            kilde = liste[indeks].kilde,

                        ),
                    )
                    sammenhengendePerioderListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                            kilde = liste[indeks].kilde,

                        ),
                    )
                } else {
                    sammenhengendePerioderListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = startdatoBeregning,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                            kilde = liste[indeks].kilde,

                        ),
                    )
                }
            } else {
                if (liste[indeks - 1].periodeTom!!.plusDays(1).isBefore(liste[indeks].periodeFom)) {
                    // Det er opphold mellom to perioder og det må lages en periode med bostatus IKKE_MED_FORELDER for oppholdet

                    sammenhengendePerioderListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks - 1].periodeTom!!.plusDays(1),
                            periodeTom = liste[indeks].periodeFom.minusDays(1),
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = liste[indeks].fødselsdato,
                            kilde = liste[indeks].kilde,

                        ),
                    )
                }

                sammenhengendePerioderListe.add(
                    BoforholdResponse(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                        fødselsdato = liste[indeks].fødselsdato,
                        kilde = liste[indeks].kilde,

                    ),
                )
            }

            // Siste forekomst. Hvis periodeTom er satt så dannes det en ny periode som dekker perioden fra periodeTom på forrige forekomst
            // og med åpen periodeTom.
            if (indeks == liste.size - 1 && liste[indeks].periodeTom != null) {
                sammenhengendePerioderListe.add(
                    BoforholdResponse(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeTom!!.plusDays(1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        fødselsdato = liste[indeks].fødselsdato,
                        kilde = liste[indeks].kilde,

                    ),
                )
            }
        }
        return sammenhengendePerioderListe
    }

    private fun justerMotAttenårsdag(attenårFraDato: LocalDate, liste: List<BoforholdResponse>): List<BoforholdResponse> {
        val listeJustertMotAttenårsdag = mutableListOf<BoforholdResponse>()

        if (attenårFraDato.isAfter(LocalDate.now())) {
            // Barnet har ikke fyllt 18 og listen returneres uendret.
            return liste
        } else {
            for (indeks in liste.indices) {
                // Perioder som avsluttes før 18årsdag skrives til returliste.
                if (liste[indeks].periodeTom != null && liste[indeks].periodeTom!!.isBefore(attenårFraDato)) {
                    listeJustertMotAttenårsdag.add(liste[indeks])
                } else {
                    if (liste[indeks].periodeFom.isBefore(attenårFraDato)) {
                        // Perioden starter før 18-årsdagen, det må lages en ekstra periode etter 18årsdagen.
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponse(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = attenårFraDato.minusDays(1),
                                bostatus = liste[indeks].bostatus,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponse(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = attenårFraDato,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                    } else {
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponse(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,
                            ),
                        )
                    }
                }
            }
        }
        return listeJustertMotAttenårsdag
    }

    private fun slåSammenManuelleOgOffentligePerioder(
        manuellePerioder: List<BoforholdResponse>,
        offentligePerioder: List<BoforholdResponse>,
    ): List<BoforholdResponse> {
        val resultatliste = mutableListOf<BoforholdResponse>()

        // Skriver alle manuelle perioder til resultatet. Perioder med identisk informasjon som en offentlig periode skrives med kilde = Offentlig
        manuellePerioder.forEach { manuellPeriode ->
            resultatliste.add(
                BoforholdResponse(
                    relatertPersonPersonId = manuellPeriode.relatertPersonPersonId,
                    periodeFom = manuellPeriode.periodeFom,
                    periodeTom = manuellPeriode.periodeTom,
                    bostatus = manuellPeriode.bostatus,
                    fødselsdato = manuellPeriode.fødselsdato,
                    kilde = if (manuellPeriodeErIdentiskMedOffentligPeriode(manuellPeriode, offentligePerioder)) Kilde.OFFENTLIG else Kilde.MANUELL,
                ),
            )
        }

        // Sjekker offentlige perioder og justerer periodeFom og periodeTom der disse overlapper med manuelle perioder
        // Offentlige perioder som helt dekkes av manuelle perioder skrives ikke til resultatet
        offentligePerioder.forEach { offentligPeriode ->
            // Finner manuelle perioder som overlapper med den offentlige perioden
            val overlappendePerioder = mutableListOf<BoforholdResponse>()
            manuellePerioder.forEach { manuellPeriode ->
                if (offentligPeriode.periodeTom == null) {
                    if (manuellPeriode.periodeTom == null || manuellPeriode.periodeTom.isAfter(offentligPeriode.periodeFom)) {
                        overlappendePerioder.add(manuellPeriode)
                    }
                } else {
                    if (manuellPeriode.periodeTom == null) {
                        if (manuellPeriode.periodeFom.isBefore(offentligPeriode.periodeTom.plusDays(1))) {
                            overlappendePerioder.add(manuellPeriode)
                        }
                    } else {
                        if (manuellPeriode.periodeFom.isBefore(offentligPeriode.periodeTom.plusDays(1)) &&
                            manuellPeriode.periodeTom.plusDays(1)
                                ?.isAfter(offentligPeriode.periodeFom) == true
                        ) {
                            overlappendePerioder.add(manuellPeriode)
                        }
                    }
                }
            }

            val justertOffentligPeriode = justerPeriodeOffentligOpplysning(offentligPeriode, overlappendePerioder.sortedBy { it.periodeFom })
            if (justertOffentligPeriode != null) {
                resultatliste.addAll(justertOffentligPeriode)
            }
        }

        return resultatliste.sortedBy { it.periodeFom }
    }

    // Offentlig periode sjekkes mot manuelle perioder og justeres til å ikke overlappe med disse. En offentlig periode kan overlappe med 0 til
    // mange manuelle perioder. Hvis en offentlig periode dekkes helt av manuelle perioder returneres null, ellers returneres en liste. Hvis
    // en offentlig perioder overlappes av flere enn to manuelle perioder så vil responsen bestå av flere offentlige perioder som dekker
    // oppholdet mellom de ulike manuelle periodene.
    private fun justerPeriodeOffentligOpplysning(
        offentligePeriode: BoforholdResponse,
        overlappendePerioder: List<BoforholdResponse>,
    ): List<BoforholdResponse>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertOffentligPeriodeListe = mutableListOf<BoforholdResponse>()

        if (overlappendePerioder.isNullOrEmpty()) {
            return listOf(offentligePeriode)
        }

        for (indeks in overlappendePerioder.indices) {
            // Sjekker først om den første manuelle perioden dekker starten, og eventuelt hele den offentlige perioden
            if (indeks == 0) {
                if (overlappendePerioder[indeks].periodeFom.isBefore(offentligePeriode.periodeFom.plusDays(1))) {
                    if (overlappendePerioder[indeks].periodeTom == null) {
                        // Den manuelle perioden dekker hele den offentlige perioden
                        return null
                    } else {
                        if (offentligePeriode.periodeTom != null && overlappendePerioder[indeks].periodeTom?.isAfter(
                                offentligePeriode.periodeTom.plusDays(1),
                            ) == true
                        ) {
                            // Den manuelle perioden dekker hele den offentlige perioden
                            return null
                        } else {
                            // Den manuelle perioden dekker starten på den offentlige perioden og periodeFom må forskyves
                            periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1)
                        }
                    }
                } else {
                    // Den manuelle perioden overlapper etter starten på den offentlige perioden og periodeTom må forskyves på den offentlige perioden
                    periodeTom = overlappendePerioder[indeks].periodeFom.minusDays(1)
                }
                if (periodeTom != null) {
                    // Første manuelle periode starter etter offentlig periode. Den offentlige perioden skrives med justert tomdato. Senere i logikken
                    // må det sjekkes på om den offentlige perioden må splittes i mer enn én periode.
                    justertOffentligPeriodeListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = offentligePeriode.relatertPersonPersonId,
                            periodeFom = offentligePeriode.periodeFom,
                            periodeTom = periodeTom,
                            bostatus = offentligePeriode.bostatus,
                            fødselsdato = offentligePeriode.fødselsdato,
                            kilde = offentligePeriode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                }
            }
            if (indeks < overlappendePerioder.size - 1) {
                if (overlappendePerioder[indeks + 1].periodeFom.isAfter(overlappendePerioder[indeks].periodeTom!!.plusDays(1))) {
                    // Det er en åpen tidsperiode mellom to manuelle perioder, og den offentlige perioden skal fylle denne tidsperioden
                    periodeTom = overlappendePerioder[indeks + 1].periodeFom.minusDays(1)
                    justertOffentligPeriodeListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = offentligePeriode.relatertPersonPersonId,
                            // periodeFom er satt hvis første manuelle periode overlapper startdato for offentlig periode
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = periodeTom,
                            bostatus = offentligePeriode.bostatus,
                            fødselsdato = offentligePeriode.fødselsdato,
                            kilde = offentligePeriode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                }
            } else {
                // Siste manuelle periode
                if (overlappendePerioder[indeks].periodeTom != null) {
                    if (offentligePeriode.periodeTom == null || offentligePeriode.periodeTom.isAfter(overlappendePerioder[indeks].periodeTom)) {
                        justertOffentligPeriodeListe.add(
                            BoforholdResponse(
                                relatertPersonPersonId = offentligePeriode.relatertPersonPersonId,
                                periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                                periodeTom = offentligePeriode.periodeTom,
                                bostatus = offentligePeriode.bostatus,
                                fødselsdato = offentligePeriode.fødselsdato,
                                kilde = offentligePeriode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertOffentligPeriodeListe
    }

    private fun personenHarFylt18År(fødselsdato: LocalDate, dato: LocalDate): Boolean {
        return ChronoUnit.YEARS.between(fødselsdato.plusMonths(1).withDayOfMonth(1), dato) >= 18
    }

    private fun beregnetAttenÅrFraDato(fødselsdato: LocalDate): LocalDate {
        return fødselsdato.plusYears(18).plusMonths(1).withDayOfMonth(1)
    }

    private fun manuellPeriodeErIdentiskMedOffentligPeriode(
        manuellPeriode: BoforholdResponse,
        offentligePerioder: List<BoforholdResponse>,
    ): Boolean {
        return offentligePerioder.any { offentligPeriode ->
            manuellPeriode.periodeFom == offentligPeriode.periodeFom && manuellPeriode.periodeTom == offentligPeriode.periodeTom &&
                manuellPeriode.bostatus == offentligPeriode.bostatus
        }
    }
}
