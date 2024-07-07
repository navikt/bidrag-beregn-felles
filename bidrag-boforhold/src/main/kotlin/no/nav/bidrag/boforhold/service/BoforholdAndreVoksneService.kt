package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.dto.BoforholdVoksneRequest
import no.nav.bidrag.boforhold.dto.BoforholdVoksneResponse
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.dto.EndreBostatus
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Familierelasjon
import java.time.LocalDate

internal class BoforholdAndreVoksneService {
    fun beregnBoforholdAndreVoksne(virkningstidspunkt: LocalDate, boforholdVoksne: BoforholdVoksneRequest): List<BoforholdVoksneResponse> {
        secureLogger.info { "Beregner om BP bor med andre voksne. Input: $virkningstidspunkt $boforholdVoksne" }

        val offentligeBostatusperioder = mutableListOf<Bostatus>()

        // Bygger opp en liste med alle perioder det har bodd en annen voksen i BPs husstand.
        boforholdVoksne.boforholdOffentligeOpplysninger
            .filter { it.relasjon != Familierelasjon.BARN }
            .filter { it.fødselsdato.isBefore(virkningstidspunkt.minusYears(18)) }
            .forEach { offentligeBostatusperioder.addAll(it.innhentedeOffentligeOpplysninger) }

        val sammenslåtteBostatusperioder =
            slåSammenBostatusperioder(
                offentligeBostatusperioder.filter {
                    it.periodeTom == null ||
                        it.periodeTom.isAfter(virkningstidspunkt.plusDays(1))
                },
            )

        return beregnPerioder(
            virkningstidspunkt,
            sammenslåtteBostatusperioder,
            boforholdVoksne.behandledeBostatusopplysninger,
            boforholdVoksne.endreBostatus,
        )
    }

    private fun beregnPerioder(
        virkningstidspunkt: LocalDate,
        offentligeBostatusperioder: List<Bostatus>,
        behandledeBostatusopplysninger: List<Bostatus>,
        endreBostatus: EndreBostatus?,
    ): List<BoforholdVoksneResponse> {
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
        val startdatoBeregning = virkningstidspunkt

        // Filterer først bort alle offentlige perioder som avsluttes før startdatoBeregning
        // Justerer så offentlige perioder slik at de starter på første dag i måneden og slutter på siste dag i måneden
        val justerteOffentligePerioder = offentligeBostatusperioder
            .sortedWith(compareBy<Bostatus> { it.periodeFom }.thenBy { it.periodeTom })
            .map {
                BoforholdVoksneResponse(
                    periodeFom = if (it.periodeFom == null) startdatoBeregning else it.periodeFom.withDayOfMonth(1),
                    periodeTom = it.periodeTom?.plusMonths(1)?.withDayOfMonth(1)?.minusDays(1),
                    bostatus = it.bostatusKode ?: Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                )
            }

        // Filterer først bort alle perioder med behandlede opplysninger som avsluttes før startdatoBeregning
        val behandledeOpplysninger = behandledeBostatusopplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }.map {
                BoforholdVoksneResponse(
                    periodeFom = if (it.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else it.periodeFom,
                    periodeTom = it.periodeTom,
                    bostatus = it.bostatusKode!!,
                    kilde = it.kilde,
                )
            }

        if (justerteOffentligePerioder.isEmpty() && behandledeOpplysninger.isEmpty() && endreBostatus == null) {
            // Ingen perioder innenfor beregningsperiode. Dette skal ikke forekomme. Hvis det ikke finnes offentlige perioder så skal
            // bidrag-behandling legge til en periode med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG i input.
            // Unntaket er hvis barnet er manuelt lagt til, da skal det ikke finnes offentlige perioder, kun manuelle perioder.
            return emptyList()
        }

        if (endreBostatus == null) {
            if (behandledeOpplysninger.isNotEmpty()) {
                // virkningstidspunkt eller offentlige perioder er endret, juster og fyll inn med offentlig informasjon.
                val sammenslåtteBehandledeOgOffentligePerioder =
                    slåSammenPrimærOgSekundærperioder(behandledeOpplysninger, justerteOffentligePerioder)

                // Slår sammen sammenhengende perioder med lik Bostatuskode
                val sammenslåttListe = slåSammenPerioderOgJusterPeriodeTom(sammenslåtteBehandledeOgOffentligePerioder)

                return sammenslåttListe.map {
                    BoforholdVoksneResponse(
                        periodeFom = it.periodeFom,
                        periodeTom = it.periodeTom,
                        bostatus = it.bostatus,
                        kilde = if (it.kilde == Kilde.MANUELL) {
                            if (beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
                                    it,
                                    justerteOffentligePerioder,
                                )
                            ) {
                                Kilde.OFFENTLIG
                            } else {
                                Kilde.MANUELL
                            }
                        } else {
                            it.kilde
                        },
                    )
                }
            }
            // Førstegangs beregning av boforhold for barnet. Beregn fra innhentede offentlige opplysninger.
            if (justerteOffentligePerioder.isEmpty()) {
                return emptyList()
            } else {
                // Slår sammen sammenhengende perioder med lik Bostatuskode.
                return slåSammenPerioderOgJusterPeriodeTom(justerteOffentligePerioder)
            }
        }

        val oppdaterteBehandledeOpplysninger = behandleEndringer(startdatoBeregning, endreBostatus, behandledeOpplysninger)

        if (behandledeOpplysninger.isEmpty()) {
            // Det finnes ingen behandlede perioder og den nye bostatusperioden skal returneres sammen med genererte perioder
            // som fyller tidslinjen fra virkningstidspunkt til dagens dato.

            if (endreBostatus.typeEndring != TypeEndring.NY) {
                // Feilsituasjon. Må alltid være ny hvis det ikke finnes perioder fra før.
                return emptyList()
            }

            return slåSammenPrimærOgSekundærperioder(oppdaterteBehandledeOpplysninger, justerteOffentligePerioder)
        }

        // Det finnes både behandlede og endrede perioder

        return oppdaterteBehandledeOpplysninger.map {
            BoforholdVoksneResponse(
                periodeFom = it.periodeFom,
                periodeTom = it.periodeTom,
                bostatus = it.bostatus,
                kilde = if (beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
                        it,
                        justerteOffentligePerioder,
                    )
                ) {
                    Kilde.OFFENTLIG
                } else {
                    Kilde.MANUELL
                },
            )
        }
    }

    private fun slåSammenPerioderOgJusterPeriodeTom(liste: List<BoforholdVoksneResponse>): List<BoforholdVoksneResponse> {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        var kilde: Kilde? = null
        val sammenslåttListe = mutableListOf<BoforholdVoksneResponse>()

        for (indeks in liste.sortedBy { it.periodeFom }.indices) {
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
                            BoforholdVoksneResponse(
                                periodeFom = periodeFom,
                                periodeTom = liste[indeks].periodeTom ?: periodeTom,
                                bostatus = liste[indeks].bostatus,
                                kilde = kilde ?: liste[indeks].kilde,

                            ),
                        )
                        periodeFom = null
                        kilde = null
                    } else {
                        sammenslåttListe.add(
                            BoforholdVoksneResponse(
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom ?: periodeTom,
                                bostatus = liste[indeks].bostatus,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                    }
                }
            } else {
                // Siste forekomst
                sammenslåttListe.add(
                    BoforholdVoksneResponse(
                        periodeFom = periodeFom ?: liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                        kilde = kilde ?: liste[indeks].kilde,

                    ),
                )
            }
        }
        return sammenslåttListe.sortedBy { it.periodeFom }
    }

    private fun justerMotAttenårsdag(attenårFraDato: LocalDate, liste: List<BoforholdVoksneResponse>): List<BoforholdVoksneResponse> {
        val listeJustertMotAttenårsdag = mutableListOf<BoforholdVoksneResponse>()

        if (attenårFraDato.isAfter(LocalDate.now())) {
            // Barnet har ikke fyllt 18 og listen returneres uendret.
            return liste
        } else {
            for (indeks in liste.sortedBy { it.periodeFom }.indices) {
                val bostatuskodeAttenÅr =
                    if (liste[indeks].kilde == Kilde.MANUELL) {
                        if (liste[indeks].bostatus == Bostatuskode.MED_FORELDER) {
                            Bostatuskode.REGNES_IKKE_SOM_BARN
                        } else {
                            if (liste[indeks].bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG) {
                                Bostatuskode.DOKUMENTERT_SKOLEGANG
                            } else {
                                Bostatuskode.IKKE_MED_FORELDER
                            }
                        }
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
                            BoforholdVoksneResponse(
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = attenårFraDato.minusDays(1),
                                bostatus = liste[indeks].bostatus,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                        listeJustertMotAttenårsdag.add(
                            BoforholdVoksneResponse(
                                periodeFom = attenårFraDato,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = bostatuskodeAttenÅr,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                    } else {
                        listeJustertMotAttenårsdag.add(
                            BoforholdVoksneResponse(
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = bostatuskodeAttenÅr,
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
        primærperioder: List<BoforholdVoksneResponse>,
        sekundærperioder: List<BoforholdVoksneResponse>,
    ): List<BoforholdVoksneResponse> {
        val resultatliste = mutableListOf<BoforholdVoksneResponse>()

        // Skriver alle primærperioder til resultatet.
        primærperioder.sortedBy { it.periodeFom }.forEach { primærperiode ->
            resultatliste.add(
                BoforholdVoksneResponse(
                    periodeFom = primærperiode.periodeFom,
                    periodeTom = primærperiode.periodeTom,
                    bostatus = primærperiode.bostatus,
                    kilde = primærperiode.kilde,
                ),
            )
        }

        // Sjekker sekundærperioder og justerer periodeFom og periodeTom der disse overlapper med primærperioder.
        // Sekundærperioder som helt dekkes av primærperioder skrives ikke til resultatet.
        sekundærperioder.sortedBy { it.periodeFom }.forEach { sekundærperiode ->
            // Finner sekundærperioder som overlapper med den primære perioden
            val overlappendePerioder = mutableListOf<BoforholdVoksneResponse>()
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
            val sammenslåttListeOverlappendePerioder = mutableListOf<BoforholdVoksneResponse>()

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

            val justertSekundærperiode = justerSekundærperiode(sekundærperiode, sammenslåttListeOverlappendePerioder)
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
    private fun justerSekundærperiode(
        sekundærperiode: BoforholdVoksneResponse,
        overlappendePerioder: List<BoforholdVoksneResponse>,
    ): List<BoforholdVoksneResponse>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertSekundærPeriodeListe = mutableListOf<BoforholdVoksneResponse>()

        if (overlappendePerioder.isNullOrEmpty()) {
            return listOf(sekundærperiode)
        }

        for (indeks in overlappendePerioder.sortedBy { it.periodeFom }.indices) {
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
                        BoforholdVoksneResponse(
                            periodeFom = sekundærperiode.periodeFom,
                            periodeTom = periodeTom,
                            bostatus = sekundærperiode.bostatus,
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
                        BoforholdVoksneResponse(
                            // periodeFom er satt hvis første primære periode overlapper startdato for sekundær periode
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = periodeTom,
                            bostatus = sekundærperiode.bostatus,
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
                            BoforholdVoksneResponse(
                                periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                                periodeTom = sekundærperiode.periodeTom,
                                bostatus = sekundærperiode.bostatus,
                                kilde = sekundærperiode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertSekundærPeriodeListe.sortedBy { it.periodeFom }
    }

    private fun beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
        beregnetPeriode: BoforholdVoksneResponse,
        offentligePerioder: List<BoforholdVoksneResponse>,
    ): Boolean = offentligePerioder.any { offentligPeriode ->
        beregnetPeriode.bostatus == offentligPeriode.bostatus &&
            beregnetPeriode.periodeFom.isAfter(offentligPeriode.periodeFom.minusDays(1)) &&
            (offentligPeriode.periodeTom == null || beregnetPeriode.periodeTom?.isBefore(offentligPeriode.periodeTom.plusDays(1)) == true)
    }

    private fun behandleEndringer(
        startdatoBeregning: LocalDate,
        endreBostatus: EndreBostatus,
        behandledeOpplysninger: List<BoforholdVoksneResponse>,
    ): List<BoforholdVoksneResponse> {
        val endredePerioder = mutableListOf<BoforholdVoksneResponse>()
        val nyBostatus = endreBostatus!!.nyBostatus
        val originalBostatus = endreBostatus.originalBostatus

        when (endreBostatus.typeEndring) {
            TypeEndring.SLETTET -> {
                if (originalBostatus == null) {
                    // Hvis det ikke finnes original bostatuskode så skal det ikke være mulig å slette en periode
                    secureLogger.info {
                        "Periode som skal slettes må være angitt som originalBostatus i input. endreBostatus: " +
                            "$endreBostatus "
                    }
                    throw IllegalStateException("Periode som skal slettes må være angitt som originalBostatus i input")
                }

                val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                if (indeksMatch == 0) {
                    // Stemmer kommentar under?
                    secureLogger.info {
                        "Periode som skal slettes er første periode i behandledeOpplysninger, denne kan ikke slettes . " +
                            "endreBostatus: " +
                            "$endreBostatus.endreBostatus "
                    }
                }

                for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                    if (indeks == indeksMatch!! - 1) {
                        // Periode før periode som skal slettes. Justerer periodeTom til å være lik slettet periodes periodeTom.
                        endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeTom = originalBostatus.periodeTom))
                    } else {
                        if (indeks == indeksMatch) {
                            // Periode som skal slettes. Hopper over denne.
                            continue
                        } else {
                            endredePerioder.add(behandledeOpplysninger[indeks])
                        }
                    }
                }

                if (endredePerioder.isEmpty()) {
                    return emptyList()
                } else {
                    // Gjør en sammenslåing av perioder med lik bostatuskode og justerer periodeTom
                    return slåSammenPerioderOgJusterPeriodeTom(endredePerioder)
                }
            }

            TypeEndring.NY -> {
                if (nyBostatus == null) {
                    // Hvis det ikke finnes en ny bostatus så kan det ikke leges til ny periode
                    secureLogger.info {
                        "Periode som skal legges til må være angitt som nyBostatus i input. endreBostatus: " +
                            "$endreBostatus "
                    }
                    throw IllegalStateException("Periode som skal legges til mangler i input")
                }
                endredePerioder.add(
                    BoforholdVoksneResponse(
                        periodeFom = if (nyBostatus.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatusKode!!,
                        kilde = Kilde.MANUELL,
                    ),
                )
                val sammenslåttListe = slåSammenPrimærOgSekundærperioder(endredePerioder, behandledeOpplysninger)
                return slåSammenPerioderOgJusterPeriodeTom(sammenslåttListe)
            }

            TypeEndring.ENDRET -> {
                if (originalBostatus == null || nyBostatus == null) {
                    // Hvis det ikke finnes original bostatus eller ny bostatus så kan ikke periode endres
                    secureLogger.info {
                        "Periode som skal endres må være angitt som originalBostatus og ny verdier må ligge i " +
                            "nyBostatus i input. endreBostatus: $endreBostatus "
                    }
                    throw IllegalStateException("OriginalBostatus og nyBostatus må være angitt for å kunne endre bostatus")
                }

                val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                if (originalBostatus.periodeFom == nyBostatus.periodeFom && originalBostatus.periodeTom == nyBostatus.periodeTom) {
                    // Ny bostatus har samme datoer som original, og denne erstattes med ny bostatus.

                    for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                        if (indeks == indeksMatch) {
                            // Periode som skal erstattes.
                            endredePerioder.add(
                                BoforholdVoksneResponse(
                                    periodeFom = nyBostatus.periodeFom!!,
                                    periodeTom = nyBostatus.periodeTom,
                                    bostatus = nyBostatus.bostatusKode!!,
                                    kilde = Kilde.MANUELL,
                                ),
                            )
                        } else {
                            endredePerioder.add(behandledeOpplysninger[indeks])
                        }
                    }
                    return slåSammenPerioderOgJusterPeriodeTom(endredePerioder)
                }

                if (originalBostatus.periodeTom != null && nyBostatus.periodeFom!!.isAfter(originalBostatus.periodeTom)) {
                    // Perioden er endret til å være helt utenfor original periode. Eventuell periode før den originale perioden får satt periodeTom
                    // lik periodeTom for den originale perioden. Den originale perioden slettes i tillegg til at den nye perioden legges til.

                    val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                    for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                        if (indeks == indeksMatch!! - 1) {
                            // Periode før periode som skal slettes. Justerer periodeTom til å være lik slettet periodes periodeTom.
                            endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeTom = originalBostatus.periodeTom))
                        } else {
                            if (indeks == indeksMatch) {
                                // Periode som skal slettes. Hopper over denne.
                                continue
                            } else {
                                endredePerioder.add(behandledeOpplysninger[indeks])
                            }
                        }
                    }

                    val nyPeriode = listOf(
                        BoforholdVoksneResponse(
                            periodeFom = nyBostatus.periodeFom,
                            periodeTom = nyBostatus.periodeTom,
                            bostatus = nyBostatus.bostatusKode!!,
                            kilde = Kilde.MANUELL,
                        ),
                    )

                    // Gjør en sammenslåing av perioder med lik bostatuskode og justerer periodeTom
                    val sammenslåttListe = slåSammenPrimærOgSekundærperioder(nyPeriode, endredePerioder)
                    return slåSammenPerioderOgJusterPeriodeTom(sammenslåttListe)
                }

                if (nyBostatus.periodeFom!!.isAfter(originalBostatus.periodeFom)) {
                    val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                    // Sjekk om det finnes en periode før den som endres og endre periodeTom på denne til å bli lik periodeFom på den nye perioden.
                    // Hvis bostatuskode er endret i nyBostatus så forkortes i stedet periodeTom for originalBostatus

                    if (originalBostatus.bostatusKode != nyBostatus.bostatusKode) {
                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks < indeksMatch!!) {
                                // Skriver alle perioder før originalBostatus til endredePerioder.
                                endredePerioder.add(behandledeOpplysninger[indeks])
                            }
                        }
                        endredePerioder.add(
                            BoforholdVoksneResponse(
                                periodeFom = originalBostatus.periodeFom!!,
                                periodeTom = nyBostatus.periodeFom.minusDays(1),
                                bostatus = originalBostatus.bostatusKode!!,
                                kilde = originalBostatus.kilde,
                            ),
                        )
                    } else {
                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks == indeksMatch!! - 1) {
                                // Periode før periode som skal slettes. Justerer periodeTom til å være lik slettet periodes periodeTom.
                                endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeTom = nyBostatus.periodeFom.minusDays(1)))
                            }
                        }
                    }
                }

                // Legger til den endrede perioden
                endredePerioder.add(
                    BoforholdVoksneResponse(
                        periodeFom = nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatusKode!!,
                        kilde = nyBostatus.kilde,
                    ),
                )
                // Hvis nyBostatus ikke dekker hele perioden til originalBostatus så må det sjekkes om det finnes en periode etter nyBostatus.
                // PeriodeFom må i så fall endres på denne til å bli lik nyBostatus' periodeTom pluss én dag.
                if ((originalBostatus.periodeTom == null && nyBostatus.periodeTom != null) ||
                    (nyBostatus.periodeTom != null && nyBostatus.periodeTom.isBefore(originalBostatus.periodeTom))
                ) {
                    // Sjekker først om bostatuskode er endret i nyBostatus. Hvis den er det så beholdes gjenstånde del av originalBostatus med
                    // justert perioderFom

                    if (originalBostatus.bostatusKode != nyBostatus.bostatusKode) {
                        endredePerioder.add(
                            BoforholdVoksneResponse(
                                periodeFom = nyBostatus.periodeTom.plusDays(1),
                                periodeTom = originalBostatus.periodeTom,
                                bostatus = originalBostatus.bostatusKode!!,
                                kilde = originalBostatus.kilde,
                            ),
                        )
                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks > indeksMatch!!) {
                                // Skriver alle perioder etter originalBostatus til endredePerioder.
                                endredePerioder.add(behandledeOpplysninger[indeks])
                            }
                        }
                    } else {
                        val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks == indeksMatch!! + 1) {
                                // Periode etter endret periode. Justerer periodeFom til å være lik endret periodes periodeTom pluss én dag.
                                endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeFom = nyBostatus.periodeTom.plusDays(1)))
                            } else {
                                if (indeks == indeksMatch) {
                                    // Periode som skal erstattes. Hopper over denne.
                                    continue
                                } else {
                                    endredePerioder.add(behandledeOpplysninger[indeks])
                                }
                            }
                        }
                    }
//
//                    endredePerioder.add(
//                        BoforholdResponseV2(
//                            gjelderPersonId = boforholdRequest.gjelderPersonId,
//                            periodeFom = nyBostatus.periodeTom.plusDays(1),
//                            periodeTom = originalBostatus.periodeTom,
//                            bostatus = originalBostatus.bostatusKode!!,
//                            fødselsdato = boforholdRequest.fødselsdato,
//                            kilde = originalBostatus.kilde,
//                        ),
//                    )
                }
                return slåSammenPerioderOgJusterPeriodeTom(endredePerioder)
            }
        }
    }

    private fun perioderErIdentiske(periode1: Bostatus, periode2: BoforholdVoksneResponse): Boolean = periode1.periodeFom == periode2.periodeFom &&
        periode1.periodeTom == periode2.periodeTom &&
        periode1.bostatusKode == periode2.bostatus &&
        periode1.kilde == periode2.kilde

    private fun finnIndeksMatch(periode: Bostatus, liste: List<BoforholdVoksneResponse>): Int {
        var indeksMatch: Int? = null
        for (indeks in liste.indices) {
            if (perioderErIdentiske(periode, liste[indeks])) {
                indeksMatch = indeks
            }
        }
        if (indeksMatch == null) {
            secureLogger.info {
                "Feil ved hent av indeks for matchende periode, perioden finnes ikke. Innsendt periode: " +
                    "$periode behandledeOpplysninger: $liste"
            }
            throw IllegalStateException("Periode som forsøkes endres finnes ikke i innsendt liste med perioder.")
        }
        return indeksMatch
    }

    private fun slåSammenBostatusperioder(offentligeBostatusperioder: List<Bostatus>): List<Bostatus> {
        val resultatliste = mutableListOf<Bostatus>()
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        for (indeks in offentligeBostatusperioder.sortedWith(compareBy<Bostatus> { it.periodeFom }.thenBy { it.periodeTom }).indices) {
            if (indeks == 0) {
                periodeFom = offentligeBostatusperioder[indeks].periodeFom
                periodeTom = offentligeBostatusperioder[indeks].periodeTom
                if (offentligeBostatusperioder.size == 1) {
                    resultatliste.add(offentligeBostatusperioder[indeks].copy(bostatusKode = Bostatuskode.BOR_MED_ANDRE_VOKSNE))
                }
            } else {
                if (offentligeBostatusperioder[indeks].periodeFom!!.isBefore(periodeTom?.plusDays(2))) {
                    // perioden overlapper med forrige periode
                    periodeTom = offentligeBostatusperioder[indeks].periodeTom
                    if (indeks == offentligeBostatusperioder.size - 1) {
                        resultatliste.add(Bostatus(periodeFom, periodeTom, Bostatuskode.BOR_MED_ANDRE_VOKSNE, Kilde.OFFENTLIG))
                    }
                } else {
                    resultatliste.add(Bostatus(periodeFom, periodeTom, Bostatuskode.BOR_MED_ANDRE_VOKSNE, Kilde.OFFENTLIG))
                    if (indeks == offentligeBostatusperioder.size - 1) {
                        resultatliste.add(offentligeBostatusperioder[indeks].copy(bostatusKode = Bostatuskode.BOR_MED_ANDRE_VOKSNE))
                    }
                }
            }
        }
        return resultatliste
    }
}
