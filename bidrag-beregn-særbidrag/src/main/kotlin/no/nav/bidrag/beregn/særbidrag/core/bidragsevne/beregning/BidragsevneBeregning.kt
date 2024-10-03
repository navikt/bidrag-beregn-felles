package no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning

import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.HashMap

class BidragsevneBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        val mathContext = MathContext(10, RoundingMode.HALF_UP)
        val bigDecimal100 = BigDecimal.valueOf(100)
        val bigDecimal12 = BigDecimal.valueOf(12)

        // Henter sjablonverdier
        val sjablonNavnVerdiMap = hentSjablonVerdier(
            sjablonPeriodeListe = grunnlag.sjablonListe,
            borMedAndre = grunnlag.bostatusVoksneIHusstand.borMedAndre,
        )

        // Legger sammen inntektene
        val inntekt = grunnlag.inntekt?.inntektBeløp ?: BigDecimal.ZERO

        // Beregner minstefradrag
        val minstefradrag = beregnMinstefradrag(
            inntekt = inntekt,
            minstefradragInntektSjablonBeløp = sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn] ?: BigDecimal.ZERO,
            minstefradragInntektSjablonProsent = sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn] ?: BigDecimal.ZERO,
        )

        // Finner personfradrag ut fra angitt skatteklasse (alltid skatteklasse 1)
        val personfradrag = sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn]

        val inntektMinusFradrag = inntekt
            .subtract(minstefradrag)
            .subtract(personfradrag)

        val skattAlminnelig = inntektMinusFradrag
            .multiply(
                (sjablonNavnVerdiMap[SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn] ?: BigDecimal.ZERO)
                    .divide(bigDecimal100, mathContext),
            )

        val trygdeavgift = inntekt
            .multiply(
                (sjablonNavnVerdiMap[SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn] ?: BigDecimal.ZERO)
                    .divide(bigDecimal100, mathContext),
            )

        val skattetrinnBeløp = beregnSkattetrinnBeløp(grunnlag = grunnlag, inntekt = inntekt)

        val boutgifter = (sjablonNavnVerdiMap[SjablonInnholdNavn.BOUTGIFT_BELØP.navn] ?: BigDecimal.ZERO)
            .multiply(bigDecimal12)

        val underholdBeløp = (sjablonNavnVerdiMap[SjablonInnholdNavn.UNDERHOLD_BELØP.navn] ?: BigDecimal.ZERO)
            .multiply(bigDecimal12)

        val underholdEgneBarn = (sjablonNavnVerdiMap[SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn] ?: BigDecimal.ZERO)
            .multiply(BigDecimal.valueOf(grunnlag.antallBarnIHusstand.antallBarn))
            .multiply(bigDecimal12)

        // Kalkulerer foreløpig bidragsevne
        val foreløpigBidragsevne = inntekt
            .subtract(skattAlminnelig)
            .subtract(trygdeavgift)
            .subtract(skattetrinnBeløp)
            .subtract(boutgifter)
            .subtract(underholdBeløp)
            .subtract(underholdEgneBarn)

        // Finner månedlig beløp for bidragsevne
        val månedligBidragsevne = maxOf(
            foreløpigBidragsevne.divide(bigDecimal12, mathContext).setScale(0, RoundingMode.HALF_UP),
            BigDecimal.ZERO,
        )

        return ResultatBeregning(
            beløp = månedligBidragsevne,
            skatt = DelberegningBidragsevne.Skatt(
                minstefradrag = minstefradrag.setScale(0, RoundingMode.HALF_UP),
                skattAlminneligInntekt = skattAlminnelig.setScale(0, RoundingMode.HALF_UP),
                trinnskatt = skattetrinnBeløp.setScale(0, RoundingMode.HALF_UP),
                trygdeavgift = trygdeavgift.setScale(0, RoundingMode.HALF_UP),
                sumSkatt = skattAlminnelig.add(skattetrinnBeløp).add(trygdeavgift).setScale(0, RoundingMode.HALF_UP),
            ),
            underholdBarnEgenHusstand = underholdEgneBarn.setScale(0, RoundingMode.HALF_UP),
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
        )
    }

    fun beregnMinstefradrag(
        inntekt: BigDecimal,
        minstefradragInntektSjablonBeløp: BigDecimal,
        minstefradragInntektSjablonProsent: BigDecimal,
    ): BigDecimal = minOf(
        inntekt.multiply(minstefradragInntektSjablonProsent.divide(BigDecimal.valueOf(100), MathContext(2, RoundingMode.HALF_UP))),
        minstefradragInntektSjablonBeløp,
    ).setScale(0, RoundingMode.HALF_UP)

    fun beregnSkattetrinnBeløp(grunnlag: GrunnlagBeregning, inntekt: BigDecimal): BigDecimal {
        val sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(
            sjablonListe = grunnlag.sjablonListe.map { it.sjablon }.toList(),
            sjablonNavn = SjablonNavn.TRINNVIS_SKATTESATS,
        )

        var samletSkattetrinnBeløp = BigDecimal.ZERO
        var indeks = 1

        // Beregner skattetrinnbeløp
        while (indeks < sortertTrinnvisSkattesatsListe.size) {
            val denneGrense = sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense
            val nesteGrense = sortertTrinnvisSkattesatsListe[indeks].inntektGrense

            if (inntekt > denneGrense) {
                val skattbarInntekt = minOf(inntekt, nesteGrense) - denneGrense
                val skattesats = sortertTrinnvisSkattesatsListe[indeks - 1].sats

                samletSkattetrinnBeløp += (skattbarInntekt * skattesats).divide(BigDecimal.valueOf(100), MathContext(10, RoundingMode.HALF_UP))
            }
            indeks++
        }

        if (inntekt > sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense) {
            val skattbarInntekt = inntekt - sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense
            val skattesats = sortertTrinnvisSkattesatsListe[indeks - 1].sats

            samletSkattetrinnBeløp += (skattbarInntekt * skattesats / BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
        }

        return samletSkattetrinnBeløp.setScale(0, RoundingMode.HALF_UP)
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>, borMedAndre: Boolean): Map<String, BigDecimal> {
        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()
        val sjablonListe = sjablonPeriodeListe.map { it.sjablon }.toList()

        // Sjablontall
        sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP)
        sjablonNavnVerdiMap[SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.TRYGDEAVGIFT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP)
        sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP)

        // Bidragsevne
        val sjablonNøkkelVerdi = if (borMedAndre) "GS" else "EN"
        sjablonNavnVerdiMap[SjablonInnholdNavn.BOUTGIFT_BELØP.navn] = SjablonUtil.hentSjablonverdi(
            sjablonListe = sjablonListe,
            sjablonNavn = SjablonNavn.BIDRAGSEVNE,
            sjablonNøkkelListe = listOf(SjablonNøkkel(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = sjablonNøkkelVerdi)),
            sjablonInnholdNavn = SjablonInnholdNavn.BOUTGIFT_BELØP,
        )
        sjablonNavnVerdiMap[SjablonInnholdNavn.UNDERHOLD_BELØP.navn] = SjablonUtil.hentSjablonverdi(
            sjablonListe = sjablonListe,
            sjablonNavn = SjablonNavn.BIDRAGSEVNE,
            sjablonNøkkelListe = listOf(SjablonNøkkel(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = sjablonNøkkelVerdi)),
            sjablonInnholdNavn = SjablonInnholdNavn.UNDERHOLD_BELØP,
        )

        // TrinnvisSkattesats
        val trinnvisSkattesatsListe =
            SjablonUtil.hentTrinnvisSkattesats(sjablonListe = sjablonListe, sjablonNavn = SjablonNavn.TRINNVIS_SKATTESATS)
        var indeks = 1
        trinnvisSkattesatsListe.forEach {
            sjablonNavnVerdiMap[SjablonNavn.TRINNVIS_SKATTESATS.navn + "InntektGrense" + indeks] = it.inntektGrense
            sjablonNavnVerdiMap[SjablonNavn.TRINNVIS_SKATTESATS.navn + "Sats" + indeks] = it.sats
            indeks++
        }

        return sjablonNavnVerdiMap
    }
}
