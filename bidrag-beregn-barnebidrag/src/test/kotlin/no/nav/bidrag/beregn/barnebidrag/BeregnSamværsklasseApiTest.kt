package no.nav.bidrag.beregn.barnebidrag

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.commons.web.mock.stubSjablonService
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
    lateinit var api: BeregnSamværsklasseApi

    @BeforeEach
    fun initMock() {
        api = BeregnSamværsklasseApi(stubSjablonService())
    }

    @Test
    fun `skal returnere gjennomsnittlig samvær`() {
        BeregnSamværsklasseApi.beregnSumGjennomsnittligSamværPerMåned(opprettKalkulatoDetaljer()) shouldBe BigDecimal("6.98")
    }

    @Test
    fun `skal beregne samværsklasse`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer())
        resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_2
        resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("6.98")
    }

    @Test
    fun `skal beregne samværsklasse for 2 regelmessig samvær`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(2),
                ferier = emptyList(),
            ),
        )
        resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_2
        resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("4.34")
    }

    @Test
    fun `skal beregne samværsklasse for 10 regelmessig samvær`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(10),
                ferier = emptyList(),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_4
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("21.72")
        }
    }

    @Test
    fun `skal beregne samværsklasse for 7 regelmessig samvær`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(7),
                ferier = emptyList(),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_4
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("15.21")
        }
    }

    @Test
    fun `skal beregne samværsklasse for høyt antall regelmessig netter`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = BigDecimal(10)))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_4
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("20.88")
        }
    }

    @Test
    fun `skal beregne samværsklasse for lavt antall regelmessig netter`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = BigDecimal(1), ferier = emptyList()))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_1
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("2.17")
        }
    }

    @Test
    fun `skal beregne samværsklasse ingen samvær`() {
        val resultat = api.beregnSamværsklasse(opprettKalkulatoDetaljer().copy(regelmessigSamværNetter = BigDecimal(0), ferier = emptyList()))
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("0.00")
        }
    }

    @Test
    fun `skal skille på hvert år og annen hvert år beregning`() {
        assertSoftly("Hvert år") {
            val resultat = api.beregnSamværsklasse(
                SamværskalkulatorDetaljer(
                    regelmessigSamværNetter = BigDecimal(0),
                    ferier = listOf(
                        SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                            type = SamværskalkulatorFerietype.SOMMERFERIE,
                            frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                            bidragsmottakerNetter = BigDecimal(14),
                            bidragspliktigNetter = BigDecimal(14),
                        ),
                    ),
                ),
            )

            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }

        assertSoftly("Annet hvert år") {
            val resultat = api.beregnSamværsklasse(
                SamværskalkulatorDetaljer(
                    regelmessigSamværNetter = BigDecimal(0),
                    ferier = listOf(
                        SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                            type = SamværskalkulatorFerietype.SOMMERFERIE,
                            frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                            bidragsmottakerNetter = BigDecimal(14),
                            bidragspliktigNetter = BigDecimal(14),
                        ),
                    ),
                ),
            )

            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("0.58")
        }
    }

    @Test
    fun `skal bruke sommerferie beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.SOMMERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal bruke jul og nyttår til beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.JUL_NYTTÅR,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal bruke høstferie til beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.HØSTFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal bruke påske til beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.PÅSKE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal bruke vinterferie til beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.VINTERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal bruke annet til beregning`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(0),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.ANNET,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragsmottakerNetter = BigDecimal(14),
                        bidragspliktigNetter = BigDecimal(14),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_0
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("1.17")
        }
    }

    @Test
    fun `skal beregne samværsklasse for en case med reglemessig netter og ferier 2`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(4),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.JUL_NYTTÅR,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(5),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.VINTERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(7),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.PÅSKE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(5),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.SOMMERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(21),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.HØSTFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(7),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_3
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("10.65")
        }
    }

    @Test
    fun `skal beregne samværsklasse for en case med reglemessig netter og ferier`() {
        val resultat = api.beregnSamværsklasse(
            SamværskalkulatorDetaljer(
                regelmessigSamværNetter = BigDecimal(12),
                ferier = listOf(
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.JUL_NYTTÅR,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(5),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.VINTERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(7),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.PÅSKE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(5),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.SOMMERFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(21),
                    ),
                    SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                        type = SamværskalkulatorFerietype.HØSTFERIE,
                        frekvens = SamværskalkulatorNetterFrekvens.ANNET_HVERT_ÅR,
                        bidragspliktigNetter = BigDecimal(7),
                    ),
                ),
            ),
        )
        assertSoftly {
            resultat.samværsklasse shouldBe Samværsklasse.SAMVÆRSKLASSE_4
            resultat.gjennomsnittligSamværPerMåned shouldBe BigDecimal("26.46")
        }
    }

    private fun opprettKalkulatoDetaljer() = SamværskalkulatorDetaljer(
        regelmessigSamværNetter = BigDecimal(2),
        ferier = listOf(
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.SOMMERFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(14),
                bidragspliktigNetter = BigDecimal(14),
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.JUL_NYTTÅR,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(2),
                bidragspliktigNetter = BigDecimal(3),
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.VINTERFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(2),
                bidragspliktigNetter = BigDecimal(3),
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.HØSTFERIE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(2),
                bidragspliktigNetter = BigDecimal(3),
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.ANNET,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(10),
                bidragspliktigNetter = BigDecimal(10),
            ),
            SamværskalkulatorDetaljer.SamværskalkulatorFerie(
                type = SamværskalkulatorFerietype.PÅSKE,
                frekvens = SamværskalkulatorNetterFrekvens.HVERT_ÅR,
                bidragsmottakerNetter = BigDecimal(1),
                bidragspliktigNetter = BigDecimal(9),
            ),
        ),
    )
}
