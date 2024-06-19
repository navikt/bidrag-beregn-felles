package no.nav.bidrag.sivilstand

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.sivilstand.dto.EndreSivilstand
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
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandUtenAktivStatusV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandMedPeriodeUtenDatoerV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandMedAktivForekomstOgKunRegistrertV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandÉnForekomstBorAleneMedBarnV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandÉnForekomstGiftSamboerV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandFlereForekomstBorAleneMedBarnV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandMedLogiskFeilV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun byggSivilstandFlereForkomsterISammeMånedV2() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun manuellOgOffentligPeriodeErIdentisk() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger =
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2020, 9, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreSivilstand = EndreSivilstand(
                typeEndring = TypeEndring.NY,
                nySivilstand = Sivilstand(
                    periodeFom = LocalDate.of(2022, 10, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                originalSivilstand = null,
            ),
        )

        fun flereManuelleOgOffentligePerioderFlereRequester() = listOf(
            // 1
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger =
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
                behandledeSivilstandsopplysninger = emptyList(),
                endreSivilstand = null,
            ),
            // 2
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger =
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
                behandledeSivilstandsopplysninger =
                listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 2, 28),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
            // 3
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger =
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
                behandledeSivilstandsopplysninger =
                listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 2, 28),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 3, 1),
                        periodeTom = LocalDate.of(2021, 6, 30),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2023, 4, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
        )

        fun kunManuellPeriode() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = EndreSivilstand(
                typeEndring = TypeEndring.NY,
                nySivilstand = Sivilstand(
                    periodeFom = LocalDate.of(2010, 7, 1),
                    periodeTom = LocalDate.of(2023, 8, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                originalSivilstand = null,
            ),

        )

        fun manuellOgOffentligPerioderLikSivilstandskode() = listOf(
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "98765432109",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.of(2020, 4, 12),
                        bekreftelsesdato = null,
                        master = "PDL",
                        registrert = null,
                        historisk = true,
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
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 3, 31),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 4, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "98765432109",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.of(2020, 4, 12),
                        bekreftelsesdato = null,
                        master = "PDL",
                        registrert = null,
                        historisk = true,
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
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 3, 31),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 4, 1),
                        periodeTom = LocalDate.of(2022, 4, 30),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
        )

        fun flereManuellePerioder() = listOf(
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.UKJENT,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2021, 8, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 6, 30),
                        sivilstandskode = Sivilstandskode.UKJENT,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2021, 8, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2021, 9, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.UKJENT,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
        )

        fun endreSivilstandNullBehandledeUtfylltOffentligeOpplysningerUtfyllt1a1c2a() = listOf(
            // 1c
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.parse("2020-02-17"),
                        bekreftelsesdato = LocalDate.parse("2020-02-16"),
                        master = "PDL",
                        registrert = LocalDateTime.now(),
                        historisk = true,
                    ),
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.SKILT,
                        gyldigFom = LocalDate.parse("2022-03-12"),
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
                ),
                behandledeSivilstandsopplysninger = emptyList(),
                endreSivilstand = null,
            ),
            // 2a
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.parse("2020-02-17"),
                        bekreftelsesdato = LocalDate.parse("2020-02-16"),
                        master = "PDL",
                        registrert = LocalDateTime.now(),
                        historisk = true,
                    ),
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.SKILT,
                        gyldigFom = LocalDate.parse("2022-03-12"),
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
                ),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = LocalDate.of(2024, 2, 29),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            ),
            // 1a med oppdaterte offentlige opplysninger og endret virkningstidspunkt
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.parse("2020-02-17"),
                        bekreftelsesdato = LocalDate.parse("2020-02-16"),
                        master = "PDL",
                        registrert = LocalDateTime.now(),
                        historisk = true,
                    ),
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.SKILT,
                        gyldigFom = LocalDate.parse("2022-03-12"),
                        bekreftelsesdato = null,
                        master = "PDL",
                        registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                        historisk = true,
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
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.parse("2023-11-17"),
                        bekreftelsesdato = LocalDate.parse("2023-11-16"),
                        master = "PDL",
                        registrert = LocalDateTime.now(),
                        historisk = false,
                    ),
                ),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2023, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = LocalDate.of(2024, 2, 29),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreSivilstand = null,
            ),
        )

        fun endreSivilstandNullBehandledeUtfylltOffentligeOpplysningerTom1b() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = null,
            )

        fun endreSivilstandNullBehandledeTomOffentligeOpplysningerTom1d() = // 1d
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = emptyList(),
                endreSivilstand = null,
            )

        fun endreSivilstandUtfylltNYBehandledeUtfylltOffentligeOpplysningerTom2b() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = LocalDate.of(2024, 2, 29),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            )

        fun endreSivilstandUtfylltENDREBehandledeUtfylltOffentligeOpplysningerTom2b() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.ENDRET,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2020, 8, 1),
                        periodeTom = LocalDate.of(2021, 3, 31),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            )

        fun endreSivilstandUtfylltSLETTBehandledeUtfylltOffentligeOpplysningerTom2b() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.SLETTET,
                    nySivilstand = null,
                    originalSivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            )

        fun endreSivilstandUtfylltSLETTMidtperiodeBehandledeUtfylltOffentligeOpplysningerTom2b() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = listOf(
                    Sivilstand(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 2, 28),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    Sivilstand(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.SLETTET,
                    nySivilstand = null,
                    originalSivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            )

        fun endreSivilstandUtfylltBehandledeTomOffentligeOpplysningerUtfyllt2c() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = listOf(
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.GIFT,
                        gyldigFom = LocalDate.parse("2020-02-17"),
                        bekreftelsesdato = LocalDate.parse("2020-02-16"),
                        master = "PDL",
                        registrert = LocalDateTime.now(),
                        historisk = true,
                    ),
                    SivilstandGrunnlagDto(
                        personId = "12345678901",
                        type = SivilstandskodePDL.SKILT,
                        gyldigFom = LocalDate.parse("2022-03-12"),
                        bekreftelsesdato = null,
                        master = "PDL",
                        registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                        historisk = true,
                    ),
                ),
                behandledeSivilstandsopplysninger = emptyList(),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = LocalDate.of(2024, 4, 30),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            )

        fun endreSivilstandUtfylltBehandledeTomOffentligeOpplysningerTom2d() = // 1b
            SivilstandRequest(
                fødselsdatoBM = LocalDate.parse("1980-01-01"),
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeSivilstandsopplysninger = emptyList(),
                endreSivilstand = EndreSivilstand(
                    typeEndring = TypeEndring.NY,
                    nySivilstand = Sivilstand(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = LocalDate.of(2024, 4, 30),
                        sivilstandskode = Sivilstandskode.GIFT_SAMBOER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalSivilstand = null,
                ),
            )

        fun endreSivilstand() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger = listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = LocalDate.parse("1978-02-17"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.now(),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.parse("2022-11-01"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.parse("2024-02-01"),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2022-03-12T12:00:00"),
                    historisk = false,
                ),
            ),
            behandledeSivilstandsopplysninger = listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2023, 1, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreSivilstand = EndreSivilstand(
                typeEndring = TypeEndring.ENDRET,
                nySivilstand = Sivilstand(
                    periodeFom = LocalDate.of(2023, 4, 1),
                    periodeTom = LocalDate.of(2023, 12, 31),
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.MANUELL,
                ),
                originalSivilstand = Sivilstand(
                    periodeFom = LocalDate.of(2023, 1, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
        )

        fun endreVirkningstidspunktFremITid() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
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
            behandledeSivilstandsopplysninger =
            listOf(
                Sivilstand(
                    periodeFom = LocalDate.of(2020, 9, 1),
                    periodeTom = null,
                    sivilstandskode = Sivilstandskode.BOR_ALENE_MED_BARN,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreSivilstand = null,
        )

        fun gyldigFomLikFødselsdatoUgift() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = LocalDate.of(2022, 12, 9),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2021-01-13T14:44:16"),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2020-12-05T14:44:16"),
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.of(2023, 5, 12),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2023-06-13T14:44:16"),
                    historisk = false,
                ),
            ),
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )

        fun aktivPeriodeErFørVirkningstidspunkt() = SivilstandRequest(
            fødselsdatoBM = LocalDate.parse("1980-01-01"),
            innhentedeOffentligeOpplysninger =
            listOf(
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.GIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.UGIFT,
                    gyldigFom = null,
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = null,
                    historisk = true,
                ),
                SivilstandGrunnlagDto(
                    personId = "12345678901",
                    type = SivilstandskodePDL.SEPARERT,
                    gyldigFom = LocalDate.of(2024, 4, 17),
                    bekreftelsesdato = null,
                    master = "PDL",
                    registrert = LocalDateTime.parse("2023-06-13T14:44:16"),
                    historisk = false,
                ),
            ),
            behandledeSivilstandsopplysninger = emptyList(),
            endreSivilstand = null,
        )
    }
}
