package no.nav.bidrag.sivilstand.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.sivilstand.TestUtil
import no.nav.bidrag.sivilstand.response.Status
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class SivilstandServiceTest {
    private lateinit var sivilstandService: SivilstandService

    @Test
    fun `Test periodisering og sammenslåing av sivilstandsforekomster`() {
        sivilstandService = SivilstandService()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSortering()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 3
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2001, 5, 1)
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
    fun `Test på at perioder før virkningstidspunkt filtreres bort`() {
        sivilstandService = SivilstandService()
        val mottatteSivilstandsforekomster = TestUtil.byggHentSivilstandResponseTestSortering()
        val vedtakstidspunkt = LocalDate.of(2017, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, mottatteSivilstandsforekomster)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 2

            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2017, 8, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER

            resultat.sivilstandListe[1].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat.sivilstandListe[1].periodeTom shouldBe null
            resultat.sivilstandListe[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }

    @Test
    fun `Test ingen aktiv status`() {
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandUtenAktivStatus()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultatIngenAktivStatus = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultatIngenAktivStatus)
            resultatIngenAktivStatus.sivilstandListe.size shouldBe 0
            resultatIngenAktivStatus.status shouldBe Status.ALLE_FOREKOMSTER_ER_HISTORISKE
        }
    }

    @Test
    fun `Test ingen datoinformasjon`() {
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandMedPeriodeUtenDatoer()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 0
            resultat.status shouldBe Status.MANGLENDE_DATOINFORMASJON
        }
    }

    @Test
    fun `Test at dato hentes fra registrert for aktiv status`() {
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandMedAktivForekomstOgKunRegistrert()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
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
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstBorAleneMedBarn()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
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
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandÉnForekomstGiftSamboer()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
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
        sivilstandService = SivilstandService()
        val grunnlagTomListe = TestUtil.byggSivilstandFlereForekomstBorAleneMedBarn()
        val vedtakstidspunkt = LocalDate.of(2010, 9, 21)
        val resultat = sivilstandService.beregn(vedtakstidspunkt, grunnlagTomListe)
        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 1
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2017, 3, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe null
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }
}
