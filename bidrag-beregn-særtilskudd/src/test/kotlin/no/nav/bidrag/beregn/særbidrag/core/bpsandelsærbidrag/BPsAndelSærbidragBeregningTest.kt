package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning.BPsAndelSærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Utgift
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BPsAndelSærbidragBeregningTest {

    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val bPsAndelUnderholdskostnadBeregning = BPsAndelSærbidragBeregning()

    @DisplayName("Beregning med inntekter for alle parter")
    @Test
    fun testBeregningMedInntekterForAlle() {
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(217666),
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(400000),
            ),
        )
        val inntektSB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(40000),
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
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
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )

        val inntektBP = mutableListOf<Inntekt>()
        inntektBP.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(200000),
            ),
        )
        inntektBP.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(17666),
            ),
        )

        val inntektBM = mutableListOf<Inntekt>()
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(100000),
            ),
        )
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(200000),
            ),
        )
        inntektBM.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(100000),
            ),
        )

        val inntektSB = mutableListOf<Inntekt>()
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )
        inntektSB.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(10000),
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(34.7)) },
            { assertThat(resultat.barnetErSelvforsørget).isFalse() },
        )
    }

    @DisplayName("Beregning der barnets inntekter er høyere enn 100 * forhøyet forskuddssats. Andel skal da bli 0")
    @Test
    fun testAndelLikNullVedHoyInntektBarn() {
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(217666),
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(400000),
            ),
        )
        val inntektSB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(400000),
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsørget).isTrue() },
        )
    }

    @DisplayName(
        "Test at beregnet andel ikke settes høyere enn 5/6 (83,3333333333). Legger inn 10 desimaler for å få likt resultat som i Bidragskalkulator",
    )
    @Test
    fun testAtMaksAndelSettes() {
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(1000000),
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(40000),
            ),
        )
        val inntektSB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(40000),
            ),
        )

        // Beregnet andel skal da bli 92,6%, overstyres til 5/6 (83,3333333333%)
        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
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
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(502000),
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(500000),
            ),
        )
        val inntektSB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.ZERO,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
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
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(550000),
            ),
        )
        val inntektBM = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(300000),
            ),
        )
        val inntektSB = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.ZERO,
            ),
        )

        val beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert =
            GrunnlagBeregning(
                utgift = utgift,
                inntektBPListe = inntektBP,
                inntektBMListe = inntektBM,
                inntektSBListe = inntektSB,
                sjablonListe = sjablonPeriodeListe,
            )

        val resultat = bPsAndelUnderholdskostnadBeregning.beregn(beregnBPsAndelUnderholdskostnadGrunnlagPeriodisert)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(64.7)) },
            { assertThat(resultat.resultatAndelBeløp).isEqualTo(BigDecimal.valueOf(647)) },
        )
    }
}
