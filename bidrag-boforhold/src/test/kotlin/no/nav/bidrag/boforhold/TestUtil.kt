package no.nav.bidrag.boforhold

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.dto.EndreBostatus
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 2),
                        periodeTom = null,
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggSammenhengendeForekomsterMedAttenÅr() = listOf(
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

        fun byggSammenhengendeForekomsterMedAttenÅrV2() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2017, 4, 2),
                        periodeTom = LocalDate.of(2019, 1, 7),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 2),
                        periodeTom = LocalDate.of(2019, 4, 7),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 10),
                        periodeTom = LocalDate.of(2019, 4, 17),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 5, 2),
                        periodeTom = LocalDate.of(2019, 7, 28),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 7, 2),
                        periodeTom = null,
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2014, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = null,
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = LocalDate.of(2022, 12, 27),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
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
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 8, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = LocalDate.of(2022, 7, 27),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 10, 12),
                        periodeTom = LocalDate.of(2022, 12, 27),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 2, 9),
                        periodeTom = null,
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun barnAttenÅrManuellPeriodeDokumentertSkolegang() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = LocalDate.of(2020, 2, 11),
                        bostatusKode = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegang() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 12),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 12),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangIngenOffentligInformasjon() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun manuellOgOffentligPeriodeErIdentisk() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 12, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 12, 1),
                        periodeTom = LocalDate.of(2022, 3, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun flereManuelleOgOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 12, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 4, 12),
                        periodeTom = LocalDate.of(2020, 12, 3),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 2, 17),
                        periodeTom = LocalDate.of(2021, 4, 17),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 8, 17),
                        periodeTom = LocalDate.of(2021, 11, 17),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 12),
                        periodeTom = LocalDate.of(2023, 5, 4),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 6, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = LocalDate.of(2022, 8, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 10, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 2, 1),
                        periodeTom = LocalDate.of(2023, 3, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun barnManuellePerioderMedOppholdFørAttenårsdag() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 2, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 1),
                        periodeTom = LocalDate.of(2021, 2, 28),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringNy() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringEndret() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun manuelleOgOffentligPeriodeMedNullIPeriodeTom() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 10, 7),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),

                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun manuellOgOffentligPeriodeMedNullIPeriodeTom2018() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2018, 5, 1),
                        periodeTom = LocalDate.of(2020, 2, 29),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(

                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun manuellOgOffentligPeriodeMedLikStatus() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 4, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
//            BoforholdBarnRequest(
//                relatertPersonPersonId = "98765432109",
//                fødselsdato = LocalDate.of(2020, 3, 1),
//                erBarnAvBmBp = true,
//                innhentedeOffentligeOpplysninger = listOf(
//                    Bostatus(
//                        periodeFom = LocalDate.of(2023, 5, 1),
//                        periodeTom = LocalDate.of(2023, 11, 30),
//                        bostatusKode = Bostatuskode.MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                    Bostatus(
//                        periodeFom = LocalDate.of(2024, 2, 1),
//                        periodeTom = LocalDate.of(2024, 3, 31),
//                        bostatusKode = Bostatuskode.MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                ),
//                behandledeBostatusopplysninger = listOf(
//                    Bostatus(
//                        periodeFom = LocalDate.of(2020, 5, 1),
//                        periodeTom = LocalDate.of(2023, 6, 30),
//                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
//                        kilde = Kilde.MANUELL,
//                    ),
//                    Bostatus(
//                        periodeFom = LocalDate.of(2023, 7, 1),
//                        periodeTom = LocalDate.of(2023, 11, 30),
//                        bostatusKode = Bostatuskode.MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                    Bostatus(
//                        periodeFom = LocalDate.of(2023, 12, 1),
//                        periodeTom = LocalDate.of(2024, 2, 29),
//                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                    Bostatus(
//                        periodeFom = LocalDate.of(2024, 2, 1),
//                        periodeTom = LocalDate.of(2024, 3, 31),
//                        bostatusKode = Bostatuskode.MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                    Bostatus(
//                        periodeFom = LocalDate.of(2024, 4, 1),
//                        periodeTom = null,
//                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
//                        kilde = Kilde.OFFENTLIG,
//                    ),
//                ),
// //                behandledeBostatusopplysninger = listOf(
// //                    Bostatus(
// //                        periodeFom = LocalDate.of(2022, 12, 1),
// //                        periodeTom = LocalDate.of(2023, 4, 30),
// //                        bostatusKode = Bostatuskode.MED_FORELDER,
// //                        kilde = Kilde.MANUELL,
// //                    ),
// //                    Bostatus(
// //                        periodeFom = LocalDate.of(2023, 12, 1),
// //                        periodeTom = LocalDate.of(2024, 1, 31),
// //                        bostatusKode = Bostatuskode.MED_FORELDER,
// //                        kilde = Kilde.MANUELL,
// //                    ),
// //                ),
//                endreBostatus = EndreBostatus(
//                    typeEndring = TypeEndring.NY,
//                    nyBostatus = Bostatus(
//                        periodeFom = LocalDate.of(2022, 12, 1),
//                        periodeTom = null,
//                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
//                        kilde = Kilde.MANUELL,
//                    ),
//                    originalBostatus = null,
//                ),
//            ),

        )

        fun manuellOgOffentligPeriodeMedLikStatusPeriodeTomErNull() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 10, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun sorteringAvPerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun flerePersonerIGrunnlagUtenOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2010, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun flerePersonerIGrunnlagMedOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2010, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 12, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun flereOffentligOgManuellPeriodeMedOppholdMellom() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2005, 9, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun flereManuellePerioderMedPeriodeTomNullLikStatus() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 17),
                        periodeTom = LocalDate.of(2022, 12, 3),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 17),
                        periodeTom = LocalDate.of(2022, 12, 3),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun flereFlereManuellePerioderMedPeriodeTomNullUlikStatus() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBostatuskode() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 1),
                        periodeTom = LocalDate.of(2019, 8, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 9, 1),
                        periodeTom = LocalDate.of(2019, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 11, 1),
                        periodeTom = LocalDate.of(2019, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggFlereOverlappendeManuellePerioderMedUlikBostatuskode() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2024, 2, 29),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggManuellPeriodeMedAttenÅr() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggManuellePerioderMedOpphold() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 9, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggManuellePerioderMedOppholdPlussOffentligPeriode() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),

                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 9, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggUtenPeriodeEtterAttenårsdagManuell() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggUtenPeriodeEtterAttenårsdagOffentlig() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggUtenPeriodeEtterAttenårsdagOffentligOgManuell() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 4, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        // Tester fra front-end
        fun byggManuellPeriodeOverlapperAlleOffentlige() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 4),
                        periodeTom = LocalDate.of(2022, 4, 20),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 12),
                        periodeTom = LocalDate.of(2022, 6, 17),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = LocalDate.of(2022, 4, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 5, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 1),
                        periodeTom = LocalDate.of(2022, 6, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 7, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun byggKunManuellIkkeMedForelder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggOffentligePerioderOverlapper() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 17),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggEndrePeriodeFomOffentligPeriode() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggNyPeriodeIngenOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeOgBostatuskodeIngenOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeFremITidIngenOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeFremITidMedOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeTilbakeITidIngenOffentligePerioder() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdato() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdatoFremITid() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 1),
                        periodeTom = LocalDate.of(2023, 3, 31),
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 4, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatusKode = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatusKode = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggTestNyeOffentligeOpplysningerEndrerKildeManuellPeriode() = listOf(
            BoforholdBarnRequest(
                relatertPersonPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erBarnAvBmBp = true,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatusKode = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )
    }
}
