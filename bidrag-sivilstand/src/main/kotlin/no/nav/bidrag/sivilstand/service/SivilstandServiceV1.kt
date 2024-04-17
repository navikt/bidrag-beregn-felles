package no.nav.bidrag.sivilstand.service

import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.response.SivilstandBo
import no.nav.bidrag.sivilstand.response.SivilstandV1
import no.nav.bidrag.sivilstand.response.Status
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class SivilstandServiceV1() {
    fun beregn(virkningstidspunkt: LocalDate, sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
        var status = Status.OK

        // Tester på innhold i grunnlag, returnerer tom liste og status med feilmelding hvis minst én av testene under slår til.
        // Sjekker først om det finnes en aktiv og gyldig forekomst
        if (sivilstandGrunnlagDtoListe.none {
                it.historisk == false
            }
        ) {
            return SivilstandBeregnet(Status.ALLE_FOREKOMSTER_ER_HISTORISKE, emptyList())
        }

        // Sjekker om det finnes forekomster uten datoinformasjon. For aktive/ikke-historiske forekomster kan også 'registrert' brukes til å angi periodeFom.
        if (sivilstandGrunnlagDtoListe.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.historisk == true
            } ||
            sivilstandGrunnlagDtoListe.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.registrert == null && it.historisk == false
            }
        ) {
            return SivilstandBeregnet(Status.MANGLENDE_DATOINFORMASJON, emptyList())
        }

        // Sjekker at sivilstandstype er angitt på alle forekomster
        if (sivilstandGrunnlagDtoListe.any {
                it.type == null
            }
        ) {
            return SivilstandBeregnet(Status.SIVILSTANDSTYPE_MANGLER, emptyList())
        }

        val sortertSivilstandGrunnlagDtoListe = sivilstandGrunnlagDtoListe.sortedWith(
            compareByDescending<SivilstandGrunnlagDto> { it.historisk }.thenBy { it.gyldigFom }
                .thenBy { it.bekreftelsesdato }.thenBy { it.registrert }.thenBy { it.type.toString() },
        )

        var periodeFom: LocalDate?
        var periodeTom: LocalDate?

        val sivilstandBoListe = sortertSivilstandGrunnlagDtoListe.mapIndexed { indeks, sivilstand ->
            periodeFom = if (sivilstand.historisk == true) {
                sivilstand.gyldigFom
                    ?: sivilstand.bekreftelsesdato
            } else {
                sivilstand.gyldigFom
                    ?: sivilstand.bekreftelsesdato
                    ?: sivilstand.registrert?.toLocalDate()
            }
            // Setter periodeTom lik periodeFom - 1 dag for neste forekomst.
            // Hvis det ikke finnes en neste forekomst så settes periodeTil lik null. Timestamp registrert brukes bare hvis neste forekomst ikke er historisk
            periodeTom = if (sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.historisk == true) {
                sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.bekreftelsesdato
            } else {
                sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.bekreftelsesdato
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.registrert?.toLocalDate()
            }

            return@mapIndexed SivilstandBo(
                periodeFom = periodeFom!!,
                periodeTom = periodeTom?.minusDays(1),
                sivilstandskodePDL = sortertSivilstandGrunnlagDtoListe[indeks].type,
            )
        }

        // Sjekk på logiske verdier i sivilstandslistenk, kun perioder som overlapper med eller er etter virkningstidspunktet sjekkes.
        status = sjekkLogiskeVerdier(
            sivilstandBoListe.filter { it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt.minusDays(1)) },
        )

        if (status == Status.OK) {
            val sivilstandListe = beregnPerioder(virkningstidspunkt, sivilstandBoListe)
            return SivilstandBeregnet(status, sivilstandListe)
        } else {
            return SivilstandBeregnet(status, emptyList())
        }
    }

    private fun sjekkLogiskeVerdier(sivilstandBoListe: List<SivilstandBo>): Status {
        // Sjekker om det finnes en status GIFT/REGISTRERT_partner hvis det finnes en status ENKE_ELLER_ENKEMANN, SKILT, SEPARERT eller SEPARERT_PARTNER
        // Sjekken gjøres bare hvis det finnes en forekomst av UGIFT. Hvis UGIFT ikke finnes så er det sannsynlig at personen er innflytter til Norge
        // og det er da vanskelig å gjøre en logisk sjekk på verdiene.

        if (sivilstandBoListe
                .any { it.sivilstandskodePDL == SivilstandskodePDL.UGIFT }
        ) {
            // Melder feil hvis personen er separert/skilt/enke_enkemann uten å ha være registrert som gift. Samme sjekk gjøres for registrerte partnere.
            if ((
                    sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.ENKE_ELLER_ENKEMANN } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT }
                    ) &&
                sivilstandBoListe.none { it.sivilstandskodePDL == SivilstandskodePDL.GIFT }
            ) {
                return Status.LOGISK_FEIL_I_TIDSLINJE
            } else {
                if ((
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT_PARTNER } ||
                            sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT_PARTNER } ||
                            sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT_PARTNER }
                        ) &&
                    sivilstandBoListe.none { it.sivilstandskodePDL == SivilstandskodePDL.REGISTRERT_PARTNER }

                ) {
                    return Status.LOGISK_FEIL_I_TIDSLINJE
                }
            }
        }
        return Status.OK
    }

    private fun finnSivilstandskode(type: SivilstandskodePDL): Sivilstandskode {
        return when (type) {
            SivilstandskodePDL.GIFT,
            SivilstandskodePDL.REGISTRERT_PARTNER,
            -> Sivilstandskode.GIFT_SAMBOER

            else -> {
                return Sivilstandskode.BOR_ALENE_MED_BARN
            }
        }
    }

    private fun beregnPerioder(virkningstidspunkt: LocalDate, sivilstandBoListe: List<SivilstandBo>): List<SivilstandV1> {
        val sammenslåttSivilstandV1Liste = mutableListOf<SivilstandV1>()

        val sivilstandV1Liste = sivilstandBoListe.map {
            SivilstandV1(
                periodeFom = it.periodeFom,
                periodeTom = it.periodeTom,
                sivilstandskode = finnSivilstandskode(it.sivilstandskodePDL!!),
            )
        }

        // Justerer datoer. Perioder med 'Bor alene med barn' skal få periodeFra lik første dag i måneden og periodeTil lik siste dag i måneden.
        val datojustertSivilstandV1Listes = sivilstandV1Liste.map {
            if (it.sivilstandskode == Sivilstandskode.BOR_ALENE_MED_BARN) {
                val periodeFom = hentFørsteDagIMåneden(it.periodeFom)
                val periodeTom = if (it.periodeTom == null) null else hentSisteDagIMåneden(it.periodeTom)
                SivilstandV1(
                    periodeFom,
                    periodeTom,
                    it.sivilstandskode,
                )
            } else {
                val periodeFom = hentFørsteDagINesteMåned(it.periodeFom)
                val periodeTom = if (it.periodeTom == null) null else hentSisteDagIForrigeMåned(it.periodeTom)
                // Forekomster med Gift/Samboer ignoreres hvis perioden er mindre enn én måned. Bor alene med barn skal da gjelde.
                if (periodeTom != null) {
                    if (ChronoUnit.MONTHS.between(periodeFom, periodeTom) >= 1) {
                        SivilstandV1(
                            periodeFom,
                            periodeTom,
                            it.sivilstandskode,
                        )
                    } else {
                        null
                    }
                } else {
                    SivilstandV1(
                        periodeFom,
                        periodeTom,
                        it.sivilstandskode,
                    )
                }
            }
        }

        val datojustertSivilstandListeFiltrert = datojustertSivilstandV1Listes.filterNotNull()
        var periodeFom = datojustertSivilstandListeFiltrert[0].periodeFom

        // Slår sammen perioder med samme sivilstandskode
        for (indeks in datojustertSivilstandListeFiltrert.indices) {
            if (datojustertSivilstandListeFiltrert.getOrNull(indeks + 1)?.sivilstandskode
                != datojustertSivilstandListeFiltrert[indeks].sivilstandskode
            ) {
                if (indeks == datojustertSivilstandListeFiltrert.size - 1) {
                    // Siste element i listen
                    sammenslåttSivilstandV1Liste.add(
                        SivilstandV1(
                            periodeFom = if (periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else periodeFom,
                            periodeTom = datojustertSivilstandListeFiltrert[indeks].periodeTom,
                            sivilstandskode = datojustertSivilstandListeFiltrert[indeks].sivilstandskode,
                        ),
                    )
                } else {
                    // Hvis det er flere elementer i listen så justeres periodeTom lik neste periodeFom - 1 dag for Gift/samboer
                    sammenslåttSivilstandV1Liste.add(
                        SivilstandV1(
                            periodeFom = if (periodeFom.isBefore(virkningstidspunkt)) virkningstidspunkt else periodeFom,
                            periodeTom = if (datojustertSivilstandListeFiltrert[indeks].sivilstandskode == Sivilstandskode.GIFT_SAMBOER) {
                                datojustertSivilstandListeFiltrert[indeks + 1].periodeFom.minusDays(1)
                            } else {
                                datojustertSivilstandListeFiltrert[indeks].periodeTom
                            },
                            sivilstandskode = datojustertSivilstandListeFiltrert[indeks].sivilstandskode,
                        ),
                    )
                    periodeFom = datojustertSivilstandListeFiltrert[indeks + 1].periodeFom
                }
            }
        }
        return sammenslåttSivilstandV1Liste.filter { it.periodeTom == null || it.periodeTom.isAfter(virkningstidspunkt.minusDays(1)) }
    }

    private fun hentFørsteDagIMåneden(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, 1)
    }

    private fun hentSisteDagIMåneden(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, dato.month.length(dato.isLeapYear))
    }

    private fun hentSisteDagIForrigeMåned(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, dato.month.length(dato.isLeapYear)).minusMonths(1)
    }

    private fun hentFørsteDagINesteMåned(dato: LocalDate): LocalDate {
        return LocalDate.of(dato.year, dato.month, 1).plusMonths(1)
    }
}
