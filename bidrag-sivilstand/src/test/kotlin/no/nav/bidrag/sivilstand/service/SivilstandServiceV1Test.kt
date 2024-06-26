package no.nav.bidrag.sivilstand.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.sivilstand.TestUtil
import no.nav.bidrag.sivilstand.response.Status
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SivilstandServiceV1Test {
    private lateinit var sivilstandServiceV1: SivilstandServiceV1

    @Test
    fun `Test periodisering og sammenslåing av sivilstandsforekomster`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSortering()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 3
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe LocalDate.of(2017, 7, 31)
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN

            resultat.sivilstandListe[1].periodeFom shouldBe LocalDate.of(2017, 8, 1)
            resultat.sivilstandListe[1].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat.sivilstandListe[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat.sivilstandListe[2].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat.sivilstandListe[2].periodeTom shouldBe null
            resultat.sivilstandListe[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test på at perioder før virkningstidspunkt filtreres bort og periodeFom settes lik virkningstidspunkt`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSortering()
        val virkningstidspunkt = LocalDate.of(2020, 5, 1)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 2

            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat.sivilstandListe[1].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat.sivilstandListe[1].periodeTom shouldBe null
            resultat.sivilstandListe[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test ingen aktiv status`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandUtenAktivStatus()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultatIngenAktivStatus = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultatIngenAktivStatus)
            resultatIngenAktivStatus.sivilstandListe.size shouldBe 0
            resultatIngenAktivStatus.status shouldBe Status.ALLE_FOREKOMSTER_ER_HISTORISKE
        }
    }

    @Test
    fun `Test ingen datoinformasjon`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandMedPeriodeUtenDatoer()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 0
            resultat.status shouldBe Status.MANGLENDE_DATOINFORMASJON
        }
    }

    @Test
    fun `Test at dato hentes fra registrert for aktiv status`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandMedAktivForekomstOgKunRegistrert()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 2
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2017, 4, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe LocalDate.of(2022, 11, 30)
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat.sivilstandListe[1].periodeFom shouldBe LocalDate.of(2022, 12, 1)
            resultat.sivilstandListe[1].periodeTom shouldBe null
            resultat.sivilstandListe[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Bor Alene Med Barn`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstBorAleneMedBarn()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2020, 5, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test med kun én forekomst Gift-Samboer`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstGiftSamboer()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2020, 6, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
        }
    }

    @Test
    fun `Test med flere forekomster med Bor Alene Med Barn`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandFlereForekomstBorAleneMedBarn()
        val virkningstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test av sjekk logisk feil i tidslinje, kun perioder etter virkningstidspunkt sjekkes`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandMedLogiskFeil()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2022, 12, 6)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 0
            resultat.status shouldBe Status.LOGISK_FEIL_I_TIDSLINJE
        }

        // virkningstidspunkt er satt til etter tomdato på ugift-status, logisk feil skal da ignoreres
        val virkningstidspunkt2 = LocalDate.of(2022, 12, 7)
        val resultat2 = sivilstandServiceV1.beregn(virkningstidspunkt2, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat2)
            resultat2.sivilstandListe.size shouldBe 1
            resultat2.status shouldBe Status.OK
        }
    }

    @Test
    fun `Test med flere forekomster i samme måned`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggSivilstandFlereForkomsterISammeMåned()
        // virkningstidspunkt er satt til før tomdato på ugift-status
        val virkningstidspunkt = LocalDate.of(2017, 3, 1)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test på at alle forekomster med periodeTom før virkningstidspunkt blir filtrert bort`() {
        sivilstandServiceV1 = SivilstandServiceV1()
        val grunnlagTomListe = TestUtil.byggHentSivilstandResponseTestSortering()
        // virkningstidspunkt er satt til etter periodeTom for alle forekomster
        val virkningstidspunkt = LocalDate.of(2023, 3, 1)
        val resultat = sivilstandServiceV1.beregn(virkningstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2023, 3, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }
}
