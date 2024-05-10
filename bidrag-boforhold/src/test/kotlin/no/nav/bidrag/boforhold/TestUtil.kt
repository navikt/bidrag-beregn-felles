package no.nav.bidrag.boforhold

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.boforhold.dto.BoforholdRequest
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.transport.behandling.grunnlag.response.BorISammeHusstandDto
import java.time.LocalDate

class TestUtil {
    companion object {
        inline fun <reified T> fileToObject(path: String): T {
            val jsonString = TestUtil::class.java.getResource(path).readText()
            return ObjectMapper().findAndRegisterModules().readValue(jsonString, T::class.java)
        }

        fun byggBarnHusstandsmedlemAttenÅr() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 12),
                        periodeTil = LocalDate.of(2020, 2, 11),
                    ),
                ),
            ),
        )

        fun byggBarnHusstandsmedlemAttenÅrV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskap() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = emptyList(),
            ),
        )

        fun byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskapV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = emptyList(),
            ),
        )

        fun byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskap() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = emptyList(),
            ),
        )

        fun byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskapV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = emptyList(),
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBrudd() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 2),
                        periodeTil = LocalDate.of(2019, 4, 7),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2023, 7, 2),
                        periodeTil = null,
                    ),
                ),
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBruddV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 2),
                        periodeTom = null,
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggFlereSammenhengendeForekomster() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 2),
                        periodeTil = LocalDate.of(2019, 4, 7),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                    ),
                ),
            ),
        )

        fun byggFlereSammenhengendeForekomsterV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggSammenhengendeForekomsterMed18År() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 2),
                        periodeTil = LocalDate.of(2019, 4, 7),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2021, 7, 2),
                        periodeTil = null,
                    ),
                ),
            ),
        )

        fun byggSammenhengendeForekomsterMed18ÅrV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2017, 4, 2),
                        periodeTom = LocalDate.of(2019, 1, 7),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 7, 2),
                        periodeTom = null,
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggPeriodeFraFørVirkningstidspunkt() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2014, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2022, 1, 12),
                        periodeTil = null,
                    ),
                ),
            ),
        )

        fun byggPeriodeFraFørVirkningstidspunktV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2014, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = null,
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggPeriodeTomEtterAttenårsdag() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2022, 1, 12),
                        periodeTil = LocalDate.of(2022, 12, 27),
                    ),
                ),
            ),
        )

        fun byggPeriodeTomEtterAttenårsdagV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = LocalDate.of(2022, 12, 27),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggOppholdPerioderHusstandsmedlemskapOgAttenår() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 8, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2022, 1, 12),
                        periodeTil = LocalDate.of(2022, 7, 27),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2022, 10, 12),
                        periodeTil = LocalDate.of(2022, 12, 27),
                    ),
                    BorISammeHusstandDto(
                        periodeFra = LocalDate.of(2023, 2, 9),
                        periodeTil = null,
                    ),
                ),
            ),
        )

        fun byggOppholdPerioderHusstandsmedlemskapOgAttenårV2() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 8, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = LocalDate.of(2022, 7, 27),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 10, 12),
                        periodeTom = LocalDate.of(2022, 12, 27),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 2, 9),
                        periodeTom = null,
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeDokumentertSkolegang() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = LocalDate.of(2020, 2, 11),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeEtter18ÅrsdagDokumentertSkolegang() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = LocalDate.of(2020, 2, 11),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun manuellOgOffentligPeriodeErIdentisk() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 12, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun flereManuelleOgOffentligePerioder() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 12, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 4, 12),
                        periodeTom = LocalDate.of(2020, 12, 3),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 2, 17),
                        periodeTom = LocalDate.of(2021, 4, 17),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 6, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 8, 17),
                        periodeTom = LocalDate.of(2021, 11, 17),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 12),
                        periodeTom = LocalDate.of(2023, 5, 4),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = LocalDate.of(2022, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 10, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 2, 1),
                        periodeTom = LocalDate.of(2023, 3, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun barnManuellePerioderMedOppholdFør18Årsdag() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 2, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 1),
                        periodeTom = LocalDate.of(2021, 2, 28),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMed18År() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 4, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun manuelleOgOffentligPeriodeMedNullIPeriodeTom() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 10, 7),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
//                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun manuellOgOffentligPeriodeMedNullIPeriodeTom2018() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2018, 5, 1),
                        periodeTom = LocalDate.of(2020, 2, 29),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun manuellOgOffentligPeriodeMedLikStatus() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun manuellOgOffentligPeriodeMedLikStatusPeriodeTomErNull() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun sorteringAvPerioder() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun flerePersonerIGrunnlagUtenOffentligePerioder() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2010, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = emptyList(),
            ),
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = emptyList(),
            ),
        )

        fun flerePersonerIGrunnlagMedOffentligePerioder() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2010, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun flereOffentligOgManuellPeriodeMedOppholdMellom() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2005, 9, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun flereFlereManuellePerioderMedPeriodeTomNullLikStatus() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun flereFlereManuellePerioderMedPeriodeTomNullUlikStatus() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBostatuskode() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 1),
                        periodeTom = LocalDate.of(2019, 8, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 9, 1),
                        periodeTom = LocalDate.of(2019, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 11, 1),
                        periodeTom = LocalDate.of(2019, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggFlereOverlappendeManuellePerioderMedUlikBostatuskode() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2024, 2, 29),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggManuellPeriodeMed18År() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggManuellePerioderMedOpphold() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 9, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggManuellePerioderMedOppholdPlussOffentligPeriode() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 9, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggUtenPeriodeEtter18årsdagManuell() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggUtenPeriodeEtter18årsdagOffentlig() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggUtenPeriodeEtter18årsdagOffentligOgManuell() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 4, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        // Tester fra front-end
        fun byggManuellPeriodeOverlapperAlleOffentlige() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = LocalDate.of(2022, 4, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 5, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 1),
                        periodeTom = LocalDate.of(2022, 6, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 7, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggKunManuellIkkeMedForelder() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggOffentligePerioderOverlapper() = listOf(
            BoforholdRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                bostatusListe = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 4, 1),
                        periodeTom = LocalDate.of(2021, 8, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 8, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )
    }
}
