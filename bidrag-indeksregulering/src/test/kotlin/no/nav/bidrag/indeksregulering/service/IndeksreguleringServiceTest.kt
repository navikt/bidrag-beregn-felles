package no.nav.bidrag.sivilstand.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.sivilstand.TestUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class IndeksreguleringServiceTest {
    private lateinit var indeksreguleringService: IndeksreguleringService

    @Test
    fun `Test periodisering og sammenslåing av sivilstandsforekomster`() {
        indeksreguleringService = IndeksreguleringService()
        val grunnlag = TestUtil.byggHentSivilstandResponseTestSortering()
        val virkningstidspunkt = LocalDate.of(2010, 9, 1)
        val resultat = indeksreguleringService.beregn(virkningstidspunkt, grunnlag)

        assertSoftly {
            Assertions.assertNotNull(resultat)
            resultat.sivilstandListe.size shouldBe 3
            resultat.sivilstandListe[0].periodeFom shouldBe LocalDate.of(2010, 9, 1)
            resultat.sivilstandListe[0].periodeTom shouldBe LocalDate.of(2017, 7, 31)
            resultat.sivilstandListe[0].sivilstandskode shouldBe Sivilstandskode.BOR_ALENE_MED_BARN
        }
    }
}
