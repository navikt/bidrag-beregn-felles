package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdAndreVoksneServiceV3Test {
    private lateinit var boforholdAndreVoksneService: BoforholdAndreVoksneService

    // Tester med kun offentlige perioder
    @Test
    fun `Test voksne i husstanden kun offentlige perioder`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 10, 31)
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 11, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[1].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test voksne i husstanden offentlig og manuell periode`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligeMauellePerioder1()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold[0])
        val resultat2 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold[1])
        val resultat3 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold[2])

        assertSoftly {
            Assertions.assertNotNull(resultat)
            // Beregning 1
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat2.size shouldBe 2
            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat2[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG

            resultat2[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat2[1].periodeTom shouldBe null
            resultat2[1].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat2[1].kilde shouldBe Kilde.MANUELL

            // Beregning 3
            resultat3.size shouldBe 1
            resultat3[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat3[0].periodeTom shouldBe null
            resultat3[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat3[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
