package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.AntallBarnIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.Inntekt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BidragsevneBeregningTest {

    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val bidragsevneberegning = BidragsevneBeregning()

    @DisplayName("Beregning med standard inntekt og utgifter gir positiv bidragsevne")
    @Test
    fun beregningMedStandardInntektOgUtgifterGirPositivBidragsevne() {
        val grunnlag = GrunnlagBeregning(
            inntekt = Inntekt(referanse = "inntektRef", inntektBeløp = BigDecimal.valueOf(500000)),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "antallBarnRef", antallBarn = 2.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "bostatusRef", borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        val resultat = bidragsevneberegning.beregn(grunnlag)

        assertThat(resultat.beløp).isGreaterThan(BigDecimal.ZERO)
    }

    @DisplayName("Beregning med høy inntekt og ingen utgifter gir høy bidragsevne")
    @Test
    fun beregningMedHoyInntektOgIngenUtgifterGirHoyBidragsevne() {
        val grunnlag = GrunnlagBeregning(
            inntekt = Inntekt(referanse = "inntektRef", inntektBeløp = BigDecimal.valueOf(1000000)),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "antallBarnRef", antallBarn = 0.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "bostatusRef", borMedAndre = true),
            sjablonListe = sjablonPeriodeListe,
        )
        val resultat = bidragsevneberegning.beregn(grunnlag)

        assertThat(resultat.beløp).isGreaterThan(BigDecimal.valueOf(40000))
    }

    @DisplayName("Beregning med negativ inntekt gir bidragsevne lik null")
    @Test
    fun beregningMedNegativInntektGirBidragsevneLikNull() {
        val grunnlag = GrunnlagBeregning(
            inntekt = Inntekt(referanse = "inntektRef", inntektBeløp = BigDecimal.valueOf(-100000)),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "antallBarnRef", antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "bostatusRef", borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        val resultat = bidragsevneberegning.beregn(grunnlag)

        assertThat(resultat.beløp).isEqualTo(BigDecimal.valueOf(0.00).setScale(2))
    }

    @DisplayName("Beregning med inntekt under skattefri grense gir redusert bidragsevne")
    @Test
    fun beregningMedInntektUnderSkattefriGrenseGirRedusertBidragsevne() {
        val grunnlag = GrunnlagBeregning(
            inntekt = Inntekt(referanse = "inntektRef", inntektBeløp = BigDecimal.valueOf(50000)),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "antallBarnRef", antallBarn = 2.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "bostatusRef", borMedAndre = true),
            sjablonListe = sjablonPeriodeListe,
        )
        val resultat = bidragsevneberegning.beregn(grunnlag)

        assertThat(resultat.beløp).isLessThan(BigDecimal.valueOf(1000))
    }

    @DisplayName("Beregning med flere barn i husstanden øker utgifter og reduserer bidragsevne")
    @Test
    fun beregningMedFlereBarnIHusstandenOkerUtgifterOgRedusererBidragsevne() {
        val grunnlag = GrunnlagBeregning(
            inntekt = Inntekt(referanse = "inntektRef", inntektBeløp = BigDecimal.valueOf(600000)),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "antallBarnRef", antallBarn = 3.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "bostatusRef", borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        val resultat = bidragsevneberegning.beregn(grunnlag)

        assertThat(resultat.beløp).isLessThan(BigDecimal.valueOf(10000))
    }
}
