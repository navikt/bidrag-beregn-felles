package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.boforhold.response.Bostatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdServiceTest {
    private lateinit var boforholdService: BoforholdService

    @Test
    fun `Test barn over 18 år hele perioden`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggBarnHusstandsmedlemAttenÅr()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatus.REGNES_IKKE_SOM_BARN
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i siste del av perioden`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskap()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[0].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatus.REGNES_IKKE_SOM_BARN
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i hele perioden`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskap()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatus.REGNES_IKKE_SOM_BARN
        }
    }

    @Test
    fun `Test at overlappende perioder med brudd slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBrudd()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatus.MED_FORELDER

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2023, 6, 30)
            resultat[2].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[3].periodeFom shouldBe LocalDate.of(2023, 7, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].bostatus shouldBe Bostatus.MED_FORELDER
        }
    }

    @Test
    fun `Test at overlappende perioder slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomster()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatus.MED_FORELDER

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatus.IKKE_MED_FORELDER
        }
    }

    @Test
    fun `Test at overlappende perioder slås sammen og at det genereres perioder for når barnet ikke bor i husstanden med 18 år`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggSammenhengendeForekomsterMed18År()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 5
            resultat[0].periodeFom shouldBe LocalDate.of(2018, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2019, 3, 31)
            resultat[0].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[1].periodeFom shouldBe LocalDate.of(2019, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2019, 7, 31)
            resultat[1].bostatus shouldBe Bostatus.MED_FORELDER

            resultat[2].periodeFom shouldBe LocalDate.of(2019, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat[2].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[3].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[3].bostatus shouldBe Bostatus.MED_FORELDER

            resultat[4].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[4].periodeTom shouldBe null
            resultat[4].bostatus shouldBe Bostatus.REGNES_IKKE_SOM_BARN
        }
    }

    @Test
    fun `Test at periodeFra før virkningstidspunkt blir justert til virkningstidspunkt`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggPeriodeFraFørVirkningstidspunkt()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatus.MED_FORELDER
        }
    }

    @Test
    fun `Test at periodeTom blir justert tilbake til 18årsdag`() {
        boforholdService = BoforholdService()
        val mottatteBoforhold = TestUtil.byggPeriodeTomEtterAttenårsdag()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdService.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[0].bostatus shouldBe Bostatus.IKKE_MED_FORELDER

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 3, 31)
            resultat[1].bostatus shouldBe Bostatus.MED_FORELDER

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 4, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatus.REGNES_IKKE_SOM_BARN
        }
    }
}
