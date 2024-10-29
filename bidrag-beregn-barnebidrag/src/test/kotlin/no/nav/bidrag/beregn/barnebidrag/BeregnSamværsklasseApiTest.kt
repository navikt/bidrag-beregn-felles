package no.nav.bidrag.beregn.barnebidrag

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.samværskalkulator.SamværskalkulatorFerietype
import no.nav.bidrag.domene.enums.samværskalkulator.SamværskalkulatorNetterFrekvens
import no.nav.bidrag.transport.behandling.beregning.samvær.SamværskalkulatorDetaljer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class BeregnSamværsklasseApiTest {
    val api: BeregnSamværsklasseApi = BeregnSamværsklasseApi()

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
    }

    @Test
    fun `skal beregne samværsklasse`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer())
        resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_2
        resultat.sumGjennomsnittligSamværPerMåned shouldBe BigDecimal("6.98")
    }

    @Test
    fun `skal beregne samværsklasse for høyt antall regelmessig netter`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = 10))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_4
            resultat.sumGjennomsnittligSamværPerMåned shouldBe BigDecimal("20.88")
        }
    }

    @Test
    fun `skal beregne samværsklasse for lavt antall regelmessig netter`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = 1, ferier = emptyList()))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_1
            resultat.sumGjennomsnittligSamværPerMåned shouldBe BigDecimal("2.17")
        }
    }

    @Test
    fun `skal beregne samværsklasse ingen samvær`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = 0, ferier = emptyList()))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.INGEN_SAMVÆR
            resultat.sumGjennomsnittligSamværPerMåned shouldBe BigDecimal("0.00")
        }
    }

    private fun opprettKalkulatoDetaljer() = SamværskalkulatorDetaljer(
        regelmessigSamværNetter = 2,
        ferier = listOf(
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.SOMMERFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 14,
                bidragspliktigNetter = 14,
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.JUL_NYTTÅR,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 2,
                bidragspliktigNetter = 3,
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.VINTERFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 2,
                bidragspliktigNetter = 3,
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.HØSTFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 2,
                bidragspliktigNetter = 3,
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.ANNET,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 10,
                bidragspliktigNetter = 10,
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.PÅSKE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = 1,
                bidragspliktigNetter = 9,
            ),
        ),
    )
}
