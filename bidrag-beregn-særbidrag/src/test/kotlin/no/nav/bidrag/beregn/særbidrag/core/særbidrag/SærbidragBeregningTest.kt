package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBp
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.SumLøpendeBidragPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class SærbidragBeregningTest {

    private val særbidragBeregning = SærbidragBeregning()

    @DisplayName("Full evne, ingenting betalt av BP")
    @Test
    fun testFullEvneIngentingBetaltAvBP() {
        val beløpBetaltAvBP = BigDecimal.ZERO
        val bPsAndelBeløp = BigDecimal.valueOf(4000)
        val bidragsevneBeløp = BigDecimal.valueOf(11000)
        val sumLøpendeBidrag = BigDecimal.valueOf(10000)

        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = beløpBetaltAvBP),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = bidragsevneBeløp),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelFaktor = BigDecimal.valueOf(0.606),
                andelBeløp = bPsAndelBeløp,
                barnetErSelvforsørget = false,
            ),
            sumLøpendeBidrag = SumLøpendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                periode = Periode(LocalDate.now(), LocalDate.now()),
                sumLøpendeBidrag = sumLøpendeBidrag,
            ),
        )

        val (beregnetBeløp, resultatKode, resultatBeløp) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertThat(beregnetBeløp).isEqualTo(bPsAndelBeløp) },
            { assertThat(resultatKode).isEqualTo(Resultatkode.SÆRBIDRAG_INNVILGET) },
            { assertThat(resultatBeløp).isEqualTo(bPsAndelBeløp) },
        )
    }

    @DisplayName("Full evne, BP har betalt deler av BPs andel")
    @Test
    fun testFullEvneNoeBetaltAvBP() {
        val beløpBetaltAvBP = BigDecimal.valueOf(2000)
        val bPsAndelBeløp = BigDecimal.valueOf(4000)
        val bidragsevneBeløp = BigDecimal.valueOf(11000)
        val sumLøpendeBidrag = BigDecimal.valueOf(10000)

        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = beløpBetaltAvBP),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = bidragsevneBeløp),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelFaktor = BigDecimal.valueOf(0.606),
                andelBeløp = bPsAndelBeløp,
                barnetErSelvforsørget = false,
            ),
            sumLøpendeBidrag = SumLøpendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                periode = Periode(LocalDate.now(), LocalDate.now()),
                sumLøpendeBidrag = sumLøpendeBidrag,
            ),
        )

        val (beregnetBeløp, resultatKode, resultatBeløp) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertThat(beregnetBeløp).isEqualTo(bPsAndelBeløp) },
            { assertThat(resultatKode).isEqualTo(Resultatkode.SÆRBIDRAG_INNVILGET) },
            { assertThat(resultatBeløp).isEqualTo(bPsAndelBeløp) },
        )
    }

    @DisplayName("Full evne, BP har betalt mer enn BPs andel")
    @Test
    fun testFullEvneForMyeBetaltAvBP() {
        val beløpBetaltAvBP = BigDecimal.valueOf(5000)
        val bPsAndelBeløp = BigDecimal.valueOf(4000)
        val bidragsevneBeløp = BigDecimal.valueOf(11000)
        val sumLøpendeBidrag = BigDecimal.valueOf(10000)

        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = beløpBetaltAvBP),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = bidragsevneBeløp),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelFaktor = BigDecimal.valueOf(0.606),
                andelBeløp = bPsAndelBeløp,
                barnetErSelvforsørget = false,
            ),
            sumLøpendeBidrag = SumLøpendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                periode = Periode(LocalDate.now(), LocalDate.now()),
                sumLøpendeBidrag = sumLøpendeBidrag,
            ),
        )

        val (beregnetBeløp, resultatKode, resultatBeløp) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertThat(beregnetBeløp).isEqualTo(bPsAndelBeløp) },
            { assertThat(resultatKode).isEqualTo(Resultatkode.SÆRBIDRAG_INNVILGET) },
            { assertThat(resultatBeløp).isEqualTo(maxOf(BigDecimal.ZERO, bPsAndelBeløp)) },
        )
    }

    @DisplayName("Evne er lavere enn BPs andel")
    @Test
    fun testEvneErLavereEnnBPsAndel() {
        val beløpBetaltAvBP = BigDecimal.valueOf(5000)
        val bPsAndelBeløp = BigDecimal.valueOf(4000)
        val bidragsevneBeløp = BigDecimal.valueOf(2000)
        val sumLøpendeBidrag = BigDecimal.valueOf(10000)

        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = beløpBetaltAvBP),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = bidragsevneBeløp),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelFaktor = BigDecimal.valueOf(0.606),
                andelBeløp = bPsAndelBeløp,
                barnetErSelvforsørget = true,
            ),
            sumLøpendeBidrag = SumLøpendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                periode = Periode(LocalDate.now(), LocalDate.now()),
                sumLøpendeBidrag = sumLøpendeBidrag,
            ),
        )

        val (beregnetBeløp, resultatKode, resultatBeløp) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertThat(beregnetBeløp).isEqualTo(bPsAndelBeløp) },
            { assertThat(resultatKode).isEqualTo(Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE) },
            { assertThat(resultatBeløp).isNull() },
        )
    }

    @DisplayName("Barnet er selvforsørget")
    @Test
    fun testBarnetErSelvforsørget() {
        val beløpBetaltAvBP = BigDecimal.valueOf(5000)
        val bPsAndelBeløp = BigDecimal.valueOf(4000)
        val bidragsevneBeløp = BigDecimal.valueOf(11000)
        val sumLøpendeBidrag = BigDecimal.valueOf(10000)

        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = beløpBetaltAvBP),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = bidragsevneBeløp),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelFaktor = BigDecimal.valueOf(0.606),
                andelBeløp = bPsAndelBeløp,
                barnetErSelvforsørget = true,
            ),
            sumLøpendeBidrag = SumLøpendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                periode = Periode(LocalDate.now(), LocalDate.now()),
                sumLøpendeBidrag = sumLøpendeBidrag,
            ),
        )

        val (beregnetBeløp, resultatKode, resultatBeløp) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertThat(beregnetBeløp).isEqualTo(bPsAndelBeløp) },
            { assertThat(resultatKode).isEqualTo(Resultatkode.BARNET_ER_SELVFORSØRGET) },
            { assertThat(resultatBeløp).isNull() },
        )
    }
}
