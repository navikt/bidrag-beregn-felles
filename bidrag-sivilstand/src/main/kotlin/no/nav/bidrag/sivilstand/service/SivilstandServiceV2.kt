package no.nav.bidrag.sivilstand.service

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.bo.SivilstandPDLBo
import no.nav.bidrag.sivilstand.dto.EndreSivilstand
import no.nav.bidrag.sivilstand.dto.Sivilstand
import no.nav.bidrag.sivilstand.dto.SivilstandRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class SivilstandServiceV2 {
    fun beregn(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        // Dette er følgende scenarier som kan forekomme i beregning:

        // 1. endreSivilstand = null. Beregning gjøres da enten på offentlige opplysninger eller behandledeSivilstandsopplysninger.
        //    1a. Hvis behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt:
        //        behandledeSivilstandsopplysninger skal da justeres mot virkningstidspunkt og/eller sjekkes mot offentlige opplysninger som kan ha
        //        endret seg siden forrige beregning. Perioder i behandledeSivilstandsopplysninger sjekkes mot offentlige perioder og kilde endres
        //        evt. til Offentlig hvis det er match. Dette vil kunne skje ved oppdatering av innhentede offentlige opplysninger som nå helt
        //        overlapper manuelt innlagte perioder. I tilfeller der virkningstidspunkt forskyves tilbake i tid så skal tidslinjen suppleres med
        //        offentlige perioder.
        //    1b. Hvis behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom:
        //        behandledeSivilstandsopplysninger skal da justeres mot virkningstidspunkt. I tilfeller der virkningstidspunkt forskyves tilbake i
        //        tid så skal tidslinjen suppleres med én offentlig periode med Sivilstandskode = UKJENT og Kilde = OFFENTLIG i tidsrommet mellom
        //        virkningstidspunkt og periodeFom for første forekomst i behandledeSivilstandsopplysninger.
        //    1c. Hvis behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Det gjøres da en beregning basert på
        //        offentlige perioder.
        //    1d. Hvis behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger  er tom: Det skal legges til en periode med
        //        Sivilstandskode = UKJENT og Kilde = OFFENTLIG
        // 2. endreSivilstand er utfyllt.
        //    2a. Hvis behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt: behandledeSivilstandsopplysninger
        //        skal da justeres etter det som er sendt inn i endreSivilstand. Det kan slettes/legges til eller endres perioder.
        //        Perioder i oppdaterte behandledeSivilstandsopplysninger sjekkes mot offentlige perioder og kilde evt. endres til Offentlig hvis det
        //        er match.
        //    2b. Hvis behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom: behandledeSivilstandsopplysninger skal
        //        da justeres etter det som er sendt inn i endreSivilstand. Det kan slettes/legges til eller endres perioder.
        //    2c. Hvis behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt: Feil.
        //        Det bør da i stedet gjøres en beregning på offentlige perioder før det kan sendes en endreSivilstand-request.
        //        Beregningen ignorerer innhentedeOffentligeOpplysninger og gjør en beregning på det som ligger i endreSivilstand. typeEndring må
        //        være lik NY, hvis ikke returneres tom liste. Hull i tidslinjen utfylles med Sivilstandskode UKJENT og Kilde = OFFENTLIG.
        //    2d. Hvis behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er tom: Beregningen gjøres på det som ligger i
        //        endreSivilstand. typeEndring må være lik NY, hvis ikke reurneres tom liste.
        //        Hull i tidslinjen utfylles med Sivilstandskode UKJENT og Kilde = OFFENTLIG.

        // Beregner offentlige perioder som er hentet inn. Hvis det ikke er hentet inn offentlige opplysninger så returneres en periode med
        // Sivilstandskode = UKJENT og Kilde = OFFENTLIG.
        val offentligePerioder = behandleOffentligePerioder(virkningstidspunkt, sivilstandRequest)

        // Filterer først bort alle perioder med behandlede opplysninger som avsluttes før virkningstidspunkt
        val behandledeOpplysninger = sivilstandRequest.behandledeSivilstandsopplysninger
            .filter { (it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt)) }
            .sortedBy { it.periodeFom }.map {
                Sivilstand(
                    periodeFom = if (it.periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else it.periodeFom,
                    periodeTom = it.periodeTom,
                    sivilstandskode = it.sivilstandskode,
                    kilde = it.kilde,
                )
            }

        if (sivilstandRequest.endreSivilstand == null) {
            // 1
            if (behandledeOpplysninger.isNotEmpty()) {
                // 1a + 1b
                // Virkningstidspunkt er endret og/eller offentlige perioder er oppdatert og perioder i behandledeOpplysninger skal
                // sjekkes mot disse og evt få kilde = Offentlig. Hvis virkningstidspunkt er endret tilbake i tid så skal hullet i tidslinjen fylles
                // med offentlige opplysninger.
                val sammenslåttListe =
                    slåSammenPrimærOgSekundærperioder(virkningstidspunkt, behandledeOpplysninger, offentligePerioder)

                return sammenslåttListe.map {
                    Sivilstand(
                        periodeFom = it.periodeFom,
                        periodeTom = it.periodeTom,
                        sivilstandskode = it.sivilstandskode,
                        // Hvis perioden har kilde Manuell og perioden dekkes helt av en offentlig periode så settes kilde = Offentlig
                        kilde = if (it.kilde == Kilde.MANUELL) {
                            if (beregnetPeriodeErInnenforOffentligPeriodeMedLikSivilstandskode(
                                    it,
                                    offentligePerioder,
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
            } else {
                return offentligePerioder
            }
        }

        val oppdaterteBehandledeOpplysninger = behandleEndringer(virkningstidspunkt, sivilstandRequest.endreSivilstand, behandledeOpplysninger)

        if (behandledeOpplysninger.isEmpty()) {
            // 2c + 2d
            // Det finnes ingen behandlede perioder og den nye sivilstandsperioden skal returneres sammen med genererte perioder
            // med Sivilstandskode = UKJENT og kilde = OFFENTLIG som fyller tidslinjen fra virkningstidspunkt til dagens dato.

            if (sivilstandRequest.endreSivilstand.typeEndring != TypeEndring.NY) {
                // Feilsituasjon. Må alltid være ny hvis det ikke finnes perioder fra før.
                return emptyList()
            }
            return slåSammenPrimærOgSekundærperioder(virkningstidspunkt, oppdaterteBehandledeOpplysninger, offentligePerioder)
        } else {
            // 2a + 2b
            // Det finnes både behandlede og endrede perioder

            return oppdaterteBehandledeOpplysninger.map {
                Sivilstand(
                    periodeFom = it.periodeFom,
                    periodeTom = it.periodeTom,
                    sivilstandskode = it.sivilstandskode,
                    kilde = if (it.kilde == Kilde.MANUELL) {
                        if (beregnetPeriodeErInnenforOffentligPeriodeMedLikSivilstandskode(
                                it,
                                offentligePerioder,
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
    }

    private fun behandleOffentligePerioder(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        // Overstyrer gyldigFom til BMs fødselsdato for ugift og uoppgitt sivilstand
        val offentligePerioder = sivilstandRequest.innhentedeOffentligeOpplysninger.map {
            SivilstandGrunnlagDto(
                personId = it.personId,
                type = it.type,
                gyldigFom = if (it.type == SivilstandskodePDL.UGIFT || it.type == SivilstandskodePDL.UOPPGITT) {
                    sivilstandRequest.fødselsdatoBM
                } else {
                    it.gyldigFom
                },
                bekreftelsesdato = it.bekreftelsesdato,
                master = it.master,
                registrert = it.registrert,
                historisk = it.historisk,
            )
        }.sortedWith(
            compareByDescending<SivilstandGrunnlagDto> { it.historisk }.thenBy { it.gyldigFom }
                .thenBy { it.bekreftelsesdato }.thenBy { it.registrert }.thenBy { it.type.toString() },
        )

        // Sjekker om aktiv sivilstand har en fradato som er før virkningstidspunktet. Hvis det er tilfellet så utelates alle logiske tester på de
        // historiske sivilstandstatusene. Hvis det ikke finnes en aktiv sivilstand så returneres UKJENT.
        val aktivSivilstand = offentligePerioder.filter { it.historisk == false }
        if (aktivSivilstand.isNotEmpty()) {
            val sivilstandskode = finnSivilstandskode(aktivSivilstand.first().type!!)
            val periodeFom = aktivSivilstand.first().gyldigFom
                ?: aktivSivilstand.first().bekreftelsesdato
                ?: aktivSivilstand.first().registrert?.toLocalDate()
                ?: virkningstidspunkt
            val justertPeriodeFom = if (sivilstandskode == Sivilstandskode.BOR_ALENE_MED_BARN) {
                periodeFom.withDayOfMonth(1)
            } else {
                periodeFom.plusMonths(1)?.withDayOfMonth(1)
            }

            if (justertPeriodeFom != null && justertPeriodeFom.isBefore(virkningstidspunkt.plusDays(1))) {
                return listOf(
                    Sivilstand(
                        periodeFom = virkningstidspunkt,
                        periodeTom = null,
                        sivilstandskode = sivilstandskode,
                        kilde = Kilde.OFFENTLIG,
                    ),
                )
            }
        } else {
            return listOf(
                Sivilstand(
                    periodeFom = virkningstidspunkt,
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.UKJENT,
                    kilde = Kilde.OFFENTLIG,
                ),
            )
        }

        val beregnedeSivilstandsperioder = mutableListOf<Sivilstand>()

        // Tester på innhold i PDL-perioder. Der det mangler informasjon eller det er ugyldige data returneres en liste med én forekomst
        // med Sivilstandskode = UKJENT

        // Sjekker først om det finnes en aktiv og gyldig forekomst.
        var ukjentSivilstandskode = false
        if (offentligePerioder.none {
                it.historisk == false
            }
        ) {
            ukjentSivilstandskode = true
        }

        // Sjekker om det finnes forekomster uten datoinformasjon. For aktive/ikke-historiske forekomster kan også 'registrert' brukes
        // til å angi periodeFom.
        if (offentligePerioder.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.historisk == true
            } ||
            offentligePerioder.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.registrert == null && it.historisk == false
            }
        ) {
            ukjentSivilstandskode = true
        }

        // Sjekker at sivilstandstype er angitt på alle forekomster
        if (offentligePerioder.any {
                it.type == null
            }
        ) {
            ukjentSivilstandskode = true
        }

        if (offentligePerioder.isEmpty()) {
            ukjentSivilstandskode = true
        }

        if (ukjentSivilstandskode) {
            return listOf(
                Sivilstand(
                    periodeFom = virkningstidspunkt,
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.UKJENT,
                    kilde = Kilde.OFFENTLIG,
                ),
            )
        }

        val sortertOffentligePeriodeListe = offentligePerioder.sortedWith(
            compareByDescending<SivilstandGrunnlagDto> { it.historisk }.thenBy { it.gyldigFom }
                .thenBy { it.bekreftelsesdato }.thenBy { it.registrert }.thenBy { it.type.toString() },
        )

        var periodeTom: LocalDate?

        val sivilstandPDLBoListe = sortertOffentligePeriodeListe.mapIndexed { indeks, sivilstand ->
            val periodeFom = if (sivilstand.historisk == true) {
                sivilstand.gyldigFom
                    ?: sivilstand.bekreftelsesdato
            } else {
                sivilstand.gyldigFom
                    ?: sivilstand.bekreftelsesdato
                    ?: sivilstand.registrert?.toLocalDate()
            }

            // Setter periodeTom lik periodeFom - 1 dag for neste forekomst.
            // Hvis det ikke finnes en neste forekomst så settes periodeTil lik null. Timestamp registrert brukes bare hvis neste forekomst ikke er historisk
            periodeTom = if (sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.historisk == true) {
                sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.bekreftelsesdato
            } else {
                sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.bekreftelsesdato
                    ?: sortertOffentligePeriodeListe.getOrNull(indeks + 1)?.registrert?.toLocalDate()
            }

            return@mapIndexed SivilstandPDLBo(
                periodeFom = periodeFom!!,
                periodeTom = periodeTom?.minusDays(1),
                sivilstandskodePDL = sortertOffentligePeriodeListe[indeks].type!!,
                kilde = Kilde.OFFENTLIG,
            )
        }

        val filtrertSivilstandPDLBoListe =
            sivilstandPDLBoListe.filter { it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt.minusDays(1)) }

        // Sjekk på logiske verdier i sivilstandslisten, kun perioder som overlapper med eller er etter virkningstidspunktet sjekkes.
        val logiskeVerdierErOK = sjekkLogiskeVerdier(filtrertSivilstandPDLBoListe)

        if (logiskeVerdierErOK) {
            val sivilstandListe = beregnPerioder(virkningstidspunkt, filtrertSivilstandPDLBoListe)
            beregnedeSivilstandsperioder.addAll(sivilstandListe)
            return beregnedeSivilstandsperioder
        } else {
            return listOf(
                Sivilstand(
                    periodeFom = virkningstidspunkt,
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.UKJENT,
                    kilde = Kilde.OFFENTLIG,
                ),
            )
        }
    }

    private fun slåSammenPrimærOgSekundærperioder(
        virkningstidspunkt: LocalDate,
        primærperioder: List<Sivilstand>,
        sekundærperioder: List<Sivilstand>,
    ): List<Sivilstand> {
        val resultatliste = mutableListOf<Sivilstand>()

        // Skriver alle primærperioder til resultatet. Perioder med identisk informasjon som en offentlig periode skrives med kilde = Offentlig
        primærperioder.forEach { primærperiode ->
            resultatliste.add(
                Sivilstand(
                    periodeFom = primærperiode.periodeFom,
                    periodeTom = primærperiode.periodeTom,
                    sivilstandskode = primærperiode.sivilstandskode,
                    kilde = if (beregnetPeriodeErInnenforOffentligPeriodeMedLikSivilstandskode(
                            primærperiode,
                            sekundærperioder,
                        )
                    ) {
                        Kilde.OFFENTLIG
                    } else {
                        primærperiode.kilde
                    },
                ),
            )
        }

        // Sjekker offentlige perioder og justerer periodeFom og periodeTom der disse overlapper med beregnede (primær)perioder
        // Offentlige perioder som helt dekkes av behandlede perioder skrives ikke til resultatet
        sekundærperioder.forEach { sekundærperiode ->
            // Finner manuelle perioder som overlapper med den offentlige perioden
            val overlappendePerioder = mutableListOf<Sivilstand>()
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

            val justertOffentligPeriode = justerSekundærPeriode(sekundærperiode, overlappendePerioder.sortedBy { it.periodeFom })
            if (justertOffentligPeriode != null) {
                resultatliste.addAll(justertOffentligPeriode)
            }
        }

        val sammenslåttePerioder = slåSammenSammenhengendePerioderMedLikSivilstandskode(
            virkningstidspunkt,
            resultatliste.sortedBy {
                it.periodeFom
                it.periodeTom ?: LocalDate.MAX
            },
        )

        return sammenslåttePerioder.sortedBy { it.periodeFom }
    }

    // Sekundær periode sjekkes mot primære perioder og justeres til å ikke overlappe med disse. En sekundær periode kan overlappe med 0 til
    // mange primære perioder. Hvis en sekundær periode dekkes helt av primære perioder returnere metoden null, ellers returneres en liste. Hvis
    // en sekundær perioder overlappes av flere enn to primære perioder så vil responsen bestå av flere sekundære perioder som dekker
    // oppholdet mellom de ulike primære periodene.
    private fun justerSekundærPeriode(sekundærPeriode: Sivilstand, overlappendePerioder: List<Sivilstand>): List<Sivilstand>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertSekundærPeriodeListe = mutableListOf<Sivilstand>()

        if (overlappendePerioder.isEmpty()) {
            return listOf(sekundærPeriode)
        }

        for (indeks in overlappendePerioder.indices) {
            // Sjekker først om den første primære perioden dekker starten, og eventuelt hele den sekundære perioden
            if (indeks == 0) {
                if (overlappendePerioder[indeks].periodeFom.isBefore(sekundærPeriode.periodeFom.plusDays(1))) {
                    if (overlappendePerioder[indeks].periodeTom == null) {
                        // Den primære perioden dekker hele den sekundære perioden
                        return null
                    } else {
                        if (sekundærPeriode.periodeTom != null &&
                            overlappendePerioder[indeks].periodeTom?.isAfter(
                                sekundærPeriode.periodeTom.plusDays(1),
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
                        Sivilstand(
                            periodeFom = sekundærPeriode.periodeFom,
                            periodeTom = periodeTom,
                            sivilstandskode = sekundærPeriode.sivilstandskode,
                            kilde = sekundærPeriode.kilde,
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
                        Sivilstand(
                            // periodeFom er satt hvis første primære periode overlapper startdato for sekundære periode
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = periodeTom,
                            sivilstandskode = sekundærPeriode.sivilstandskode,
                            kilde = sekundærPeriode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                }
            } else {
                // Siste primære periode
                if (overlappendePerioder[indeks].periodeTom != null) {
                    if (sekundærPeriode.periodeTom == null || sekundærPeriode.periodeTom.isAfter(overlappendePerioder[indeks].periodeTom)) {
                        justertSekundærPeriodeListe.add(
                            Sivilstand(
                                periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                                periodeTom = sekundærPeriode.periodeTom,
                                sivilstandskode = sekundærPeriode.sivilstandskode,
                                kilde = sekundærPeriode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertSekundærPeriodeListe
    }

    private fun behandleEndringer(
        virkningstidspunkt: LocalDate,
        endreSivilstand: EndreSivilstand,
        behandledeOpplysninger: List<Sivilstand>,
    ): List<Sivilstand> {
        val endredePerioder = mutableListOf<Sivilstand>()
        val nySivilstand = endreSivilstand.nySivilstand
        val originalSivilstand = endreSivilstand.originalSivilstand

        when (endreSivilstand.typeEndring) {
            TypeEndring.SLETTET -> {
                // Hvis original sivilstand ikke er angitt eller ikke finnes i behandledeOpplysninger så skal det kastes en exception.
                if (originalSivilstand == null ||
                    behandledeOpplysninger.none { behandletOpplysning ->
                        perioderErIdentiske(originalSivilstand, behandletOpplysning)
                    }
                ) {
                    secureLogger.info {
                        "Periode som skal slettes er enten ikke angitt som originalSivilstand eller finnes ikke i behandledeOpplysninger. " +
                            "endreSivilstand: " +
                            "$endreSivilstand "
                    }
                    throw IllegalStateException(
                        "Periode som skal slettes er enten ikke angitt som originalSivilstand eller finnes " +
                            "ikke i behandledeOpplysninger",
                    )
                }

                var indeksMatch = -1

                for (indeks in behandledeOpplysninger.indices) {
                    if (perioderErIdentiske(originalSivilstand, behandledeOpplysninger[indeks])) {
                        indeksMatch = indeks
                    }
                }

                if (indeksMatch == 0) {
                    secureLogger.info {
                        "Periode som skal slettes er første periode i behandledeOpplysninger, denne kan ikke slettes . " +
                            "endreSivilstand: " +
                            "$endreSivilstand "
                    }
                }
                val oppdatertbehandledeOpplysninger = mutableListOf<Sivilstand>()

                // Fjerner perioden som skal slettes fra behandledeOpplysninger
                for (indeks in behandledeOpplysninger.indices) {
                    if (indeks == indeksMatch - 1) {
                        // Periode før periode som skal slettes. Justerer periodeTom til å være lik slettet periodes periodeTom.
                        oppdatertbehandledeOpplysninger.add(behandledeOpplysninger[indeks].copy(periodeTom = originalSivilstand.periodeTom))
                    } else {
                        if (indeks == indeksMatch) {
                            // Periode som skal slettes. Hopper over denne.
                            continue
                        } else {
                            oppdatertbehandledeOpplysninger.add(behandledeOpplysninger[indeks])
                        }
                    }
                }

                return slåSammenSammenhengendePerioderMedLikSivilstandskode(virkningstidspunkt, oppdatertbehandledeOpplysninger)
            }

            TypeEndring.NY -> {
                if (nySivilstand == null) {
                    // Hvis det ikke finnes en ny sivilstandsperiode så kan det ikke legges til ny periode
                    secureLogger.info {
                        "Periode som skal legges til må være angitt som nySivilstand i input. endreSivilstand: " +
                            "$endreSivilstand "
                    }
                    throw IllegalStateException("Periode som skal legges til mangler i input")
                }
                endredePerioder.add(
                    Sivilstand(
                        periodeFom = if (nySivilstand.periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else nySivilstand.periodeFom,
                        periodeTom = nySivilstand.periodeTom,
                        sivilstandskode = nySivilstand.sivilstandskode,
                        kilde = Kilde.MANUELL,
                    ),
                )
                return slåSammenPrimærOgSekundærperioder(virkningstidspunkt, endredePerioder, behandledeOpplysninger)
            }

            TypeEndring.ENDRET -> {
                if (originalSivilstand == null || nySivilstand == null) {
                    // Hvis det ikke er angitt original sivilstand eller ny sivilstand så kan ikke periode endres
                    secureLogger.info {
                        "Periode som skal endres må være angitt som originalSivilstand og ny verdier må ligge i " +
                            "nySivilstand i input. endreSivilstand: $endreSivilstand "
                    }
                    throw IllegalStateException("OriginalSivilstand og nySivilstand må være angitt for å kunne endre sivilstand")
                }

                if (originalSivilstand.periodeFom == nySivilstand.periodeFom && originalSivilstand.periodeTom == nySivilstand.periodeTom) {
                    // Hvis periodene er uendret så returneres den nye sivilstanden. Man vil komme hit hvis bare sivilstandskode er endret.
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = nySivilstand.periodeFom,
                            periodeTom = nySivilstand.periodeTom,
                            sivilstandskode = nySivilstand.sivilstandskode,
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    return slåSammenPrimærOgSekundærperioder(virkningstidspunkt, endredePerioder, behandledeOpplysninger)
                }

                if (originalSivilstand.periodeTom != null && nySivilstand.periodeFom.isAfter(originalSivilstand.periodeTom)) {
                    // Perioden er endret til å være helt utenfor original periode. Det må genereres en periode med motsatt sivilstandskode i
                    // tidsrommet for den originale perioden, i tillegg til at den nye perioden legges til.
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = originalSivilstand.periodeFom,
                            periodeTom = originalSivilstand.periodeTom,
                            sivilstandskode = motsattSivilstandskode(originalSivilstand.sivilstandskode),
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = nySivilstand.periodeFom,
                            periodeTom = nySivilstand.periodeTom,
                            sivilstandskode = nySivilstand.sivilstandskode,
                            kilde = Kilde.MANUELL,
                        ),
                    )
                    return slåSammenPrimærOgSekundærperioder(virkningstidspunkt, endredePerioder, behandledeOpplysninger)
                }

                if (nySivilstand.periodeFom.isAfter(originalSivilstand.periodeFom)) {
                    // Det må lages en ny periode med motsatt sivilstandskode for perioden mellom gammel og ny periodeFom
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = originalSivilstand.periodeFom,
                            periodeTom = nySivilstand.periodeFom.minusDays(1),
                            sivilstandskode = motsattSivilstandskode(nySivilstand.sivilstandskode),
                            kilde = Kilde.MANUELL,
                        ),
                    )
                }

                // Legger til den endrede perioden
                endredePerioder.add(
                    Sivilstand(
                        periodeFom = nySivilstand.periodeFom,
                        periodeTom = nySivilstand.periodeTom,
                        sivilstandskode = nySivilstand.sivilstandskode,
                        kilde = nySivilstand.kilde,
                    ),
                )
                // Sjekk om det må lages en ekstra periode etter nySivilstand med motsatt sivilstandskode.
                if (originalSivilstand.periodeTom == null) {
                    if (nySivilstand.periodeTom != null) {
                        // Det må lages en ny periode med motsatt sivilstandskode for perioden mellom ny periodeTom og gammel periodeTom
                        endredePerioder.add(
                            Sivilstand(
                                periodeFom = nySivilstand.periodeTom.plusDays(1),
                                periodeTom = null,
                                sivilstandskode = motsattSivilstandskode(originalSivilstand.sivilstandskode),
                                kilde = Kilde.MANUELL,
                            ),
                        )
                    }
                } else {
                    if (nySivilstand.periodeTom != null && nySivilstand.periodeTom.isBefore(originalSivilstand.periodeTom)) {
                        endredePerioder.add(
                            Sivilstand(
                                periodeFom = nySivilstand.periodeTom.plusDays(1),
                                periodeTom = originalSivilstand.periodeTom,
                                sivilstandskode = motsattSivilstandskode(nySivilstand.sivilstandskode),
                                kilde = originalSivilstand.kilde,
                            ),
                        )
                    }
                }
                return slåSammenPrimærOgSekundærperioder(virkningstidspunkt, endredePerioder, behandledeOpplysninger)
            }
        }
    }

    private fun beregnetPeriodeErInnenforOffentligPeriodeMedLikSivilstandskode(
        beregnetPeriode: Sivilstand,
        offentligePerioder: List<Sivilstand>,
    ): Boolean = offentligePerioder.any { offentligPeriode ->
        beregnetPeriode.sivilstandskode == offentligPeriode.sivilstandskode &&
            beregnetPeriode.periodeFom.isAfter(offentligPeriode.periodeFom.minusDays(1)) &&
            (offentligPeriode.periodeTom == null || beregnetPeriode.periodeTom?.isBefore(offentligPeriode.periodeTom.plusDays(1)) == true) &&
            beregnetPeriode.kilde != offentligPeriode.kilde
    }

    private fun sjekkLogiskeVerdier(sivilstandPDLBoListe: List<SivilstandPDLBo>): Boolean {
        // Sjekker om det finnes en status GIFT/REGISTRERT_partner hvis det finnes en status ENKE_ELLER_ENKEMANN, SKILT, SEPARERT eller SEPARERT_PARTNER
        // Sjekken gjøres bare hvis det finnes en forekomst av UGIFT. Hvis UGIFT ikke finnes så er det sannsynlig at personen er innflytter til Norge
        // og det er da vanskelig å gjøre en logisk sjekk på verdiene.

        if (sivilstandPDLBoListe
                .any { it.sivilstandskodePDL == SivilstandskodePDL.UGIFT }
        ) {
            // Melder feil hvis personen er separert/skilt/enke_enkemann uten å ha være registrert som gift. Samme sjekk gjøres for registrerte partnere.
            if ((
                    sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.ENKE_ELLER_ENKEMANN } ||
                        sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT } ||
                        sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT } ||
                        sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT }
                    ) &&
                sivilstandPDLBoListe.none { it.sivilstandskodePDL == SivilstandskodePDL.GIFT }
            ) {
                return false
            } else {
                if ((
                        sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT_PARTNER } ||
                            sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT_PARTNER } ||
                            sivilstandPDLBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT_PARTNER }
                        ) &&
                    sivilstandPDLBoListe.none { it.sivilstandskodePDL == SivilstandskodePDL.REGISTRERT_PARTNER }

                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun finnSivilstandskode(type: SivilstandskodePDL): Sivilstandskode = when (type) {
        SivilstandskodePDL.GIFT, SivilstandskodePDL.REGISTRERT_PARTNER -> Sivilstandskode.GIFT_SAMBOER
        SivilstandskodePDL.UOPPGITT -> Sivilstandskode.UKJENT
        else -> Sivilstandskode.BOR_ALENE_MED_BARN
    }

    private fun beregnPerioder(virkningstidspunkt: LocalDate, sivilstandPDLBoListe: List<SivilstandPDLBo>): List<Sivilstand> {
        val sivilstandListe = sivilstandPDLBoListe.map {
            Sivilstand(
                periodeFom = if (it.periodeFom.isBefore(virkningstidspunkt)) {
                    virkningstidspunkt
                } else {
                    it.periodeFom
                },
                periodeTom = it.periodeTom,
                sivilstandskode = finnSivilstandskode(it.sivilstandskodePDL),
                kilde = it.kilde,
            )
        }

        // Justerer datoer. Perioder med 'Bor alene med barn' skal få periodeFom lik første dag i måneden og periodeTil lik siste dag i måneden.
        // Justerer ikke frem periodeFom hvis første periode har status GIFT/SAMBOER eller UKJENT og periodeFom = virkningstidpunkt.
        val mappetSivilstandListe = sivilstandListe.mapNotNull {
            if (it.sivilstandskode == Sivilstandskode.BOR_ALENE_MED_BARN) {
                val periodeFom = hentFørsteDagIMåneden(it.periodeFom)
                val periodeTom = if (it.periodeTom == null) null else hentSisteDagIMåneden(it.periodeTom)
                Sivilstand(
                    periodeFom,
                    periodeTom,
                    it.sivilstandskode,
                    it.kilde,
                )
            } else {
                // Overstyrer ikke periodeFom til første dag i neste måned hvis periodeFom er første dag i måneden.
                // Samme gjelder om periodeTom er siste dag i måneden.
                val periodeFom = if (it.periodeFom == virkningstidspunkt || it.periodeFom.dayOfMonth == 1) {
                    it.periodeFom
                } else {
                    hentFørsteDagINesteMåned(it.periodeFom)
                }
                val periodeTom = if (it.periodeTom == null) {
                    null
                } else if (erSisteDagIMåneden(it.periodeTom)) {
                    it.periodeTom
                } else {
                    hentSisteDagIForrigeMåned(it.periodeTom)
                }
                // Forekomster med Gift/Samboer ignoreres hvis perioden er mindre enn én måned. Bor alene med barn skal da gjelde.
                // Legger til én dag i periodeTom slik at testen fungerer for perioder som er første og siste dag i samme måned.
                if (periodeTom != null) {
                    if (ChronoUnit.MONTHS.between(periodeFom, periodeTom.plusDays(1)) >= 1) {
                        Sivilstand(
                            periodeFom,
                            periodeTom,
                            it.sivilstandskode,
                            it.kilde,
                        )
                    } else {
                        null
                    }
                } else {
                    Sivilstand(
                        periodeFom,
                        null,
                        it.sivilstandskode,
                        it.kilde,
                    )
                }
            }
        }

        val datojustertSivilstandListe = mutableListOf<Sivilstand>()

        // sjekk om første element i sivilstandListe har periodeFom etter virkningstidspunkt
        if (mappetSivilstandListe.first().periodeFom.isAfter(virkningstidspunkt)) {
            datojustertSivilstandListe.add(
                Sivilstand(
                    periodeFom = virkningstidspunkt,
                    periodeTom = mappetSivilstandListe.first()!!.periodeFom.minusDays(1),
                    sivilstandskode = Sivilstandskode.UKJENT,
                    kilde = Kilde.OFFENTLIG,
                ),
            )
        }

        datojustertSivilstandListe.addAll(mappetSivilstandListe.filterNotNull())

        return slåSammenSammenhengendePerioderMedLikSivilstandskode(virkningstidspunkt, datojustertSivilstandListe)
            .filter { it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt.minusDays(1)) }
    }

    private fun slåSammenSammenhengendePerioderMedLikSivilstandskode(
        virkningstidspunkt: LocalDate,
        sivilstandsperioder: List<Sivilstand>,
    ): List<Sivilstand> {
        val resultat = mutableListOf<Sivilstand>()
        var kilde: Kilde? = null

        var periodeFom: LocalDate? = null

        for (indeks in sivilstandsperioder.indices) {
            if (indeks < sivilstandsperioder.size - 1) {
                if (sivilstandsperioder[indeks + 1].periodeFom.isBefore(sivilstandsperioder[indeks].periodeTom?.plusDays(2)) &&
                    sivilstandsperioder[indeks + 1].sivilstandskode == sivilstandsperioder[indeks].sivilstandskode
                ) {
                    // perioden overlapper og skal slås sammen
                    if (periodeFom == null) {
                        periodeFom = sivilstandsperioder[indeks].periodeFom
                    }
                    if (sivilstandsperioder[indeks].kilde == Kilde.MANUELL) {
                        kilde = Kilde.MANUELL
                    }
                } else {
                    // neste periode overlapper ikke og det skal lages ny forekomst i sammenslåttListe
                    if (periodeFom != null) {
                        resultat.add(
                            Sivilstand(
                                periodeFom = periodeFom,
                                periodeTom = sivilstandsperioder[indeks].periodeTom,
                                sivilstandskode = sivilstandsperioder[indeks].sivilstandskode,
                                kilde = sivilstandsperioder[indeks].kilde,
                            ),
                        )
                        periodeFom = null
                        kilde = null
                    } else {
                        resultat.add(
                            Sivilstand(
                                periodeFom = sivilstandsperioder[indeks].periodeFom,
                                periodeTom = sivilstandsperioder[indeks].periodeTom,
                                sivilstandskode = sivilstandsperioder[indeks].sivilstandskode,
                                kilde = sivilstandsperioder[indeks].kilde,
                            ),
                        )
                    }
                }
            } else {
                // Siste forekomst
                resultat.add(
                    Sivilstand(
                        periodeFom = periodeFom ?: sivilstandsperioder[indeks].periodeFom,
                        periodeTom = sivilstandsperioder[indeks].periodeTom,
                        sivilstandskode = sivilstandsperioder[indeks].sivilstandskode,
                        kilde = kilde ?: sivilstandsperioder[indeks].kilde,

                    ),
                )
            }
        }
        return resultat.filter { it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt.minusDays(1)) }
    }

    private fun leggTilUkjentForPerioderUtenInfo(virkningstidspunkt: LocalDate, sammenslåttePerioder: List<Sivilstand>): List<Sivilstand> {
        val resultat = mutableListOf<Sivilstand>()

        for (indeks in sammenslåttePerioder.indices) {
            if (indeks == 0) {
                if (sammenslåttePerioder[indeks].periodeFom.isAfter(virkningstidspunkt)) {
                    resultat.add(
                        Sivilstand(
                            periodeFom = virkningstidspunkt,
                            periodeTom = sammenslåttePerioder[indeks].periodeFom.minusDays(1),
                            sivilstandskode = Sivilstandskode.UKJENT,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    )
                }
                resultat.add(sammenslåttePerioder[indeks])
            } else {
                if (sammenslåttePerioder[indeks].periodeFom.isAfter(sammenslåttePerioder[indeks - 1].periodeTom?.plusDays(1))) {
                    resultat.add(
                        Sivilstand(
                            periodeFom = sammenslåttePerioder[indeks - 1].periodeTom!!.plusDays(1),
                            periodeTom = sammenslåttePerioder[indeks].periodeFom.minusDays(1),
                            sivilstandskode = Sivilstandskode.UKJENT,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    )
                }
                resultat.add(sammenslåttePerioder[indeks])
            }
            // Hvis siste element i listen har satt periodeTom så skal det legges til en periode med Ukjent og null i periodeTom
            if (indeks == sammenslåttePerioder.size - 1) {
                if (sammenslåttePerioder[indeks].periodeTom != null) {
                    resultat.add(
                        Sivilstand(
                            periodeFom = sammenslåttePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = null,
                            sivilstandskode = Sivilstandskode.UKJENT,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    )
                }
            }
        }
        return resultat
    }

    private fun motsattSivilstandskode(sivilstandskode: Sivilstandskode): Sivilstandskode {
        return when (sivilstandskode) {
            Sivilstandskode.GIFT_SAMBOER -> return Sivilstandskode.BOR_ALENE_MED_BARN
            Sivilstandskode.BOR_ALENE_MED_BARN -> return Sivilstandskode.GIFT_SAMBOER
            Sivilstandskode.UKJENT -> return Sivilstandskode.UKJENT
            else -> sivilstandskode
        }
    }

    private fun perioderErIdentiske(periode1: Sivilstand, periode2: Sivilstand): Boolean = periode1.periodeFom == periode2.periodeFom &&
        periode1.periodeTom == periode2.periodeTom &&
        periode1.sivilstandskode == periode2.sivilstandskode &&
        periode1.kilde == periode2.kilde

    private fun hentFørsteDagIMåneden(dato: LocalDate): LocalDate = LocalDate.of(dato.year, dato.month, 1)

    private fun hentSisteDagIMåneden(dato: LocalDate): LocalDate = LocalDate.of(dato.year, dato.month, dato.month.length(dato.isLeapYear))

    private fun hentSisteDagIForrigeMåned(dato: LocalDate): LocalDate =
        LocalDate.of(dato.year, dato.month.minus(1), dato.month.minus(1).length(dato.isLeapYear))

    private fun hentFørsteDagINesteMåned(dato: LocalDate): LocalDate = LocalDate.of(dato.year, dato.month, 1).plusMonths(1)

    private fun erSisteDagIMåneden(dato: LocalDate): Boolean {
        // Finner den siste dagen i måneden for den gitte datoen
        val lastDayOfMonth = dato.withDayOfMonth(dato.lengthOfMonth())
        // Sjekker om den gitte datoen er lik den siste dagen i måneden
        return dato == lastDayOfMonth
    }

    // lag metode som sjekker om mottatt dato er siste dag i måneden
}
