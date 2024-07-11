package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.AntallBarnIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.Inntekt
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

internal class BidragsevneBeregningTest {

    private val sjablonListe = TestUtil.byggSjabloner()
    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val bidragsevneberegning = BidragsevneBeregning()

    @ParameterizedTest
    @CsvSource(
        // Test på beregning med ulike inntekter
        "1000000, 1.0, false, 31859",
        "520000, 1.0, false, 8322",
        "666000, 3.0, false, 8424",
        "480000, 0.0, false, 9976",
        // Test på at beregnet bidragsevne blir satt til 0 når evne er negativ
        "100000, 3.0, true, 0",
    )
    fun testBidragsevneBeregningStandardSjabloner(inntektBelop: BigDecimal, antallBarn: Double, borMedAndre: Boolean, expectedResult: Int) {
        val inntekter = listOf(Inntekt(TestUtil.INNTEKT_REFERANSE, "LONN_SKE", inntektBelop))
        val grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre),
            sjablonListe = sjablonPeriodeListe,
        )
        val result = bidragsevneberegning.beregn(grunnlagBeregning).beløp
        assertEquals(expectedResult.toBigDecimal(), result)
    }

    @Test
    fun beregnMinstefradrag() {
        val inntekter = mutableListOf<Inntekt>()

        inntekter.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(200000),
            ),
        )
        var grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )

        assertThat(
            bidragsevneberegning.beregnMinstefradrag(
                inntekt = grunnlagBeregning.inntektListe.sumOf { it.inntektBeløp },
                minstefradragInntektSjablonBeløp = SjablonUtil.hentSjablonverdi(
                    sjablonListe = sjablonListe,
                    sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP,
                ),
                minstefradragInntektSjablonProsent = SjablonUtil.hentSjablonverdi(
                    sjablonListe = sjablonListe,
                    sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT,
                ),
            ),
        )
            .isEqualTo(BigDecimal.valueOf(62000))

        inntekter[0] = Inntekt(
            referanse = TestUtil.INNTEKT_REFERANSE,
            inntektType = "LONN_SKE",
            inntektBeløp = BigDecimal.valueOf(1000000),
        )
        grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )

        assertThat(
            bidragsevneberegning.beregnMinstefradrag(
                inntekt = grunnlagBeregning.inntektListe.sumOf { it.inntektBeløp },
                minstefradragInntektSjablonBeløp = SjablonUtil.hentSjablonverdi(
                    sjablonListe = sjablonListe,
                    sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP,
                ),
                minstefradragInntektSjablonProsent = SjablonUtil.hentSjablonverdi(
                    sjablonListe = sjablonListe,
                    sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT,
                ),
            ),
        )
            .isEqualTo(BigDecimal.valueOf(87450))
    }

    @Test
    fun beregnSkattetrinnBeløp() {
        val inntekter = mutableListOf<Inntekt>()

        inntekter.add(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBeløp = BigDecimal.valueOf(666000),
            ),
        )
        var grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        assertEquals(
            BigDecimal.valueOf((1400 + 16181 + 3465 + 0).toLong()),
            bidragsevneberegning.beregnSkattetrinnBeløp(grunnlagBeregning, grunnlagBeregning.inntektListe.sumOf { it.inntektBeløp }),
        )

        inntekter[0] = Inntekt(
            referanse = TestUtil.INNTEKT_REFERANSE,
            inntektType = "LONN_SKE",
            inntektBeløp = BigDecimal.valueOf(174600),
        )
        grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        assertEquals(
            BigDecimal.ZERO,
            bidragsevneberegning.beregnSkattetrinnBeløp(grunnlagBeregning, grunnlagBeregning.inntektListe.sumOf { it.inntektBeløp }),
        )

        inntekter[0] = Inntekt(
            referanse = TestUtil.INNTEKT_REFERANSE,
            inntektType = "LONN_SKE",
            inntektBeløp = BigDecimal.valueOf(250000),
        )
        grunnlagBeregning = GrunnlagBeregning(
            inntektListe = inntekter,
            antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
            sjablonListe = sjablonPeriodeListe,
        )
        assertEquals(
            BigDecimal.valueOf(1315),
            bidragsevneberegning.beregnSkattetrinnBeløp(grunnlagBeregning, grunnlagBeregning.inntektListe.sumOf { it.inntektBeløp }),
        )
    }
}
