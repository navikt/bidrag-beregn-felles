package no.nav.bidrag.boforhold

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.boforhold.response.BorISammeHusstandBeregningDto
import no.nav.bidrag.boforhold.response.RelatertPerson
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
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 12),
                        periodeTil = LocalDate.of(2020, 2, 11),
                        grunnlagsreferanse = "ref_20190412",
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

        fun byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskap() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = emptyList(),
            ),
        )

        fun byggFlereSammenhengendeForekomster() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 2),
                        periodeTil = LocalDate.of(2019, 4, 7),
                        grunnlagsreferanse = "ref_20190402",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                        grunnlagsreferanse = "ref_20190410",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                        grunnlagsreferanse = "ref_20190502",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2023, 7, 2),
                        periodeTil = null,
                        grunnlagsreferanse = "ref_20230702",
                    ),
                ),
            ),

        )

        fun byggSammenhengendeForekomsterMedHullPerioder() = listOf(
            RelatertPerson(
                relatertPersonPersonId = "28765432109",
                fødselsdato = LocalDate.of(2010, 3, 17),
                erBarnAvBmBp = true,
                borISammeHusstandDtoListe = listOf(
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 3, 1),
                        periodeTil = LocalDate.of(2019, 4, 7),
                        grunnlagsreferanse = "ref_20190301",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                        grunnlagsreferanse = "ref_20190410",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                        grunnlagsreferanse = "ref_20190502",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2021, 7, 2),
                        periodeTil = null,
                        grunnlagsreferanse = "ref_20210702",
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
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 2),
                        periodeTil = LocalDate.of(2019, 4, 7),
                        grunnlagsreferanse = "ref_20190402",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 4, 10),
                        periodeTil = LocalDate.of(2019, 4, 17),
                        grunnlagsreferanse = "ref_20190410",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2019, 5, 2),
                        periodeTil = LocalDate.of(2019, 7, 28),
                        grunnlagsreferanse = "ref_20190502",
                    ),
                    BorISammeHusstandBeregningDto(
                        periodeFra = LocalDate.of(2021, 7, 2),
                        periodeTil = null,
                        grunnlagsreferanse = "ref_20190702",
                    ),
                ),
            ),

        )
    }
}
