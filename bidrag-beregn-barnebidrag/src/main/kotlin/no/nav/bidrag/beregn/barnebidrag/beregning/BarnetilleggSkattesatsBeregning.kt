package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

internal object BarnetilleggSkattesatsBeregning {

    private val bigDecimal100 = BigDecimal.valueOf(100)

    fun beregn(grunnlag: BarnetilleggSkattesatsBeregningGrunnlag): BarnetilleggSkattesatsBeregningResultat {
        // Henter sjablonverdier
        val sjablonverdier = hentSjablonverdier(grunnlag)

        val sumInntekt = grunnlag.inntektBeregningGrunnlag.sumInntekt

        val minstefradrag = (sumInntekt * (sjablonverdier.minstefradragInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP)))
            .min(sjablonverdier.minstefradragInntektBeløp)

        val skattAlminneligInntekt = (
            (sumInntekt - minstefradrag - sjablonverdier.personfradragKlasse1Beløp) *
                (sjablonverdier.skattesatsAlminneligInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))
            )
            .coerceAtLeast(BigDecimal.ZERO)

        val trygdeavgift = sumInntekt * (sjablonverdier.trygdeavgiftProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))

        val trinnskatt = beregnTrinnskatt(grunnlag = grunnlag, inntekt = sumInntekt)

        val sumSkatt = skattAlminneligInntekt + trygdeavgift + trinnskatt

        val sumSkattFaktor =
            if (sumInntekt.avrundetMedNullDesimaler == BigDecimal.ZERO) BigDecimal.ZERO else sumSkatt.divide(sumInntekt, 10, RoundingMode.HALF_UP)

        return BarnetilleggSkattesatsBeregningResultat(
            skattFaktor = sumSkattFaktor.avrundetMedTiDesimaler,
            minstefradrag = minstefradrag.avrundetMedToDesimaler,
            skattAlminneligInntekt = skattAlminneligInntekt.avrundetMedToDesimaler,
            trygdeavgift = trygdeavgift.avrundetMedToDesimaler,
            trinnskatt = trinnskatt.avrundetMedToDesimaler,
            sumSkatt = sumSkatt.avrundetMedToDesimaler,
            sumInntekt = sumInntekt.avrundetMedToDesimaler,
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

    private fun hentSjablonverdier(grunnlag: BarnetilleggSkattesatsBeregningGrunnlag): Sjablonverdier {
        val trygdeavgiftProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        val minstefradragInntektBeløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        val minstefradragInntektProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        val personfradragKlasse1Beløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        val skattesatsAlminneligInntektProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()

        return Sjablonverdier(
            trygdeavgiftProsent = trygdeavgiftProsent,
            minstefradragInntektBeløp = minstefradragInntektBeløp,
            minstefradragInntektProsent = minstefradragInntektProsent,
            personfradragKlasse1Beløp = personfradragKlasse1Beløp,
            skattesatsAlminneligInntektProsent = skattesatsAlminneligInntektProsent,
        )
    }

    private data class Sjablonverdier(
        val trygdeavgiftProsent: BigDecimal,
        val minstefradragInntektBeløp: BigDecimal,
        val minstefradragInntektProsent: BigDecimal,
        val personfradragKlasse1Beløp: BigDecimal,
        val skattesatsAlminneligInntektProsent: BigDecimal,
    )
}
