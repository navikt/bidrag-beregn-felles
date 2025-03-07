package no.nav.bidrag.boforhold

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.boforhold.dto.BoforholdBarnRequest
import no.nav.bidrag.boforhold.dto.BoforholdBarnRequestV3
import no.nav.bidrag.boforhold.dto.BoforholdVoksneRequest
import no.nav.bidrag.boforhold.dto.Bostatus
import no.nav.bidrag.boforhold.dto.EndreBostatus
import no.nav.bidrag.boforhold.dto.Husstandsmedlemmer
import no.nav.bidrag.boforhold.response.RelatertPerson
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Familierelasjon
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggBarnHusstandsmedlemAttenÅrV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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

        fun byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskapV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
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

        fun byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskapV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
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
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBruddV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggFlereSammenhengendeForekomsterV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggSammenhengendeForekomsterMedAttenÅrV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggPeriodeFraFørVirkningstidspunktV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2014, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = null,
                        bostatus = null,
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
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggPeriodeTomEtterAttenårsdagV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2004, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 12),
                        periodeTom = LocalDate.of(2022, 12, 27),
                        bostatus = null,
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
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggOppholdPerioderHusstandsmedlemskapOgAttenårV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 8, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeDokumentertSkolegangV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2019, 4, 12),
                        periodeTom = LocalDate.of(2020, 2, 11),
                        bostatus = null,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangIngenOffentligInformasjonV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2000, 2, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 12, 1),
                        periodeTom = LocalDate.of(2022, 3, 31),
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
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun manuellOgOffentligPeriodeErIdentiskV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 12, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 12, 1),
                        periodeTom = LocalDate.of(2022, 3, 31),
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
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 6, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringNyV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringEndretV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 10, 7),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2005, 4, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2021, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 11, 1),
                        periodeTom = LocalDate.of(2021, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 2, 1),
                        periodeTom = LocalDate.of(2023, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),

                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(

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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
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
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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

        fun manuellOgOffentligPeriodeMedLikStatusV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 11, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
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
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 10, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun manuellOgOffentligPeriodeMedLikStatusPeriodeTomErNullV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 5, 1),
                        periodeTom = LocalDate.of(2023, 4, 30),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 5, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 10, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 5, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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

        fun flerePersonerIGrunnlagUtenOffentligePerioderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "12345678901",
                fødselsdato = LocalDate.of(2010, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2020, 3, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
        )

        fun flereManuellePerioderMedPeriodeTomNullLikStatusV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 17),
                        periodeTom = LocalDate.of(2022, 12, 3),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 17),
                        periodeTom = LocalDate.of(2022, 12, 3),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 12),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 1, 1),
                        periodeTom = LocalDate.of(2022, 12, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "12345678901",
                fødselsdato = LocalDate.of(2017, 9, 1),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                ),
                behandledeBostatusopplysninger = listOf(
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 12, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
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
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggFlereSammenhengendeForekomsterMedBostatuskodeV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2017, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggUtenPeriodeEtterAttenårsdagOffentligV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggUtenPeriodeEtterAttenårsdagOffentligV3Særbidrag() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 1, 12),
                        periodeTom = LocalDate.of(2022, 1, 3),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 17),
                        periodeTom = LocalDate.of(2023, 12, 12),
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2021, 7, 1),
                        periodeTom = LocalDate.of(2022, 1, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = LocalDate.of(2024, 4, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 12),
                        periodeTom = LocalDate.of(2022, 6, 17),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2022, 3, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggKunManuellIkkeMedForelderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 5, 1),
                        periodeTom = LocalDate.of(2022, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )

        fun byggOffentligePerioderOverlapperV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 8, 17),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeFomOffentligPeriodeV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggNyPeriodeIngenOffentligePerioderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeOgBostatuskodeIngenOffentligePerioderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeFremITidIngenOffentligePerioderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeFremITidMedOffentligePerioderSlettOffentligOgManuellV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 6, 30),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 7, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
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
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 11, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
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
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggEndrePeriodeTilbakeITidIngenOffentligePerioderV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 1, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2023, 10, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdatoV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 1),
                        periodeTom = LocalDate.of(2023, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 4, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdatoFremITidV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2005, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 6, 1),
                        periodeTom = LocalDate.of(2023, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 4, 1),
                        periodeTom = LocalDate.of(2023, 7, 31),
                        bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 8, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.DOKUMENTERT_SKOLEGANG,
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
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun byggTestNyeOffentligeOpplysningerEndrerKildeManuellPeriodeV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 6, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        // Tester for voksne i husstanden
        fun byggBorMedAndreVoksneOffentligePerioder() = BoforholdVoksneRequest(
            innhentedeOffentligeOpplysninger = listOf(
                Husstandsmedlemmer(
                    gjelderPersonId = "98765432109",
                    fødselsdato = LocalDate.of(1980, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2020, 1, 11),
                            periodeTom = LocalDate.of(2023, 8, 21),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 9, 21),
                            periodeTom = LocalDate.of(2023, 10, 5),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 11, 21),
                            periodeTom = LocalDate.of(2023, 11, 27),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 12, 2),
                            periodeTom = null,
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
                Husstandsmedlemmer(
                    gjelderPersonId = "123456789",
                    fødselsdato = LocalDate.of(1977, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2022, 2, 17),
                            periodeTom = LocalDate.of(2023, 9, 12),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2024, 2, 17),
                            periodeTom = null,
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,

        )

        fun byggBorMedAndreVoksneOffentligNyManuellePeriode() = listOf(
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = listOf(
                    Husstandsmedlemmer(
                        gjelderPersonId = "98765432109",
                        fødselsdato = LocalDate.of(1980, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2021, 1, 11),
                                periodeTom = LocalDate.of(2023, 8, 21),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 9, 21),
                                periodeTom = LocalDate.of(2023, 10, 5),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 12, 2),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1977, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2022, 2, 17),
                                periodeTom = LocalDate.of(2023, 9, 12),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2024, 2, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1975, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2012, 1, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = listOf(
                    Husstandsmedlemmer(
                        gjelderPersonId = "98765432109",
                        fødselsdato = LocalDate.of(1980, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2021, 1, 11),
                                periodeTom = LocalDate.of(2023, 8, 21),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 9, 21),
                                periodeTom = LocalDate.of(2023, 10, 5),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 12, 2),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1977, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2022, 2, 17),
                                periodeTom = LocalDate.of(2023, 9, 12),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2024, 2, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1975, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2012, 1, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),

            ),
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = listOf(
                    Husstandsmedlemmer(
                        gjelderPersonId = "98765432109",
                        fødselsdato = LocalDate.of(1980, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2021, 1, 11),
                                periodeTom = LocalDate.of(2023, 8, 21),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 9, 21),
                                periodeTom = LocalDate.of(2023, 10, 5),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2023, 12, 2),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1977, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2022, 2, 17),
                                periodeTom = LocalDate.of(2023, 9, 12),
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                            Bostatus(
                                periodeFom = LocalDate.of(2024, 2, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                    Husstandsmedlemmer(
                        gjelderPersonId = "123456789",
                        fødselsdato = LocalDate.of(1975, 3, 17),
                        relasjon = Familierelasjon.INGEN,
                        borISammeHusstandListe = listOf(
                            Bostatus(
                                periodeFom = LocalDate.of(2012, 1, 17),
                                periodeTom = null,
                                bostatus = null,
                                kilde = Kilde.OFFENTLIG,
                            ),
                        ),
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),

            ),
        )

        fun byggBorMedAndreVoksneOffentligeNyManuellePeriodeEndre() = listOf(
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.NY,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = null,
                ),

            ),
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2023, 8, 31),
                        bostatus = Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2023, 9, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),

            ),
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 9, 1),
                        periodeTom = LocalDate.of(2024, 1, 31),
                        bostatus = Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.SLETTET,
                    nyBostatus = null,
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 2, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.MANUELL,
                    ),
                ),

            ),
        )

        fun byggTestÉnmånedsgrenseHusstandsmedlemskap() = BoforholdVoksneRequest(
            innhentedeOffentligeOpplysninger = listOf(
                Husstandsmedlemmer(
                    gjelderPersonId = "98765432109",
                    fødselsdato = LocalDate.of(1980, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 1, 11),
                            periodeTom = LocalDate.of(2023, 1, 22),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 3, 21),
                            periodeTom = LocalDate.of(2023, 4, 12),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 5, 21),
                            periodeTom = LocalDate.of(2023, 7, 27),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,

        )

        fun byggIngenOffentligeOpplysningerNyManuellPeriode() = BoforholdVoksneRequest(
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.NY,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = null,
            ),
        )

        fun byggSettTomdatoPåNyPeriodeV3() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "98765432109",
                fødselsdato = LocalDate.of(2015, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2020, 1, 23),
                        periodeTom = null,
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2023, 3, 1),
                        periodeTom = LocalDate.of(2024, 3, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
                endreBostatus = EndreBostatus(
                    typeEndring = TypeEndring.ENDRET,
                    nyBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 4, 1),
                        periodeTom = LocalDate.of(2024, 7, 31),
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                    originalBostatus = Bostatus(
                        periodeFom = LocalDate.of(2024, 4, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.IKKE_MED_FORELDER,
                        kilde = Kilde.MANUELL,
                    ),
                ),
            ),
        )

        fun byggIngenPerioder() = BoforholdVoksneRequest(
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,
        )

        fun byggTestFlereHusstandsmedlemmerPeriodeTomNull() = BoforholdVoksneRequest(
            innhentedeOffentligeOpplysninger = listOf(
                Husstandsmedlemmer(
                    gjelderPersonId = "1",
                    fødselsdato = LocalDate.of(1980, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2022, 3, 5),
                            periodeTom = LocalDate.of(2024, 3, 18),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
                Husstandsmedlemmer(
                    gjelderPersonId = "2",
                    fødselsdato = LocalDate.of(1981, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2023, 8, 1),
                            periodeTom = LocalDate.of(2024, 4, 21),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
                Husstandsmedlemmer(
                    gjelderPersonId = "3",
                    fødselsdato = LocalDate.of(1982, 3, 17),
                    relasjon = Familierelasjon.INGEN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2022, 3, 5),
                            periodeTom = LocalDate.of(2024, 3, 18),
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
                Husstandsmedlemmer(
                    gjelderPersonId = "4",
                    fødselsdato = LocalDate.of(1983, 3, 17),
                    relasjon = Familierelasjon.MOTPART_TIL_FELLES_BARN,
                    borISammeHusstandListe = listOf(
                        Bostatus(
                            periodeFom = LocalDate.of(2022, 5, 10),
                            periodeTom = null,
                            bostatus = null,
                            kilde = Kilde.OFFENTLIG,
                        ),
                    ),
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,

        )

        fun byggEndreFørstePeriodeFremITid() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2020, 3, 1),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2024, 10, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 11, 1),
                    periodeTom = LocalDate.of(2024, 11, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 12, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 9, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun byggEndreAndrePeriodeFremITid() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2020, 3, 1),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2024, 12, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2025, 1, 1),
                    periodeTom = LocalDate.of(2025, 3, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2025, 4, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2025, 1, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2024, 12, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun byggEndreOverFlerePerioderFremITid() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2020, 3, 1),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = emptyList(),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2024, 12, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2025, 1, 1),
                    periodeTom = LocalDate.of(2025, 3, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2025, 4, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2025, 5, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun barnAvslutterPeriodeUtfyllesMedOffentligPeriode() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2015, 10, 7),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2015, 10, 7),
                    periodeTom = null,
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2020, 9, 1),
                    periodeTom = LocalDate.of(2021, 10, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2021, 11, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2021, 11, 1),
                    periodeTom = LocalDate.of(2024, 5, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2021, 11, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
            ),
        )

        fun barnUtvidPeriodeForbiNestePeriode() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2015, 10, 7),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 4, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 5, 1),
                    periodeTom = LocalDate.of(2024, 6, 30),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 7, 1),
                    periodeTom = LocalDate.of(2024, 9, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 10, 1),
                    periodeTom = LocalDate.of(2024, 10, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 11, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 7, 1),
                    periodeTom = LocalDate.of(2024, 10, 31),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 7, 1),
                    periodeTom = LocalDate.of(2024, 9, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
        )

        fun justerVirkningstidspunktTilbakeITid() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2005, 10, 31),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 5, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.REGNES_IKKE_SOM_BARN,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 7, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreBostatus = null,
        )

        fun byggEndreVirkningstidspunktVoksne() = listOf(
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdVoksneRequest(
                innhentedeOffentligeOpplysninger = emptyList(),
                behandledeBostatusopplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2024, 3, 1),
                        periodeTom = null,
                        bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                endreBostatus = null,
            ),
        )

        fun justerTildatoSistePeriodeTilbakeITid() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2014, 5, 13),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.of(2024, 6, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 7, 1),
                    periodeTom = LocalDate.of(2024, 8, 31),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            endreBostatus = EndreBostatus(
                typeEndring = TypeEndring.ENDRET,
                nyBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = LocalDate.of(2024, 11, 30),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.MANUELL,
                ),
                originalBostatus = Bostatus(
                    periodeFom = LocalDate.of(2024, 9, 1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
        )

        fun testBarnFlytterUtIBeregningsmåned() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2014, 5, 13),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2024, 1, 1),
                    periodeTom = LocalDate.now(),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,
        )

        fun filtererBortFremtidigePerioder() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2020, 3, 1),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.now().minusMonths(2),
                    periodeTom = LocalDate.now().plusYears(1),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.now().plusYears(2),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,
        )

        fun toSammenhengendePerioderIHusstand() = BoforholdBarnRequestV3(
            gjelderPersonId = "98765432109",
            fødselsdato = LocalDate.of(2020, 3, 1),
            erSøknadsbarn = true,
            relasjon = Familierelasjon.BARN,
            innhentedeOffentligeOpplysninger = listOf(
                Bostatus(
                    periodeFom = LocalDate.of(2023, 3, 1),
                    periodeTom = LocalDate.now().minusDays(2),
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
                Bostatus(
                    periodeFom = LocalDate.now().minusDays(1),
                    periodeTom = null,
                    bostatus = Bostatuskode.MED_FORELDER,
                    kilde = Kilde.OFFENTLIG,
                ),
            ),
            behandledeBostatusopplysninger = emptyList(),
            endreBostatus = null,
        )

        fun beregningPerioder18årsbidragSøknadsbarnOgAnnetBarn() = listOf(
            BoforholdBarnRequestV3(
                gjelderPersonId = "1",
                // 18 år fra 01.04.2024
                fødselsdato = LocalDate.of(2006, 3, 17),
                erSøknadsbarn = true,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2024, 7, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
            BoforholdBarnRequestV3(
                gjelderPersonId = "2",
                // Også 18 år fra 01.04.2024
                fødselsdato = LocalDate.of(2006, 3, 17),
                erSøknadsbarn = false,
                relasjon = Familierelasjon.BARN,
                innhentedeOffentligeOpplysninger = listOf(
                    Bostatus(
                        periodeFom = LocalDate.of(2022, 12, 1),
                        periodeTom = LocalDate.of(2024, 7, 31),
                        bostatus = Bostatuskode.MED_FORELDER,
                        kilde = Kilde.OFFENTLIG,
                    ),
                ),
                behandledeBostatusopplysninger = emptyList(),
                endreBostatus = null,
            ),
        )
    }
}
