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

    fun beregn(grunnlag: BidragsevneBeregningGrunnlag): BidragsevneBeregningResultat {
        // Henter sjablonverdier
        val sjablonverdier = hentSjablonverdier(grunnlag)

        val sumInntekt = grunnlag.inntektBPBeregningGrunnlag.sumInntekt

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

        val boutgift = sjablonverdier.boutgiftBeløp * bigDecimal12

        val egetUnderhold = sjablonverdier.egetUnderholdBeløp * bigDecimal12

        val underholdBarnEgenHusstand = sjablonverdier.underholdEgneBarnIHusstandBeløp * bigDecimal12 *
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

    private fun hentSjablonverdier(grunnlag: BidragsevneBeregningGrunnlag): Sjablonverdier {
        val trygdeavgiftProsent = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
        val underholdEgneBarnIHusstandBeløp = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn }
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

        val boutgiftBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.boutgift }
            .firstOrNull() ?: BigDecimal.ZERO
        val egetUnderholdBeløp = grunnlag.sjablonBidragsevneBeregningGrunnlagListe
            .map { it.underhold }
            .firstOrNull() ?: BigDecimal.ZERO

        return Sjablonverdier(
            trygdeavgiftProsent = trygdeavgiftProsent,
            underholdEgneBarnIHusstandBeløp = underholdEgneBarnIHusstandBeløp,
            minstefradragInntektBeløp = minstefradragInntektBeløp,
            minstefradragInntektProsent = minstefradragInntektProsent,
            personfradragKlasse1Beløp = personfradragKlasse1Beløp,
            skattesatsAlminneligInntektProsent = skattesatsAlminneligInntektProsent,
            boutgiftBeløp = boutgiftBeløp,
            egetUnderholdBeløp = egetUnderholdBeløp,
        )
    }

    private data class Sjablonverdier(
        val trygdeavgiftProsent: BigDecimal,
        val underholdEgneBarnIHusstandBeløp: BigDecimal,
        val minstefradragInntektBeløp: BigDecimal,
        val minstefradragInntektProsent: BigDecimal,
        val personfradragKlasse1Beløp: BigDecimal,
        val skattesatsAlminneligInntektProsent: BigDecimal,
        val boutgiftBeløp: BigDecimal,
        val egetUnderholdBeløp: BigDecimal,
    )
}
