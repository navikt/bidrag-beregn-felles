package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevneBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragspliktigesAndelDeltBostedBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragspliktigesAndelDeltBostedBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningBarnebidragV2BeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningBarnebidragV2BeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingBeregningResultat
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import java.math.BigDecimal
import java.math.RoundingMode

internal object EndeligBidragBeregningV2 {

    fun beregnBidragspliktigesAndelDeltBosted(
        grunnlag: BidragspliktigesAndelDeltBostedBeregningGrunnlag,
    ): BidragspliktigesAndelDeltBostedBeregningResultat {
        var bpAndelAvUVedDeltBostedFaktor: BigDecimal? = null
        var bpAndelAvUVedDeltBostedBeløp: BigDecimal? = null
        if (grunnlag.deltBostedBeregningGrunnlag?.deltBosted == true) {
            bpAndelAvUVedDeltBostedFaktor =
                (grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelFaktor - BigDecimal.valueOf(0.5)).coerceAtLeast(BigDecimal.ZERO)
            bpAndelAvUVedDeltBostedBeløp = grunnlag.underholdskostnadBeregningGrunnlag.beløp.multiply(bpAndelAvUVedDeltBostedFaktor)
        }

        return BidragspliktigesAndelDeltBostedBeregningResultat(
            bpAndelAvUVedDeltBostedFaktor = bpAndelAvUVedDeltBostedFaktor?.avrundetMedTiDesimaler,
            bpAndelAvUVedDeltBostedBeløp = bpAndelAvUVedDeltBostedBeløp?.avrundetMedToDesimaler,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                grunnlag.deltBostedBeregningGrunnlag?.referanse,
            ),
        )
    }

    fun beregnBidragTilFordeling(grunnlag: BidragTilFordelingBeregningGrunnlag): BidragTilFordelingBeregningResultat {
        val erDeltBosted = grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag != null
        val samværsfradrag = if (erDeltBosted) BigDecimal.ZERO else grunnlag.samværsfradragBeregningGrunnlag.beløp
        val underholdskostnad = grunnlag.underholdskostnadBeregningGrunnlag.beløp
        val bpAndelBeløp =
            if (erDeltBosted) {
                grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag.bpAndelAvUVedDeltBostedBeløp
            } else {
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.andelBeløp
            }
        val nettoBarnetilleggBM = if (erDeltBosted) BigDecimal.ZERO else grunnlag.barnetilleggBMBeregningGrunnlag?.beløp ?: BigDecimal.ZERO

        val uMinusNettoBarnetilleggBM = maxOf(underholdskostnad - nettoBarnetilleggBM, BigDecimal.ZERO)
        val bpAndelAvUMinusSamværsfradrag = maxOf(bpAndelBeløp - samværsfradrag, BigDecimal.ZERO)
        val bidragTilFordeling = minOf(uMinusNettoBarnetilleggBM, bpAndelAvUMinusSamværsfradrag) + samværsfradrag
        val nettoBidragEtterBarnetilleggBM = maxOf(bidragTilFordeling - samværsfradrag, BigDecimal.ZERO)
        val erBidragJustertForNettoBarnetilleggBM = uMinusNettoBarnetilleggBM == bidragTilFordeling - samværsfradrag

        return BidragTilFordelingBeregningResultat(
            uMinusNettoBarnetilleggBM = uMinusNettoBarnetilleggBM.avrundetMedToDesimaler,
            bpAndelAvUMinusSamværsfradrag = bpAndelAvUMinusSamværsfradrag.avrundetMedToDesimaler,
            bidragTilFordeling = bidragTilFordeling.avrundetMedToDesimaler,
            nettoBidragEtterBarnetilleggBM = nettoBidragEtterBarnetilleggBM.avrundetMedToDesimaler,
            bruttoBidragEtterBarnetilleggBM = bidragTilFordeling.avrundetMedToDesimaler,
            erBidragJustertForNettoBarnetilleggBM = erBidragJustertForNettoBarnetilleggBM,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.underholdskostnadBeregningGrunnlag.referanse,
                grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse,
                grunnlag.barnetilleggBMBeregningGrunnlag?.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
                grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag?.referanse,
            ),
        )
    }

    fun beregnSumBidragTilFordeling(grunnlag: SumBidragTilFordelingBeregningGrunnlag) = SumBidragTilFordelingBeregningResultat(
        sumBidragTilFordeling = grunnlag.bidragTilFordelingBeregningGrunnlagListe.sumOf { it.bidragTilFordeling } +
            grunnlag.bidragTilFordelingLøpendeBidragBeregningGrunnlagListe.sumOf { it.bidragTilFordeling }.avrundetMedToDesimaler,
        sumPrioriterteBidragTilFordeling = BigDecimal.ZERO.avrundetMedToDesimaler, // TODO
        erKompletteGrunnlagForAlleLøpendeBidrag = grunnlag.bidragTilFordelingLøpendeBidragBeregningGrunnlagListe.isEmpty(),
        grunnlagsreferanseListe =
        grunnlag.bidragTilFordelingBeregningGrunnlagListe.map { it.referanse } +
            grunnlag.bidragTilFordelingLøpendeBidragBeregningGrunnlagListe.map { it.referanse },
    )

    fun beregnEvne25ProsentAvInntekt(grunnlag: Evne25ProsentAvInntektBeregningGrunnlag) = Evne25ProsentAvInntektBeregningResultat(
        evneJustertFor25ProsentAvInntekt = minOf(
            grunnlag.bidragsevneBeregningGrunnlag.beløp,
            grunnlag.bidragsevneBeregningGrunnlag.sumInntekt25Prosent,
        ).avrundetMedToDesimaler,
        erEvneJustertNedTil25ProsentAvInntekt =
        grunnlag.bidragsevneBeregningGrunnlag.sumInntekt25Prosent < grunnlag.bidragsevneBeregningGrunnlag.beløp,
        grunnlagsreferanseListe = listOfNotNull(grunnlag.bidragsevneBeregningGrunnlag.referanse),
    )

    fun beregnAndelAvBidragsevne(grunnlag: AndelAvBidragsevneBeregningGrunnlag): AndelAvBidragsevneBeregningResultat {
        val bidragTilFordeling = grunnlag.bidragTilFordelingBeregningGrunnlag.bidragTilFordeling
        val andelAvSumBidragTilFordeling = bidragTilFordeling.divide(
            grunnlag.sumBidragTilFordelingBeregningGrunnlag.sumBidragTilFordeling,
            10,
            RoundingMode.HALF_UP,
        )

        val bidragsevne = grunnlag.evne25ProsentAvInntektBeregningGrunnlag.evneJustertFor25ProsentAvInntekt
        val andelAvEvneBeløp = bidragsevne * andelAvSumBidragTilFordeling
        val bidragEtterFordeling = minOf(bidragTilFordeling, andelAvEvneBeløp)
        val harBPFullEvne = andelAvEvneBeløp >= bidragTilFordeling
        val bruttoBidragJustertForEvneOg25Prosent = minOf(bidragTilFordeling, bidragsevne)

        return AndelAvBidragsevneBeregningResultat(
            andelAvSumBidragTilFordelingFaktor = andelAvSumBidragTilFordeling.avrundetMedTiDesimaler,
            andelAvEvneBeløp = andelAvEvneBeløp.avrundetMedToDesimaler,
            bidragEtterFordeling = bidragEtterFordeling.avrundetMedToDesimaler,
            bruttoBidragJustertForEvneOg25Prosent = bruttoBidragJustertForEvneOg25Prosent.avrundetMedToDesimaler,
            harBPFullEvne = harBPFullEvne,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.evne25ProsentAvInntektBeregningGrunnlag.referanse,
                grunnlag.sumBidragTilFordelingBeregningGrunnlag.referanse,
                grunnlag.bidragTilFordelingBeregningGrunnlag.referanse,
            ),
        )
    }

    fun beregnBidragTilFordelingLøpendeBidrag(
        grunnlag: BidragTilFordelingLøpendeBidragBeregningGrunnlag,
    ): BidragTilFordelingLøpendeBidragBeregningResultat {
        val beregnetBeløp = grunnlag.løpendeBidragBeregningGrunnlag.løpendeBidrag.beregnetBeløp
        val faktiskBeløp = grunnlag.løpendeBidragBeregningGrunnlag.løpendeBidrag.faktiskBeløp
        val løpendeBeløp = grunnlag.løpendeBidragBeregningGrunnlag.løpendeBidrag.løpendeBeløp
        val samværsfradrag = grunnlag.samværsfradragBeregningGrunnlag.beløp

        val reduksjonUnderholdskostnad = (beregnetBeløp - faktiskBeløp).coerceAtLeast(BigDecimal.ZERO)
        val bidragTilFordeling = løpendeBeløp + samværsfradrag + reduksjonUnderholdskostnad

        return BidragTilFordelingLøpendeBidragBeregningResultat(
            reduksjonUnderholdskostnad = reduksjonUnderholdskostnad.avrundetMedToDesimaler,
            bidragTilFordeling = bidragTilFordeling.avrundetMedToDesimaler,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.løpendeBidragBeregningGrunnlag.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
            ),
        )
    }

    fun beregnBidragJustertForBPBarnetillegg(
        grunnlag: BidragJustertForBPBarnetilleggBeregningGrunnlag,
    ): BidragJustertForBPBarnetilleggBeregningResultat {
        val erDeltBosted = grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag != null
        val nettoBarnetilleggBP = grunnlag.barnetilleggBPBeregningGrunnlag?.beløp ?: BigDecimal.ZERO
        val bidragEtterFordelingAvBidragsevne = grunnlag.andelAvBidragsevneBeregningGrunnlag.bidragEtterFordeling
        val bidragJustertForNettoBarnetilleggBP: BigDecimal

        if (erDeltBosted) {
            bidragJustertForNettoBarnetilleggBP = bidragEtterFordelingAvBidragsevne
        } else if (nettoBarnetilleggBP > bidragEtterFordelingAvBidragsevne) {
            bidragJustertForNettoBarnetilleggBP = nettoBarnetilleggBP
        } else {
            bidragJustertForNettoBarnetilleggBP = bidragEtterFordelingAvBidragsevne
        }

        val erBidragJustertTilNettoBarnetilleggBP = bidragJustertForNettoBarnetilleggBP == nettoBarnetilleggBP

        return BidragJustertForBPBarnetilleggBeregningResultat(
            bidragJustertForNettoBarnetilleggBP = bidragJustertForNettoBarnetilleggBP.avrundetMedToDesimaler,
            erBidragJustertTilNettoBarnetilleggBP = erBidragJustertTilNettoBarnetilleggBP,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.andelAvBidragsevneBeregningGrunnlag.referanse,
                grunnlag.barnetilleggBPBeregningGrunnlag?.referanse,
                grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag?.referanse,
            ),
        )
    }

    fun beregnSluttberegningBarnebidrag(grunnlag: SluttberegningBarnebidragV2BeregningGrunnlag): SluttberegningBarnebidragV2BeregningResultat {
        // Hvis søknadsbarnet bor hos BP skal det resultere i avslag og bidragsbeløp settes til null
        if (grunnlag.søknadsbarnetBorHosBpGrunnlag.søknadsbarnetBorHosBp) {
            return SluttberegningBarnebidragV2BeregningResultat(
                ikkeOmsorgForBarnet = true,
                beregnetBeløp = null,
                resultatBeløp = null,
                grunnlagsreferanseListe = listOf(grunnlag.søknadsbarnetBorHosBpGrunnlag.referanse),
            )
        }

        // Hvis søknadsbarnet er selvforsørget skal det resultere i avslag og bidragsbeløp settes til null
        if (grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.barnetErSelvforsørget) {
            return SluttberegningBarnebidragV2BeregningResultat(
                barnetErSelvforsørget = true,
                beregnetBeløp = null,
                resultatBeløp = null,
                grunnlagsreferanseListe = listOf(grunnlag.bpAndelUnderholdskostnadBeregningGrunnlag.referanse),
            )
        }

        val erDeltBosted = grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag != null
        val bidragJustertForNettoBarnetilleggBP =
            grunnlag.bidragJustertForBPBarnetilleggBeregningGrunnlag.bidragJustertForNettoBarnetilleggBP
        val samværsfradrag = if (erDeltBosted) BigDecimal.ZERO else grunnlag.samværsfradragBeregningGrunnlag.beløp
        val beregnetBeløp = maxOf((bidragJustertForNettoBarnetilleggBP - samværsfradrag), BigDecimal.ZERO)

        return SluttberegningBarnebidragV2BeregningResultat(
            beregnetBeløp = beregnetBeløp.avrundetMedToDesimaler,
            resultatBeløp = beregnetBeløp.avrundetTilNærmesteTier,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.bidragJustertForBPBarnetilleggBeregningGrunnlag.referanse,
                grunnlag.samværsfradragBeregningGrunnlag.referanse,
                grunnlag.bidragspliktigesAndelDeltBostedBeregningGrunnlag?.referanse,
            ),
        )
    }
}
