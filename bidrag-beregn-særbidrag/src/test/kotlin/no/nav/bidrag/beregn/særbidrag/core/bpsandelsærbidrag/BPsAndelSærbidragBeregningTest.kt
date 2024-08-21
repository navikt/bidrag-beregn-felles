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
    private val bPsAndelSærbidragBeregning = BPsAndelSærbidragBeregning()

    @DisplayName("Beregning med normal inntekt for alle parter")
    @Test
    fun beregningMedNormalInntektForAlle() {
        val utgift = Utgift(referanse = TestUtil.UTGIFT_REFERANSE, beløp = BigDecimal.valueOf(1000))
        val inntektBP = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(500000))
        val inntektBM = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(400000))
        val inntektSB = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(50000))

        val grunnlag = GrunnlagBeregning(utgift, inntektBP, inntektBM, inntektSB, sjablonPeriodeListe)

        val resultat = bPsAndelSærbidragBeregning.beregn(grunnlag)

        assertAll(
            { assertThat(resultat.resultatAndelFaktor).isGreaterThan(BigDecimal.ZERO) },
            { assertThat(resultat.resultatAndelBeløp).isGreaterThan(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsørget).isFalse() },
        )
    }

    @DisplayName("Beregning når barnets inntekt er null")
    @Test
    fun beregningNårBarnetsInntektErNull() {
        val utgift = Utgift(referanse = TestUtil.UTGIFT_REFERANSE, beløp = BigDecimal.valueOf(1000))
        val inntektBP = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(300000))
        val inntektBM = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(300000))
        val inntektSB = null

        val grunnlag = GrunnlagBeregning(utgift, inntektBP, inntektBM, inntektSB, sjablonPeriodeListe)

        val resultat = bPsAndelSærbidragBeregning.beregn(grunnlag)

        assertAll(
            { assertThat(resultat.resultatAndelFaktor).isGreaterThan(BigDecimal.ZERO) },
            { assertThat(resultat.resultatAndelBeløp).isGreaterThan(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsørget).isFalse() },
        )
    }

    @DisplayName("Beregning med ekstremt høy inntekt for BP")
    @Test
    fun beregningMedEkstremtHøyInntektForBP() {
        val utgift = Utgift(referanse = TestUtil.UTGIFT_REFERANSE, beløp = BigDecimal.valueOf(1000))
        val inntektBP = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(10000000))
        val inntektBM = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(300000))
        val inntektSB = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(50000))

        val grunnlag = GrunnlagBeregning(utgift, inntektBP, inntektBM, inntektSB, sjablonPeriodeListe)

        val resultat = bPsAndelSærbidragBeregning.beregn(grunnlag)

        assertAll(
            { assertThat(resultat.resultatAndelFaktor).isEqualTo(BigDecimal.valueOf(0.833333333333)) },
            { assertThat(resultat.resultatAndelBeløp).isGreaterThan(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsørget).isFalse() },
        )
    }

    @DisplayName("Beregning når barnet er selvforsørget")
    @Test
    fun beregningNårBarnetErSelvforsørget() {
        val utgift = Utgift(referanse = TestUtil.UTGIFT_REFERANSE, beløp = BigDecimal.valueOf(1000))
        val inntektBP = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(300000))
        val inntektBM = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(200000))
        val inntektSB = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(5000000))

        val grunnlag = GrunnlagBeregning(utgift, inntektBP, inntektBM, inntektSB, sjablonPeriodeListe)

        val resultat = bPsAndelSærbidragBeregning.beregn(grunnlag)

        assertAll(
            { assertThat(resultat.resultatAndelFaktor).isEqualTo(BigDecimal.ZERO) },
            { assertThat(resultat.resultatAndelBeløp).isEqualTo(BigDecimal.ZERO) },
            { assertThat(resultat.barnetErSelvforsørget).isTrue() },
        )
    }
}
