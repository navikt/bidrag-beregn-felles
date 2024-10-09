package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.math.RoundingMode

internal object BidragsevneBeregning {

    val bigDecimal100 = BigDecimal.valueOf(100)
    val bigDecimal12 = BigDecimal.valueOf(12)
    var sjablonverdiTrygdeavgiftProsent = BigDecimal.ZERO
    var sjablonverdiUnderholdEgneBarnIHusstandBeløp = BigDecimal.ZERO
    var sjablonverdiMinstefradragInntektBeløp = BigDecimal.ZERO
    var sjablonverdiMinstefradragInntektProsent = BigDecimal.ZERO
    var sjablonverdiPersonfradragKlasse1Beløp = BigDecimal.ZERO
    var sjablonverdiSkattesatsAlminneligInntektProsent = BigDecimal.ZERO
    var sjablonverdiBoutgiftBeløp = BigDecimal.ZERO
    var sjablonverdiEgetUnderholdBeløp = BigDecimal.ZERO

    fun beregn(grunnlag: BidragsevneBeregningGrunnlag): BidragsevneBeregningResultat {

        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val sumInntekt = grunnlag.inntektBPBeregningGrunnlag.sumInntekt

        val minstefradrag = minOf(
            sumInntekt.multiply(sjablonverdiMinstefradragInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP)),
            sjablonverdiMinstefradragInntektBeløp
        )

        val skattAlminneligInntekt = (sumInntekt.subtract(minstefradrag).subtract(sjablonverdiPersonfradragKlasse1Beløp))
            .multiply(sjablonverdiSkattesatsAlminneligInntektProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))
            .max(BigDecimal.ZERO)

        val trygdeavgift = sumInntekt.multiply(sjablonverdiTrygdeavgiftProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP))

        val trinnskatt = beregnTrinnskatt(grunnlag = grunnlag, inntekt = sumInntekt)

        val sumSkatt = skattAlminneligInntekt.add(trygdeavgift).add(trinnskatt)

        val boutgift = sjablonverdiBoutgiftBeløp.multiply(bigDecimal12)

        val egetUnderhold = sjablonverdiEgetUnderholdBeløp.multiply(bigDecimal12)

        val underholdBarnEgenHusstand = sjablonverdiUnderholdEgneBarnIHusstandBeløp
            .multiply(bigDecimal12)
            .multiply(grunnlag.barnIHusstandenBeregningGrunnlag.antallBarn.toBigDecimal())

        // Kalkulerer månedlig bidragsevne
        val bidragsevne = sumInntekt
            .subtract(sumSkatt)
            .subtract(boutgift)
            .subtract(egetUnderhold)
            .subtract(underholdBarnEgenHusstand)
            .divide(bigDecimal12, 10, RoundingMode.HALF_UP)
            .max(BigDecimal.ZERO)

        return BidragsevneBeregningResultat(
            bidragsevne = bidragsevne.setScale(0, RoundingMode.HALF_UP),
            minstefradrag = minstefradrag.setScale(0, RoundingMode.HALF_UP),
            skattAlminneligInntekt = skattAlminneligInntekt.setScale(0, RoundingMode.HALF_UP),
            trygdeavgift = trygdeavgift.setScale(0, RoundingMode.HALF_UP),
            trinnskatt = trinnskatt.setScale(0, RoundingMode.HALF_UP),
            sumSkatt = sumSkatt.setScale(0, RoundingMode.HALF_UP),
            underholdBarnEgenHusstand = underholdBarnEgenHusstand.setScale(0, RoundingMode.HALF_UP),
            grunnlagsreferanseListe = listOf(
                grunnlag.inntektBPBeregningGrunnlag.referanse,
                grunnlag.barnIHusstandenBeregningGrunnlag.referanse,
                grunnlag.voksneIHusstandenBeregningGrunnlag.referanse,
                grunnlag.sjablonTrinnvisSkattesatsBeregningGrunnlag.referanse,
            ) +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse } +
                grunnlag.sjablonBidragsevneBeregningGrunnlagListe.map { it.referanse }
        )
    }

    // Beregner og akkumulerer trinnskatt. Regelen er at det skal betales skatt for hvert trinn inntekten overstiger. Beløpet det skal betales skatt
    // for på hvert trinn er minsteverdien av inntekten og neste inntektsgrense, fratrukket inntektsgrensen på det aktuelle trinnet. Dette beløpet
    // multipliseres med satsen på det aktuelle trinnet. Hvis inntekten er lavere enn laveste inntektsgrense skal det ikke betales trinnskatt.
    private fun beregnTrinnskatt(grunnlag: BidragsevneBeregningGrunnlag, inntekt: BigDecimal): BigDecimal {

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

    private fun hentSjablonverdier(grunnlag: BidragsevneBeregningGrunnlag) {
        sjablonverdiTrygdeavgiftProsent = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()
        sjablonverdiUnderholdEgneBarnIHusstandBeløp = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()
        sjablonverdiMinstefradragInntektBeløp = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()
        sjablonverdiMinstefradragInntektProsent = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()
        sjablonverdiPersonfradragKlasse1Beløp = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()
        sjablonverdiSkattesatsAlminneligInntektProsent = (grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0)
            .toBigDecimal()

        sjablonverdiBoutgiftBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.boutgift }
            .firstOrNull() ?: BigDecimal.ZERO
        sjablonverdiEgetUnderholdBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.underhold }
            .firstOrNull() ?: BigDecimal.ZERO
    }
}
