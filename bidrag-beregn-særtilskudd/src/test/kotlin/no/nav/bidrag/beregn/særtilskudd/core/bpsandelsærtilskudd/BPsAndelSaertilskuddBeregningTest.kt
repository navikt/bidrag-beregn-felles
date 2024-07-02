package no.nav.bidrag.beregn.særtilskudd.core.bpsandelsærtilskudd

import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.beregning.BPsAndelSaertilskuddBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.Inntekt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BPsAndelSaertilskuddBeregningTest {

    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val bPsAndelUnderholdskostnadBeregning = BPsAndelSaertilskuddBeregning()

    @DisplayName("Beregning med inntekter for alle parter")
    @Test
    fun testBeregningMedInntekterForAlle() {
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(217666),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(400000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(40000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(35.2)) },
        )
    }

    @DisplayName(
        "Beregning med flere inntekter for alle parter, tester også at det kalkuleres riktig etter fratrekk av 30 * forhøyet forskudd på barnets inntekt",
    )
    @Test
    fun testBeregningMedFlereInntekterForAlle() {
        val inntektBP = mutableListOf<Inntekt>()
        inntektBP.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBP.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(17666),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val inntektBM = mutableListOf<Inntekt>()
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(100000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(100000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val inntektBB = mutableListOf<Inntekt>()
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        inntektBB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(10000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(34.7)) },
            { assertThat(resultat.barnetErSelvforsorget).isFalse() },
        )
    }

    @DisplayName("Beregning der barnets inntekter er høyere enn 100 * forhøyet forskuddssats. Andel skal da bli 0")
    @Test
    fun testAndelLikNullVedHoyInntektBarn() {
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(217666),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(400000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(400000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsorget).isTrue() },
        )
    }

    @DisplayName(
        "Test at beregnet andel ikke settes høyere enn 5/6 (83,3333333333). Legger inn 10 desimaler for å få likt resultat som i Bidragskalkulator",
    )
    @Test
    fun testAtMaksAndelSettes() {
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(1000000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(40000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(40000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        // Beregnet andel skal da bli 92,6%, overstyres til 5/6 (83,3333333333%)
        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(83.3333333333)) },
        )
    }

    @DisplayName("Beregning med 0 i inntekt for barn")
    @Test
    fun testBeregningMed0InntektBarn() {
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(502000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(500000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.ZERO,
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(50.1)) },
        )
    }

    @DisplayName("Beregning med 0 i inntekt for barn")
    @Test
    fun testBeregningMed0InntektBarn2() {
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(550000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(300000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.ZERO,
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektBBListe = inntektBB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(64.7)) },
            { assertThat(resultat.resultatAndelBelop).isEqualTo(BigDecimal.valueOf(647)) },
        )
    }
}
