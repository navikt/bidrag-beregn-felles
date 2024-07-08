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
    fun `Test voksne i husstanden`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksne1()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[1].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
