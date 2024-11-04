package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningResultat
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import java.math.BigDecimal
import java.math.RoundingMode

internal object EndeligBidragBeregning {

    fun beregn(grunnlag: EndeligBidragBeregningGrunnlag): EndeligBidragBeregningResultat {
        // Hvis barnet er selvforsørget gjøres det ingen videre beregning (standardverdier benyttes for alt bortsett fra resultatkode og referanser)
        if (grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.barnetErSelvforsørget) {
            return EndeligBidragBeregningResultat(
                resultatKode = Resultatkode.BARNET_ER_SELVFORSØRGET,
                grunnlagsreferanseListe = listOf(grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse),
            )
        }

        // Justerer BP's andel hvis det er delt bosted
        val bpAndelJustert = justerBpAndel(grunnlag)

        // Beregner netto barnetillegg for BP og BM
        val nettoBarnetilleggBP = beregnNettoBarnetillegg(grunnlag.barnetilleggBPBeregningGrunnlag)
        val nettoBarnetilleggBM = beregnNettoBarnetillegg(grunnlag.barnetilleggBMBeregningGrunnlag)

        // Finner maksverdi av bidragsevne og 25% av inntekt
        val maksBidragsbeløp = minOf(grunnlag.bidragsevneBeregningGrunnlag.beløp, grunnlag.bidragsevneBeregningGrunnlag.tjuefemProsentInntekt)
        val bidragRedusertAvBidragsevne = grunnlag.bidragsevneBeregningGrunnlag.beløp <= grunnlag.bidragsevneBeregningGrunnlag.tjuefemProsentInntekt

        // Beregner kostnadsberegnet bidrag og bruker det som utgangspunkt for videre beregning
        var resultatkode = Resultatkode.KOSTNADSBEREGNET_BIDRAG
        val kostnadsberegnetBidrag = (bpAndelJustert.beløp - grunnlag.samværsfradragBeregningGrunnlag.beløp).coerceAtLeast(BigDecimal.ZERO)
        var foreløpigBarnebidrag = kostnadsberegnetBidrag

        // Initierer indikatorer
        var justertNedTilEvne = false
        var justertNedTil25ProsentAvInntekt = false
        var justertForNettoBarnetilleggBP = false
        var justertForNettoBarnetilleggBM = false

        // Sjekker om BP's andel av U er større enn bidragsevne eller 25% av inntekt
        if (maksBidragsbeløp < bpAndelJustert.beløp) {
            foreløpigBarnebidrag = maksBidragsbeløp - grunnlag.samværsfradragBeregningGrunnlag.beløp
            if (bidragRedusertAvBidragsevne) {
                resultatkode = Resultatkode.BIDRAG_REDUSERT_AV_EVNE
                justertNedTilEvne = true
            } else {
                resultatkode = Resultatkode.BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT
                justertNedTil25ProsentAvInntekt = true
            }
        }

        // Sjekker om eventuelt barnetillegg for BM skal benyttes
        if (foreløpigBarnebidrag > (grunnlag.underholdskostnadBeregningGrunnlag.beløp - nettoBarnetilleggBM)) {
            foreløpigBarnebidrag =
                grunnlag.underholdskostnadBeregningGrunnlag.beløp - nettoBarnetilleggBM - grunnlag.samværsfradragBeregningGrunnlag.beløp
            resultatkode = Resultatkode.BIDRAG_SATT_TIL_UNDERHOLDSKOSTNAD_MINUS_BARNETILLEGG_BM
            justertForNettoBarnetilleggBM = true
        }

        // Sjekker om eventuelt barnetillegg for BP skal benyttes
        if ((nettoBarnetilleggBP > BigDecimal.ZERO) && (foreløpigBarnebidrag < nettoBarnetilleggBP)) {
            foreløpigBarnebidrag = nettoBarnetilleggBP - grunnlag.samværsfradragBeregningGrunnlag.beløp
            resultatkode = Resultatkode.BIDRAG_SATT_TIL_BARNETILLEGG_BP
            justertForNettoBarnetilleggBP = true
        }

        // Bestemmer resultatkode hvis det er delt bosted
        if (grunnlag.deltBostedBeregningGrunnlag.deltBosted) {
            resultatkode = bestemDeltBostedResultatkode(bpAndel = bpAndelJustert.andel, foreløpigResultatkode = resultatkode)
            foreløpigBarnebidrag = kostnadsberegnetBidrag
        }

        return EndeligBidragBeregningResultat(
            beregnetBeløp = foreløpigBarnebidrag.coerceAtLeast(BigDecimal.ZERO).avrundetMedToDesimaler,
            resultatKode = resultatkode,
            resultatBeløp = foreløpigBarnebidrag.coerceAtLeast(BigDecimal.ZERO).avrundetTilNærmesteTier,
            kostnadsberegnetBidrag = kostnadsberegnetBidrag.avrundetMedToDesimaler,
            nettoBarnetilleggBP = nettoBarnetilleggBP.avrundetMedToDesimaler,
            nettoBarnetilleggBM = nettoBarnetilleggBM.avrundetMedToDesimaler,
            justertNedTilEvne = justertNedTilEvne,
            justertNedTil25ProsentAvInntekt = justertNedTil25ProsentAvInntekt,
            justertForNettoBarnetilleggBP = justertForNettoBarnetilleggBP,
            justertForNettoBarnetilleggBM = justertForNettoBarnetilleggBM,
            grunnlagsreferanseListe = listOf(
                grunnlag.bidragsevneBeregningGrunnlag.referanse,
                grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
                grunnlag.deltBostedBeregningGrunnlag.referanse,
                grunnlag.barnetilleggBPBeregningGrunnlag.referanse,
                grunnlag.barnetilleggBMBeregningGrunnlag.referanse,
            ),
        )
    }

    // Beregner netto barnetillegg ut fra brutto barnetillegg og skattesats
    private fun beregnNettoBarnetillegg(barnetillegg: BarnetilleggBeregningGrunnlag): BigDecimal =
        barnetillegg.beløp - (barnetillegg.beløp * barnetillegg.skattFaktor)

    // Justerer BP's andel hvis det er delt bosted
    private fun justerBpAndel(grunnlag: EndeligBidragBeregningGrunnlag): BpAndelJustert {
        var andel = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelFaktor
        var beløp = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelBeløp

        if (grunnlag.deltBostedBeregningGrunnlag.deltBosted) {
            andel = (andel - BigDecimal.valueOf(0.5)).coerceAtLeast(BigDecimal.ZERO)
            beløp = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelBeløp
                .divide(grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelFaktor, 10, RoundingMode.HALF_UP)
                .multiply(andel)
        }

        return BpAndelJustert(andel = andel, beløp = beløp)
    }

    // Resultatkode settes til delt bosted hvis barnet har delt bosted og bidrag ikke er redusert under beregningen eller satt opp til barnetillegg BP
    private fun bestemDeltBostedResultatkode(bpAndel: BigDecimal, foreløpigResultatkode: Resultatkode): Resultatkode = when {
        bpAndel > BigDecimal.ZERO &&
            (
                foreløpigResultatkode == Resultatkode.KOSTNADSBEREGNET_BIDRAG ||
                    foreløpigResultatkode == Resultatkode.BIDRAG_SATT_TIL_BARNETILLEGG_BP
                ) ->
            Resultatkode.DELT_BOSTED

        bpAndel == BigDecimal.ZERO -> Resultatkode.BIDRAG_IKKE_BEREGNET_DELT_BOSTED
        else -> foreløpigResultatkode
    }

    data class BpAndelJustert(val andel: BigDecimal, val beløp: BigDecimal)
}
