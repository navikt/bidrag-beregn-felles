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
        val virkningstidspunkt = LocalDate.of(2022, 11, 1)
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
        val virkningstidspunkt2 = LocalDate.of(2023, 1, 1)
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
        val mottattSivilstand = TestUtil.manuellOgOffentligPeriodeErIdentisk()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand)

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
        val mottattSivilstand = TestUtil.flereManuelleOgOffentligePerioderFlereRequester()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat1 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[0])
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[1])
        val resultat3 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[2])

        assertSoftly {
            Assertions.assertNotNull(resultat3)
//            resultat3.size shouldBe 6
            // Beregning 1
            // Offentlig periode der periodeFom forskyves frem til virkningstidspunkt
            resultat1[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat1[0].periodeTom shouldBe LocalDate.of(2021, 2, 28)
            resultat1[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat1[0].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som ligger med null i periodeTom. Perioden splittes senere opp i de underliggende offentlige periodene
            // for å dekke oppholdet mellom nye perioder som blir lagt til manuelt.
            resultat1[1].periodeFom shouldBe LocalDate.of(2021, 3, 1)
            resultat1[1].periodeTom shouldBe null
            resultat1[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat1[1].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            // Offentlig periode der periodeFom forskyves frem til virkningstidspunkt
            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2021, 2, 28)
            resultat2[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG

            // Offentlig periode som ligger med null i periodeTom. Perioden er splittet opp i de underliggende offentlige periodene
            // for å dekke oppholdet mellom nye perioder som blir lagt til manuelt.
            resultat2[1].periodeFom shouldBe LocalDate.of(2021, 3, 1)
            resultat2[1].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat2[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[1].kilde shouldBe Kilde.OFFENTLIG

            // Nyregistrert periode som er lagt til manuelt
            resultat2[2].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat2[2].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat2[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[2].kilde shouldBe Kilde.MANUELL

            resultat2[3].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat2[3].periodeTom shouldBe null
            resultat2[3].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[3].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 3
            resultat3[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat3[0].periodeTom shouldBe LocalDate.of(2021, 2, 28)
            resultat3[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[0].kilde shouldBe Kilde.OFFENTLIG

            resultat3[1].periodeFom shouldBe LocalDate.of(2021, 3, 1)
            resultat3[1].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat3[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat3[1].kilde shouldBe Kilde.OFFENTLIG

            resultat3[2].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat3[2].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat3[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[2].kilde shouldBe Kilde.MANUELL

            resultat3[3].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat3[3].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat3[3].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat3[3].kilde shouldBe Kilde.OFFENTLIG

            resultat3[4].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat3[4].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat3[4].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[4].kilde shouldBe Kilde.MANUELL

            resultat3[5].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat3[5].periodeTom shouldBe null
            resultat3[5].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat3[5].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test kun manuell periode med periodeTom satt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.kunManuellPeriode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand)

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
        val mottattSivilstand = TestUtil.manuellOgOffentligPerioderLikSivilstandskode()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat1 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[0])
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[1])

        assertSoftly {
            Assertions.assertNotNull(resultat1)
            resultat1.size shouldBe 3
            resultat2.size shouldBe 4

            resultat1[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat1[0].periodeTom shouldBe LocalDate.of(2021, 3, 31)
            resultat1[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat1[0].kilde shouldBe Kilde.OFFENTLIG

            resultat1[1].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat1[1].periodeTom shouldBe LocalDate.of(2022, 4, 30)
            resultat1[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat1[1].kilde shouldBe Kilde.OFFENTLIG

            resultat1[2].periodeFom shouldBe LocalDate.of(2022, 5, 1)
            resultat1[2].periodeTom shouldBe null
            resultat1[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat1[2].kilde shouldBe Kilde.MANUELL

            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2021, 3, 31)
            resultat2[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG

            resultat2[1].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat2[1].periodeTom shouldBe LocalDate.of(2022, 4, 30)
            resultat2[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[1].kilde shouldBe Kilde.OFFENTLIG

            resultat2[2].periodeFom shouldBe LocalDate.of(2022, 5, 1)
            resultat2[2].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat2[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[2].kilde shouldBe Kilde.MANUELL

            resultat2[3].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat2[3].periodeTom shouldBe null
            resultat2[3].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[3].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test kun manuelle perioder`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.flereManuellePerioder()
        val virkningstidspunkt = LocalDate.of(2020, 9, 1)
        val resultat1 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[0])
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt, mottattSivilstand[1])

        // Det genereres en offentlig periode med Sivilstandskode = UKJENT for etter den manuelle perioden.
        assertSoftly {
            Assertions.assertNotNull(resultat1)
            resultat1.size shouldBe 3

            resultat1[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat1[0].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat1[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat1[0].kilde shouldBe Kilde.OFFENTLIG

            resultat1[1].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat1[1].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat1[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat1[1].kilde shouldBe Kilde.MANUELL

            resultat1[2].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat1[2].periodeTom shouldBe null
            resultat1[2].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat1[2].kilde shouldBe Kilde.OFFENTLIG

            // Beregning 2
            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2021, 6, 30)
            resultat2[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG

            resultat2[1].periodeFom shouldBe LocalDate.of(2021, 7, 1)
            resultat2[1].periodeTom shouldBe LocalDate.of(2021, 8, 31)
            resultat2[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[1].kilde shouldBe Kilde.MANUELL

            resultat2[2].periodeFom shouldBe LocalDate.of(2021, 9, 1)
            resultat2[2].periodeTom shouldBe LocalDate.of(2021, 12, 31)
            resultat2[2].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat2[2].kilde shouldBe Kilde.OFFENTLIG

            resultat2[3].periodeFom shouldBe LocalDate.of(2022, 1, 1)
            resultat2[3].periodeTom shouldBe LocalDate.of(2023, 8, 31)
            resultat2[3].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[3].kilde shouldBe Kilde.MANUELL

            resultat2[4].periodeFom shouldBe LocalDate.of(2023, 9, 1)
            resultat2[4].periodeTom shouldBe null
            resultat2[4].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat2[4].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test 1a, 1c, 2a endreSivilstand = null behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er utfyllt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandNullBehandledeUtfylltOffentligeOpplysningerUtfyllt1a1c2a()
        val virkningstidspunkt1 = LocalDate.of(2020, 9, 1)
        // Beregner på offentlige perioder
        val resultat1 = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand[0])

        // Legger til manuell periode
        val resultat2 = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand[1])

        // Endrer offentlige opplysninger og virkningstidspunkt tilbake i tid
        val virkningstidspunkt2 = LocalDate.of(2019, 10, 1)
        val resultat3 = sivilstandServiceV2.beregn(virkningstidspunkt2, mottattSivilstand[2])

        assertSoftly {
            Assertions.assertNotNull(resultat1)
            resultat1.size shouldBe 2
            resultat2.size shouldBe 4
            resultat3.size shouldBe 5

            // 1c
            resultat1[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat1[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat1[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat1[0].kilde shouldBe Kilde.OFFENTLIG

            resultat1[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat1[1].periodeTom shouldBe null
            resultat1[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat1[1].kilde shouldBe Kilde.OFFENTLIG

            // 2a
            resultat2[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat2[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat2[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[0].kilde shouldBe Kilde.OFFENTLIG

            resultat2[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat2[1].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat2[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[1].kilde shouldBe Kilde.OFFENTLIG

            resultat2[2].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat2[2].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat2[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat2[2].kilde shouldBe Kilde.MANUELL

            resultat2[3].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat2[3].periodeTom shouldBe null
            resultat2[3].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat2[3].kilde shouldBe Kilde.OFFENTLIG

            // 1a med oppdaterte offentlige opplysninger og virkningstidspunkt endret tilbake i tid
            resultat3[0].periodeFom shouldBe LocalDate.of(2019, 10, 1)
            resultat3[0].periodeTom shouldBe LocalDate.of(2020, 2, 29)
            resultat3[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[0].kilde shouldBe Kilde.OFFENTLIG

            resultat3[1].periodeFom shouldBe LocalDate.of(2020, 3, 1)
            resultat3[1].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat3[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat3[1].kilde shouldBe Kilde.OFFENTLIG

            // Denne perioden matcher ikke lenger med oppdatert offentlig informasjon, men kilde endres ikke til manuell
            resultat3[2].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat3[2].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat3[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[2].kilde shouldBe Kilde.OFFENTLIG

            resultat3[3].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat3[3].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat3[3].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat3[3].kilde shouldBe Kilde.OFFENTLIG

            resultat3[4].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat3[4].periodeTom shouldBe null
            resultat3[4].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat3[4].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test 1b endreSivilstand = null behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandNullBehandledeUtfylltOffentligeOpplysningerTom1b()
        // Beregner med tidligere virkningstidspunkt enn behandledeSivilstandsopplysninger ble beregnet ut fra
        val virkningstidspunkt1 = LocalDate.of(2020, 2, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // 1b
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 2, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2020, 8, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test 1d endreSivilstand = null behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandNullBehandledeTomOffentligeOpplysningerTom1d()
        // Beregner med tidligere virkningstidspunkt enn behandledeSivilstandsopplysninger ble beregnet ut fra
        val virkningstidspunkt1 = LocalDate.of(2020, 2, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            // 1d
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 2, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test 2b endreSivilstand er utfyllt NY behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltNYBehandledeUtfylltOffentligeOpplysningerTom2b()
        // Beregner med tidligere virkningstidspunkt enn behandledeSivilstandsopplysninger ble beregnet ut fra
        val virkningstidspunkt1 = LocalDate.of(2020, 2, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 4

            // 2b

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[2].periodeTom shouldBe LocalDate.of(2024, 2, 29)
            resultat[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[2].kilde shouldBe Kilde.MANUELL

            resultat[3].periodeFom shouldBe LocalDate.of(2024, 3, 1)
            resultat[3].periodeTom shouldBe null
            resultat[3].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[3].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test 2b endreSivilstand er utfyllt ENDRE behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltENDREBehandledeUtfylltOffentligeOpplysningerTom2b()
        // Beregner med tidligere virkningstidspunkt enn behandledeSivilstandsopplysninger ble beregnet ut fra
        val virkningstidspunkt1 = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            // 2b ENDRE

            resultat[0].periodeFom shouldBe LocalDate.of(2020, 8, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2021, 3, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test 2b endreSivilstand er utfyllt SLETT behandledeSivilstandsopplysninger er utfyllt og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltSLETTBehandledeUtfylltOffentligeOpplysningerTom2b()
        // Slettet periode erstattes med ny periode med motsatt sivilstandskode. Perioden har da lik sivilstandskode som perioden etter og slås
        // sammen med denne.
        val virkningstidspunkt1 = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            // 2b SLETT
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 2, 28)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2022, 3, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test 2b endreSivilstand er utfyllt SLETT behandledeSivilstandsopplysninger er utfyllt slett periode midt i rekken`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltSLETTMidtperiodeBehandledeUtfylltOffentligeOpplysningerTom2b()
        // Slettet periode erstattes med ny periode med motsatt sivilstandskode. Perioden har da lik sivilstandskode som perioden etter og slås
        // sammen med denne.
        val virkningstidspunkt1 = LocalDate.of(2020, 9, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 2

            // 2b SLETT
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 9, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2024, 1, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[0].kilde shouldBe Kilde.MANUELL

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 2, 1)
            resultat[1].periodeTom shouldBe null
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[1].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test 2c endreSivilstand er utfyllt behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er utfyllt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltBehandledeTomOffentligeOpplysningerUtfyllt2c()
        //

        val virkningstidspunkt1 = LocalDate.of(2020, 2, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // 2c
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 2, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 4, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 5, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test 2d endreSivilstand er utfyllt behandledeSivilstandsopplysninger er tom og innhentedeOffentligeOpplysninger er tom`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstandUtfylltBehandledeTomOffentligeOpplysningerTom2d()

        val virkningstidspunkt1 = LocalDate.of(2020, 2, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // 2d SLETT
            resultat[0].periodeFom shouldBe LocalDate.of(2020, 2, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2024, 4, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.MANUELL

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 5, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test endreSivilstand`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreSivilstand()

        val virkningstidspunkt1 = LocalDate.of(2023, 1, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            // Første periode får kilde satt til Offentlig pga matchende offentlig periode
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 3, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 4, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 12, 31)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2024, 1, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[2].kilde shouldBe Kilde.MANUELL
        }
    }

    @Test
    fun `Test endreVirkningstidspunktFremITid`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.endreVirkningstidspunktFremITid()

        val virkningstidspunkt1 = LocalDate.of(2023, 1, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            // Første periode får kilde satt til Offentlig pga matchende offentlig periode
            resultat[0].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at gyldigFom blir satt lik BMs fødselsdato ved status UGIFT`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.gyldigFomLikFødselsdatoUgift()

        val virkningstidspunkt1 = LocalDate.of(2022, 8, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].periodeFom shouldBe LocalDate.of(2022, 8, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2022, 12, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 1, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 4, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 5, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at det ikke genereres periode med UKJENT hvis aktiv periode er før virkningstidspunkt`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.aktivPeriodeErFørVirkningstidspunkt()

        val virkningstidspunkt1 = LocalDate.of(2024, 4, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2024, 4, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test periodeTom blir riktig når neste periode starter den første i måneden`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.hullIPeriode()

        val virkningstidspunkt1 = LocalDate.of(2021, 4, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 3

            resultat[0].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat[0].periodeTom shouldBe LocalDate.of(2023, 5, 31)
            resultat[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[0].kilde shouldBe Kilde.OFFENTLIG

            resultat[1].periodeFom shouldBe LocalDate.of(2023, 6, 1)
            resultat[1].periodeTom shouldBe LocalDate.of(2023, 11, 30)
            resultat[1].sivilstandskode shouldBe Sivilstandskode.GIFT_SAMBOER
            resultat[1].kilde shouldBe Kilde.OFFENTLIG

            resultat[2].periodeFom shouldBe LocalDate.of(2023, 12, 1)
            resultat[2].periodeTom shouldBe null
            resultat[2].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
            resultat[2].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at det ikke genereres to Ukjent-periode`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.ikkeToUkjentPerioder()

        val virkningstidspunkt1 = LocalDate.of(2021, 4, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }

    @Test
    fun `Test at det ikke blir np`() {
        sivilstandServiceV2 = SivilstandServiceV2()
        val mottattSivilstand = TestUtil.ikkeNPPlease()

        val virkningstidspunkt1 = LocalDate.of(2021, 4, 1)
        val resultat = sivilstandServiceV2.beregn(virkningstidspunkt1, mottattSivilstand)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.size shouldBe 1

            resultat[0].periodeFom shouldBe LocalDate.of(2021, 4, 1)
            resultat[0].periodeTom shouldBe null
            resultat[0].sivilstandskode shouldBe Sivilstandskode.UKJENT
            resultat[0].kilde shouldBe Kilde.OFFENTLIG
        }
    }
}
