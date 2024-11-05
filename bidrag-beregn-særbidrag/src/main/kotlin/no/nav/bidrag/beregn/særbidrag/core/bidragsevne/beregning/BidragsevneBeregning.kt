package no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning

import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonVerdiGrunnlag
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class BidragsevneBeregning : FellesBeregning() {

    val mathContext = MathContext(10, RoundingMode.HALF_UP)
    val bigDecimal100 = BigDecimal.valueOf(100)
    val bigDecimal12 = BigDecimal.valueOf(12)

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
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
            minstefradragInntektSjablonBeløp = sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn]?.verdi ?: BigDecimal.ZERO,
            minstefradragInntektSjablonProsent = sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn]?.verdi ?: BigDecimal.ZERO,
        )

        // Finner personfradrag ut fra angitt skatteklasse (alltid skatteklasse 1)
        val personfradrag = sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn]?.verdi

        val inntektMinusFradrag = inntekt
            .subtract(minstefradrag)
            .subtract(personfradrag)

        val skattAlminnelig = inntektMinusFradrag
            .multiply(
                (sjablonNavnVerdiMap[SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn]?.verdi ?: BigDecimal.ZERO)
                    .divide(bigDecimal100, mathContext),
            )

        val trygdeavgift = inntekt
            .multiply(
                (sjablonNavnVerdiMap[SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn]?.verdi ?: BigDecimal.ZERO)
                    .divide(bigDecimal100, mathContext),
            )

        val skattetrinnBeløp = beregnSkattetrinnBeløp(grunnlag = grunnlag, inntekt = inntekt)

        val boutgifter = (sjablonNavnVerdiMap[SjablonInnholdNavn.BOUTGIFT_BELØP.navn]?.verdi ?: BigDecimal.ZERO)
            .multiply(bigDecimal12)

        val underholdBeløp = (sjablonNavnVerdiMap[SjablonInnholdNavn.UNDERHOLD_BELØP.navn]?.verdi ?: BigDecimal.ZERO)
            .multiply(bigDecimal12)

        val underholdEgneBarn = (sjablonNavnVerdiMap[SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn]?.verdi ?: BigDecimal.ZERO)
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
            foreløpigBidragsevne.divide(bigDecimal12, mathContext).avrundetMedToDesimaler,
            BigDecimal.ZERO,
        )

        return ResultatBeregning(
            beløp = månedligBidragsevne.avrundetMedToDesimaler,
            skatt = DelberegningBidragsevne.Skatt(
                minstefradrag = minstefradrag.avrundetMedToDesimaler,
                skattAlminneligInntekt = skattAlminnelig.avrundetMedToDesimaler,
                trinnskatt = skattetrinnBeløp.avrundetMedToDesimaler,
                trygdeavgift = trygdeavgift.avrundetMedToDesimaler,
                sumSkatt = skattAlminnelig.add(skattetrinnBeløp).add(trygdeavgift).avrundetMedToDesimaler,
            ),
            underholdBarnEgenHusstand = underholdEgneBarn.avrundetMedToDesimaler,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
        )
    }

    fun beregnMinstefradrag(
        inntekt: BigDecimal,
        minstefradragInntektSjablonBeløp: BigDecimal,
        minstefradragInntektSjablonProsent: BigDecimal,
    ): BigDecimal = minOf(
        inntekt.multiply(minstefradragInntektSjablonProsent.divide(bigDecimal100, MathContext(2, RoundingMode.HALF_UP))),
        minstefradragInntektSjablonBeløp,
    ).avrundetMedToDesimaler

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

                samletSkattetrinnBeløp += (skattbarInntekt * skattesats).divide(bigDecimal100, MathContext(10, RoundingMode.HALF_UP))
            }
            indeks++
        }

        if (inntekt > sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense) {
            val skattbarInntekt = inntekt - sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense
            val skattesats = sortertTrinnvisSkattesatsListe[indeks - 1].sats

            samletSkattetrinnBeløp += (skattbarInntekt * skattesats / bigDecimal100).avrundetMedToDesimaler
        }

        return samletSkattetrinnBeløp.avrundetMedToDesimaler
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>, borMedAndre: Boolean): Map<String, SjablonVerdiGrunnlag> {
        val sjablonNavnVerdiMap = HashMap<String, SjablonVerdiGrunnlag>()
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
            sjablonNavnVerdiMap[SjablonNavn.TRINNVIS_SKATTESATS.navn + "InntektGrense" + indeks]?.verdi = it.inntektGrense
            sjablonNavnVerdiMap[SjablonNavn.TRINNVIS_SKATTESATS.navn + "Sats" + indeks]?.verdi = it.sats
            indeks++
        }

        return sjablonNavnVerdiMap
    }
}
