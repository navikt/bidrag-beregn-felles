package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate

internal class BoforholdBarnServiceV2() {
    fun beregnBoforholdBarn(virkningstidspunkt: LocalDate, boforholdGrunnlagListe: List<BoforholdBarnRequest>): List<BoforholdResponse> {
        secureLogger.info { "Beregner bostatus for BMs egne barn V2. Input: $virkningstidspunkt $boforholdGrunnlagListe" }

        val resultat = mutableListOf<BoforholdResponse>()
        boforholdGrunnlagListe
            .filter { relatertPerson ->
                relatertPerson.erBarnAvBmBp || relatertPerson.manuelleBostatusopplysninger.any { it.kilde == Kilde.MANUELL }
            }
            .sortedWith(
                compareBy { it.relatertPersonPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderForBarn(virkningstidspunkt, barn))
            }

        secureLogger.info { "Resultat av beregning bostatus for BMs egne barn V2: $resultat" }

        return resultat
    }

    private fun beregnPerioderForBarn(virkningstidspunkt: LocalDate, boforholdBarnRequest: BoforholdBarnRequest): List<BoforholdResponse> {
        // Bruker fødselsdato som startdato for beregning hvis barnet er født etter virkningstidspunkt
        val startdatoBeregning = if (virkningstidspunkt.isBefore(boforholdBarnRequest.fødselsdato)) {
            boforholdBarnRequest.fødselsdato.withDayOfMonth(1)
        } else {
            virkningstidspunkt
        }

        // Filterer først bort alle perioder som avsluttes før startdatoBeregning
        // Justerer offentlige perioder slik at de starter på første dag i måneden og slutter på siste dag i måneden
        val justerteOffentligePerioder = boforholdBarnRequest.innhentedeOffentligeOpplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }
            .map {
                BoforholdResponse(
                    relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                    periodeFom = if (it.periodeFom == null) startdatoBeregning else it.periodeFom.withDayOfMonth(1),
                    periodeTom = it.periodeTom?.plusMonths(1)?.withDayOfMonth(1)?.minusDays(1),
                    bostatus = it.bostatus ?: Bostatuskode.MED_FORELDER,
                    fødselsdato = boforholdBarnRequest.fødselsdato,
                    kilde = Kilde.OFFENTLIG,
                )
            }

        // Filterer først bort alle perioder som avsluttes før startdatoBeregning
        val manuelleOpplysninger = boforholdBarnRequest.manuelleBostatusopplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .filter { it.kilde == Kilde.MANUELL }.sortedBy { it.periodeFom }.map {
                BoforholdResponse(
                    relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                    periodeFom = if (it.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else it.periodeFom,
                    periodeTom = it.periodeTom,
                    bostatus = it.bostatus!!,
                    fødselsdato = boforholdBarnRequest.fødselsdato,
                    kilde = it.kilde,
                )
            }

        if (justerteOffentligePerioder.isEmpty() && manuelleOpplysninger.isEmpty()) {
            // Ingen perioder innenfor beregningsperiode. Dette skal ikke forekomme. Hvis det ikke finnes offentlige perioder så skal
            // bidrag-behanding legge til en periode med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENLIG i input.
            // Unntaket er hvis barnet er manuelt lagt til, da skal det ikke finnes offentlige perioder, kun manuelle perioder.
            return emptyList()
        }

        // Finner 18-årsdagen til barnet, settes lik første dag i måneden etter 18-årsdagen
        val attenårFraDato = beregnetAttenÅrFraDato(boforholdBarnRequest.fødselsdato)

        // Hvis det ikke finnes offentlige perioder skal det bygges en tidslinje med bare manuelle perioder. Perioder uten data i input genereres.
        if (justerteOffentligePerioder.isEmpty()) {
            // Leser gjennom manuelle perioder og slår sammen sammenhengende perioder med lik Bostatuskode. Hvis det er flere perioder med
            // periodeTom = null så slås disse sammen til én periode hvis de har lik Bostatuskode. Hvis ikke så settes periodeTom lik neste periodes
            // periodeFom minus én dag.
            val justerteManuellePerioder = slåSammenPerioderOgJusterPeriodeTom(manuelleOpplysninger)
            // Fyller ut perioder der det ikke finnes informasjon om barnet i manuelle opplysninger. Bostatuskode settes lik IKKE_MED_FORELDER og
            // kilde = MANUELL. Hvis det kun finnes manuelle perioder med IKKE_MED_FORELDER så returneres disse uendret.
            if (justerteManuellePerioder.all { it.bostatus == Bostatuskode.IKKE_MED_FORELDER }) {
                return justerteManuellePerioder
            }
            val komplettManuellTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(startdatoBeregning, justerteManuellePerioder)
            // Gjør en ny sammenslåing av sammenhengende perioder med lik bostatus for å få med perioder generert i komplettManuellTidslinje.
            val sammenslåttManuellTidslinje = slåSammenPerioderOgJusterPeriodeTom(komplettManuellTidslinje)
            // Manuelle perioder justeres mot 18årsdag
            val manuellePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, sammenslåttManuellTidslinje)
            return manuellePerioderJustertMotAttenårsdag
        } else {
            if (manuelleOpplysninger.isEmpty()) {
                // Fyller ut perioder der det ikke finnes informasjon om barnet i offentlige opplysninger
                val komplettOffentligTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(startdatoBeregning, justerteOffentligePerioder)
                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, komplettOffentligTidslinje)
                // Slår sammen sammenhengende perioder med lik Bostatuskode og setter kilde = Manuell

                return slåSammenPerioderOgJusterPeriodeTom(offentligePerioderJustertMotAttenårsdag)
            } else {
                // Det finnes både offentlige og manuelle perioder

                // Leser gjennom manuelle perioder og slår sammen sammenhengende perioder med lik Bostatuskode. Hvis det er flere perioder med
                // periodeTom = null så slås disse sammen til én periode hvis de har lik Bostatuskode. Hvis ikke så settes periodeTom lik neste periodes
                // periodeFom minus én dag.
                val justerteManuellePerioder = slåSammenPerioderOgJusterPeriodeTom(manuelleOpplysninger)

                // Manuelle perioder justeres mot 18årsdag. Perioder som enten overlapper med 18årsdag splittes i to der periode nr to får oppdatert
                // bostatuskode. Perioder som er etter 18årsdag får endret bostatuskode.
                val manuellePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, justerteManuellePerioder)

                // Fyller ut perioder der det ikke finnes informasjon om barnet i offentlige opplysninger. Bostatuskode settes lik IKKE_MED_FORELDER
                // og kilde = OFFENTLIG.
                val komplettOffentligTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(startdatoBeregning, justerteOffentligePerioder)

                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag =
                    justerMotAttenårsdag(
                        attenårFraDato,
                        komplettOffentligTidslinje,
                    )

                val sammenslåtteManuelleOgOffentligePerioder =
                    slåSammenManuelleOgOffentligePerioder(manuellePerioderJustertMotAttenårsdag, offentligePerioderJustertMotAttenårsdag)

                // Slår sammen sammenhengende perioder med lik Bostatuskode og setter kilde = Manuell
                return slåSammenPerioderOgJusterPeriodeTom(sammenslåtteManuelleOgOffentligePerioder)
            }
        }
    }

    private fun slåSammenPerioderOgJusterPeriodeTom(liste: List<BoforholdResponse>): List<BoforholdResponse> {
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
                        kilde = kilde ?: liste[indeks].kilde,

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
                val bostatuskodeAttenÅr =
                    if (liste[indeks].kilde == Kilde.MANUELL && (
                            liste[indeks].bostatus == Bostatuskode.MED_FORELDER || liste[indeks].bostatus
                                == Bostatuskode.DOKUMENTERT_SKOLEGANG
                            )
                    ) {
                        Bostatuskode.DOKUMENTERT_SKOLEGANG
                    } else {
                        Bostatuskode.REGNES_IKKE_SOM_BARN
                    }
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
                                bostatus = bostatuskodeAttenÅr,
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
                                bostatus = bostatuskodeAttenÅr,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,
                            ),
                        )
                    }
                }
            }
        }
        // Slår sammen perioder med lik status og returnerer.
        return slåSammenPerioderOgJusterPeriodeTom(listeJustertMotAttenårsdag)
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

            // Lag en ny liste fra overlappendePerioder der perioder som henger sammen uavhengig av bostatus blir slått sammen
            val sammenslåttListeOverlappendePerioder = mutableListOf<BoforholdResponse>()

            var periodeFom: LocalDate? = null
            for (indeks in overlappendePerioder.indices) {
                if (indeks < overlappendePerioder.size - 1) {
                    if (overlappendePerioder[indeks + 1].periodeFom.isAfter(overlappendePerioder[indeks].periodeTom!!.plusDays(1))) {
                        sammenslåttListeOverlappendePerioder.add(
                            overlappendePerioder[indeks].copy(
                                periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeFom,
                            ),
                        )
                        periodeFom = null
                    } else {
                        if (periodeFom == null) {
                            periodeFom = overlappendePerioder[indeks].periodeFom
                        }
                    }
                } else {
                    sammenslåttListeOverlappendePerioder.add(
                        overlappendePerioder[indeks].copy(
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeFom,
                        ),
                    )
                    periodeFom = null
                }
            }

            val justertOffentligPeriode =
                justerPeriodeOffentligOpplysning(offentligPeriode, sammenslåttListeOverlappendePerioder.sortedBy { it.periodeFom })
            if (justertOffentligPeriode != null) {
                resultatliste.addAll(justertOffentligPeriode)
            }
        }

        return resultatliste.sortedBy { it.periodeFom }
    }

    // Offentlig periode sjekkes mot manuelle perioder og justeres til å ikke overlappe med disse. En offentlig periode kan overlappe med 0 til
    // mange manuelle perioder. Hvis en offentlig periode dekkes helt av manuelle perioder returneres null, ellers returneres en liste. Hvis
    // en offentlig perioder overlappes av flere enn to manuelle perioder så vil responsen bestå av flere offentlige perioder som dekker
    // oppholdet mellom de ulike manuelle periodene. Kilde endres til Manuell for offentlig periode som har fått endret perioder.
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
                            kilde = Kilde.MANUELL,
//                            kilde = offentligePeriode.kilde,
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
                            kilde = Kilde.MANUELL,
//                            kilde = offentligePeriode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                } else {
//                    periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1)
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
                                kilde = Kilde.MANUELL,
//                            kilde = offentligePeriode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertOffentligPeriodeListe
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
