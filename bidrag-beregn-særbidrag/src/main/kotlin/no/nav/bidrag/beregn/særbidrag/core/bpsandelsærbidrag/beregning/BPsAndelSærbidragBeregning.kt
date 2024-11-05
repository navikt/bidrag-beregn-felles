package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonVerdiGrunnlag
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

class BPsAndelSærbidragBeregning : FellesBeregning() {
    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        // Henter sjablonverdier
        val sjablonNavnVerdiMap = hentSjablonVerdier(grunnlag.sjablonListe)

        var endeligAndelFaktor = BigDecimal.ZERO
        var beregnetAndelFaktor = BigDecimal.ZERO
        var andelBeløp = BigDecimal.ZERO
        val forskuddssats = sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn]!!

        // Legger sammen inntektene
        val inntektBP = grunnlag.inntektBP?.inntektBeløp ?: BigDecimal.ZERO
        val inntektBM = grunnlag.inntektBM?.inntektBeløp ?: BigDecimal.ZERO
        var inntektSB = grunnlag.inntektSB?.inntektBeløp ?: BigDecimal.ZERO

        // Tester om barnets inntekt er høyere enn 100 ganger sats for forhøyet forskudd. I så fall skal ikke BPs andel regnes ut.
        val barnetErSelvforsorget = (inntektSB >= forskuddssats.verdi.multiply(BigDecimal.valueOf(100)))

        if (!barnetErSelvforsorget) {
            inntektSB = (inntektSB - forskuddssats.verdi.multiply(BigDecimal.valueOf(30))).coerceAtLeast(BigDecimal.ZERO)
            beregnetAndelFaktor = inntektBP.divide(inntektBP + inntektBM + inntektSB, 10, RoundingMode.HALF_UP)
            endeligAndelFaktor = beregnetAndelFaktor.coerceAtMost(BigDecimal.valueOf(0.833333333333))
            andelBeløp = (grunnlag.utgift.beløp * endeligAndelFaktor).avrundetMedToDesimaler
        }

        return ResultatBeregning(
            endeligAndelFaktor = endeligAndelFaktor.avrundetMedTiDesimaler,
            andelBeløp = andelBeløp.avrundetMedToDesimaler,
            beregnetAndelFaktor = beregnetAndelFaktor.avrundetMedTiDesimaler,
            barnEndeligInntekt = inntektSB.avrundetMedToDesimaler,
            barnetErSelvforsørget = barnetErSelvforsorget,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
        )
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>): Map<String, SjablonVerdiGrunnlag> {
        val sjablonNavnVerdiMap = HashMap<String, SjablonVerdiGrunnlag>()
        val sjablonListe = sjablonPeriodeListe
            .map { it.sjablon }

        // Sjablontall
        sjablonNavnVerdiMap[SjablonTallNavn.FORSKUDDSSATS_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP)

        return sjablonNavnVerdiMap
    }
}
