package no.nav.bidrag.sivilstand.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.sivilstand.TestUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SivilstandServiceV2Test {
    private lateinit var sivilstandServiceV2: SivilstandServiceV2

    @Test
    fun `Test periodisering og sammenslåing av sivilstandsforekomster`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSorteringV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2011, 1, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT

            resultat[1].periodeFom shouldBe LocalDate.of(2011, 2, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2017, 7, 31)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN

            resultat[2].periodeFom shouldBe LocalDate.of(2017, 8, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat[3].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test på at perioder før virkningstidspunkt filtreres bort og periodeFom settes lik virkningstidspunkt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSorteringV2()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat[1].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test ingen aktiv status`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandUtenAktivStatusV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultatIngenAktivStatus = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultatIngenAktivStatus)
            resultatIngenAktivStatus.size shouldBe 1
            resultatIngenAktivStatus[0].periodeFom shouldBe virkningstidspunkt
            resultatIngenAktivStatus[0].periodeTom shouldBe null
            resultatIngenAktivStatus[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultatIngenAktivStatus[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test ingen datoinformasjon`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandMedPeriodeUtenDatoerV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe virkningstidspunkt
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at dato hentes fra registrert for aktiv status`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandMedAktivForekomstOgKunRegistrertV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2017, 3, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT

            resultat[1].periodeFom shouldBe LocalDate.of(2017, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 11, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 12, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Bor Alene Med Barn`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandÉnForekomstBorAleneMedBarnV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2020, 4, 30)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT

            resultat[1].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Gift-Samboer med periodeFom etter virkningstidspunkt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandÉnForekomstGiftSamboerV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2020, 5, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT

            resultat[1].periodeFom shouldBe LocalDate.of(2020, 6, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
        }
    }

    @Test
    fun `Test med flere forekomster med Bor Alene Med Barn`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandFlereForekomstBorAleneMedBarnV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2017, 2, 28)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT

            resultat[1].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test av sjekk logisk feil i tidslinje, kun perioder etter virkningstidspunkt sjekkes`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandMedLogiskFeilV2()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2022, 12, 6)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe virkningstidspunkt
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }

        // virkningstidspunkt er satt til etter tomdato på ugift-status, logisk feil skal da ignoreres
        val virkningstidspunkt2 = LocalDate.of(2022, 12, 7)
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt2, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat2)
            resultat2.size shouldBe 1
            resultat2[0].periodeFom shouldBe virkningstidspunkt2
            resultat2[0].periodeTom shouldBe null
            resultat2[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test med flere forekomster i samme måned`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggSivilstandFlereForkomsterISammeMånedV2()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2017, 3, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test på at alle forekomster med periodeTom før virkningstidspunkt blir filtrert bort`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlag = TestUtil.byggHentSivilstandResponseTestSorteringV2()
        // virkningstidspunkt er satt til etter periodeTom for alle forekomster
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlag)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test at manuell periode som har en identisk offentlig periode endres til kilde = Offentlig `() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPeriodeErIdentisk()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test periodisering med flere manuelle og offentlige perioder og perioder uten status `() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteBoforhold = TestUtil.flereManuelleOgOffentligePerioder()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 6
            // Offentlig periode der periodeFom forskyves frem til virkningstidspunkt
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 2, 28)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som ligger med null i preiodeTom. Perioden splittes opp i de underliggende offentlige periodene for å dekke
            // oppholdet mellom manuelle perioder.
            resultat[1].periodeFom shouldBe LocalDate.of(2021, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat[3].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[3].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[3].kilde shouldBe Kilde.OFFENTLIG

            resultat[4].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[4].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[4].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[4].kilde shouldBe Kilde.MANUELL

            resultat[5].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[5].periodeTom shouldBe null
            resultat[5].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[5].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test kun manuell periode med periodeTom satt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteBoforhold = TestUtil.kunManuellPeriode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteBoforhold)

        // Det genereres en offentlig periode med Sivilstandskode = UKJENT for etter den manuelle perioden.
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at sammenhengende manuell og offentlig periode med lik sivilstandskode slås sammen til manuell periode`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottatteBoforhold = TestUtil.manuellOgOffentligPerioderLikSivilstandskode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottatteBoforhold)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
