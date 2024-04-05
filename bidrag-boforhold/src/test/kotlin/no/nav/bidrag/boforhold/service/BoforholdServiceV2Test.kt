package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.boforhold.dto.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdServiceV2Test {
    private lateinit var boforholdServiceV2: BoforholdServiceV2

    @Test
    fun `Test barn over 18 år hele perioden`() {
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnHusstandsmedlemAttenÅrV2()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIPeriodenUtenHusstandsmedlemskapV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[0].bostatus shouldBe Bostatuskode.IKKE_MED_FORELDER
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[1].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test ingen perioder som husstandsmedlem, over 18 år i hele perioden`() {
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggBarnAttenÅrIHelePeriodenUtenHusstandsmedlemskapV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2022, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.REGNES_IKKE_SOM_BARN
            resultat[0].fødselsdato shouldBe mottatteBoforhold[0].fødselsdato
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }


    @Test
    fun `Test at overlappende perioder med brudd slås sammen og at det genereres perioder for når barnet ikke bor i husstanden`() {
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterMedBruddV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggFlereSammenhengendeForekomsterV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggSammenhengendeForekomsterMed18ÅrV2()
        val virkningstidspunkt = LocalDate.of(2018, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggPeriodeFraFørVirkningstidspunktV2()
        val virkningstidspunkt = LocalDate.of(2022, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggPeriodeTomEtterAttenårsdagV2()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
    fun `TestOppholdPerioderHusstandsmedlemskapOgAttenår`() {
        boforholdServiceV2 = BoforholdServiceV2()
        val mottatteBoforhold = TestUtil.byggOppholdPerioderHusstandsmedlemskapOgAttenårV2()
        val virkningstidspunkt = LocalDate.of(2021, 9, 1)
        val resultat = boforholdServiceV2.beregnEgneBarn(virkningstidspunkt, mottatteBoforhold)

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
}
