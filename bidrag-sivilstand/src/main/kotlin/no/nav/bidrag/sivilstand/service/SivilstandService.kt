package no.nav.bidrag.sivilstand.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.response.Sivilstand
import no.nav.bidrag.sivilstand.response.SivilstandBeregnet
import no.nav.bidrag.sivilstand.response.SivilstandBo
import no.nav.bidrag.sivilstand.response.Status
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

internal class SivilstandService() {
    fun beregn(sivilstandGrunnlagDtoListe: List<SivilstandGrunnlagDto>): SivilstandBeregnet {
        var status = Status.OK

        // Tester på innhold i grunnlag.
        // Sjekker først om det finnes en aktiv og gyldig forekomst
        if (!sivilstandGrunnlagDtoListe.any {
                it.historisk == false
            }
        ) {
            return SivilstandBeregnet(Status.ALLE_FOREKOMSTER_ER_HISTORISKE, emptyList())
        }

        // Sjekker om det finnes forekomster uten datoinformasjon. For ikke-historiske sjekkes det også om registrert har verdi. Hvis ikke feilmeldes det.
        if (sivilstandGrunnlagDtoListe.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.historisk == true
            } ||
            sivilstandGrunnlagDtoListe.any {
                it.gyldigFom == null && it.bekreftelsesdato == null && it.registrert == null && it.historisk == false
            }
        ) {
            return SivilstandBeregnet(Status.MANGLENDE_DATOINFORMASJON, emptyList())
        }

        // Sjekker at sivilstandstype finnes
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

        val sivilstandBoListe = mutableListOf<SivilstandBo>()
        var antallPerioderFunnet = 0

        var periodeTil: LocalDate?

        for (indeks in sortertSivilstandGrunnlagDtoListe.indices) {
            // Setter periodeTil lik periodeFra for neste forekomst.
            // Hvis det ikke finnes en neste forekomst så settes periodeTil lik null. Timestamp registrert brukes bare hvis neste forekomst ikke er historisk
            periodeTil = if (sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.historisk == true) {
                sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.bekreftelsesdato
            } else {
                sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.gyldigFom
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.bekreftelsesdato
                    ?: sortertSivilstandGrunnlagDtoListe.getOrNull(indeks + 1)?.registrert?.toLocalDate()
            }

            sivilstandBoListe.add(
                SivilstandBo(
                    periodeFra = sortertSivilstandGrunnlagDtoListe[indeks].gyldigFom,
                    periodeTil = periodeTil,
                    sivilstandskodePDL = sortertSivilstandGrunnlagDtoListe[indeks].type,
                ),
            )

            // Sjekk på logiske verdier i resultatet
            status = sjekkLogiskeVerdier(sivilstandBoListe)

            antallPerioderFunnet++
        }

        if (status == Status.OK) {
            val sivilstandListe = mutableListOf<Sivilstand>()
            sivilstandBoListe.forEach { sivilstandBo ->
                sivilstandListe.add(
                    Sivilstand(
                        periodeFra = sivilstandBo.periodeFra,
                        periodeTil = sivilstandBo.periodeTil,
                        sivilstandskode = finnSivilstandskode(sivilstandBo.sivilstandskodePDL!!),
                    ),
                )
            }
            return SivilstandBeregnet(status, sivilstandListe)
        } else {
            return SivilstandBeregnet(status, emptyList())
        }
    }

    private fun sjekkLogiskeVerdier(sivilstandBoListe: List<SivilstandBo>): Status {
        // Sjekker om det finnes en status GIFT/REGISTRERT_partner hvis det finnes en status ENKE_ELLER_ENKEMANN, SKILT, SEPARERT eller SEPARERT_PARTNER
        // Sjekken gjøres bare hvis det finnes en forekomst av UGIFT. Hvis UGIFT ikke finnes så er det sannsynlig at personen er innflytter til Norge
        // og det er da vanskelig å gjøre en logisk sjekk på verdiene.

        if (sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.UGIFT }) {
            // Melder feil hvis personen er separert/skilt/enke_enkemann uten å ha være registrert som gift. Samme sjekk gjøres for registrerte partnere.
            if ((
                    sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.ENKE_ELLER_ENKEMANN } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT }
                    ) &&
                !sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.GIFT } ||
                (
                    sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT_PARTNER } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SEPARERT } ||
                        sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.SKILT_PARTNER }
                    ) &&
                !sivilstandBoListe.any { it.sivilstandskodePDL == SivilstandskodePDL.REGISTRERT_PARTNER }
            ) {
                return Status.LOGISK_FEIL_I_TIDSLINJE
            } else {
                return Status.OK
            }
        }
        return Status.OK
    }

    private fun finnSivilstandskode(type: SivilstandskodePDL): Sivilstandskode {
        return when (type) {
            SivilstandskodePDL.UGIFT,
            SivilstandskodePDL.ENKE_ELLER_ENKEMANN,
            SivilstandskodePDL.SKILT,
            SivilstandskodePDL.SEPARERT,
            SivilstandskodePDL.SEPARERT_PARTNER,
            SivilstandskodePDL.SKILT_PARTNER,
            -> Sivilstandskode.BOR_ALENE_MED_BARN

            SivilstandskodePDL.GIFT,
            SivilstandskodePDL.REGISTRERT_PARTNER,
            -> Sivilstandskode.GIFT_SAMBOER

            else -> {
                logger.warn { "Ukjent sivilstandskode: $type" }
                return Sivilstandskode.BOR_ALENE_MED_BARN
            }
        }
    }
}
