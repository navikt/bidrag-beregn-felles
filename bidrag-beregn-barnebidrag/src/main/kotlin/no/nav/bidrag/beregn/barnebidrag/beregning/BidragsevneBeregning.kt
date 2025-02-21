package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

internal object BidragsevneBeregning {

    private val bigDecimal100 = BigDecimal.valueOf(100)
    private val bigDecimal12 = BigDecimal.valueOf(12)
    private val bigDecimal025 = BigDecimal.valueOf(0.25).setScale(2)
    private var sjablonverdiTrygdeavgiftProsent = BigDecimal.ZERO
    private var sjablonverdiUnderholdEgneBarnIHusstandBeløp = BigDecimal.ZERO
    private var sjablonverdiMinstefradragInntektBeløp = BigDecimal.ZERO
    private var sjablonverdiMinstefradragInntektProsent = BigDecimal.ZERO
    private var sjablonverdiPersonfradragKlasse1Beløp = BigDecimal.ZERO
    private var sjablonverdiSkattesatsAlminneligInntektProsent = BigDecimal.ZERO
    private var sjablonverdiBoutgiftBeløp = BigDecimal.ZERO
    private var sjablonverdiEgetUnderholdBeløp = BigDecimal.ZERO

    fun beregn(grunnlag: BidragsevneBeregningGrunnlag): BidragsevneBeregningResultat {
        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val sumInntekt = grunnlag.inntektBPBeregningGrunnlag.sumInntekt

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

        val boutgift = sjablonverdiBoutgiftBeløp * bigDecimal12

        val egetUnderhold = sjablonverdiEgetUnderholdBeløp * bigDecimal12

        val underholdBarnEgenHusstand = sjablonverdiUnderholdEgneBarnIHusstandBeløp * bigDecimal12 *
            grunnlag.barnIHusstandenBeregningGrunnlag.antallBarn.toBigDecimal()

        val sumInntekt25Prosent = (sumInntekt * bigDecimal025).divide(bigDecimal12, 10, RoundingMode.HALF_UP)

        // Kalkulerer månedlig bidragsevne
        val bidragsevne = (sumInntekt - sumSkatt - boutgift - egetUnderhold - underholdBarnEgenHusstand)
            .divide(bigDecimal12, 10, RoundingMode.HALF_UP)
            .coerceAtLeast(BigDecimal.ZERO)

        return BidragsevneBeregningResultat(
            bidragsevne = bidragsevne.avrundetMedToDesimaler,
            minstefradrag = minstefradrag.avrundetMedToDesimaler,
            skattAlminneligInntekt = skattAlminneligInntekt.avrundetMedToDesimaler,
            trygdeavgift = trygdeavgift.avrundetMedToDesimaler,
            trinnskatt = trinnskatt.avrundetMedToDesimaler,
            sumSkatt = sumSkatt.avrundetMedToDesimaler,
            sumSkattFaktor = sumSkattFaktor.avrundetMedTiDesimaler,
            underholdBarnEgenHusstand = underholdBarnEgenHusstand.avrundetMedToDesimaler,
            sumInntekt25Prosent = sumInntekt25Prosent.avrundetMedToDesimaler,
            grunnlagsreferanseListe = listOf(
                grunnlag.inntektBPBeregningGrunnlag.referanse,
                grunnlag.barnIHusstandenBeregningGrunnlag.referanse,
                grunnlag.voksneIHusstandenBeregningGrunnlag.referanse,
                grunnlag.sjablonTrinnvisSkattesatsBeregningGrunnlag.referanse,
            ) +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse } +
                grunnlag.sjablonBidragsevneBeregningGrunnlagListe.map { it.referanse },
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
        sjablonverdiTrygdeavgiftProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        sjablonverdiUnderholdEgneBarnIHusstandBeløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn }
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

        sjablonverdiBoutgiftBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.boutgift }
            .firstOrNull() ?: BigDecimal.ZERO
        sjablonverdiEgetUnderholdBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.underhold }
            .firstOrNull() ?: BigDecimal.ZERO
    }
}
