package no.nav.bidrag.boforhold.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.boforhold.TestUtil
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BoforholdAndreVoksneServiceTest {
    private lateinit var boforholdAndreVoksneService: BoforholdAndreVoksneService

    private val beregnTilDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)

    // Tester med kun offentlige perioder
    @Test
    fun `Test voksne i husstanden kun offentlige perioder`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
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
    fun `Test voksne i husstanden offentlig og ny manuell periode som så slettes`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligNyManuellePeriode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold[0], beregnTilDato = beregnTilDato)
        val resultat2 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(
            virkningstidspunkt,
            mottatteBoforhold[1],
            beregnTilDato = beregnTilDato,
        )
        val resultat3 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(
            virkningstidspunkt,
            mottatteBoforhold[2],
            beregnTilDato = beregnTilDato,
        )

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

    @Test
    fun `Test voksne i husstanden ingen offentlige perioder og ny manuell periode som så endres`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligeNyManuellePeriodeEndre()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold[0], beregnTilDato = beregnTilDato)
        val resultat2 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(
            virkningstidspunkt,
            mottatteBoforhold[1],
            beregnTilDato = beregnTilDato,
        )
        val resultat3 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(
            virkningstidspunkt,
            mottatteBoforhold[2],
            beregnTilDato = beregnTilDato,
        )

        assertSoftly {
            Assertions.assertNotNull(resultat)
            // Beregning 1
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat2.size shouldBe 2
            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat2[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat2[0].kilde shouldBe Kilde.MANUELL

            resultat2[1].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat2[1].periodeTom shouldBe null
            resultat2[1].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat2[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 3
            resultat3.size shouldBe 1
            resultat3[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat3[0].periodeTom shouldBe null
            resultat3[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat3[0].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test at husstandsmedlemskap under én måned filtreres bort`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggTestÉnmånedsgrenseHusstandsmedlemskap()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 4, 30)
            resultat[0].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 5, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 6, 30)
            resultat[1].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 7, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at kilde for bor_ikke_med_andre_voksne settes til offentlig når det ikke finnes offentlige perioder`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggIngenOffentligeOpplysningerNyManuellPeriode()
        val virkningstidspunkt = LocalDate.of(2024, 1, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at kilde for bor_ikke_med_andre_voksne settes til offentlig når det ikke finnes noen perioder`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggIngenPerioder()
        val virkningstidspunkt = LocalDate.of(2024, 1, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test flere husstandsmedlemmer med periodeTom er lik null`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggTestFlereHusstandsmedlemmerPeriodeTomNull()
        val virkningstidspunkt = LocalDate.of(2024, 1, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test endre virkningsidspunkt`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val grunnlag1 = TestUtil.byggEndreVirkningstidspunktVoksne()[0]
        var virkningstidspunkt = LocalDate.of(2024, 3, 1)
        val resultat1 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, grunnlag1, beregnTilDato = beregnTilDato)

        val grunnlag2 = TestUtil.byggEndreVirkningstidspunktVoksne()[0]
        virkningstidspunkt = LocalDate.of(2024, 1, 1)
        val resultat2 = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, grunnlag2, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat1)
//            resultat1.size shouldBe 1

            resultat1[0].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat1[0].periodeTom shouldBe null
            resultat1[0].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat1[0].kilde shouldBe Kilde.OFFENTLIG

            resultat2[0].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat2[0].periodeTom shouldBe null
            resultat2[0].bostatus shouldBe Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    // Tester med kun offentlige perioder
    @Test
    fun `Test voksne i husstanden kun offentlige perioder to husstander`() {
        boforholdAndreVoksneService = BoforholdAndreVoksneService()
        val mottatteBoforhold = TestUtil.byggBorMedAndreVoksneOffentligePerioderToHusstander()
        val virkningstidspunkt = LocalDate.of(2025, 1, 1)
        val resultat = boforholdAndreVoksneService.beregnBoforholdAndreVoksne(virkningstidspunkt, mottatteBoforhold, beregnTilDato = beregnTilDato)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2025, 1, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].bostatus shouldBe Bostatuskode.BOR_MED_ANDRE_VOKSNE
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
