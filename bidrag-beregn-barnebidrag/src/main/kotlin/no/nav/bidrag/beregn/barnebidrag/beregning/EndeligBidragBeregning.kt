package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningResultat
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import java.math.BigDecimal
import java.math.RoundingMode

internal object EndeligBidragBeregning {

    val bigDecimal12 = BigDecimal.valueOf(12)

    fun beregn(grunnlag: EndeligBidragBeregningGrunnlag): EndeligBidragBeregningResultat {
        // Hvis barnet er selvforsørget gjøres det ingen videre beregning
        if (grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.barnetErSelvforsørget) {
            return EndeligBidragBeregningResultat(
                barnetErSelvforsørget = true,
                grunnlagsreferanseListe = listOf(grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse),
            )
        }

        val bidragsevne = grunnlag.bidragsevneBeregningGrunnlag.beløp
        val sumInntekt25Prosent = grunnlag.bidragsevneBeregningGrunnlag.sumInntekt25Prosent
        val samværsfradrag = grunnlag.samværsfradragBeregningGrunnlag.beløp
        val underholdskostnad = grunnlag.underholdskostnadBeregningGrunnlag.beløp
        val bpAndelBeløp = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelBeløp

        val bruttoBidragJustertForEvneOg25Prosent: BigDecimal
        val bidragJustertNedTilEvne: Boolean
        val bidragJustertNedTil25ProsentAvInntekt: Boolean

        // Hvis det er delt bosted gjøres det ingen videre beregning og evt. barnetillegg hensyntas ikke
        if (grunnlag.deltBostedBeregningGrunnlag.deltBosted) {

            // Justerer BP's andel hvis det er delt bosted
            val bpAndelJustert = justerBpAndel(grunnlag)

            // Setter kostnadsberegnet bidrag lik laveste verdi av BP's andel av U, bidragsevne og 25% av inntekt
            bruttoBidragJustertForEvneOg25Prosent = minOf(bpAndelJustert.beløp, bidragsevne, sumInntekt25Prosent)
            bidragJustertNedTilEvne = (bidragsevne < bpAndelJustert.beløp) && (bidragsevne <= sumInntekt25Prosent)
            bidragJustertNedTil25ProsentAvInntekt = (sumInntekt25Prosent < bpAndelJustert.beløp) && (sumInntekt25Prosent <= bidragsevne)

            return EndeligBidragBeregningResultat(
                beregnetBeløp = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
                resultatBeløp = bruttoBidragJustertForEvneOg25Prosent.avrundetTilNærmesteTier,
                bruttoBidragJustertForEvneOg25Prosent = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
                nettoBidragEtterSamværsfradrag = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
                bpAndelAvUVedDeltBostedFaktor = bpAndelJustert.andel.avrundetMedTiDesimaler,
                bpAndelAvUVedDeltBostedBeløp = bpAndelJustert.beløp.avrundetMedToDesimaler,
                bidragJustertForDeltBosted = true,
                bidragJustertNedTilEvne = bidragJustertNedTilEvne,
                bidragJustertNedTil25ProsentAvInntekt = bidragJustertNedTil25ProsentAvInntekt,
                grunnlagsreferanseListe = listOf(
                    grunnlag.bidragsevneBeregningGrunnlag.referanse,
                    grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                    grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                    grunnlag.samværsfradragBeregningGrunnlag.referanse,
                    grunnlag.deltBostedBeregningGrunnlag.referanse,
                ),
            )
        }

        // Beregner netto barnetillegg for BP og BM
        val nettoBarnetilleggBP = beregnNettoBarnetillegg(grunnlag.barnetilleggBPBeregningGrunnlag)
        val nettoBarnetilleggBM = beregnNettoBarnetillegg(grunnlag.barnetilleggBMBeregningGrunnlag)
        val uMinusNettoBarnetilleggBM = underholdskostnad - nettoBarnetilleggBM
        var foreløpigBeregnetBeløp = maxOf((bpAndelBeløp - samværsfradrag), BigDecimal.ZERO)
        val bruttoBidragEtterBarnetilleggBM: BigDecimal
        val nettoBidragEtterBarnetilleggBM: BigDecimal
        val bruttoBidragEtterBarnetilleggBP: BigDecimal
        var bidragJustertForNettoBarnetilleggBM = false
        var bidragJustertForNettoBarnetilleggBP = false

        // Sjekker om eventuelt barnetillegg for BM skal benyttes
        // Samværsfradrag trekkes fra i sammenligningen, men ikke beregningen
        if (nettoBarnetilleggBM > BigDecimal.ZERO) {
            if (foreløpigBeregnetBeløp > uMinusNettoBarnetilleggBM) {
                foreløpigBeregnetBeløp = uMinusNettoBarnetilleggBM
                bruttoBidragEtterBarnetilleggBM = uMinusNettoBarnetilleggBM + samværsfradrag
                nettoBidragEtterBarnetilleggBM = maxOf(uMinusNettoBarnetilleggBM, BigDecimal.ZERO)
                bidragJustertForNettoBarnetilleggBM = true
            } else {
                bruttoBidragEtterBarnetilleggBM = foreløpigBeregnetBeløp + samværsfradrag
                nettoBidragEtterBarnetilleggBM = foreløpigBeregnetBeløp
            }
        } else {
            bruttoBidragEtterBarnetilleggBM = foreløpigBeregnetBeløp + samværsfradrag
            nettoBidragEtterBarnetilleggBM = foreløpigBeregnetBeløp
        }

        // Setter kostnadsberegnet bidrag lik laveste verdi av BP's andel av U, bidragsevne og 25% av inntekt
        bruttoBidragJustertForEvneOg25Prosent = minOf((foreløpigBeregnetBeløp + samværsfradrag), bidragsevne, sumInntekt25Prosent)
        bidragJustertNedTilEvne = (bidragsevne < (foreløpigBeregnetBeløp + samværsfradrag)) && (bidragsevne <= sumInntekt25Prosent)
        bidragJustertNedTil25ProsentAvInntekt =
            (sumInntekt25Prosent < (foreløpigBeregnetBeløp + samværsfradrag)) && (sumInntekt25Prosent <= bidragsevne)
        foreløpigBeregnetBeløp = bruttoBidragJustertForEvneOg25Prosent

        // Sjekker om eventuelt barnetillegg for BP skal benyttes (hvis regel for BP's barnetillegg slår til overstyrer den BM's barnetillegg)
        // Samværsfradrag trekkes fra i beregningen, men ikke i sammenligningen
        if (nettoBarnetilleggBP > BigDecimal.ZERO) {
            if (nettoBarnetilleggBP > foreløpigBeregnetBeløp) {
                foreløpigBeregnetBeløp = nettoBarnetilleggBP
                bruttoBidragEtterBarnetilleggBP = nettoBarnetilleggBP
                bidragJustertForNettoBarnetilleggBP = true
            } else {
                bruttoBidragEtterBarnetilleggBP = foreløpigBeregnetBeløp
            }
        } else {
            bruttoBidragEtterBarnetilleggBP = foreløpigBeregnetBeløp
        }

        // Hvis ingen av barnetilleggene eksisterer eller skal benyttes, settes beregnet beløp lik "kostnadsberegnet" bidrag - samværsfradrag
        val nettoBidragEtterSamværsfradrag = maxOf((foreløpigBeregnetBeløp - samværsfradrag), BigDecimal.ZERO)

        return EndeligBidragBeregningResultat(
            beregnetBeløp = nettoBidragEtterSamværsfradrag.avrundetMedToDesimaler,
            resultatBeløp = nettoBidragEtterSamværsfradrag.avrundetTilNærmesteTier,
            uMinusNettoBarnetilleggBM = uMinusNettoBarnetilleggBM.avrundetMedToDesimaler,
            bruttoBidragEtterBarnetilleggBM = bruttoBidragEtterBarnetilleggBM.avrundetMedToDesimaler,
            nettoBidragEtterBarnetilleggBM = nettoBidragEtterBarnetilleggBM.avrundetMedToDesimaler,
            bruttoBidragJustertForEvneOg25Prosent = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
            bruttoBidragEtterBarnetilleggBP = bruttoBidragEtterBarnetilleggBP.avrundetMedToDesimaler,
            nettoBidragEtterSamværsfradrag = nettoBidragEtterSamværsfradrag.avrundetMedToDesimaler,
            bidragJustertForNettoBarnetilleggBP = bidragJustertForNettoBarnetilleggBP,
            bidragJustertForNettoBarnetilleggBM = bidragJustertForNettoBarnetilleggBM,
            bidragJustertNedTilEvne = bidragJustertNedTilEvne,
            bidragJustertNedTil25ProsentAvInntekt = bidragJustertNedTil25ProsentAvInntekt,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.bidragsevneBeregningGrunnlag.referanse,
                grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
                grunnlag.deltBostedBeregningGrunnlag.referanse,
            ) +
                (grunnlag.barnetilleggBPBeregningGrunnlag?.referanse ?: emptyList()) +
                (grunnlag.barnetilleggBMBeregningGrunnlag?.referanse ?: emptyList())
        )
    }

    // Beregner netto barnetillegg ut fra brutto barnetillegg og skattesats
    //TODO Flytte til egen delberegning
    private fun beregnNettoBarnetillegg(barnetillegg: BarnetilleggBeregningGrunnlag?): BigDecimal {
        if (barnetillegg == null) {
            return BigDecimal.ZERO
        }

        return (barnetillegg.beløp - (barnetillegg.beløp * barnetillegg.skattFaktor)).divide(bigDecimal12, 10, RoundingMode.HALF_UP)
    }

    // Justerer BP's andel hvis det er delt bosted
    private fun justerBpAndel(grunnlag: EndeligBidragBeregningGrunnlag): BpAndelJustert {
        var andel = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelFaktor
        var beløp = grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelBeløp

        if (grunnlag.deltBostedBeregningGrunnlag.deltBosted) {
            andel = (andel - BigDecimal.valueOf(0.5)).coerceAtLeast(BigDecimal.ZERO)
            beløp = grunnlag.underholdskostnadBeregningGrunnlag.beløp.multiply(andel)
        }

        return BpAndelJustert(andel = andel, beløp = beløp)
    }

    data class BpAndelJustert(val andel: BigDecimal, val beløp: BigDecimal)
}
