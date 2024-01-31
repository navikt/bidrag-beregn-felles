package no.nav.bidrag.sivilstand

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
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

        fun byggHentSivilstandResponseTestUtenDatoerUtenRegistrertEnForekomstHistorisk() = listOf(
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
    }
}
