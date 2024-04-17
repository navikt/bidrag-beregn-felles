package no.nav.bidrag.sivilstand.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.sivilstand.TestUtil
import no.nav.bidrag.sivilstand.dto.Kilde
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
            resultat.size shouldBe 3
            resultat[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2017, 7, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN

            resultat[1].periodeFom shouldBe LocalDate.of(2017, 8, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat[2].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
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
        val grunnlagTomListe = TestUtil.byggSivilstandUtenAktivStatusV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultatIngenAktivStatus = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
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
        val grunnlagTomListe = TestUtil.byggSivilstandMedPeriodeUtenDatoerV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
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
        val grunnlagTomListe = TestUtil.byggSivilstandMedAktivForekomstOgKunRegistrertV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2
            resultat[0].periodeFom shouldBe LocalDate.of(2017, 4, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 11, 30)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 12, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Bor Alene Med Barn`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstBorAleneMedBarnV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Gift-Samboer`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstGiftSamboerV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 6, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
        }
    }

    @Test
    fun `Test med flere forekomster med Bor Alene Med Barn`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlagTomListe = TestUtil.byggSivilstandFlereForekomstBorAleneMedBarnV2()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test av sjekk logisk feil i tidslinje, kun perioder etter virkningstidspunkt sjekkes`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val grunnlagTomListe = TestUtil.byggSivilstandMedLogiskFeilV2()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2022, 12, 6)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
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
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt2, grunnlagTomListe)
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
        val grunnlagTomListe = TestUtil.byggSivilstandFlereForkomsterISammeMånedV2()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2017, 3, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
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
        val grunnlagTomListe = TestUtil.byggHentSivilstandResponseTestSorteringV2()
        // virkningstidspunkt er satt til etter periodeTom for alle forekomster
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }
}
