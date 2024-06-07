package no.nav.bidrag.beregn.bpsandelsaertilskudd.beregning

import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bpsandelsaertilskudd.bo.ResultatBeregning
import no.nav.bidrag.beregn.felles.FellesBeregning
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.util.SjablonUtil
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.math.RoundingMode

class BPsAndelSaertilskuddBeregningImpl : FellesBeregning(), BPsAndelSaertilskuddBeregning {
    override fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        // Henter sjablonverdier
        val sjablonNavnVerdiMap = hentSjablonVerdier(grunnlag.sjablonListe)

        var andelProsent = BigDecimal.ZERO
        var andelBelop = BigDecimal.ZERO
        var barnetErSelvforsorget = false

        // Legger sammen inntektene
        val inntektBP = grunnlag.inntektBPListe.sumOf { it.inntektBelop }
        val inntektBM = grunnlag.inntektBMListe.sumOf { it.inntektBelop }
        var inntektBB = grunnlag.inntektBBListe.sumOf { it.inntektBelop }

        // Tester om barnets inntekt er høyere enn 100 ganger sats for forhøyet forskudd. I så fall skal ikke BPs andel regnes ut.
        if (inntektBB > sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn]!!.multiply(BigDecimal.valueOf(100))) {
            barnetErSelvforsorget = true
        } else {
            inntektBB -= sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn]!!.multiply(BigDecimal.valueOf(30))
            inntektBB = inntektBB.coerceAtLeast(BigDecimal.ZERO)

            andelProsent = (inntektBP * BigDecimal.valueOf(100))
                .divide(inntektBP + inntektBM + inntektBB, 1, RoundingMode.HALF_UP)

            if (andelProsent > BigDecimal.valueOf(83.3333333333)) {
                andelProsent = BigDecimal.valueOf(83.3333333333)
            }

            andelBelop = (grunnlag.nettoSaertilskuddBelop * andelProsent / BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
        }

        return ResultatBeregning(
            resultatAndelProsent = andelProsent,
            resultatAndelBelop = andelBelop,
            barnetErSelvforsorget = barnetErSelvforsorget,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap, grunnlag.sjablonListe),
        )
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>): Map<String, BigDecimal> {
        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()
        val sjablonListe = sjablonPeriodeListe
            .map { it.sjablon }

        // Sjablontall
        sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP)

        return sjablonNavnVerdiMap
    }
}
