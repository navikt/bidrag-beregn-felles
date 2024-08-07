package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.BoforholdResponse
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate

internal class BoforholdBarnServiceV2 {
    fun beregnBoforholdBarn(virkningstidspunkt: LocalDate, boforholdGrunnlagListe: List<BoforholdBarnRequest>): List<BoforholdResponse> {
        secureLogger.info { "Beregner bostatus for BM/BPs egne barn V2. Input: $virkningstidspunkt $boforholdGrunnlagListe" }

        val resultat = mutableListOf<BoforholdResponse>()
        boforholdGrunnlagListe
            .filter { relatertPerson ->
                relatertPerson.erBarnAvBmBp || relatertPerson.behandledeBostatusopplysninger.isNotEmpty() || relatertPerson.endreBostatus != null
            }
            .sortedWith(
                compareBy { it.relatertPersonPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderForBarn(virkningstidspunkt, barn))
            }

        secureLogger.info { "Resultat av beregning bostatus for BM/BPs egne barn V2: $resultat" }

        return resultat
    }

    private fun beregnPerioderForBarn(virkningstidspunkt: LocalDate, boforholdBarnRequest: BoforholdBarnRequest): List<BoforholdResponse> {
        // 1. endreBoforhold = null. Beregning gjøres da enten på offentlige opplysninger eller behandledeBoforholdopplysninger.
        //    1a. Hvis behandledeBoforholdopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt:
        //        behandledeBoforholdopplysninger skal da justeres mot virkningstidspunkt. Perioder i behandledeBoforholdopplysninger sjekkes mot
        //        offentlige perioder og kilde evt. endres til Offentlig hvis det er match. Dette vil kunne skje ved endring av innhentede offentlige
        //        opplysninger som nå helt overlapper manuelt innlagte perioder.
        //        I tilfeller der virkningstidspunkt forskyves tilbake i tid så skal tidslinjen suppleres med offentlige perioder.
        //    1b. Hvis behandledeBoforholdopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom:
        //        behandledeBoforholdopplysninger skal da justeres mot virkningstidspunkt. I tilfeller der virkningstidspunkt forskyves tilbake i
        //        tid så skal tidslinjen suppleres med én offentlig perioder med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG i tidsrommet
        //        mellom virkningstidspunkt og periodeFom for første forekomst i behandledeBoforholdopplysninger.
        //    1c. Hvis behandledeBoforholdopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Det gjøres da en beregning basert på
        //        offentlige perioder.
        //    1d. Hvis behandledeBoforholdopplysninger er tom og innhentedeOffentligeOpplysninger  er tom: Det skal legges til en periode med
        //        Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG
        // 2. endreBoforhold er utfyllt.
        //    2a. Hvis behandledeBoforholdopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt: behandledeBoforholdopplysninger
        //        skal da justeres etter det som er sendt inn i endreBoforhold. Det kan slettes/legges til eller endres perioder.
        //        Perioder i oppdaterte behandledeBoforholdopplysninger sjekkes mot offentlige perioder og kilde evt. endres til Offentlig hvis det
        //        er match.
        //    2b. Hvis behandledeBoforholdopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom: behandledeBoforholdopplysninger skal
        //        da justeres etter det som er sendt inn i endreBoforhold. Det kan slettes/legges til eller endres perioder.
        //        Perioder i oppdaterte behandledeBoforholdopplysninger sjekkes mot genererte offentlige perioder (IKKE_MED_FORELDER/
        //        REGNES_IKKE_SOM_BARN). Kilde endres til Offentlig hvis det er match.
        //    2c. Hvis behandledeBoforholdopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Feil.
        //        Det bør da i stedet gjøres en beregning på offentlige perioder før det kan sendes en endreBoforhold-request.
        //        Beregningen ignorerer innhentedeOffentligeOpplysninger og gjør en beregning på det som ligger i endreBoforhold. typeEndring må
        //        være lik NY, hvis ikke reurneres tom liste. Hull i tidslinjen utfylles med Bostatuskode = IKKE_MED_FORELDER og Kilde = MANUELL.
        //    2d. Hvis behandledeBoforholdopplysninger er tom og innhentedeOffentligeOpplysninger er tom: Beregningen gjøres på det som ligger i
        //        endreBoforhold. typeEndring må være lik NY, hvis ikke reurneres tom liste.
        //        Hull i tidslinjen utfylles med Bostatuskode = IKKE_MED_FORELDER og Kilde = MANUELL.

        // Bruker fødselsdato som startdato for beregning hvis barnet er født etter virkningstidspunkt
        val startdatoBeregning = if (virkningstidspunkt.isBefore(boforholdBarnRequest.fødselsdato)) {
            boforholdBarnRequest.fødselsdato.withDayOfMonth(1)
        } else {
            virkningstidspunkt
        }

        // Filterer først bort alle offentlige perioder som avsluttes før startdatoBeregning
        // Justerer så offentlige perioder slik at de starter på første dag i måneden og slutter på siste dag i måneden
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

        // Filterer først bort alle perioder med behandlede opplysninger som avsluttes før startdatoBeregning
        val behandledeOpplysninger = boforholdBarnRequest.behandledeBostatusopplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }.map {
                BoforholdResponse(
                    relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                    periodeFom = if (it.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else it.periodeFom,
                    periodeTom = it.periodeTom,
                    bostatus = it.bostatus!!,
                    fødselsdato = boforholdBarnRequest.fødselsdato,
                    kilde = it.kilde,
                )
            }

        if (justerteOffentligePerioder.isEmpty() && behandledeOpplysninger.isEmpty() && boforholdBarnRequest.endreBostatus == null) {
            // Ingen perioder innenfor beregningsperiode. Dette skal ikke forekomme. Hvis det ikke finnes offentlige perioder så skal
            // bidrag-behandling legge til en periode med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG i input.
            // Unntaket er hvis barnet er manuelt lagt til, da skal det ikke finnes offentlige perioder, kun manuelle perioder.
            return emptyList()
        }

        // Finner 18-årsdagen til barnet, settes lik første dag i måneden etter 18-årsdagen
        val attenårFraDato = beregnetAttenÅrFraDato(boforholdBarnRequest.fødselsdato)

        if (boforholdBarnRequest.endreBostatus == null) {
            if (behandledeOpplysninger.isNotEmpty()) {
                // virkningstidspunkt eller offentlige perioder er endret, juster og fyll inn med offentlig informasjon.
                val komplettOffentligTidslinje =
                    fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, komplettOffentligTidslinje)
                val sammenslåtteBehandledeOgOffentligePerioder =
                    slåSammenPrimærOgSekundærperioder(behandledeOpplysninger, offentligePerioderJustertMotAttenårsdag)

                // Slår sammen sammenhengende perioder med lik Bostatuskode og setter kilde = Manuell
                val sammenslåttListe = slåSammenPerioderOgJusterPeriodeTom(sammenslåtteBehandledeOgOffentligePerioder)

                return sammenslåttListe.map {
                    BoforholdResponse(
                        relatertPersonPersonId = it.relatertPersonPersonId,
                        periodeFom = it.periodeFom,
                        periodeTom = it.periodeTom,
                        bostatus = it.bostatus,
                        fødselsdato = it.fødselsdato,
                        kilde = if (beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
                                it,
                                offentligePerioderJustertMotAttenårsdag,
                            )
                        ) {
                            Kilde.OFFENTLIG
                        } else {
                            Kilde.MANUELL
                        },
                    )
                }
            }

            // Førstegangs beregning av boforhold for barnet. Beregn fra innhentede offentlige opplysninger.
            if (justerteOffentligePerioder.isEmpty()) {
                return emptyList()
            } else {
                // Fyller ut perioder der det ikke finnes informasjon om barnet i offentlige opplysninger
                val komplettOffentligTidslinje =
                    fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, komplettOffentligTidslinje)
                // Slår sammen sammenhengende perioder med lik Bostatuskode.
                return slåSammenPerioderOgJusterPeriodeTom(offentligePerioderJustertMotAttenårsdag)
            }
        }

        val endredeBostatusPerioder = behandleEndringer(startdatoBeregning, boforholdBarnRequest)

        if (behandledeOpplysninger.isEmpty()) {
            // Det finnes ingen behandlede perioder og den nye bostatusperioden skal returneres sammen med genererte perioder
            // som fyller tidslinjen fra virkningstidspunkt til dagens dato.

            if (boforholdBarnRequest.endreBostatus.typeEndring != TypeEndring.NY) {
                // Feilsituasjon. Må alltid være ny hvis det ikke finnes perioder fra før.
                return emptyList()
            }
            val komplettManuellTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.MANUELL, startdatoBeregning, endredeBostatusPerioder)
            // Gjør en ny sammenslåing av sammenhengende perioder med lik bostatus for å få med perioder generert i komplettManuellTidslinje.
            val sammenslåttManuellTidslinje = slåSammenPerioderOgJusterPeriodeTom(komplettManuellTidslinje)
            // Manuelle perioder justeres mot 18årsdag
            val manuellePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, sammenslåttManuellTidslinje)
            return manuellePerioderJustertMotAttenårsdag
        }

        // Det finnes både behandlede og endrede perioder
        // Endrede perioder justeres mot 18årsdag. Perioder som overlapper med 18årsdag splittes i to der periode nr to får oppdatert
        // bostatuskode. Perioder som er etter 18årsdag får endret bostatuskode.
        val endredePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, endredeBostatusPerioder)
        val sammenslåtteEndredeOgBehandledePerioder =
            slåSammenPrimærOgSekundærperioder(endredePerioderJustertMotAttenårsdag, behandledeOpplysninger)

        // Slår sammen sammenhengende perioder med lik Bostatuskode og setter kilde = Manuell
        val sammenslåttListe = slåSammenPerioderOgJusterPeriodeTom(sammenslåtteEndredeOgBehandledePerioder)
        // Lager komplett tidslinje basert på offentlige opplysninger for å kunne sjekke alle perioder mot offentlige perioder. Alle beregnede
        // perioder som her helt innenfor en offentlig periode med lik bostatuskode får kilde = OFFENTLIG.
        val komplettOffentligTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
        val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, komplettOffentligTidslinje)

        return sammenslåttListe.map {
            BoforholdResponse(
                relatertPersonPersonId = it.relatertPersonPersonId,
                periodeFom = it.periodeFom,
                periodeTom = it.periodeTom,
                bostatus = it.bostatus,
                fødselsdato = it.fødselsdato,
                kilde = if (beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
                        it,
                        offentligePerioderJustertMotAttenårsdag,
                    )
                ) {
                    Kilde.OFFENTLIG
                } else {
                    Kilde.MANUELL
                },
            )
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

    private fun fyllUtMedPerioderBarnetIkkeBorIHusstanden(
        kilde: Kilde,
        startdatoBeregning: LocalDate,
        liste: List<BoforholdResponse>,
    ): List<BoforholdResponse> {
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
                            kilde = kilde,

                        ),
                    )
                    sammenhengendePerioderListe.add(liste[indeks])
                } else {
                    sammenhengendePerioderListe.add(liste[indeks].copy(periodeFom = startdatoBeregning))
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
                            kilde = kilde,
                        ),
                    )
                }
                sammenhengendePerioderListe.add(liste[indeks])
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
                        kilde = kilde,
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
                    if (liste[indeks].kilde == Kilde.MANUELL &&
                        (
                            liste[indeks].bostatus == Bostatuskode.MED_FORELDER ||
                                liste[indeks].bostatus
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

    // Funksjon for å slå sammen to lister med hhv hoved- og sekundærperioder. Hovedperioder skrives uendret til output mens sekundærperioder
    // justeres mot hovedperioder. Hvis en sekundærperiode overlapper med én eller flere hovedperioder så splittes sekundærperioden til å dekke
    // eventuelt opphold mellom hovedperioder.
    private fun slåSammenPrimærOgSekundærperioder(
        primærperioder: List<BoforholdResponse>,
        sekundærperioder: List<BoforholdResponse>,
    ): List<BoforholdResponse> {
        val resultatliste = mutableListOf<BoforholdResponse>()

        // Skriver alle primærperioder til resultatet.
        primærperioder.forEach { primærperiode ->
            resultatliste.add(
                BoforholdResponse(
                    relatertPersonPersonId = primærperiode.relatertPersonPersonId,
                    periodeFom = primærperiode.periodeFom,
                    periodeTom = primærperiode.periodeTom,
                    bostatus = primærperiode.bostatus,
                    fødselsdato = primærperiode.fødselsdato,
                    kilde = primærperiode.kilde,
                ),
            )
        }

        // Sjekker sekundærperioder og justerer periodeFom og periodeTom der disse overlapper med primærperioder.
        // Sekundærperioder som helt dekkes av primærperioder skrives ikke til resultatet.
        sekundærperioder.forEach { sekundærperiode ->
            // Finner sekundærperioder som overlapper med den primære perioden
            val overlappendePerioder = mutableListOf<BoforholdResponse>()
            primærperioder.forEach { primærperiode ->
                if (sekundærperiode.periodeTom == null) {
                    if (primærperiode.periodeTom == null || primærperiode.periodeTom.isAfter(sekundærperiode.periodeFom)) {
                        overlappendePerioder.add(primærperiode)
                    }
                } else {
                    if (primærperiode.periodeTom == null) {
                        if (primærperiode.periodeFom.isBefore(sekundærperiode.periodeTom.plusDays(1))) {
                            overlappendePerioder.add(primærperiode)
                        }
                    } else {
                        if (primærperiode.periodeFom.isBefore(sekundærperiode.periodeTom.plusDays(1)) &&
                            primærperiode.periodeTom.plusDays(1)
                                ?.isAfter(sekundærperiode.periodeFom) == true
                        ) {
                            overlappendePerioder.add(primærperiode)
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

            val justertSekundærperiode =
                justerSekundærperiode(sekundærperiode, sammenslåttListeOverlappendePerioder.sortedBy { it.periodeFom })
            if (justertSekundærperiode != null) {
                resultatliste.addAll(justertSekundærperiode)
            }
        }

        return resultatliste.sortedBy { it.periodeFom }
    }

    // Sekundær periode sjekkes mot primærperioder og justeres til å ikke overlappe med disse. En sekundær periode kan overlappe med 0 til
    // mange primærperioder. Hvis en sekundær periode dekkes helt av primære perioder returneres null, ellers returneres en liste. Hvis
    // en sekundær periode overlappes av flere enn to primærperioder så vil responsen bestå av flere sekundærperioder som dekker
    // oppholdet mellom de ulike primærperiodene.
    private fun justerSekundærperiode(sekundærperiode: BoforholdResponse, overlappendePerioder: List<BoforholdResponse>): List<BoforholdResponse>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertSekundærPeriodeListe = mutableListOf<BoforholdResponse>()

        if (overlappendePerioder.isNullOrEmpty()) {
            return listOf(sekundærperiode)
        }

        for (indeks in overlappendePerioder.indices) {
            // Sjekker først om den første primærperioder dekker starten, og eventuelt hele den sekundære perioden
            if (indeks == 0) {
                if (overlappendePerioder[indeks].periodeFom.isBefore(sekundærperiode.periodeFom.plusDays(1))) {
                    if (overlappendePerioder[indeks].periodeTom == null) {
                        // Den primære perioden dekker hele den sekundære perioden
                        return null
                    } else {
                        if (sekundærperiode.periodeTom != null &&
                            overlappendePerioder[indeks].periodeTom?.isAfter(
                                sekundærperiode.periodeTom.plusDays(1),
                            ) == true
                        ) {
                            // Den primære perioden dekker hele den sekundære perioden
                            return null
                        } else {
                            // Den primære perioden dekker starten på den sekundære perioden og periodeFom må forskyves
                            periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1)
                        }
                    }
                } else {
                    // Den primære perioden overlapper etter starten på den sekundære perioden og periodeTom må forskyves på den sekundære perioden
                    periodeTom = overlappendePerioder[indeks].periodeFom.minusDays(1)
                }
                if (periodeTom != null) {
                    // Første primære periode starter etter sekundær periode. Den sekundære perioden skrives med justert tomdato. Senere i logikken
                    // må det sjekkes på om den sekundære perioden må splittes i mer enn én periode.
                    justertSekundærPeriodeListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = sekundærperiode.relatertPersonPersonId,
                            periodeFom = sekundærperiode.periodeFom,
                            periodeTom = periodeTom,
                            bostatus = sekundærperiode.bostatus,
                            fødselsdato = sekundærperiode.fødselsdato,
                            kilde = sekundærperiode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                }
            }
            if (indeks < overlappendePerioder.size - 1) {
                if (overlappendePerioder[indeks + 1].periodeFom.isAfter(overlappendePerioder[indeks].periodeTom!!.plusDays(1))) {
                    // Det er en åpen tidsperiode mellom to primære perioder, og den sekundære perioden skal fylle denne tidsperioden
                    periodeTom = overlappendePerioder[indeks + 1].periodeFom.minusDays(1)
                    justertSekundærPeriodeListe.add(
                        BoforholdResponse(
                            relatertPersonPersonId = sekundærperiode.relatertPersonPersonId,
                            // periodeFom er satt hvis første primære periode overlapper startdato for sekundære periode
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = periodeTom,
                            bostatus = sekundærperiode.bostatus,
                            fødselsdato = sekundærperiode.fødselsdato,
                            kilde = sekundærperiode.kilde,
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
                    if (sekundærperiode.periodeTom == null || sekundærperiode.periodeTom.isAfter(overlappendePerioder[indeks].periodeTom)) {
                        justertSekundærPeriodeListe.add(
                            BoforholdResponse(
                                relatertPersonPersonId = sekundærperiode.relatertPersonPersonId,
                                periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                                periodeTom = sekundærperiode.periodeTom,
                                bostatus = sekundærperiode.bostatus,
                                fødselsdato = sekundærperiode.fødselsdato,
                                kilde = sekundærperiode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertSekundærPeriodeListe
    }

    private fun beregnetAttenÅrFraDato(fødselsdato: LocalDate): LocalDate = fødselsdato.plusYears(18).plusMonths(1).withDayOfMonth(1)

    private fun beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
        beregnetPeriode: BoforholdResponse,
        offentligePerioder: List<BoforholdResponse>,
    ): Boolean = offentligePerioder.any { offentligPeriode ->
        beregnetPeriode.bostatus == offentligPeriode.bostatus &&
            beregnetPeriode.periodeFom.isAfter(offentligPeriode.periodeFom.minusDays(1)) &&
            (offentligPeriode.periodeTom == null || beregnetPeriode.periodeTom?.isBefore(offentligPeriode.periodeTom.plusDays(1)) == true)
    }

    private fun behandleEndringer(startdatoBeregning: LocalDate, boforholdBarnRequest: BoforholdBarnRequest): List<BoforholdResponse> {
        val endredePerioder = mutableListOf<BoforholdResponse>()
        val nyBostatus = boforholdBarnRequest.endreBostatus!!.nyBostatus
        val originalBostatus = boforholdBarnRequest.endreBostatus.originalBostatus

        when (boforholdBarnRequest.endreBostatus.typeEndring) {
            TypeEndring.SLETTET -> {
                if (originalBostatus == null) {
                    // Hvis det ikke finnes original bostatuskode så skal det ikke være mulig å slette en periode
                    secureLogger.info {
                        "Periode som skal slettes må være angitt som originalBostatus i input. endreBostatus: " +
                            "${boforholdBarnRequest.endreBostatus} "
                    }
                    throw IllegalStateException("Periode som skal slettes må være angitt som originalBostatus i input")
                }
                // Returnerer en periode med samme periodeFom og periodeTom som original periode med motsatt bostatuskode
                endredePerioder.add(
                    BoforholdResponse(
                        relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                        fødselsdato = boforholdBarnRequest.fødselsdato,
                        periodeFom = originalBostatus.periodeFom!!,
                        periodeTom = originalBostatus.periodeTom,
                        bostatus = motsattBostatuskode(originalBostatus.bostatus!!),
                        kilde = Kilde.MANUELL,
                    ),
                )
                return endredePerioder
            }

            TypeEndring.NY -> {
                if (nyBostatus == null) {
                    // Hvis det ikke finnes en ny bostatus så kan det ikke leges til ny periode
                    secureLogger.info {
                        "Periode som skal legges til må være angitt som nyBostatus i input. endreBostatus: " +
                            "${boforholdBarnRequest.endreBostatus} "
                    }
                    throw IllegalStateException("Periode som skal legges til mangler i input")
                }
                endredePerioder.add(
                    BoforholdResponse(
                        relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                        periodeFom = if (nyBostatus.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatus!!,
                        fødselsdato = boforholdBarnRequest.fødselsdato,
                        kilde = Kilde.MANUELL,
                    ),
                )
                return endredePerioder
            }

            TypeEndring.ENDRET -> {
                if (originalBostatus == null || nyBostatus == null) {
                    // Hvis det ikke finnes original bostatus eller ny bostatus så kan ikke periode endres
                    secureLogger.info {
                        "Periode som skal endres må være angitt som originalBostatus og ny verdier må ligge i " +
                            "nyBostatus i input. endreBostatus: ${boforholdBarnRequest.endreBostatus} "
                    }
                    throw IllegalStateException("OriginalBostatus og nyBostatus må være angitt for å kunne endre bostatus")
                }

                if (originalBostatus.periodeFom == nyBostatus.periodeFom && originalBostatus.periodeTom == nyBostatus.periodeTom) {
                    // Hvis periodene er uendret så returneres den nye bostatusen. Man vil komme hit hvis bare bostatuskode er endret.
                    endredePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                            periodeFom = nyBostatus.periodeFom!!,
                            periodeTom = nyBostatus.periodeTom,
                            bostatus = nyBostatus.bostatus!!,
                            fødselsdato = boforholdBarnRequest.fødselsdato,
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    return endredePerioder
                }

                if (originalBostatus.periodeTom != null && nyBostatus.periodeFom!!.isAfter(originalBostatus.periodeTom)) {
                    // Perioden er endret til å være helt utenfor original periode. Det må genereres en periode med motsatt bostatuskode i tidsrommet
                    // for den originale perioden, i tillegg til at den nye perioden legges til.
                    endredePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                            periodeFom = originalBostatus.periodeFom!!,
                            periodeTom = originalBostatus.periodeTom,
                            bostatus = motsattBostatuskode(originalBostatus.bostatus!!),
                            fødselsdato = boforholdBarnRequest.fødselsdato,
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    endredePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                            periodeFom = nyBostatus.periodeFom,
                            periodeTom = nyBostatus.periodeTom,
                            bostatus = nyBostatus.bostatus!!,
                            fødselsdato = boforholdBarnRequest.fødselsdato,
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    return endredePerioder
                }

                if (nyBostatus.periodeFom!!.isAfter(originalBostatus.periodeFom)) {
                    // Det må lages en ny periode med original bostatuskode for perioden mellom gammel og ny periodeFom
                    endredePerioder.add(
                        BoforholdResponse(
                            relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                            periodeFom = originalBostatus.periodeFom!!,
                            periodeTom = nyBostatus.periodeFom.minusDays(1),
                            bostatus = motsattBostatuskode(nyBostatus.bostatus!!),
                            fødselsdato = boforholdBarnRequest.fødselsdato,
                            kilde = originalBostatus.kilde,
                        ),
                    )
                }

                // Legger til den endrede perioden
                endredePerioder.add(
                    BoforholdResponse(
                        relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                        periodeFom = nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatus!!,
                        fødselsdato = boforholdBarnRequest.fødselsdato,
                        kilde = nyBostatus.kilde,
                    ),
                )
                // Sjekk om det må lages en ekstra periode etter nyBostatus med motsatt bostatuskode.
                if (originalBostatus.periodeTom == null) {
                    if (nyBostatus.periodeTom != null) {
                        // Det må lages en ny periode med motsatt bostatuskode for perioden mellom ny periodeTom og gammel periodeTom
                        endredePerioder.add(
                            BoforholdResponse(
                                relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                                periodeFom = nyBostatus.periodeTom.plusDays(1),
                                periodeTom = null,
                                bostatus = motsattBostatuskode(originalBostatus.bostatus!!),
                                fødselsdato = boforholdBarnRequest.fødselsdato,
                                kilde = originalBostatus.kilde,
                            ),
                        )
                    }
                } else {
                    if (nyBostatus.periodeTom != null && nyBostatus.periodeTom.isBefore(originalBostatus.periodeTom)) {
                        endredePerioder.add(
                            BoforholdResponse(
                                relatertPersonPersonId = boforholdBarnRequest.relatertPersonPersonId,
                                periodeFom = nyBostatus.periodeTom.plusDays(1),
                                periodeTom = originalBostatus.periodeTom,
                                bostatus = motsattBostatuskode(nyBostatus.bostatus),
                                fødselsdato = boforholdBarnRequest.fødselsdato,
                                kilde = originalBostatus.kilde,
                            ),
                        )
                    }
                }
                return endredePerioder
            }
        }
    }

    private fun motsattBostatuskode(bostatuskode: Bostatuskode): Bostatuskode {
        return when (bostatuskode) {
            Bostatuskode.MED_FORELDER -> return Bostatuskode.IKKE_MED_FORELDER
            Bostatuskode.IKKE_MED_FORELDER -> return Bostatuskode.MED_FORELDER
            Bostatuskode.DOKUMENTERT_SKOLEGANG -> return Bostatuskode.REGNES_IKKE_SOM_BARN
            else -> bostatuskode
        }
    }
}
