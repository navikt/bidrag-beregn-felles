package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningResultat
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import java.math.BigDecimal

internal object EndeligBidragBeregning {

    fun beregn(grunnlag: EndeligBidragBeregningGrunnlag, førsteElement: Boolean): EndeligBidragBeregningResultat {
        // Hvis søknadsbarnet bor hos BP gjøres det ingen videre beregning (skal resultere i avslag og bidragsbeløp settes til null
        if (grunnlag.søknadsbarnetBorHosBpGrunnlag.søknadsbarnetBorHosBp) {
            return EndeligBidragBeregningResultat(
                ikkeOmsorgForBarnet = true,
                beregnetBeløp = null,
                resultatBeløp = null,
                grunnlagsreferanseListe = listOf(grunnlag.søknadsbarnetBorHosBpGrunnlag.referanse),
            )
        }

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
        var bidragJustertTilForskuddssats = false

        // Hvis det er delt bosted gjøres det ingen videre beregning og evt. barnetillegg hensyntas ikke
        if (grunnlag.deltBostedBeregningGrunnlag.deltBosted) {
            // Justerer BP's andel hvis det er delt bosted
            val bpAndelJustert = justerBpAndel(grunnlag)

            // Setter kostnadsberegnet bidrag lik laveste verdi av BP's andel av U, bidragsevne og 25% av inntekt
            bruttoBidragJustertForEvneOg25Prosent = minOf(bpAndelJustert.beløp, bidragsevne, sumInntekt25Prosent)
            bidragJustertNedTilEvne = (bidragsevne < bpAndelJustert.beløp) && (bidragsevne <= sumInntekt25Prosent)
            bidragJustertNedTil25ProsentAvInntekt = (sumInntekt25Prosent < bpAndelJustert.beløp) && (sumInntekt25Prosent <= bidragsevne)
            var beregnetBeløp = bruttoBidragJustertForEvneOg25Prosent

            // Sjekker om det er begrenset revurdering
            // Hvis beregnet beløp er større enn løpende forskudd settes beregnet beløp = løpende forskudd
            if (grunnlag.utførBegrensetRevurdering && beregnetBeløp > grunnlag.løpendeForskuddBeløp) {
                bidragJustertTilForskuddssats = true
                beregnetBeløp = grunnlag.løpendeForskuddBeløp!!
            }

            // Setter et flagg hvis det er begrenset revurdering og beregnet beløp er lavere enn løpende bidrag
            val beregnetBidragErLavereEnnLøpendeBidrag =
                grunnlag.utførBegrensetRevurdering && (beregnetBeløp.avrundetTilNærmesteTier < grunnlag.løpendeBidragBeløp)

            // Setter et flagg hvis det er begrenset revurdering, løpende forskudd er 0 (dvs. at løpende forskudd mangler for den aktuelle perioden)
            // og det er første periode som beregnes (det er kun hvis løpende forskudd mangler i starten av beregningsperioden at det skal kastes
            // exception)
            val løpendeForskuddMangler =
                grunnlag.utførBegrensetRevurdering && førsteElement && (grunnlag.løpendeForskuddBeløp == BigDecimal.ZERO)

            return EndeligBidragBeregningResultat(
                beregnetBeløp = beregnetBeløp.avrundetMedToDesimaler,
                resultatBeløp = beregnetBeløp.avrundetTilNærmesteTier,
                bruttoBidragJustertForEvneOg25Prosent = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
                nettoBidragEtterSamværsfradrag = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
                bpAndelAvUVedDeltBostedFaktor = bpAndelJustert.andel.avrundetMedTiDesimaler,
                bpAndelAvUVedDeltBostedBeløp = bpAndelJustert.beløp.avrundetMedToDesimaler,
                løpendeForskudd = grunnlag.løpendeForskuddBeløp,
                løpendeBidrag = grunnlag.løpendeBidragBeløp,
                bidragJustertForDeltBosted = true,
                bidragJustertNedTilEvne = bidragJustertNedTilEvne,
                bidragJustertNedTil25ProsentAvInntekt = bidragJustertNedTil25ProsentAvInntekt,
                bidragJustertTilForskuddssats = bidragJustertTilForskuddssats,
                begrensetRevurderingUtført = grunnlag.utførBegrensetRevurdering,
                beregnetBidragErLavereEnnLøpendeBidrag = beregnetBidragErLavereEnnLøpendeBidrag,
                løpendeForskuddMangler = løpendeForskuddMangler,
                grunnlagsreferanseListe = listOfNotNull(
                    grunnlag.bidragsevneBeregningGrunnlag.referanse,
                    grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                    grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                    grunnlag.samværsfradragBeregningGrunnlag.referanse,
                    grunnlag.deltBostedBeregningGrunnlag.referanse,
                    grunnlag.søknadsbarnetBorHosBpGrunnlag.referanse,
                    grunnlag.barnetilleggBPBeregningGrunnlag?.referanse,
                    grunnlag.barnetilleggBMBeregningGrunnlag?.referanse,
                ) + grunnlag.engangsreferanser,
            )
        }

        // Beregner netto barnetillegg for BP og BM
        val nettoBarnetilleggBP = grunnlag.barnetilleggBPBeregningGrunnlag?.beløp ?: BigDecimal.ZERO
        val nettoBarnetilleggBM = grunnlag.barnetilleggBMBeregningGrunnlag?.beløp ?: BigDecimal.ZERO
        val uMinusNettoBarnetilleggBM = underholdskostnad - nettoBarnetilleggBM
        var foreløpigBeregnetBeløp = maxOf((bpAndelBeløp - samværsfradrag), BigDecimal.ZERO)
        val bruttoBidragEtterBarnetilleggBM: BigDecimal
        val nettoBidragEtterBarnetilleggBM: BigDecimal
        val bruttoBidragEtterBegrensetRevurdering: BigDecimal
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
            (sumInntekt25Prosent < (foreløpigBeregnetBeløp + samværsfradrag)) &&
            (sumInntekt25Prosent <= bidragsevne)
        foreløpigBeregnetBeløp = bruttoBidragJustertForEvneOg25Prosent

        // Sjekker om det er begrenset revurdering
        // Hvis beregnet beløp er større enn løpende forskudd settes beregnet beløp = løpende forskudd
        if (grunnlag.utførBegrensetRevurdering && foreløpigBeregnetBeløp > (grunnlag.løpendeForskuddBeløp!! + samværsfradrag)) {
            bidragJustertTilForskuddssats = true
            foreløpigBeregnetBeløp = grunnlag.løpendeForskuddBeløp + samværsfradrag
        }
        bruttoBidragEtterBegrensetRevurdering = foreløpigBeregnetBeløp

        // Sjekker om eventuelt barnetillegg for BP skal benyttes (hvis regel for BP's barnetillegg slår til overstyrer den BM's barnetillegg)
        if ((nettoBarnetilleggBP > BigDecimal.ZERO) && (nettoBarnetilleggBP > foreløpigBeregnetBeløp)) {
            foreløpigBeregnetBeløp = nettoBarnetilleggBP
            bidragJustertForNettoBarnetilleggBP = true
        }
        bruttoBidragEtterBarnetilleggBP = foreløpigBeregnetBeløp

        // Samværsfradrag trekkes fra til slutt
        val nettoBidragEtterSamværsfradrag = maxOf((foreløpigBeregnetBeløp - samværsfradrag), BigDecimal.ZERO)

        // Setter et flagg hvis det er begrenset revurdering og beregnet beløp er lavere enn løpende bidrag
        val beregnetBidragErLavereEnnLøpendeBidrag =
            grunnlag.utførBegrensetRevurdering && (nettoBidragEtterSamværsfradrag.avrundetTilNærmesteTier <= grunnlag.løpendeBidragBeløp)

        // Setter et flagg hvis det er begrenset revurdering, løpende forskudd er 0 (dvs. at løpende forskudd mangler for den aktuelle perioden)
        // og det er første periode som beregnes (det er kun hvis løpende forskudd mangler i starten av beregningsperioden at det skal kastes
        // exception)
        val løpendeForskuddMangler =
            grunnlag.utførBegrensetRevurdering && førsteElement && (grunnlag.løpendeForskuddBeløp == BigDecimal.ZERO)

        return EndeligBidragBeregningResultat(
            beregnetBeløp = nettoBidragEtterSamværsfradrag.avrundetMedToDesimaler,
            resultatBeløp = nettoBidragEtterSamværsfradrag.avrundetTilNærmesteTier,
            uMinusNettoBarnetilleggBM = uMinusNettoBarnetilleggBM.avrundetMedToDesimaler,
            bruttoBidragEtterBarnetilleggBM = bruttoBidragEtterBarnetilleggBM.avrundetMedToDesimaler,
            nettoBidragEtterBarnetilleggBM = nettoBidragEtterBarnetilleggBM.avrundetMedToDesimaler,
            bruttoBidragJustertForEvneOg25Prosent = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
            bruttoBidragEtterBegrensetRevurdering = bruttoBidragEtterBegrensetRevurdering.avrundetMedToDesimaler,
            bruttoBidragEtterBarnetilleggBP = bruttoBidragEtterBarnetilleggBP.avrundetMedToDesimaler,
            nettoBidragEtterSamværsfradrag = nettoBidragEtterSamværsfradrag.avrundetMedToDesimaler,
            løpendeForskudd = grunnlag.løpendeForskuddBeløp,
            løpendeBidrag = grunnlag.løpendeBidragBeløp,
            bidragJustertForNettoBarnetilleggBP = bidragJustertForNettoBarnetilleggBP,
            bidragJustertForNettoBarnetilleggBM = bidragJustertForNettoBarnetilleggBM,
            bidragJustertNedTilEvne = bidragJustertNedTilEvne,
            bidragJustertNedTil25ProsentAvInntekt = bidragJustertNedTil25ProsentAvInntekt,
            bidragJustertTilForskuddssats = bidragJustertTilForskuddssats,
            begrensetRevurderingUtført = grunnlag.utførBegrensetRevurdering,
            beregnetBidragErLavereEnnLøpendeBidrag = beregnetBidragErLavereEnnLøpendeBidrag,
            løpendeForskuddMangler = løpendeForskuddMangler,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.bidragsevneBeregningGrunnlag.referanse,
                grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
                grunnlag.deltBostedBeregningGrunnlag.referanse,
                grunnlag.søknadsbarnetBorHosBpGrunnlag.referanse,
                grunnlag.barnetilleggBPBeregningGrunnlag?.referanse,
                grunnlag.barnetilleggBMBeregningGrunnlag?.referanse,
            ) + grunnlag.engangsreferanser,
        )
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
