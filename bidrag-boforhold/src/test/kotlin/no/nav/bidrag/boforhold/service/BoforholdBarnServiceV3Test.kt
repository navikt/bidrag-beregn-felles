package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.domene.enums.behandling.TypeBehandling
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdBarnServiceV3Test {
    private lateinit var boforholdBarnServiceV3: BoforholdBarnServiceV3

    // Tester med kun offentlige perioder
    @Test
    fun `Test barn over 18 år hele perioden`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggBarnHusstandsmedlemAttenÅrV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskapV3()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i hele perioden`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskapV3()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Test
    fun `Test at overlappende perioder med brudd slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBruddV3()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterV3()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggSammenhengendeForekomsterMedAttenÅrV3()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggPeriodeFraFørVirkningstidspunktV3()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggPeriodeTomEtterAttenårsdagV3()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggOppholdPerioderHusstandsmedlemskapOgAttenårV3()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeDokumentertSkolegangV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnAttenÅrManuellPeriodeEtterAttenårsdagDokumentertSkolegangIngenOffentligInformasjonV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Beregning 1

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 2, 28)
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test at manuell periode som har en identisk offentlig periode endres til kilde = Offentlig `() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeErIdentiskV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test manuelle perioder overlapper periodeTom på offentlig periode TypeEndring NY`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringNyV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnManuellePeriodeOverlapperPeriodeTomOffentligPeriodeMedAttenÅrTypeEndringEndretV3()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test sammenhengende offentlige og manuelle perioder med lik status slås sammen som Manuell `() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeMedLikStatusV3()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 10

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 4, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 5, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2024, 3, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2024, 4, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[5].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2023, 6, 30)
            resultat[5].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[5].kilde shouldBe Kilde.MANUELL

            resultat[6].periodeFom shouldBe LocalDate.of(2023, 7, 1)
            resultat[6].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[6].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[6].kilde shouldBe Kilde.OFFENTLIG

            resultat[7].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[7].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[7].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[7].kilde shouldBe Kilde.OFFENTLIG

            resultat[8].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[8].periodeTom shouldBe LocalDate.of(2024, 3, 31)
            resultat[8].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[8].kilde shouldBe Kilde.OFFENTLIG

            resultat[9].periodeFom shouldBe LocalDate.of(2024, 4, 1)
            resultat[9].periodeTom shouldBe null
            resultat[9].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[9].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test sammenhengende offentlige og manuelle perioder med lik status slås sammen som Manuell med null i periodeTom `() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeMedLikStatusPeriodeTomErNullV3()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 4, 30)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 5, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[3].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 4, 30)
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 5, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[4].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test flere personer i grunnlag uten offentlige perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.flerePersonerIGrunnlagUtenOffentligePerioderV3()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 0
        }
    }

    @Test
    fun `Test med flere perioder med periodeTom = null og lik status`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.flereManuellePerioderMedPeriodeTomNullLikStatusV3()
        val virkningstidspunkt = LocalDate.of(2022, 1, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test at overlappende perioder med lik Bostatuskode slås sammen`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBostatuskodeV3()
        val virkningstidspunkt = LocalDate.of(2019, 6, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test at det genereres periode med riktig status for 18 åring - offentlig periode`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggUtenPeriodeEtterAttenårsdagOffentligV3()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test med kun lukket manuell periode IKKE_MED_FORELDER`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggKunManuellIkkeMedForelderV3()
        val virkningstidspunkt = LocalDate.of(2022, 4, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

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

    @Test
    fun `Test offentlige perioder overlapper`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggOffentligePerioderOverlapperV3()
        val virkningstidspunkt = LocalDate.of(2022, 4, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            // Generert periode for tidsrom mellom virkningstidspunkt og periodeFom for første periode i input.
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 7, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 8, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test endre periodeFom offentlig periode`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeFomOffentligPeriodeV3()
        val virkningstidspunkt = LocalDate.of(2023, 1, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test ny periode, ny periode og slett periode, uten offentlige perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggNyPeriodeIngenOffentligePerioderV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

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

            // Beregning 3. Her slettes IKKE_MED_FORELDER-perioden og erstattes med ny periode med MED_FORELDER.
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test ny periode, endre periode og bostatuskode og slett periode, uten offentlige perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeOgBostatuskodeIngenOffentligePerioderV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            // Beregning 3. Her slettes MED_FORELDER-perioden og erstattes med ny periode med IKKE_MED_FORELDER.
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test ny periode, endre periode frem i tid og slett periode, uten offentlige perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeFremITidIngenOffentligePerioderV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Beregning 2

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            // Beregning 3. Her slettes perioden
            // Ingen respons siden det ikke lenger finnes perioder
        }
    }

    @Test
    fun `Test ny periode, endre periode tilbake i tid og slett periode, uten offentlige perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeTilbakeITidIngenOffentligePerioderV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            // Beregning 3.
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[4].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test ny periode, endre periode frem i tid og slett offentlig periode, så slett manuell periode`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndrePeriodeFremITidMedOffentligePerioderSlettOffentligOgManuellV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 8

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat[1].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 6, 30)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 7, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            // Beregning 3.
            resultat[3].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[3].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].kilde shouldBe Kilde.MANUELL

            // Beregning 4 med forsøk på å slette offentlig periode, dette er ikke tillatt og periode blir værende.
            resultat[5].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[5].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[5].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[5].kilde shouldBe Kilde.OFFENTLIG

            resultat[6].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[6].periodeTom shouldBe null
            resultat[6].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[6].kilde shouldBe Kilde.MANUELL

            // Beregning 5. Sletter manuell periode.
            resultat[7].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[7].periodeTom shouldBe null
            resultat[7].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[7].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test juster behandlede bostatusopplysninger etter endret virkningstidspunkt`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdatoV3()
        val virkningstidspunkt = LocalDate.of(2022, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            // Test som verfiserer at det genereres en ny periode basert på offentlige opplysninger ved endring av virkningstidspunkt tilbake i tid.
            // Behandlede bostatusopplysninger er opprinnelig beregnet fra virkningstidspunkt 2023-06-01, etter barnets attenårsdag.
            // Denne kjøringen er ved virkningstidspunkt 2022-03-01.

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test juster behandlede bostatusopplysninger etter endret virkningstidspunkt frem i tid`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggTestJusterBehandledeBostatusopplysningerEtterEndretVirkningsdatoFremITidV3()
        val virkningstidspunkt = LocalDate.of(2023, 6, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Test som verfiserer at det genereres en ny periode basert på offentlige opplysninger ved endring av virkningstidspunkt tilbake i tid.
            // Behandlede bostatusopplysninger er opprinnelig beregnet fra virkningstidspunkt 2023-06-01, etter barnets attenårsdag.
            // Denne kjøringen er ved virkningstidspunkt 2022-03-01.

            resultat[0].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 7, 31)
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 8, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.DOKUMENTERT_SKOLEGANG
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test nye offentlige opplysninger endrer kilde på manuelle perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggTestNyeOffentligeOpplysningerEndrerKildeManuellPeriodeV3()
        val virkningstidspunkt = LocalDate.of(2023, 6, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            // Test som verifiserer at kilde på manuell periode endres til Offentlig hvis det kommer nye offentlige opplysninger som dekker
            // manuell periode.

            resultat[0].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Særbidrag - Test at det genereres periode med riktig status for 18 åring - offentlig periode`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggUtenPeriodeEtterAttenårsdagOffentligV3Særbidrag()
        val virkningstidspunkt = LocalDate.of(2021, 5, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.SÆRBIDRAG, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5
            //
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 1, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[3].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[4].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test setter tomdato på ny periode der offentlig periode skal fortsette etter satt tomdato`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggSettTomdatoPåNyPeriodeV3()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 7, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 8, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test endring tomdato frem i tid kun manuelle perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndreFørstePeriodeFremITid()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, listOf(mottatteBoforhold))

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 9, 30)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 10, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 10, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 11, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2024, 11, 30)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2024, 12, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test endring tomdato frem i tid andre periode kun manuelle perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndreAndrePeriodeFremITid()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, listOf(mottatteBoforhold))

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 8, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 9, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2025, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2025, 2, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2025, 3, 31)
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2025, 4, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test endring tomdato frem i tid over flere perioder kun manuelle perioder`() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.byggEndreOverFlerePerioderFremITid()
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, listOf(mottatteBoforhold))

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2025, 5, 31)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2025, 6, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test avslutter manuell periode offentlig periode legges til `() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnAvslutterPeriodeUtfyllesMedOffentligPeriode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, listOf(mottatteBoforhold))

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 10, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2021, 11, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 5, 31)
            resultat[1].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 6, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test utvid periodeforbi neste periode `() {
        boforholdBarnServiceV3 = BoforholdBarnServiceV3()
        val mottatteBoforhold = TestUtil.barnUtvidPeriodeForbiNestePeriode()
        val virkningstidspunkt = LocalDate.of(2024, 1, 1)
        val resultat = boforholdBarnServiceV3.beregnBoforholdBarn(virkningstidspunkt, TypeBehandling.FORSKUDD, listOf(mottatteBoforhold))

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Beregning 1
            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 4, 30)
            resultat[0].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 5, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 6, 30)
            resultat[1].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 7, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.MED_FORELDER
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
