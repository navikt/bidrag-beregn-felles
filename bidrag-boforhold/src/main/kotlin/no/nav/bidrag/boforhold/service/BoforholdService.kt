package no.nav.bidrag.boforhold.service

import no.nav.bidrag.boforhold.response.BoforholdBeregnet
import no.nav.bidrag.boforhold.response.Bostatus
import no.nav.bidrag.boforhold.response.RelatertPerson
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class BoforholdService() {
    fun beregnEgneBarn(virkningstidspunkt: LocalDate, boforholdGrunnlagListe: List<RelatertPerson>): List<BoforholdBeregnet> {
        val resultat = mutableListOf<BoforholdBeregnet>()
        boforholdGrunnlagListe
            .filter { it.erBarnAvBmBp }
            .sortedWith(
                compareBy { it.relatertPersonPersonId },
            ).forEach { barn ->
                resultat.addAll(beregnPerioderEgneBarn(virkningstidspunkt, barn))
            }

        return resultat
    }

    private fun beregnPerioderEgneBarn(virkningstidspunkt: LocalDate, relatertPerson: RelatertPerson): List<BoforholdBeregnet> {
        val boforholdBeregnetListe = mutableListOf<BoforholdBeregnet>()

        // Barn som har fyllt 18 år på virkningstidspunket skal få én periode med bostatus REGNES_IKKE_SOM_BARN
        if (personenHarFyllt18År(relatertPerson.fødselsdato!!, virkningstidspunkt)) {
            boforholdBeregnetListe.add(
                BoforholdBeregnet(
                    relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                    periodeFom = virkningstidspunkt,
                    periodeTom = null,
                    bostatus = Bostatus.REGNES_IKKE_SOM_BARN,
                ),
            )
            return boforholdBeregnetListe
        }

        val attenårFraDato = beregnetAttenårFraDato(relatertPerson.fødselsdato)

        if (relatertPerson.borISammeHusstandDtoListe.isEmpty()) {
            if (!personenHarFyllt18År(relatertPerson.fødselsdato, LocalDate.now())) {
                boforholdBeregnetListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                        periodeFom = virkningstidspunkt,
                        periodeTom = null,
                        bostatus = Bostatus.IKKE_MED_FORELDER,
                    ),
                )
            } else {
                if (attenårFraDato.isBefore(virkningstidspunkt)) {
                    // Barnet har fyllt 18 år før virkningstidspunktet
                    boforholdBeregnetListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                            periodeFom = virkningstidspunkt,
                            periodeTom = beregnetAttenårFraDato(relatertPerson.fødselsdato).minusDays(1),
                            bostatus = Bostatus.REGNES_IKKE_SOM_BARN,
                        ),
                    )
                } else {
                    // Barnet har ikke fyllt 18 år før virkningstidspunktet og det må lages to perioder, én før og én etter 18årsdagen
                    boforholdBeregnetListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                            periodeFom = virkningstidspunkt,
                            periodeTom = attenårFraDato.minusDays(1),
                            bostatus = Bostatus.IKKE_MED_FORELDER,
                        ),
                    )
                    boforholdBeregnetListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = relatertPerson.relatertPersonPersonId,
                            periodeFom = attenårFraDato,
                            periodeTom = null,
                            bostatus = Bostatus.REGNES_IKKE_SOM_BARN,
                        ),
                    )
                }
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
                    bostatus = Bostatus.MED_FORELDER,
                )
            }

        val sammenslåttListe = slåSammenOverlappendePerioder(justertBorISammeHusstandDtoListe)

        val komplettTidslinjeListe = fyllUtMedPerioderBarnetIkkeBorIHusstanden(virkningstidspunkt, sammenslåttListe)

        val listeJustertMotAttenårsdag =
            justerMotAttenårsdag(attenårFraDato, komplettTidslinjeListe.filter { it.periodeFom.isBefore(attenårFraDato) })

        return listeJustertMotAttenårsdag
    }

    private fun slåSammenOverlappendePerioder(liste: List<BoforholdBeregnet>): List<BoforholdBeregnet> {
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
                    // perioden overlapper ikke og skal legges til i sammenslåttListe
                    if (periodeFom != null) {
                        sammenslåttListe.add(
                            BoforholdBeregnet(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatus.MED_FORELDER,
                            ),
                        )
                        periodeFom = null
                    } else {
                        sammenslåttListe.add(
                            BoforholdBeregnet(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeFom,
                                periodeTom = liste[indeks].periodeTom,
                                bostatus = Bostatus.MED_FORELDER,
                            ),
                        )
                    }
                }
            } else {
                // Siste forekomst
                sammenslåttListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = Bostatus.MED_FORELDER,
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
                            bostatus = Bostatus.IKKE_MED_FORELDER,
                        ),
                    )
                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                        ),
                    )
                } else {
                    sammenhengendePerioderListe.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = virkningstidspunkt,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
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
                            bostatus = Bostatus.IKKE_MED_FORELDER,
                        ),
                    )
                }

                sammenhengendePerioderListe.add(
                    BoforholdBeregnet(
                        relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                        periodeFom = liste[indeks].periodeFom,
                        periodeTom = liste[indeks].periodeTom,
                        bostatus = liste[indeks].bostatus,
                    ),
                )

                if (indeks == liste.size - 1) {
                    // Siste forekomst
                    if (liste[indeks].periodeTom != null) {
                        sammenhengendePerioderListe.add(
                            BoforholdBeregnet(
                                relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                                periodeFom = liste[indeks].periodeTom!!.plusDays(1),
                                periodeTom = null,
                                bostatus = Bostatus.IKKE_MED_FORELDER,
                            ),
                        )
                    }
                }
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
                        ),
                    )
                    listeJustertMotAttenårsdag.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = attenårFraDato,
                            periodeTom = null,
                            bostatus = Bostatus.REGNES_IKKE_SOM_BARN,
                        ),
                    )
                } else {
                    listeJustertMotAttenårsdag.add(
                        BoforholdBeregnet(
                            relatertPersonPersonId = liste[indeks].relatertPersonPersonId,
                            periodeFom = liste[indeks].periodeFom,
                            periodeTom = liste[indeks].periodeTom,
                            bostatus = liste[indeks].bostatus,
                        ),
                    )
                }
            }
        }
        return listeJustertMotAttenårsdag
    }

    private fun personenHarFyllt18År(fødselsdato: LocalDate, dato: LocalDate): Boolean {
        return ChronoUnit.YEARS.between(fødselsdato.plusMonths(1).withDayOfMonth(1), dato) >= 18
    }

    private fun beregnetAttenårFraDato(fødselsdato: LocalDate): LocalDate {
        return fødselsdato.plusYears(18).plusMonths(1).withDayOfMonth(1)
    }
}
