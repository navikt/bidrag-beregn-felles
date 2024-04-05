package no.nav.bidrag.boforhold

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.boforhold.dto.BoforholdRequest
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.dto.Kilde
import no.nav.bidrag.boforhold.response.RelatertPerson
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
                        periodeTom = LocalDate.of(2020, 2, 11),
                        bostatus = null,
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
    }
}
