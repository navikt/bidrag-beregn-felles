package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class BoforholdServiceV1 {
    fun beregnEgneBarn(virkningstidspunkt: LocalDate, boforholdGrunnlagListe: List<RelatertPerson>): List<BoforholdBeregnet> {
        secureLogger.debug { "Beregner bostatus for BMs egne barn. Input: $virkningstidspunkt $boforholdGrunnlagListe" }

        val resultat = mutableListOf<BoforholdBeregnet>()
        boforholdGrunnlagListe
            .filter { it.erBarnAvBmBp }
            .sortedWith(
                compareBy { it.relatertPersonPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderEgneBarn(virkningstidspunkt, barn))
            }

        secureLogger.debug { "Resultat av beregning bostatus for BMs egne barn: $resultat" }

        return resultat
    }

    private fun beregnPerioderEgneBarn(virkningstidspunkt: LocalDate, relatertPerson: RelatertPerson): List<BoforholdBeregnet> {
        val boforholdBeregnetListe = mutableListOf<BoforholdBeregnet>()

        // Barn som har fylt 18 år på virkningstidspunket skal få én periode med bostatus REGNES_IKKE_SOM_BARN
        if (personenHarFylt18År(relatertPerson.fødselsdato!!, virkningstidspunkt)) {
            boforholdBeregnetListe.add(
                BoforholdBeregnet(
                    relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                    periodeFom = virkningstidspunkt,
                    periodeTom = null,
                    bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                    fødselsdato = relatertPerson.fødselsdato,
                ),
            )
            return boforholdBeregnetListe
        }

        val attenårFraDato = beregnetAttenÅrFraDato(relatertPerson.fødselsdato)

        // Behandler først barn uten husstandsmedlemskap
        if (relatertPerson.borISammeHusstandDtoListe.isEmpty()) {
            if (!personenHarFylt18År(relatertPerson.fødselsdato, LocalDate.now())) {
                boforholdBeregnetListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                        periodeFom = virkningstidspunkt,
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        fødselsdato = relatertPerson.fødselsdato,
                    ),
                )
            } else {
                // Barnet fyller 18 år mellom virkningstidspunktet og dagens dato, og det må lages to perioder, én før og én etter 18årsdagen
                boforholdBeregnetListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                        periodeFom = virkningstidspunkt,
                        periodeTom = attenårFraDato.minusDays(1),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        fødselsdato = relatertPerson.fødselsdato,
                    ),
                )
                boforholdBeregnetListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                        periodeFom = attenårFraDato,
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        fødselsdato = relatertPerson.fødselsdato,
                    ),
                )
            }
            return boforholdBeregnetListe
        }

        val justertBorISammeHusstandDtoListe = relatertPerson.borISammeHusstandDtoListe
            .filter { (it.periodeTil == null || it.periodeTil!!.isAfter(virkningstidspunkt)) }
            .map {
                BoforholdBeregnet(
                    relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                    periodeFom = if (it.periodeFra == null) virkningstidspunkt else it.periodeFra!!.withDayOfMonth(1),
                    periodeTom = it.periodeTil?.plusMonths(1)?.withDayOfMonth(1)?.minusDays(1),
                    bostatus = Bostatuskode.MED_FORELDER,
                    fødselsdato = relatertPerson.fødselsdato,
                )
            }

        val sammenslåttListe = slåSammenOverlappendeMedForelderPerioder(justertBorISammeHusstandDtoListe)

        val komplettTidslinjeListe = fyllUtMedPerioderBarnetIkkeBorIHusstanden(virkningstidspunkt, sammenslåttListe)

        val listeJustertMotAttenårsdag =
            justerMotAttenårsdag(attenårFraDato, komplettTidslinjeListe.filter { it.periodeFom.isBefore(attenårFraDato) })

        return listeJustertMotAttenårsdag
    }

    private fun slåSammenOverlappendeMedForelderPerioder(liste: List<BoforholdBeregnet>): List<BoforholdBeregnet> {
        var periodeFom: LocalDate? = null
        val sammenslåttListe = mutableListOf<BoforholdBeregnet>()

        for (indeks in liste.indices) {
            if (indeks < liste.size - 1) {
                if (liste[indeks + 1].periodeFom.isBefore(liste[indeks].periodeTom?.plusDays(2))) {
                    // perioden overlapper og skal slås sammen
                    if (periodeFom == null) {
                        periodeFom = liste[indeks].periodeFom
                    }
                } else {
                    // neste periode overlapper ikke og det skal lages ny forekomst i sammenslåttListe
                    if (periodeFom != null) {
                        sammenslåttListe.add(
                            BoforholdBeregnet(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatuskode.MED_FORELDER,
                                fødselsdato = liste[indeks].fødselsdato,
                            ),
                        )
                        periodeFom = null
                    } else {
                        sammenslåttListe.add(
                            BoforholdBeregnet(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatuskode.MED_FORELDER,
                                fødselsdato = liste[indeks].fødselsdato,
                            ),
                        )
                    }
                }
            } else {
                // Siste forekomst
                sammenslåttListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = periodeFom ?: liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = Bostatuskode.MED_FORELDER,
                        fødselsdato = liste[indeks].fødselsdato,
                    ),
                )
            }
        }
        return sammenslåttListe
    }

    private fun fyllUtMedPerioderBarnetIkkeBorIHusstanden(virkningstidspunkt: LocalDate, liste: List<BoforholdBeregnet>): List<BoforholdBeregnet> {
        val sammenhengendePerioderListe = mutableListOf<BoforholdBeregnet>()

        for (indeks in liste.indices) {
//            Sjekker første forekomst og danner periode mellom virkningstidspunkt og første forekomst hvis det er opphold
            if (indeks == 0) {
                if (liste[indeks].periodeFom.isAfter(virkningstidspunkt)) {
                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = virkningstidspunkt,
                            periodeTom = liste[indeks].periodeFom.minusDays(1),
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                } else {
                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = virkningstidspunkt,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                }
            } else {
                if (liste[indeks - 1].periodeTom!!.isBefore(liste[indeks].periodeFom.plusDays(1))) {
                    // Det er opphold mellom to perioder og det må lages en periode med bostatus IKKE_MED_FORELDER for oppholdet

                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks - 1].periodeTom!!.plusDays(1),
                            periodeTom = liste[indeks].periodeFom.minusDays(1),
                            bostatus = Bostatuskode.IKKE_MED_FORELDER,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                }

                sammenhengendePerioderListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                        fødselsdato = liste[indeks].fødselsdato,
                    ),
                )
            }

            // Siste forekomst
            if (indeks == liste.size - 1 && liste[indeks].periodeTom != null) {
                sammenhengendePerioderListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeTom!!.plusDays(1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        fødselsdato = liste[indeks].fødselsdato,
                    ),
                )
            }
        }
        return sammenhengendePerioderListe
    }

    private fun justerMotAttenårsdag(attenårFraDato: LocalDate, liste: List<BoforholdBeregnet>): List<BoforholdBeregnet> {
        val listeJustertMotAttenårsdag = mutableListOf<BoforholdBeregnet>()
        for (indeks in liste.indices) {
            if (liste[indeks].periodeTom != null && liste[indeks].periodeTom!!.isAfter(attenårFraDato)) {
                listeJustertMotAttenårsdag.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeFom,
                        periodeTom = attenårFraDato.minusDays(1),
                        bostatus = liste[indeks].bostatus,
                        fødselsdato = liste[indeks].fødselsdato,
                    ),
                )
                listeJustertMotAttenårsdag.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = attenårFraDato,
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        fødselsdato = liste[indeks].fødselsdato,
                    ),
                )
            } else {
                if (liste[indeks].periodeTom == null && attenårFraDato.isBefore(LocalDate.now())) {
                    listeJustertMotAttenårsdag.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = attenårFraDato.minusDays(1),
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                    listeJustertMotAttenårsdag.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = attenårFraDato,
                            periodeTom = null,
                            bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                } else {
                    listeJustertMotAttenårsdag.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                            fødselsdato = liste[indeks].fødselsdato,
                        ),
                    )
                }
            }
        }
        return listeJustertMotAttenårsdag
    }

    private fun personenHarFylt18År(fødselsdato: LocalDate, dato: LocalDate): Boolean =
        ChronoUnit.YEARS.between(fødselsdato.plusMonths(1).withDayOfMonth(1), dato) >= 18

    private fun beregnetAttenÅrFraDato(fødselsdato: LocalDate): LocalDate = fødselsdato.plusYears(18).plusMonths(1).withDayOfMonth(1)
}
