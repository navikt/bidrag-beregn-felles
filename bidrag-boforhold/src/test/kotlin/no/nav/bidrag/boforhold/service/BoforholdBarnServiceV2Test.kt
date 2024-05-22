package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdBarnServiceV2Test {
    private lateinit var boforholdBarnServiceV2: BoforholdBarnServiceV2

    // Tester med kun offentlige perioder
    @Test
    fun `Test barn over 18 år hele perioden`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnHusstandsmedlemAttenÅrV2()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i siste del av perioden`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskapV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i hele perioden`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskapV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Test
    fun `Test at overlappende perioder med brudd slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBruddV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 6, 30)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 7, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[3].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at overlappende perioder slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at overlappende perioder slås sammen og at det genereres perioder for når barnet ikke bor i husstanden med 18 år`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggSammenhengendeForekomsterMedAttenÅrV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[4].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[4].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at periodeFra før virkningstidspunkt blir justert til virkningstidspunkt`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggPeriodeFraFørVirkningstidspunktV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at periodeTom blir justert tilbake til 18årsdag`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggPeriodeTomEtterAttenårsdagV2()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test opphold perioder husstandsmedlemskap og 18 år`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggOppholdPerioderHusstandsmedlemskapOgAttenårV2()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            resultat.size shouldBe 7
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2022, 9, 30)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2022, 10, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 1, 31)
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[4].kilde shouldBe Kilde.OFFENTLIG

            resultat[5].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[5].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[5].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            resultat[6].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[6].periodeTom shouldBe null
            resultat[6].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[6].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[6].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    // Tester med manuell periode der og offentlig periode er før virkningstidspunkt
    @Test
    fun `Test barn over 18 år hele perioden, manuell periode Dokumentert skolegang fra virkningstidspunkt `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeDokumentertSkolegang()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test barn over 18 år hele perioden, manuell periode Dokumentert skolegang deler av perioden `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegang()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test barn over 18 år hele perioden, manuell periode Dokumentert skolegang deler av perioden - ingen offentlig informasjon `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangIngenOffentligInformasjon()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.MANUELL

            // Beregning 2
            resultat[2].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[2].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[3].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[3].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[3].kilde shouldBe Kilde.MANUELL

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[4].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[4].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test at manuell periode som har en identisk offentlig periode endres til kilde = Offentlig `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeErIdentisk()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            // fødselsdato er etter virkningstidspunkt og periodeFom settes lik første dag i fødselsmåned
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 12, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test periodisering med flere manuelle og offentlige perioder og perioder uten status `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flereManuelleOgOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 14
            // Offentlig periode der periodeFom forskyves frem til første dag i fødselsmåned
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 12, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2020, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som er generert pga manglende info om husstandsmedlemskap
            resultat[1].periodeFom shouldBe LocalDate.of(2021, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2021, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2021, 2, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 4, 30)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som er generert pga manglende info om husstandsmedlemskap
            resultat[3].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2021, 5, 31)
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            // Manuell periode som fullstendig overlapper offentlig periode fra 2021.08.17
            resultat[4].periodeFom shouldBe LocalDate.of(2021, 6, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[4].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[4].kilde shouldBe Kilde.MANUELL

            // Offentlig periode som er generert pga manglende info om husstandsmedlemskap
            resultat[5].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[5].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode fra 2022.02.12 til 2023.05.04 blir splittet og første periode har avkortet periodeTom pga manuell periode som
            // overlapper. Del to av den offentlige perioden ligger i element 8.
            resultat[6].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[6].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[6].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[6].kilde shouldBe Kilde.OFFENTLIG

            resultat[7].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[7].periodeTom shouldBe LocalDate.of(2022, 8, 31)
            resultat[7].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[7].kilde shouldBe Kilde.MANUELL

            // Del to av offentlig periode fra 2022.02.12 til 2023.05.04. Ny manuell periode gir ny splitt i element 10.
            resultat[8].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[8].periodeTom shouldBe LocalDate.of(2022, 9, 30)
            resultat[8].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[8].kilde shouldBe Kilde.OFFENTLIG

            resultat[9].periodeFom shouldBe LocalDate.of(2022, 10, 1)
            resultat[9].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[9].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[9].kilde shouldBe Kilde.MANUELL

            resultat[10].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[10].periodeTom shouldBe LocalDate.of(2023, 1, 31)
            resultat[10].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[10].kilde shouldBe Kilde.OFFENTLIG

            resultat[11].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[11].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[11].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[11].kilde shouldBe Kilde.MANUELL

            resultat[12].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[12].periodeTom shouldBe LocalDate.of(2023, 5, 31)
            resultat[12].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[12].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som er generert pga manglende info om husstandsmedlemskap
            resultat[13].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[13].periodeTom shouldBe null
            resultat[13].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[13].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test manuelle perioder under 18 år`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnManuellePerioderMedOppholdFørAttenårsdag()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 2, 28)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2021, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test manuelle perioder overlapper periodeTom på offentlig periode TypeEndring NY`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringNy()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 12

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[2].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 10, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2021, 11, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL

            resultat[4].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].kilde shouldBe Kilde.OFFENTLIG

            resultat[5].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[5].periodeTom shouldBe null
            resultat[5].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 3

            resultat[6].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[6].periodeTom shouldBe LocalDate.of(2021, 10, 31)
            resultat[6].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[6].kilde shouldBe Kilde.OFFENTLIG

            resultat[7].periodeFom shouldBe LocalDate.of(2021, 11, 1)
            resultat[7].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[7].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[7].kilde shouldBe Kilde.MANUELL

            resultat[8].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[8].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[8].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[8].kilde shouldBe Kilde.OFFENTLIG

            resultat[9].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[9].periodeTom shouldBe LocalDate.of(2023, 1, 31)
            resultat[9].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[9].kilde shouldBe Kilde.MANUELL

            resultat[10].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[10].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[10].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[10].kilde shouldBe Kilde.OFFENTLIG

            resultat[11].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[11].periodeTom shouldBe null
            resultat[11].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[11].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    // Samme test som over, men med TypeEndring = Endret i stedet for Ny. Skal gi samme resultat
    @Test
    fun `Test manuelle perioder overlapper periodeTom på offentlig periode TypeEndring ENDRET`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringEndret()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 12

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[2].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 10, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2021, 11, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL

            resultat[4].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].kilde shouldBe Kilde.OFFENTLIG

            resultat[5].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[5].periodeTom shouldBe null
            resultat[5].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 3

            resultat[6].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[6].periodeTom shouldBe LocalDate.of(2021, 10, 31)
            resultat[6].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[6].kilde shouldBe Kilde.OFFENTLIG

            resultat[7].periodeFom shouldBe LocalDate.of(2021, 11, 1)
            resultat[7].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[7].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[7].kilde shouldBe Kilde.MANUELL

            resultat[8].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[8].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[8].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[8].kilde shouldBe Kilde.OFFENTLIG

            resultat[9].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[9].periodeTom shouldBe LocalDate.of(2023, 1, 31)
            resultat[9].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[9].kilde shouldBe Kilde.MANUELL

            resultat[10].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[10].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[10].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[10].kilde shouldBe Kilde.OFFENTLIG

            resultat[11].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[11].periodeTom shouldBe null
            resultat[11].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[11].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test periodeTom = null både manuell og offentlig periode `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.manuelleOgOffentligPeriodeMedNullIPeriodeTom()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 5, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 7, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 8, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test periodeTom = null både manuell og offentlig periode - virkningsdato 2018 `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeMedNullIPeriodeTom2018()
        val virkningstidspunkt = LocalDate.of(2018, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 3, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 5, 31)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test sammenhengende offentlige og manuelle perioder med lik status slås sammen som Manuell `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeMedLikStatus()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 11, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 12, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 3, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 4, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test sammenhengende offentlige og manuelle perioder med lik status slås sammen som Manuell med null i periodeTom `() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeMedLikStatusPeriodeTomErNull()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 11, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 12, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test sortering av perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.sorteringAvPerioder()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 5, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 7, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 8, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test flere personer i grunnlag uten offentlige perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flerePersonerIGrunnlagUtenOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Disabled
    @Test
    fun `Test flere personer i grunnlag med offentlige perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flerePersonerIGrunnlagMedOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2023, 2, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].relatertPersonPersonId shouldBe "12345678901"
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].relatertPersonPersonId shouldBe "98765432109"
            resultat[1].periodeFom shouldBe LocalDate.of(2023, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].relatertPersonPersonId shouldBe "98765432109"
            resultat[2].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test med offentlig og manuell periode med opphold mellom`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flereOffentligOgManuellPeriodeMedOppholdMellom()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 9, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 10, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test med flere perioder med periodeTom = null og lik status`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flereManuellePerioderMedPeriodeTomNullLikStatus()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 9

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[2].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[4].kilde shouldBe Kilde.MANUELL

            // Beregning 3
            resultat[5].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[5].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            resultat[6].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[6].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[6].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[6].kilde shouldBe Kilde.OFFENTLIG

            resultat[7].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[7].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[7].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[7].kilde shouldBe Kilde.MANUELL

            resultat[8].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[8].periodeTom shouldBe null
            resultat[8].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[8].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test med flere perioder med periodeTom = null og ulik status`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.flereFlereManuellePerioderMedPeriodeTomNullUlikStatus()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test at overlappende perioder med lik Bostatuskode slås sammen`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBostatuskode()
        val virkningstidspunkt = LocalDate.of(2019, 6, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2019, 6, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 12, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2020, 1, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test at overlappende perioder med ulik Bostatuskode justeres`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereOverlappendeManuellePerioderMedUlikBostatuskode()
        val virkningstidspunkt = LocalDate.of(2023, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test at det genereres periode for 18 år med manuell periode med periodeTom = null`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggManuellPeriodeMedAttenÅr()
        val virkningstidspunkt = LocalDate.of(2023, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test at det genereres IKKE_MED_FORELDER-perioder der det er opphold, kun manuelle perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggManuellePerioderMedOpphold()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 7
            // Periode 1 i input, avkortes til periodeFom neste periode minus 1 dag
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Periode 2 i input
            resultat[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            // Generert periode pga opphold i inputperioder
            resultat[2].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            // Periode 3 i input. Avkortes pga barnet fyller 18 år 01.04.2023
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL

            // Basert på periode 3 i input, barnet har fyllt 18 år.
            resultat[4].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 9, 30)
            resultat[4].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[4].kilde shouldBe Kilde.MANUELL

            // Generert periode pga opphold i inputperioder
            resultat[5].periodeFom shouldBe LocalDate.of(2023, 10, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat[5].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[5].kilde shouldBe Kilde.MANUELL

            // Periode 4 i input, bostatuskode endret pga 18 år.
            resultat[6].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat[6].periodeTom shouldBe null
            resultat[6].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[6].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test at det genereres IKKE_MED_FORELDER-perioder der det er opphold, manuelle og offentlige perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggManuellePerioderMedOppholdPlussOffentligPeriode()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 7
            // Periode 1 i input, avkortes til periodeFom neste periode minus 1 dag
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Periode 2 i input
            resultat[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            // Periode 3 i input, offentlig.
            resultat[2].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            // Periode 4 i input. Avkortes pga barnet fyller 18 år 01.04.2023
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL

            // Basert på periode 4 i input, barnet har fyllt 18 år.
            resultat[4].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 9, 30)
            resultat[4].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[4].kilde shouldBe Kilde.MANUELL

            // Generert periode pga opphold i inputperioder
            resultat[5].periodeFom shouldBe LocalDate.of(2023, 10, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat[5].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            // Periode 4 i input, bostatuskode endret pga 18 år.
            resultat[6].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat[6].periodeTom shouldBe null
            resultat[6].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[6].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test at det genereres periode med riktig status for 18 åring - manuell periode`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggUtenPeriodeEtterAttenårsdagManuell()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            //
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test at det genereres periode med riktig status for 18 åring - offentlig periode`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggUtenPeriodeEtterAttenårsdagOffentlig()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            //
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Disabled
    @Test
    fun `Test at det genereres periode med riktig status for 18 åring - offentlig og manuell periode`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggUtenPeriodeEtterAttenårsdagOffentligOgManuell()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 6
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom for første periode i input.
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Periode 1 i input
            resultat[1].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            // Generert periode for tidsrom mellom periode 1 og attenårsdag.
            resultat[2].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            // Barnet har fyllt 18 år. Offentlig periode med MED_FORELDER får bostatuskode REGNES_IKKE_SOM_BARN.
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[3].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2024, 4, 30)
            resultat[4].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[4].kilde shouldBe Kilde.MANUELL

            resultat[5].periodeFom shouldBe LocalDate.of(2024, 5, 1)
            resultat[5].periodeTom shouldBe null
            resultat[5].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[5].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    // Tester fra front-end
    @Disabled
    @Test
    fun `Test med manuell periode som overlapper alle offentlige perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggManuellPeriodeOverlapperAlleOffentlige()
        val virkningstidspunkt = LocalDate.of(2022, 4, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom for første periode i input.
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test med kun lukket manuell periode IKKE_MED_FORELDER`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggKunManuellIkkeMedForelder()
        val virkningstidspunkt = LocalDate.of(2022, 4, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom for første periode i input.
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 7, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test offentlige perioder overlapper`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggOffentligePerioderOverlapper()
        val virkningstidspunkt = LocalDate.of(2022, 4, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom for første periode i input.
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 7, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL
        }
    }

    @Disabled
    @Test
    fun `Test endre periodeFom offentlig periode`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeFomOffentligPeriode()
        val virkningstidspunkt = LocalDate.of(2023, 1, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom i endret periode.
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test legge til periode uten offentlige perioder`() {
        boforholdBarnServiceV2 = BoforholdBarnServiceV2()
        val mottatteBoforhold = TestUtil.byggNyPeriodeIngenOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV2.beregnBoforholdBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }
}
