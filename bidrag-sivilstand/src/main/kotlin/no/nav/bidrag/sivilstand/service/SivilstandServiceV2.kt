package no.nav.bidrag.sivilstand.service

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.bo.SivilstandPDLBo
import no.nav.bidrag.sivilstand.dto.Sivilstand
import no.nav.bidrag.sivilstand.dto.SivilstandRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class SivilstandServiceV2() {
    fun beregn(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        val offentligePerioder = if (sivilstandRequest.offentligePerioder.isNotEmpty()) {
            behandleOffentligePerioder(virkningstidspunkt, sivilstandRequest)
        } else {
            emptyList()
        }

        return if (sivilstandRequest.manuellePerioder.isEmpty()) {
            offentligePerioder
        } else {
            slåSammenManuelleOgOffentligePerioder(virkningstidspunkt, offentligePerioder, sivilstandRequest.manuellePerioder)
        }
    }

    private fun behandleOffentligePerioder(virkningstidspunkt: LocalDate, sivilstandRequest: SivilstandRequest): List<Sivilstand> {
        val offentligePerioder = sivilstandRequest.offentligePerioder

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

    private fun slåSammenManuelleOgOffentligePerioder(
        virkningstidspunkt: LocalDate,
        offentligePerioder: List<Sivilstand>,
        manuellePerioder: List<Sivilstand>,
    ): List<Sivilstand> {
        val resultatliste = mutableListOf<Sivilstand>()

        // Skriver alle manuelle perioder til resultatet. Perioder med identisk informasjon som en offentlig periode skrives med kilde = Offentlig
        manuellePerioder.forEach { manuellPeriode ->
            resultatliste.add(
                Sivilstand(
                    periodeFom = if (manuellPeriode.periodeFom.isBefore(virkningstidspunkt)) {
                        virkningstidspunkt
                    } else {
                        manuellPeriode.periodeFom
                    },
                    periodeTom = manuellPeriode.periodeTom,
                    sivilstandskode = manuellPeriode.sivilstandskode,
                    kilde = if (manuellPeriodeErIdentiskMedOffentligPeriode(
                            manuellPeriode,
                            offentligePerioder,
                        )
                    ) {
                        Kilde.OFFENTLIG
                    } else {
                        manuellPeriode.kilde
                    },
                ),
            )
        }

        // Sjekker offentlige perioder og justerer periodeFom og periodeTom der disse overlapper med manuelle perioder
        // Offentlige perioder som helt dekkes av manuelle perioder skrives ikke til resultatet
        offentligePerioder.forEach { offentligPeriode ->
            // Finner manuelle perioder som overlapper med den offentlige perioden
            val overlappendePerioder = mutableListOf<Sivilstand>()
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

        val sammenslåttePerioder = slåSammenSammenhengendePerioderMedLikSivilstandskode(virkningstidspunkt, resultatliste.sortedBy { it.periodeFom })

        val komplettTidslinje = leggTilUkjentForPerioderUtenInfo(virkningstidspunkt, sammenslåttePerioder.sortedBy { it.periodeFom })
        return komplettTidslinje
    }

    // Offentlig periode sjekkes mot manuelle perioder og justeres til å ikke overlappe med disse. En offentlig periode kan overlappe med 0 til
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

    private fun manuellPeriodeErIdentiskMedOffentligPeriode(manuellPeriode: Sivilstand, offentligePerioder: List<Sivilstand>): Boolean {
        return offentligePerioder.any { offentligPeriode ->
            manuellPeriode.periodeFom == offentligPeriode.periodeFom && manuellPeriode.periodeTom == offentligPeriode.periodeTom &&
                manuellPeriode.sivilstandskode == offentligPeriode.sivilstandskode
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

        var periodeFom = sivilstandsperioder[0].periodeFom

        // Slår sammen perioder med samme sivilstandskode
        for (indeks in sivilstandsperioder.indices) {
            if (sivilstandsperioder.getOrNull(indeks + 1)?.sivilstandskode
                != sivilstandsperioder[indeks].sivilstandskode
            ) {
                if (indeks == sivilstandsperioder.size - 1) {
                    // Siste element i listen
                    resultat.add(
                        Sivilstand(
                            periodeFom = if (periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else periodeFom,
                            periodeTom = sivilstandsperioder[indeks].periodeTom,
                            sivilstandskode = sivilstandsperioder[indeks].sivilstandskode,
                            kilde = kilde ?: sivilstandsperioder[indeks].kilde,
                        ),
                    )
                } else {
                    // Hvis det er flere elementer i listen så justeres periodeTom lik neste periodeFom - 1 dag for Gift/samboer
                    resultat.add(
                        Sivilstand(
                            periodeFom = if (periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else periodeFom,
                            periodeTom = sivilstandsperioder[indeks].periodeTom,
                            sivilstandskode = sivilstandsperioder[indeks].sivilstandskode,
                            kilde = kilde ?: sivilstandsperioder[indeks].kilde,
                        ),
                    )
                    periodeFom = sivilstandsperioder[indeks + 1].periodeFom
                    kilde = null
                }
            } else {
                if (sivilstandsperioder[indeks].kilde == Kilde.MANUELL) {
                    kilde = Kilde.MANUELL
                }
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
