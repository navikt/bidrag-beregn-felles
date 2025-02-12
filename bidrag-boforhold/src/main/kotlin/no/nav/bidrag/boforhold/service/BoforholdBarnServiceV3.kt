package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.dto.BoforholdBarnRequestV3
import no.nav.bidrag.boforhold.dto.BoforholdResponseV2
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.behandling.TypeBehandling
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Familierelasjon
import java.time.LocalDate

internal class BoforholdBarnServiceV3 {
    fun beregnBoforholdBarn(
        virkningstidspunkt: LocalDate,
        typeBehandling: TypeBehandling?,
        boforholdGrunnlagListe: List<BoforholdBarnRequestV3>,
        opphørsdato: LocalDate? = null,
    ): List<BoforholdResponseV2> {
        secureLogger.info { "Beregner bostatus for BM/BPs egne barn V3. Input: $virkningstidspunkt $boforholdGrunnlagListe" }

        val resultat = mutableListOf<BoforholdResponseV2>()
        boforholdGrunnlagListe
            .filter { relatertPerson ->
                relatertPerson.relasjon == Familierelasjon.BARN ||
                    relatertPerson.behandledeBostatusopplysninger.isNotEmpty() ||
                    relatertPerson.endreBostatus != null
            }
            .sortedWith(
                compareBy { it.gjelderPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderForBarn(virkningstidspunkt, typeBehandling!!, barn, opphørsdato))
            }

        secureLogger.info { "Resultat av beregning bostatus for BM/BPs egne barn V3: $resultat" }

        return resultat
    }

    private fun beregnPerioderForBarn(
        virkningstidspunkt: LocalDate,
        typeBehandling: TypeBehandling,
        boforholdRequest: BoforholdBarnRequestV3,
        opphørsdato: LocalDate?,
    ): List<BoforholdResponseV2> {
        // 1. endreBoforhold = null. Beregning gjøres da enten på offentlige opplysninger eller behandledeBostatusopplysninger.
        //    1a. Hvis behandledeBostatusopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt:
        //        behandledeBostatusopplysninger skal da justeres mot virkningstidspunkt. Perioder i behandledeBostatusopplysninger sjekkes mot
        //        offentlige perioder og kilde evt. endres til Offentlig hvis det er match. Dette vil kunne skje ved endring av innhentede offentlige
        //        opplysninger som nå helt overlapper manuelt innlagte perioder.
        //        I tilfeller der virkningstidspunkt forskyves tilbake i tid så skal tidslinjen suppleres med offentlige perioder.
        //    1b. Hvis behandledeBostatusopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom:
        //        behandledeBostatusopplysninger skal da justeres mot virkningstidspunkt. I tilfeller der virkningstidspunkt forskyves tilbake i
        //        tid så skal tidslinjen suppleres med én offentlig perioder med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG i tidsrommet
        //        mellom virkningstidspunkt og periodeFom for første forekomst i behandledeBostatusopplysninger.
        //    1c. Hvis behandledeBostatusopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Det gjøres da en beregning basert på
        //        offentlige perioder.
        //    1d. Hvis behandledeBostatusopplysninger er tom og innhentedeOffentligeOpplysninger  er tom: Det skal legges til en periode med
        //        Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG
        // 2. endreBoforhold er utfyllt.
        //    2a. Hvis behandledeBostatusopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt: behandledeBostatusopplysninger
        //        skal da justeres etter det som er sendt inn i endreBoforhold. Det kan slettes/legges til eller endres perioder.
        //        Perioder i oppdaterte behandledeBostatusopplysninger sjekkes mot offentlige perioder og kilde evt. endres til Offentlig hvis det
        //        er match. Kun manuelle perioder kan slettes.
        //    2b. Hvis behandledeBostatusopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom: behandledeBostatusopplysninger skal
        //        da justeres etter det som er sendt inn i endreBoforhold. Det kan slettes/legges til eller endres perioder.
        //        Perioder i oppdaterte behandledeBostatusopplysninger sjekkes mot genererte offentlige perioder (IKKE_MED_FORELDER/
        //        REGNES_IKKE_SOM_BARN). Kilde endres til Offentlig hvis det er match.
        //    2c. Hvis behandledeBostatusopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Feil.
        //        Det bør da i stedet gjøres en beregning på offentlige perioder før det kan sendes en endreBoforhold-request.
        //        Beregningen ignorerer innhentedeOffentligeOpplysninger og gjør en beregning på det som ligger i endreBoforhold. typeEndring må
        //        være lik NY, hvis ikke reurneres tom liste. Hull i tidslinjen utfylles med Bostatuskode = IKKE_MED_FORELDER og Kilde = MANUELL.
        //    2d. Hvis behandledeBostatusopplysninger er tom og innhentedeOffentligeOpplysninger er tom: Beregningen gjøres på det som ligger i
        //        endreBoforhold. typeEndring må være lik NY, hvis ikke reurneres tom liste.
        //        Hull i tidslinjen utfylles med Bostatuskode = IKKE_MED_FORELDER og Kilde = MANUELL.

        // Bruker fødselsdato som startdato for beregning hvis barnet er født etter virkningstidspunkt
        val startdatoBeregning = if (virkningstidspunkt.isBefore(boforholdRequest.fødselsdato)) {
            boforholdRequest.fødselsdato.withDayOfMonth(1)
        } else {
            virkningstidspunkt
        }

        // Filterer først bort alle offentlige perioder som avsluttes før startdatoBeregning
        // Justerer så offentlige perioder slik at de starter på første dag i måneden og slutter på siste dag i måneden
        val justerteOffentligePerioder = boforholdRequest.innhentedeOffentligeOpplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }
            .map {
                BoforholdResponseV2(
                    gjelderPersonId = boforholdRequest.gjelderPersonId,
                    periodeFom = if (it.periodeFom == null) startdatoBeregning else it.periodeFom.withDayOfMonth(1),
                    periodeTom = it.periodeTom?.plusMonths(1)?.withDayOfMonth(1)?.minusDays(1),
                    bostatus = it.bostatus ?: Bostatuskode.MED_FORELDER,
                    fødselsdato = boforholdRequest.fødselsdato,
                    kilde = Kilde.OFFENTLIG,
                )
            }

        // Filterer først bort alle perioder med behandlede opplysninger som avsluttes før startdatoBeregning
        val behandledeOpplysninger = boforholdRequest.behandledeBostatusopplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(startdatoBeregning)) }
            .sortedBy { it.periodeFom }.map {
                BoforholdResponseV2(
                    gjelderPersonId = boforholdRequest.gjelderPersonId,
                    periodeFom = if (it.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else it.periodeFom,
                    periodeTom = it.periodeTom,
                    bostatus = it.bostatus!!,
                    fødselsdato = boforholdRequest.fødselsdato,
                    kilde = it.kilde,
                )
            }

        if (justerteOffentligePerioder.isEmpty() && behandledeOpplysninger.isEmpty() && boforholdRequest.endreBostatus == null) {
            // Ingen perioder innenfor beregningsperiode. Dette skal ikke forekomme. Hvis det ikke finnes offentlige perioder så skal
            // bidrag-behandling legge til en periode med Bostatuskode = IKKE_MED_FORELDER og Kilde = OFFENTLIG i input.
            // Unntaket er hvis barnet er manuelt lagt til, da skal det ikke finnes offentlige perioder, kun manuelle perioder.
            return emptyList()
        }

        // Finner 18-årsdagen til barnet, settes lik første dag i måneden etter 18-årsdagen
        val attenårFraDato = beregnetAttenÅrFraDato(boforholdRequest.fødselsdato)

        if (boforholdRequest.endreBostatus == null) {
            if (behandledeOpplysninger.isNotEmpty()) {
                // virkningstidspunkt eller offentlige perioder er endret, juster og fyll inn med offentlig informasjon.
                val komplettOffentligTidslinje =
                    fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag =
                    justerMotAttenårsdag(attenårFraDato, typeBehandling, komplettOffentligTidslinje)
                val sammenslåtteBehandledeOgOffentligePerioder =
                    slåSammenPrimærOgSekundærperioder(behandledeOpplysninger, offentligePerioderJustertMotAttenårsdag)

                // Slår sammen sammenhengende perioder med lik Bostatuskode
                val sammenslåttListe = slåSammenPerioderOgJusterPeriodeTom(sammenslåtteBehandledeOgOffentligePerioder)

                return justerPerioderForOpphørsdato(sammenslåttListe, opphørsdato).map {
                    BoforholdResponseV2(
                        gjelderPersonId = it.gjelderPersonId,
                        periodeFom = it.periodeFom,
                        periodeTom = it.periodeTom,
                        bostatus = it.bostatus,
                        fødselsdato = it.fødselsdato,
                        kilde = if (it.kilde == Kilde.MANUELL) {
                            if (beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
                                    it,
                                    offentligePerioderJustertMotAttenårsdag,
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
                // Fyller ut perioder der det ikke finnes informasjon om barnet i offentlige opplysninger
                val komplettOffentligTidslinje =
                    fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
                // Justerer offentlige perioder mot 18-årsdager og lager bruddperiode hvis barnet fyllet 18 år i perioden bor i husstanden
                val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, typeBehandling, komplettOffentligTidslinje)
                // Slår sammen sammenhengende perioder med lik Bostatuskode.
                return justerPerioderForOpphørsdato(slåSammenPerioderOgJusterPeriodeTom(offentligePerioderJustertMotAttenårsdag), opphørsdato)
            }
        }

        val oppdaterteBehandledeOpplysninger =
            behandleEndringer(startdatoBeregning, attenårFraDato, typeBehandling, boforholdRequest, behandledeOpplysninger)

        if (behandledeOpplysninger.isEmpty()) {
            // Det finnes ingen behandlede perioder og den nye bostatusperioden skal returneres sammen med genererte perioder
            // som fyller tidslinjen fra virkningstidspunkt til dagens dato.

            if (boforholdRequest.endreBostatus.typeEndring != TypeEndring.NY) {
                // Feilsituasjon. Må alltid være ny hvis det ikke finnes perioder fra før.
                return emptyList()
            }

            return justerPerioderForOpphørsdato(
                slåSammenPrimærOgSekundærperioder(oppdaterteBehandledeOpplysninger, justerteOffentligePerioder),
                opphørsdato,
            )
        }

        // Det finnes både behandlede og endrede perioder

        // Lager komplett tidslinje basert på offentlige opplysninger for å kunne sjekke alle perioder mot offentlige perioder. Alle beregnede
        // perioder som her helt innenfor en offentlig periode med lik bostatuskode får kilde = OFFENTLIG.
        val komplettOffentligTidslinje = fyllUtMedPerioderBarnetIkkeBorIHusstanden(Kilde.OFFENTLIG, startdatoBeregning, justerteOffentligePerioder)
        val offentligePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, typeBehandling, komplettOffentligTidslinje)

        val sammenslåtteBehandledeOgOffentligePerioder =
            slåSammenPrimærOgSekundærperioder(oppdaterteBehandledeOpplysninger, offentligePerioderJustertMotAttenårsdag)

        return justerPerioderForOpphørsdato(sammenslåtteBehandledeOgOffentligePerioder, opphørsdato).map {
            BoforholdResponseV2(
                gjelderPersonId = it.gjelderPersonId,
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
    private fun justerPerioderForOpphørsdato(grunnlagsliste: List<BoforholdResponseV2>, opphørsdato: LocalDate?): List<BoforholdResponseV2> {
        if (opphørsdato == null) return grunnlagsliste
        // Antar at opphørsdato er måneden perioden skal opphøre
        val justerOpphørsdato = opphørsdato.withDayOfMonth(1).minusDays(1)
        return grunnlagsliste.filter {
            it.periodeFom.isBefore(justerOpphørsdato)
        }
            .map { grunnlag ->
                if (grunnlag.periodeTom == null || grunnlag.periodeTom.isAfter(justerOpphørsdato)) {
                    grunnlag.copy(periodeTom = justerOpphørsdato)
                } else {
                    grunnlag
                }
            }
    }
    private fun slåSammenPerioderOgJusterPeriodeTom(liste: List<BoforholdResponseV2>): List<BoforholdResponseV2> {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        var kilde: Kilde? = null
        val sammenslåttListe = mutableListOf<BoforholdResponseV2>()

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
                            BoforholdResponseV2(
                                gjelderPersonId = liste[indeks].gjelderPersonId,
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
                            BoforholdResponseV2(
                                gjelderPersonId = liste[indeks].gjelderPersonId,
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
                    BoforholdResponseV2(
                        gjelderPersonId = liste[indeks].gjelderPersonId,
                        periodeFom = periodeFom ?: liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                        fødselsdato = liste[indeks].fødselsdato,
                        kilde = kilde ?: liste[indeks].kilde,

                    ),
                )
            }
        }
        return sammenslåttListe.sortedBy { it.periodeFom }
    }

    private fun fyllUtMedPerioderBarnetIkkeBorIHusstanden(
        kilde: Kilde,
        startdatoBeregning: LocalDate,
        liste: List<BoforholdResponseV2>,
    ): List<BoforholdResponseV2> {
        val sammenhengendePerioderListe = mutableListOf<BoforholdResponseV2>()

        for (indeks in liste.sortedBy { it.periodeFom }.indices) {
//            Sjekker første forekomst og danner periode mellom startdatoBeregning og første forekomst hvis det er opphold
            if (indeks == 0) {
                if (liste[indeks].periodeFom.isAfter(startdatoBeregning)) {
                    sammenhengendePerioderListe.add(
                        BoforholdResponseV2(
                            gjelderPersonId = liste[indeks].gjelderPersonId,
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
                        BoforholdResponseV2(
                            gjelderPersonId = liste[indeks].gjelderPersonId,
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
                    BoforholdResponseV2(
                        gjelderPersonId = liste[indeks].gjelderPersonId,
                        periodeFom = liste[indeks].periodeTom!!.plusDays(1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        fødselsdato = liste[indeks].fødselsdato,
                        kilde = kilde,
                    ),
                )
            }
        }
        return sammenhengendePerioderListe.sortedBy { it.periodeFom }
    }

    private fun justerMotAttenårsdag(
        attenårFraDato: LocalDate,
        typeBehandling: TypeBehandling,
        liste: List<BoforholdResponseV2>,
    ): List<BoforholdResponseV2> {
        val listeJustertMotAttenårsdag = mutableListOf<BoforholdResponseV2>()

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
                            } else if (liste[indeks].bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN) {
                                Bostatuskode.REGNES_IKKE_SOM_BARN
                            } else {
                                Bostatuskode.IKKE_MED_FORELDER
                            }
                        }
                    } else {
                        // For forskudd skal status alltid settes til REGNES_IKKE_SOM_BARN etter fyllte 18 år, uansett bostatus.
                        // For andre behandlingstyper skal status bare endres til REGNES_IKKE_SOM_BARN hvis barnet bor med forelder.
                        if (typeBehandling == TypeBehandling.FORSKUDD) {
                            Bostatuskode.REGNES_IKKE_SOM_BARN
                        } else {
                            if (liste[indeks].bostatus == Bostatuskode.MED_FORELDER) {
                                Bostatuskode.REGNES_IKKE_SOM_BARN
                            } else {
                                Bostatuskode.IKKE_MED_FORELDER
                            }
                        }
                    }

                // Perioder som avsluttes før 18årsdag skrives til returliste.
                if (liste[indeks].periodeTom != null && liste[indeks].periodeTom!!.isBefore(attenårFraDato)) {
                    listeJustertMotAttenårsdag.add(liste[indeks])
                } else {
                    if (liste[indeks].periodeFom.isBefore(attenårFraDato)) {
                        // Perioden starter før 18-årsdagen, det må lages en ekstra periode etter 18årsdagen.
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponseV2(
                                gjelderPersonId = liste[indeks].gjelderPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = attenårFraDato.minusDays(1),
                                bostatus = liste[indeks].bostatus,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponseV2(
                                gjelderPersonId = liste[indeks].gjelderPersonId,
                                periodeFom = attenårFraDato,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = bostatuskodeAttenÅr,
                                fødselsdato = liste[indeks].fødselsdato,
                                kilde = liste[indeks].kilde,

                            ),
                        )
                    } else {
                        listeJustertMotAttenårsdag.add(
                            BoforholdResponseV2(
                                gjelderPersonId = liste[indeks].gjelderPersonId,
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
        primærperioder: List<BoforholdResponseV2>,
        sekundærperioder: List<BoforholdResponseV2>,
    ): List<BoforholdResponseV2> {
        val resultatliste = mutableListOf<BoforholdResponseV2>()

        // Skriver alle primærperioder til resultatet.
        primærperioder.sortedBy { it.periodeFom }.forEach { primærperiode ->
            resultatliste.add(
                BoforholdResponseV2(
                    gjelderPersonId = primærperiode.gjelderPersonId,
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
        sekundærperioder.sortedBy { it.periodeFom }.forEach { sekundærperiode ->
            // Finner sekundærperioder som overlapper med den primære perioden
            val overlappendePerioder = mutableListOf<BoforholdResponseV2>()
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
            val sammenslåttListeOverlappendePerioder = mutableListOf<BoforholdResponseV2>()

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
        sekundærperiode: BoforholdResponseV2,
        overlappendePerioder: List<BoforholdResponseV2>,
    ): List<BoforholdResponseV2>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertSekundærPeriodeListe = mutableListOf<BoforholdResponseV2>()

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
                if (periodeTom != null &&
                    (
                        overlappendePerioder[indeks].kilde != sekundærperiode.kilde ||
                            overlappendePerioder[indeks].bostatus != sekundærperiode.bostatus
                        )
                ) {
                    // Første primære periode starter etter sekundær periode. Den sekundære perioden skrives med justert tomdato. Senere i logikken
                    // må det sjekkes på om den sekundære perioden må splittes i mer enn én periode.
                    justertSekundærPeriodeListe.add(
                        BoforholdResponseV2(
                            gjelderPersonId = sekundærperiode.gjelderPersonId,
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
                if (overlappendePerioder[indeks + 1].periodeFom.isAfter(overlappendePerioder[indeks].periodeTom!!.plusDays(1)) &&
                    (
                        overlappendePerioder[indeks].kilde != sekundærperiode.kilde ||
                            overlappendePerioder[indeks].bostatus != sekundærperiode.bostatus
                        )
                ) {
                    // Det er en åpen tidsperiode mellom to primære perioder, og den sekundære perioden skal fylle denne tidsperioden
                    periodeTom = overlappendePerioder[indeks + 1].periodeFom.minusDays(1)
                    justertSekundærPeriodeListe.add(
                        BoforholdResponseV2(
                            gjelderPersonId = sekundærperiode.gjelderPersonId,
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
                // Siste sekundærperiode
                if (overlappendePerioder[indeks].periodeTom != null) {
                    if (sekundærperiode.periodeTom == null || sekundærperiode.periodeTom.isAfter(overlappendePerioder[indeks].periodeTom)) {
                        justertSekundærPeriodeListe.add(
                            BoforholdResponseV2(
                                gjelderPersonId = sekundærperiode.gjelderPersonId,
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
        return justertSekundærPeriodeListe.sortedBy { it.periodeFom }
    }

    private fun beregnetAttenÅrFraDato(fødselsdato: LocalDate): LocalDate = fødselsdato.plusYears(18).plusMonths(1).withDayOfMonth(1)

    private fun beregnetPeriodeErInnenforOffentligPeriodeMedLikBostatuskode(
        beregnetPeriode: BoforholdResponseV2,
        offentligePerioder: List<BoforholdResponseV2>,
    ): Boolean = offentligePerioder.any { offentligPeriode ->
        beregnetPeriode.bostatus == offentligPeriode.bostatus &&
            beregnetPeriode.periodeFom.isAfter(offentligPeriode.periodeFom.minusDays(1)) &&
            (offentligPeriode.periodeTom == null || beregnetPeriode.periodeTom?.isBefore(offentligPeriode.periodeTom.plusDays(1)) == true)
    }

    private fun behandleEndringer(
        startdatoBeregning: LocalDate,
        attenårFraDato: LocalDate,
        typeBehandling: TypeBehandling,
        boforholdRequest: BoforholdBarnRequestV3,
        behandledeOpplysninger: List<BoforholdResponseV2>,
    ): List<BoforholdResponseV2> {
        val endredePerioder = mutableListOf<BoforholdResponseV2>()
        val nyBostatus = boforholdRequest.endreBostatus!!.nyBostatus
        val originalBostatus = boforholdRequest.endreBostatus.originalBostatus

        when (boforholdRequest.endreBostatus.typeEndring) {
            TypeEndring.SLETTET -> {
                if (originalBostatus == null) {
                    // Hvis det ikke finnes original bostatuskode så skal det ikke være mulig å slette en periode
                    secureLogger.info {
                        "Periode som skal slettes må være angitt som originalBostatus i input. endreBostatus: " +
                            "${boforholdRequest.endreBostatus} "
                    }
                    throw IllegalStateException("Periode som skal slettes må være angitt som originalBostatus i input")
                }

                val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                if (originalBostatus.kilde == Kilde.OFFENTLIG) {
                    // Offentlige perioder skal ikke kunne slettes
                    secureLogger.info {
                        "Offentlig periode kan ikke slettes. " +
                            "endreBostatus: " +
                            "$boforholdRequest.endreBostatus "
                    }
                    return behandledeOpplysninger
                } else {
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
                        // Justerer perioder mot 18årsdag. Dette må gjøres siden periodeTom er endret på periode før den som er slettet.
                        val endredePerioderJustertMotAttenårsdag =
                            justerMotAttenårsdag(attenårFraDato, typeBehandling, endredePerioder)
                        // Gjør en sammenslåing av perioder med lik bostatuskode og justerer periodeTom
                        return slåSammenPerioderOgJusterPeriodeTom(endredePerioderJustertMotAttenårsdag)
                    }
                }
            }

            TypeEndring.NY -> {
                if (nyBostatus == null) {
                    // Hvis det ikke finnes en ny bostatus så kan det ikke leges til ny periode
                    secureLogger.info {
                        "Periode som skal legges til må være angitt som nyBostatus i input. endreBostatus: " +
                            "${boforholdRequest.endreBostatus} "
                    }
                    throw IllegalStateException("Periode som skal legges til mangler i input")
                }
                endredePerioder.add(
                    BoforholdResponseV2(
                        gjelderPersonId = boforholdRequest.gjelderPersonId,
                        periodeFom = if (nyBostatus.periodeFom!!.isBefore(startdatoBeregning)) startdatoBeregning else nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatus!!,
                        fødselsdato = boforholdRequest.fødselsdato,
                        kilde = Kilde.MANUELL,
                    ),
                )
                // Justerer perioder mot 18årsdag
                val nyBostatusJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, typeBehandling, endredePerioder)
                val sammenslåttListe = slåSammenPrimærOgSekundærperioder(nyBostatusJustertMotAttenårsdag, behandledeOpplysninger)
                return slåSammenPerioderOgJusterPeriodeTom(sammenslåttListe)
            }

            TypeEndring.ENDRET -> {
                if (originalBostatus == null || nyBostatus == null) {
                    // Hvis det ikke finnes original bostatus eller ny bostatus så kan ikke periode endres
                    secureLogger.info {
                        "Periode som skal endres må være angitt som originalBostatus og ny verdier må ligge i " +
                            "nyBostatus i input. endreBostatus: ${boforholdRequest.endreBostatus} "
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
                                BoforholdResponseV2(
                                    gjelderPersonId = boforholdRequest.gjelderPersonId,
                                    periodeFom = nyBostatus.periodeFom!!,
                                    periodeTom = nyBostatus.periodeTom,
                                    bostatus = nyBostatus.bostatus!!,
                                    fødselsdato = boforholdRequest.fødselsdato,
                                    kilde = Kilde.MANUELL,
                                ),
                            )
                        } else {
                            endredePerioder.add(behandledeOpplysninger[indeks])
                        }
                    }
                    // Justerer perioder mot 18årsdag
                    val endredePerioderJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, typeBehandling, endredePerioder)
                    return slåSammenPerioderOgJusterPeriodeTom(endredePerioderJustertMotAttenårsdag)
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
                        BoforholdResponseV2(
                            gjelderPersonId = boforholdRequest.gjelderPersonId,
                            periodeFom = nyBostatus.periodeFom,
                            periodeTom = nyBostatus.periodeTom,
                            bostatus = nyBostatus.bostatus!!,
                            fødselsdato = boforholdRequest.fødselsdato,
                            kilde = Kilde.MANUELL,
                        ),
                    )

                    // Justerer perioder mot 18årsdag
                    val nyPeriodeJustertMotAttenårsdag = justerMotAttenårsdag(attenårFraDato, typeBehandling, nyPeriode)
                    // Gjør en sammenslåing av perioder med lik bostatuskode og justerer periodeTom
                    val sammenslåttListe = slåSammenPrimærOgSekundærperioder(nyPeriodeJustertMotAttenårsdag, endredePerioder)
                    return slåSammenPerioderOgJusterPeriodeTom(sammenslåttListe)
                }

                if (nyBostatus.periodeFom!!.isAfter(originalBostatus.periodeFom)) {
                    val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                    // Sjekk om det finnes en periode før den som endres og endre periodeTom på denne til å bli lik periodeFom på den nye perioden.
                    // Hvis bostatuskode er endret i nyBostatus så forkortes i stedet periodeTom for originalBostatus

                    if (originalBostatus.bostatus != nyBostatus.bostatus) {
                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks < indeksMatch!!) {
                                // Skriver alle perioder før originalBostatus til endredePerioder.
                                endredePerioder.add(behandledeOpplysninger[indeks])
                            }
                        }
                        endredePerioder.add(
                            BoforholdResponseV2(
                                gjelderPersonId = boforholdRequest.gjelderPersonId,
                                periodeFom = originalBostatus.periodeFom!!,
                                periodeTom = nyBostatus.periodeFom.minusDays(1),
                                bostatus = originalBostatus.bostatus!!,
                                fødselsdato = boforholdRequest.fødselsdato,
                                kilde = originalBostatus.kilde,
                            ),
                        )
                    } else {
                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks == indeksMatch!! - 1) {
                                // Periode før periode som skal slettes. Justerer periodeTom til å være lik slettet periodes periodeTom.
                                endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeTom = nyBostatus.periodeFom.minusDays(1)))
                            } else {
                                if (indeks == indeksMatch) {
                                    // Periode som skal slettes. Hopper over denne.
                                    continue
                                } else {
                                    endredePerioder.add(behandledeOpplysninger[indeks])
                                }
                            }
                        }
                    }
                }

                // Legger til den endrede perioden
                endredePerioder.add(
                    BoforholdResponseV2(
                        gjelderPersonId = boforholdRequest.gjelderPersonId,
                        periodeFom = nyBostatus.periodeFom,
                        periodeTom = nyBostatus.periodeTom,
                        bostatus = nyBostatus.bostatus!!,
                        fødselsdato = boforholdRequest.fødselsdato,
                        kilde = nyBostatus.kilde,
                    ),
                )
                // Hvis nyBostatus ikke dekker hele perioden til originalBostatus så må det sjekkes om det finnes en periode etter originalBostatus.
                // PeriodeFom må i så fall endres på denne til å bli lik nyBostatus' periodeTom pluss én dag.
                if ((originalBostatus.periodeTom == null && nyBostatus.periodeTom != null) ||
                    (nyBostatus.periodeTom != null && nyBostatus.periodeTom.isBefore(originalBostatus.periodeTom))
                ) {
                    // Sjekker først om bostatuskode er endret i nyBostatus. Hvis den er det så beholdes gjenstånde del av originalBostatus med
                    // justert perioderFom

                    if (originalBostatus.bostatus != nyBostatus.bostatus) {
                        endredePerioder.add(
                            BoforholdResponseV2(
                                gjelderPersonId = boforholdRequest.gjelderPersonId,
                                periodeFom = nyBostatus.periodeTom.plusDays(1),
                                periodeTom = originalBostatus.periodeTom,
                                bostatus = originalBostatus.bostatus!!,
                                fødselsdato = boforholdRequest.fødselsdato,
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
                } else {
                    // Hvis nyBostatus dekker hele perioden til originalBostatus så må det sjekkes om det finnes en periode etter originalBostatus.
                    // PeriodeFom må i så fall endres på denne til å bli lik nyBostatus' periodeTom pluss én dag. Perioder som helt dekkes av nyBostatus
                    // skal ikke skrives til endredePerioder.
                    if (nyBostatus.periodeTom != null && nyBostatus.periodeTom.isAfter(originalBostatus.periodeTom)
                    ) {
                        val indeksMatch = finnIndeksMatch(originalBostatus, behandledeOpplysninger)

                        for (indeks in behandledeOpplysninger.sortedBy { it.periodeFom }.indices) {
                            if (indeks > indeksMatch!!) {
                                if (behandledeOpplysninger[indeks].periodeTom == null ||
                                    behandledeOpplysninger[indeks].periodeTom!!.isAfter(nyBostatus.periodeFom)
                                ) {
                                    if (behandledeOpplysninger[indeks].periodeFom.isBefore(nyBostatus.periodeTom) &&
                                        (
                                            behandledeOpplysninger[indeks].periodeTom == null ||
                                                behandledeOpplysninger[indeks].periodeTom!!.isAfter(nyBostatus.periodeTom)
                                            )
                                    ) {
                                        // Periode som overlapper med nyBostatus. Justerer periodeFom til å være lik nyBostatus periodeTom pluss én dag.
                                        endredePerioder.add(behandledeOpplysninger[indeks].copy(periodeFom = nyBostatus.periodeTom.plusDays(1)))
                                    } else {
                                        if (behandledeOpplysninger[indeks].periodeFom.isAfter(nyBostatus.periodeTom)) {
                                            endredePerioder.add(behandledeOpplysninger[indeks])
                                        }
                                    }
                                } else {
                                    endredePerioder.add(behandledeOpplysninger[indeks])
                                }
                            } else if (indeks < indeksMatch) {
                                endredePerioder.add(behandledeOpplysninger[indeks])
                            }
                        }
                    }
                }

                val endredePerioderJustertMotAttenårsdag =
                    justerMotAttenårsdag(attenårFraDato, typeBehandling, endredePerioder.sortedBy { it.periodeFom })
                return slåSammenPerioderOgJusterPeriodeTom(endredePerioderJustertMotAttenårsdag)
            }
        }
    }

    private fun perioderErIdentiske(periode1: Bostatus, periode2: BoforholdResponseV2): Boolean = periode1.periodeFom == periode2.periodeFom &&
        periode1.periodeTom == periode2.periodeTom &&
        periode1.bostatus == periode2.bostatus &&
        periode1.kilde == periode2.kilde

    private fun finnIndeksMatch(periode: Bostatus, liste: List<BoforholdResponseV2>): Int? {
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
}
