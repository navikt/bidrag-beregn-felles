package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.math.RoundingMode

class BPsAndelSærbidragBeregning : FellesBeregning() {
    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        // Henter sjablonverdier
        val sjablonNavnVerdiMap = hentSjablonVerdier(grunnlag.sjablonListe)

        var andelProsent = BigDecimal.ZERO
        var andelBeløp = BigDecimal.ZERO
        val forskuddssats = sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn] ?: BigDecimal.ZERO

        // Legger sammen inntektene
        val inntektBP = grunnlag.inntektBP?.inntektBeløp ?: BigDecimal.ZERO
        val inntektBM = grunnlag.inntektBM?.inntektBeløp ?: BigDecimal.ZERO
        var inntektSB = grunnlag.inntektSB?.inntektBeløp ?: BigDecimal.ZERO

        // Tester om barnets inntekt er høyere enn 100 ganger sats for forhøyet forskudd. I så fall skal ikke BPs andel regnes ut.
        val barnetErSelvforsorget = (inntektSB >= forskuddssats.multiply(BigDecimal.valueOf(100)))

        if (!barnetErSelvforsorget) {
            inntektSB = (inntektSB - forskuddssats.multiply(BigDecimal.valueOf(30))).coerceAtLeast(BigDecimal.ZERO)

            andelProsent = (inntektBP * BigDecimal.valueOf(100))
                .divide(inntektBP + inntektBM + inntektSB, 1, RoundingMode.HALF_UP)
                .coerceAtMost(BigDecimal.valueOf(83.3333333333))

            andelBeløp = (grunnlag.utgift.beløp * andelProsent / BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
        }

        return ResultatBeregning(
            resultatAndelProsent = andelProsent,
            resultatAndelBeløp = andelBeløp,
            barnetErSelvforsørget = barnetErSelvforsorget,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
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
