package no.nav.bidrag.sivilstand

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.dto.Sivilstand
import no.nav.bidrag.sivilstand.dto.SivilstandRequest
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {
    companion object {
        inline fun <reified T> fileToObject(path: String): T {
            val jsonString = TestUtil::class.java.getResource(path).readText()
            return ObjectMapper().findAndRegisterModules().readValue(jsonString, T::class.java)
        }

        fun byggHentSivilstandResponseTestSortering() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.parse("2017-07-17"),
                bekreftelsesdato = LocalDate.parse("2016-06-16"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                historisk = false,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.parse("2011-02-24"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UOPPGITT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.parse("2001-05-17"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = LocalDate.parse("2021-09-21"),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2021-03-01T12:00:00"),
                historisk = true,
            ),
        )

        fun byggSivilstandUtenAktivStatus() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
            ),
        )

        fun byggSivilstandMedPeriodeUtenDatoer() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                historisk = true,
            ),
        )

        fun byggSivilstandÉnForekomstBorAleneMedBarn() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
            ),
        )

        fun byggSivilstandFlereForekomstBorAleneMedBarn() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = LocalDate.of(2019, 7, 12),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.of(2017, 3, 17),
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = true,
            ),
        )

        fun byggSivilstandÉnForekomstGiftSamboer() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerMedRegistrertEnForekomstHistorisk() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                historisk = true,
            ),
        )

        fun byggSivilstandMedAktivForekomstOgKunRegistrert() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
            ),
        )

        fun byggSivilstandMedLogiskFeil() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
            ),
        )

        fun byggSivilstandFlereForkomsterISammeMåned() = listOf(
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.of(2017, 4, 21),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
            ),
            SivilstandGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = LocalDate.of(2017, 4, 30),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
            ),
        )

        fun byggHentSivilstandResponseTestSorteringV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.parse("2017-07-17"),
                    bekreftelsesdato = LocalDate.parse("2016-06-16"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.parse("2011-02-24"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UOPPGITT,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.parse("2001-05-17"),
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.parse("2021-09-21"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-03-01T12:00:00"),
                    historisk = true,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandUtenAktivStatusV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandMedPeriodeUtenDatoerV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                    historisk = true,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandMedAktivForekomstOgKunRegistrertV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.of(2017, 3, 7),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                    historisk = false,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandÉnForekomstBorAleneMedBarnV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = false,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandÉnForekomstGiftSamboerV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = false,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandFlereForekomstBorAleneMedBarnV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = LocalDate.of(2019, 7, 12),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = null,
                    bekreftelsesdato = LocalDate.of(2017, 3, 17),
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                    historisk = true,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandMedLogiskFeilV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = LocalDate.of(2017, 3, 7),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                    historisk = false,
                ),
            ),
            emptyList(),
        )

        fun byggSivilstandFlereForkomsterISammeMånedV2() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = LocalDate.of(2017, 3, 7),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.of(2017, 4, 21),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.of(2017, 4, 30),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                    historisk = false,
                ),
            ),
            emptyList(),
        )

        fun manuellOgOffentligPeriodeErIdentisk() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = LocalDate.of(2017, 3, 7),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = false,
                ),
            ),
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2020, 9, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun flereManuelleOgOffentligePerioder() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "98765432109",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.of(2021, 2, 17),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "98765432109",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = LocalDate.of(2020, 4, 12),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
            ),
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2023, 4, 1),
                    periodeTom = LocalDate.of(2023, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                Sivilstand(
                    periodeFom = LocalDate.of(2021, 7, 1),
                    periodeTom = LocalDate.of(2021, 12, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun kunManuellPeriode() = SivilstandRequest(
            emptyList(),
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2010, 7, 1),
                    periodeTom = LocalDate.of(2023, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun manuellOgOffentligPerioderLikSivilstandskode() = SivilstandRequest(
            listOf(
                SivilstandGrunnlagDto(
                    personId = "98765432109",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.of(2020, 4, 12),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = false,
                ),
                SivilstandGrunnlagDto(
                    personId = "98765432109",
                    type = SivilstandskodePDL.SKILT,
                    gyldigFom = LocalDate.of(2021, 4, 12),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = false,
                ),
            ),
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2021, 7, 1),
                    periodeTom = LocalDate.of(2023, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun flereManuellePerioder() = SivilstandRequest(
            emptyList(),
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2022, 9, 1),
                    periodeTom = LocalDate.of(2023, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                Sivilstand(
                    periodeFom = LocalDate.of(2021, 7, 1),
                    periodeTom = LocalDate.of(2021, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                Sivilstand(
                    periodeFom = LocalDate.of(2022, 1, 1),
                    periodeTom = LocalDate.of(2022, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )
    }
}
