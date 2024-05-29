package no.nav.bidrag.sivilstand.service

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.bo.SivilstandPDLBo
import no.nav.bidrag.sivilstand.dto.EndreSivilstand
import no.nav.bidrag.sivilstand.dto.Sivilstand
import no.nav.bidrag.sivilstand.dto.SivilstandRequest
import no.nav.bidrag.sivilstand.dto.TypeEndring
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class SivilstandServiceV2() {
    fun beregn(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        // Dette er følgende scenarier som kan forekomme i beregning:
        // 1.

        // Beregner offentlige perioder som er hentet inn.
        val offentligePerioder = behandleOffentligePerioder(virkningstidspunkt, sivilstandRequest)

        if (sivilstandRequest.endreSivilstand == null) {
            if (sivilstandRequest.behandledeSivilstandsopplysninger.isNotEmpty()) {
                // virkningstidspunkt er endret, juster og fyll inn med offentlig informasjon.
                val sammenslåtteBehandledeOgOffentligePerioder =
                    slåSammenPrimærOgSekundærperioder(virkningstidspunkt, sivilstandRequest.behandledeSivilstandsopplysninger, offentligePerioder)

                return sammenslåtteBehandledeOgOffentligePerioder
            }

            // Hvis behandledeSivilstandsopplysninger er tom så returneres offentlige opplysninger uendret. Hvis behandledeSivilstandsopplysninger
            // er utfyllt betyr det at virkningstidspunkt er endret og behandledeSivilstandsopplysninger skal justeres deretter. Hvis
            // virkningstidspunkt er endret tilbake i tid så skal hullet i tidslinjen fylles med offentlige opplysninger.
            return if (sivilstandRequest.behandledeSivilstandsopplysninger.isEmpty()) {
                offentligePerioder
            } else {
                // Virkningstidspunkt er endret og behandlede perioder skal justeres.
                slåSammenPrimærOgSekundærperioder(virkningstidspunkt, sivilstandRequest.behandledeSivilstandsopplysninger, offentligePerioder)
            }
        }

        // Filterer først bort alle perioder med behandlede opplysninger som avsluttes før startdatoBeregning
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

        if (offentligePerioder.isEmpty() && behandledeOpplysninger.isEmpty() && sivilstandRequest.endreSivilstand == null) {
            // Ingen perioder innenfor beregningsperiode. Dette skal ikke forekomme. Hvis det ikke finnes offentlige perioder så skal
            // bidrag-behandling legge til en periode med Sivilstandskode = UKJENT og Kilde = OFFENTLIG i input.
            return emptyList()
        }

//        val offentligePerioder = if (sivilstandRequest.innhentedeOffentligeOpplysninger.isNotEmpty() &&
//            sivilstandRequest.endreSivilstand == null
//        ) {
//            behandleOffentligePerioder(virkningstidspunkt, sivilstandRequest)
//        } else {
//            emptyList()
//        }

        val endredeSivilstandsperioder = behandleEndringer(virkningstidspunkt, sivilstandRequest.endreSivilstand)

        if (behandledeOpplysninger.isEmpty()) {
            // Det finnes ingen offentlige eller behandlede perioder og den nye bostatusperioden skal returneres sammen med genererte perioder
            // som fyller tidslinjen fra virkningstidspunkt til dagens dato.

            if (sivilstandRequest.endreSivilstand.typeEndring != TypeEndring.NY) {
                // Feilsituasjon. Må alltid være ny hvis det ikke finnes perioder fra før.
                return emptyList()
            }

            return if (behandledeOpplysninger.isEmpty()) {
                offentligePerioder
            } else {
                slåSammenPrimærOgSekundærperioder(virkningstidspunkt, sivilstandRequest.behandledeSivilstandsopplysninger, offentligePerioder)
            }
        }
        return emptyList()
    }

    private fun behandleOffentligePerioder(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        val offentligePerioder = sivilstandRequest.innhentedeOffentligeOpplysninger

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

        // Skriver alle manuelle perioder til resultatet. Perioder med identisk informasjon som en offentlig periode skrives med kilde = Offentlig
        primærperioder.forEach { primærperiode ->
            resultatliste.add(
                Sivilstand(
                    periodeFom = if (primærperiode.periodeFom.isBefore(virkningstidspunkt)) {
                        virkningstidspunkt
                    } else {
                        primærperiode.periodeFom
                    },
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

            val justertOffentligPeriode = justerPeriodeOffentligOpplysning(sekundærperiode, overlappendePerioder.sortedBy { it.periodeFom })
            if (justertOffentligPeriode != null) {
                resultatliste.addAll(justertOffentligPeriode)
            }
        }

        val sammenslåttePerioder = slåSammenSammenhengendePerioderMedLikSivilstandskode(virkningstidspunkt, resultatliste.sortedBy { it.periodeFom })

        val komplettTidslinje = leggTilUkjentForPerioderUtenInfo(virkningstidspunkt, sammenslåttePerioder.sortedBy { it.periodeFom })
        return komplettTidslinje
    }

    // Offentlig periode sjekkes mot behandlede perioder og justeres til å ikke overlappe med disse. En offentlig periode kan overlappe med 0 til
    // mange manuelle perioder. Hvis en offentlig periode dekkes helt av manuelle perioder returnere metoden null, ellers returneres en liste. Hvis
    // en offentlig perioder overlappes av flere enn to manuelle perioder så vil responsen bestå av flere offentlige perioder som dekker
    // oppholdet mellom de ulike manuelle periodene.
    private fun justerPeriodeOffentligOpplysning(offentligePeriode: Sivilstand, overlappendePerioder: List<Sivilstand>): List<Sivilstand>? {
        var periodeFom: LocalDate? = null
        var periodeTom: LocalDate? = null
        val justertOffentligPeriodeListe = mutableListOf<Sivilstand>()

        if (overlappendePerioder.isEmpty()) {
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
                        Sivilstand(
                            periodeFom = offentligePeriode.periodeFom,
                            periodeTom = periodeTom,
                            sivilstandskode = offentligePeriode.sivilstandskode,
                            kilde = offentligePeriode.kilde,
                        ),
                    )
                    periodeFom = null
                    periodeTom = null
                }
            }
            if (indeks < overlappendePerioder.size - 1) {
                if (overlappendePerioder[indeks + 1].periodeFom.isAfter(overlappendePerioder[indeks].periodeTom)) {
                    // Det er en åpen tidsperiode mellom to manuelle perioder, og den offentlige perioden skal fylle denne tidsperioden
                    periodeTom = overlappendePerioder[indeks + 1].periodeFom.minusDays(1)
                    justertOffentligPeriodeListe.add(
                        Sivilstand(
                            // periodeFom er satt hvis første manuelle periode overlapper startdato for offentlig periode
                            periodeFom = periodeFom ?: overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                            periodeTom = periodeTom,
                            sivilstandskode = offentligePeriode.sivilstandskode,
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
                            Sivilstand(
                                periodeFom = overlappendePerioder[indeks].periodeTom!!.plusDays(1),
                                periodeTom = offentligePeriode.periodeTom,
                                sivilstandskode = offentligePeriode.sivilstandskode,
                                kilde = offentligePeriode.kilde,
                            ),
                        )
                    }
                }
            }
        }
        return justertOffentligPeriodeListe
    }

    private fun behandleEndringer(startdatoBeregning: LocalDate, endreSivilstand: EndreSivilstand): List<Sivilstand> {
        val endredePerioder = mutableListOf<Sivilstand>()
        val nySivilstand = endreSivilstand.nySivilstand
        val originalSivilstand = endreSivilstand.originalSivilstand

        when (endreSivilstand.typeEndring) {
            TypeEndring.SLETTET -> {
                if (originalSivilstand == null) {
                    // Hvis det ikke finnes original bostatuskode så skal det ikke være mulig å slette en periode
                    secureLogger.info {
                        "Periode som skal slettes må være angitt som originalSivilstand i input. endreSivilstand: " +
                            "$endreSivilstand "
                    }
                    throw IllegalStateException("Periode som skal slettes må være angitt som originalSivilstand i input")
                }
                // Returnerer en periode med samme periodeFom og periodeTom som original periode med motsatt bostatuskode
                endredePerioder.add(
                    Sivilstand(
                        periodeFom = originalSivilstand.periodeFom,
                        periodeTom = originalSivilstand.periodeTom,
                        sivilstandskode = motsattSivilstandskode(originalSivilstand.sivilstandskode),
                        kilde = Kilde.MANUELL,
                    ),
                )
                return endredePerioder
            }

            TypeEndring.NY -> {
                if (nySivilstand == null) {
                    // Hvis det ikke finnes en ny bostatus så kan det ikke leges til ny periode
                    secureLogger.info {
                        "Periode som skal legges til må være angitt som nySivilstand i input. endreSivilstand: " +
                            "$endreSivilstand "
                    }
                    throw IllegalStateException("Periode som skal legges til mangler i input")
                }
                endredePerioder.add(
                    Sivilstand(
                        periodeFom = if (nySivilstand.periodeFom.isBefore(startdatoBeregning)) startdatoBeregning else nySivilstand.periodeFom,
                        periodeTom = nySivilstand.periodeTom,
                        sivilstandskode = nySivilstand.sivilstandskode,
                        kilde = Kilde.MANUELL,
                    ),
                )
                return endredePerioder
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
                    return endredePerioder
                }

                if (originalSivilstand.periodeTom != null && nySivilstand.periodeFom.isAfter(originalSivilstand.periodeTom)) {
                    // Perioden er endret til å være helt utenfor original periode. Det må genereres en periode med motsatt sivilstandskode i
                    // tidsrommet for den originale perioden, i tillegg til at den nye perioden legges til.
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = originalSivilstand.periodeFom!!,
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
                    return endredePerioder
                }

                if (nySivilstand.periodeFom.isAfter(originalSivilstand.periodeFom)) {
                    // Det må lages en ny periode med original bostatuskode for perioden mellom gammel og ny periodeFom
                    endredePerioder.add(
                        Sivilstand(
                            periodeFom = originalSivilstand.periodeFom,
                            periodeTom = nySivilstand.periodeFom.minusDays(1),
                            sivilstandskode = motsattSivilstandskode(nySivilstand.sivilstandskode),
                            kilde = originalSivilstand.kilde,
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
                        // Det må lages en ny periode med motsatt bostatuskode for perioden mellom ny periodeTom og gammel periodeTom
                        endredePerioder.add(
                            Sivilstand(
                                periodeFom = nySivilstand.periodeTom.plusDays(1),
                                periodeTom = null,
                                sivilstandskode = motsattSivilstandskode(originalSivilstand.sivilstandskode),
                                kilde = originalSivilstand.kilde,
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
                return endredePerioder
            }
        }
    }

    private fun beregnetPeriodeErInnenforOffentligPeriodeMedLikSivilstandskode(
        beregnetPeriode: Sivilstand,
        offentligePerioder: List<Sivilstand>,
    ): Boolean {
        return offentligePerioder.any { offentligPeriode ->
            beregnetPeriode.sivilstandskode == offentligPeriode.sivilstandskode &&
                beregnetPeriode.periodeFom.isAfter(offentligPeriode.periodeFom.minusDays(1)) &&
                (offentligPeriode.periodeTom == null || beregnetPeriode.periodeTom?.isBefore(offentligPeriode.periodeTom.plusDays(1)) == true)
        }
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

    private fun finnSivilstandskode(type: SivilstandskodePDL): Sivilstandskode {
        return when (type) {
            SivilstandskodePDL.GIFT, SivilstandskodePDL.REGISTRERT_PARTNER -> Sivilstandskode.GIFT_SAMBOER
            SivilstandskodePDL.UOPPGITT -> Sivilstandskode.UKJENT
            else -> Sivilstandskode.BOR_ALENE_MED_BARN
        }
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
        val mappetSivilstandListe = sivilstandListe.map {
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
                val periodeFom = if (it.periodeFom == virkningstidspunkt) {
                    it.periodeFom
                } else {
                    hentFørsteDagINesteMåned(it.periodeFom)
                }
                val periodeTom = if (it.periodeTom == null) null else hentSisteDagIForrigeMåned(it.periodeTom)
                // Forekomster med Gift/Samboer ignoreres hvis perioden er mindre enn én måned. Bor alene med barn skal da gjelde.
                if (periodeTom != null) {
                    if (ChronoUnit.MONTHS.between(periodeFom, periodeTom) >= 1) {
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
                        periodeTom,
                        it.sivilstandskode,
                        it.kilde,
                    )
                }
            }
        }

        val datojustertSivilstandListe = mutableListOf<Sivilstand>()

        // sjekk om første element i sivilstandListe har periodeFom etter virkningstidspunkt
        if (mappetSivilstandListe.first()!!.periodeFom.isAfter(virkningstidspunkt)) {
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

    private fun hentFørsteDagIMåneden(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, 1)
    }

    private fun hentSisteDagIMåneden(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, dato.month.length(dato.isLeapYear))
    }

    private fun hentSisteDagIForrigeMåned(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month.minus(1), dato.month.minus(1).length(dato.isLeapYear))
    }

    private fun hentFørsteDagINesteMåned(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, 1).plusMonths(1)
    }
}
