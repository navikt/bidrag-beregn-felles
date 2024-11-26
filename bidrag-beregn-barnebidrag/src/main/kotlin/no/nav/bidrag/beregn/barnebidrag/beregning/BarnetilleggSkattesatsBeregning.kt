package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

internal object BarnetilleggSkattesatsBeregning {

    val bigDecimal100 = BigDecimal.valueOf(100)
    var sjablonverdiTrygdeavgiftProsent = BigDecimal.ZERO
    var sjablonverdiMinstefradragInntektBeløp = BigDecimal.ZERO
    var sjablonverdiMinstefradragInntektProsent = BigDecimal.ZERO
    var sjablonverdiPersonfradragKlasse1Beløp = BigDecimal.ZERO
    var sjablonverdiSkattesatsAlminneligInntektProsent = BigDecimal.ZERO

    fun beregn(grunnlag: BarnetilleggSkattesatsBeregningGrunnlag): BarnetilleggSkattesatsBeregningResultat {
        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val sumInntekt = grunnlag.inntektBeregningGrunnlag.sumInntekt

        val minstefradrag = (sumInntekt * (sjablonverdiMinstefradragInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP)))
            .min(sjablonverdiMinstefradragInntektBeløp)

        val skattAlminneligInntekt = (
            (sumInntekt - minstefradrag - sjablonverdiPersonfradragKlasse1Beløp) *
                (sjablonverdiSkattesatsAlminneligInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))
            )
            .coerceAtLeast(BigDecimal.ZERO)

        val trygdeavgift = sumInntekt * (sjablonverdiTrygdeavgiftProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))

        val trinnskatt = beregnTrinnskatt(grunnlag = grunnlag, inntekt = sumInntekt)

        val sumSkatt = skattAlminneligInntekt + trygdeavgift + trinnskatt

        val sumSkattFaktor =
            if (sumInntekt.avrundetMedNullDesimaler == BigDecimal.ZERO) BigDecimal.ZERO else sumSkatt.divide(sumInntekt, 10, RoundingMode.HALF_UP)

        return BarnetilleggSkattesatsBeregningResultat(
            skattFaktor = sumSkattFaktor.avrundetMedTiDesimaler,
            grunnlagsreferanseListe = listOf(
                grunnlag.inntektBeregningGrunnlag.referanse,
                grunnlag.sjablonTrinnvisSkattesatsBeregningGrunnlag.referanse,
            ) +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse },
        )
    }

    // Beregner og akkumulerer trinnskatt. Regelen er at det skal betales skatt for hvert trinn inntekten overstiger. Beløpet det skal betales skatt
    // for på hvert trinn er minsteverdien av inntekten og neste inntektsgrense, fratrukket inntektsgrensen på det aktuelle trinnet. Dette beløpet
    // multipliseres med satsen på det aktuelle trinnet. Hvis inntekten er lavere enn laveste inntektsgrense skal det ikke betales trinnskatt.
    private fun beregnTrinnskatt(grunnlag: BarnetilleggSkattesatsBeregningGrunnlag, inntekt: BigDecimal): BigDecimal {
        val sortertTrinnListe = grunnlag.sjablonTrinnvisSkattesatsBeregningGrunnlag.trinnliste.sortedBy { it.inntektsgrense }
        var trinnskatt = BigDecimal.ZERO

        sortertTrinnListe.forEachIndexed { index, trinn ->
            val nesteGrense = sortertTrinnListe.getOrNull(index + 1)?.inntektsgrense?.toBigDecimal() ?: inntekt
            if (inntekt > trinn.inntektsgrense.toBigDecimal()) {
                val inntektsgrense = minOf(inntekt, nesteGrense) - trinn.inntektsgrense.toBigDecimal()
                trinnskatt += (inntektsgrense * trinn.sats).divide(bigDecimal100, 10, RoundingMode.HALF_UP)
            }
        }

        return trinnskatt
    }

    private fun hentSjablonverdier(grunnlag: BarnetilleggSkattesatsBeregningGrunnlag) {
        sjablonverdiTrygdeavgiftProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        sjablonverdiMinstefradragInntektBeløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        sjablonverdiMinstefradragInntektProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        sjablonverdiPersonfradragKlasse1Beløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        sjablonverdiSkattesatsAlminneligInntektProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
    }
}
