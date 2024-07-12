package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBp
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SærbidragBeregningTest {

    private val særbidragBeregning = SærbidragBeregning()

    @DisplayName("Full evne")
    @Test
    fun testFullEvne() {
        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = BigDecimal.ZERO),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = BigDecimal.valueOf(11069)),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelProsent = BigDecimal.valueOf(60.6),
                andelBeløp = BigDecimal.valueOf(4242),
                barnetErSelvforsørget = false,
            ),
        )

        val (resultatBeløp, resultatkode) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertEquals(4242.0, resultatBeløp.toDouble()) },
            { assertEquals(Resultatkode.SÆRBIDRAG_INNVILGET, resultatkode) },
        )
    }

    @DisplayName("Evne er lavere enn BPs andel")
    @Test
    fun testEvneErLavereEnnBPsAndel() {
        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = BigDecimal.ZERO),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = BigDecimal.valueOf(3000)),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelProsent = BigDecimal.valueOf(60.6),
                andelBeløp = BigDecimal.valueOf(4242),
                barnetErSelvforsørget = true,
            ),
        )

        val (resultatBeløp, resultatkode) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertEquals(0.0, resultatBeløp.toDouble()) },
            { assertEquals(Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE, resultatkode) },
        )
    }

    @DisplayName("Barnet er selvforsørget")
    @Test
    fun testBarnetErSelvforsørget() {
        val grunnlagBeregningPeriodisert = GrunnlagBeregning(
            betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = BigDecimal.ZERO),
            bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = BigDecimal.valueOf(10000)),
            bPsAndelSærbidrag = BPsAndelSærbidrag(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                andelProsent = BigDecimal.valueOf(60.6),
                andelBeløp = BigDecimal.valueOf(4242),
                barnetErSelvforsørget = true,
            ),
        )

        val (resultatBeløp, resultatkode) = særbidragBeregning.beregn(grunnlagBeregningPeriodisert)

        assertAll(
            { assertEquals(0.0, resultatBeløp.toDouble()) },
            { assertEquals(Resultatkode.BARNET_ER_SELVFORSØRGET, resultatkode) },
        )
    }
}
