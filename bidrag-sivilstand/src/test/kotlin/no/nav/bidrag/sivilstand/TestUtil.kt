package no.nav.bidrag.sivilstand

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.response.SivilstandBeregningGrunnlagDto
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {
    companion object {
        inline fun <reified T> fileToObject(path: String): T {
            val jsonString = TestUtil::class.java.getResource(path).readText()
            return ObjectMapper().findAndRegisterModules().readValue(jsonString, T::class.java)
        }

        fun byggHentSivilstandResponseTestSortering() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.parse("2017-07-17"),
                bekreftelsesdato = LocalDate.parse("2016-06-16"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
                grunnlagsreferanse = "ref_GIFT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SKILT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.parse("2011-02-24"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
                grunnlagsreferanse = "ref_UGIFT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UOPPGITT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.parse("2001-05-17"),
                master = "PDL",
                registrert = LocalDateTime.now(),
                historisk = true,
                grunnlagsreferanse = "ref_UOPPGITT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = LocalDate.parse("2021-09-21"),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2021-03-01T12:00:00"),
                historisk = true,
                grunnlagsreferanse = "ref_SEPARERT",
            ),
        )

        fun byggSivilstandUtenAktivStatus() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
                grunnlagsreferanse = "ref_SKILT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
                grunnlagsreferanse = "ref_GIFT",
            ),
        )

        fun byggSivilstandMedPeriodeUtenDatoer() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SKILT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                historisk = true,
                grunnlagsreferanse = "ref_GIFT",
            ),
        )

        fun byggSivilstandÉnForekomstBorAleneMedBarn() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SKILT",
            ),
        )

        fun byggSivilstandFlereForekomstBorAleneMedBarn() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SKILT_1",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = LocalDate.of(2019, 7, 12),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = true,
                grunnlagsreferanse = "ref_SKILT_2",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SKILT,
                gyldigFom = null,
                bekreftelsesdato = LocalDate.of(2017, 3, 17),
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = true,
                grunnlagsreferanse = "ref_SKILT_3",
            ),
        )

        fun byggSivilstandÉnForekomstGiftSamboer() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2020-05-12T11:30:00"),
                historisk = false,
                grunnlagsreferanse = "ref_GIFT",
            ),
        )

        fun byggHentSivilstandResponseTestUtenDatoerMedRegistrertEnForekomstHistorisk() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2017-03-01T12:00:00"),
                historisk = true,
                grunnlagsreferanse = "ref_GIFT",
            ),
        )

        fun byggSivilstandMedAktivForekomstOgKunRegistrert() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                grunnlagsreferanse = "ref_GIFT",
                historisk = true,
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SEPARERT",
            ),
        )

        fun byggSivilstandMedLogiskFeil() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
                grunnlagsreferanse = "ref_UGIFT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = null,
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SEPARERT",
            ),
        )

        fun byggSivilstandFlereForkomsterISammeMåned() = listOf(
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.UGIFT,
                gyldigFom = LocalDate.of(2017, 3, 7),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = null,
                historisk = true,
                grunnlagsreferanse = "ref_UGIFT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.GIFT,
                gyldigFom = LocalDate.of(2017, 4, 21),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
                grunnlagsreferanse = "ref_GIFT",
            ),
            SivilstandBeregningGrunnlagDto(
                personId = "12345678901",
                type = SivilstandskodePDL.SEPARERT,
                gyldigFom = LocalDate.of(2017, 4, 30),
                bekreftelsesdato = null,
                master = "PDL",
                registrert = LocalDateTime.parse("2022-12-07T12:00:00"),
                historisk = false,
                grunnlagsreferanse = "ref_SEPARERT",
            ),
        )
    }
}
