package no.nav.bidrag.beregn.bidragsevne.beregning

import no.nav.bidrag.beregn.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.bidragsevne.bo.Inntekt
import no.nav.bidrag.beregn.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.felles.FellesBeregning
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.util.SjablonUtil
import no.nav.bidrag.domene.enums.beregning.Særfradragskode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class BidragsevneBeregningImpl : FellesBeregning(), BidragsevneBeregning {

    override fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        // Henter sjablonverdier
        val sjablonNavnVerdiMap = hentSjablonVerdier(
            sjablonPeriodeListe = grunnlag.sjablonListe,
            bostatusKode = grunnlag.bostatus.kode,
            skatteklasse = grunnlag.skatteklasse.skatteklasse,
        )

        // Beregner minstefradrag
        val minstefradrag = beregnMinstefradrag(
            grunnlag = grunnlag,
            minstefradragInntektSjablonBelop = (sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn])!!,
            minstefradragInntektSjablonProsent = (sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn])!!,
        )

        // Legger sammen inntektene
        val inntekt = grunnlag.inntektListe.stream()
            .map(Inntekt::inntektBelop)
            .reduce(BigDecimal.ZERO, BigDecimal::add)

        // Finner personfradragklasse ut fra angitt skatteklasse
        val personfradrag = if (grunnlag.skatteklasse.skatteklasse == 1) {
            sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn]
        } else {
            sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELØP.navn]
        }

        val inntektMinusFradrag = inntekt.subtract(minstefradrag).subtract(personfradrag)

        var forelopigBidragsevne = inntekt
            // Trekker fra skatt
            .subtract(
                inntektMinusFradrag.multiply(
                    sjablonNavnVerdiMap[SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn]!!.divide(
                        BigDecimal.valueOf(100),
                        MathContext(10, RoundingMode.HALF_UP),
                    ),
                ),
            )
            .subtract(
                inntekt.multiply(
                    sjablonNavnVerdiMap[SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn]!!.divide(
                        BigDecimal.valueOf(100),
                        MathContext(10, RoundingMode.HALF_UP),
                    ),
                ),
            )
            // Trekker fra trinnvis skatt
            .subtract(beregnSkattetrinnBelop(grunnlag))
            // Trekker fra boutgifter og midler til eget underhold
            .subtract(sjablonNavnVerdiMap[SjablonInnholdNavn.BOUTGIFT_BELØP.navn]!!.multiply(BigDecimal.valueOf(12)))
            .subtract(sjablonNavnVerdiMap[SjablonInnholdNavn.UNDERHOLD_BELØP.navn]!!.multiply(BigDecimal.valueOf(12)))
            // Trekker fra midler til underhold egne barn i egen husstand
            .subtract(
                sjablonNavnVerdiMap[SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn]!!
                    .multiply(BigDecimal.valueOf(grunnlag.barnIHusstand.antallBarn))
                    .multiply(BigDecimal.valueOf(12)),
            )

        // Sjekker om og kalkulerer eventuell fordel særfradrag
        val fordelSaerfradragBelop = sjablonNavnVerdiMap[SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP.navn]
        forelopigBidragsevne = when (grunnlag.saerfradrag.kode) {
            Særfradragskode.HELT -> forelopigBidragsevne + (fordelSaerfradragBelop ?: BigDecimal.ZERO)
            Særfradragskode.HALVT -> {
                val halvVerdi = fordelSaerfradragBelop?.divide(BigDecimal.valueOf(2), MathContext(10, RoundingMode.HALF_UP))
                forelopigBidragsevne + (halvVerdi ?: BigDecimal.ZERO)
            }

            else -> forelopigBidragsevne
        }

        // Legger til fordel skatteklasse2
        if (grunnlag.skatteklasse.skatteklasse == 2) {
            forelopigBidragsevne = forelopigBidragsevne.add(sjablonNavnVerdiMap[SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP.navn])
        }

        // Finner månedlig beløp for bidragsevne
        val maanedligBidragsevne = maxOf(
            forelopigBidragsevne.divide(BigDecimal.valueOf(12), MathContext(10, RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP),
            BigDecimal.ZERO,
        )

        return ResultatBeregning(
            belop = maanedligBidragsevne,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
        )
    }

    override fun beregnMinstefradrag(
        grunnlag: GrunnlagBeregning,
        minstefradragInntektSjablonBelop: BigDecimal,
        minstefradragInntektSjablonProsent: BigDecimal,
    ): BigDecimal {
        // Legger sammen inntektene
        val inntekt = grunnlag.inntektListe.stream()
            .map(Inntekt::inntektBelop)
            .reduce(BigDecimal.ZERO, BigDecimal::add)

        return minOf(
            inntekt.multiply(minstefradragInntektSjablonProsent.divide(BigDecimal.valueOf(100), MathContext(2, RoundingMode.HALF_UP))),
            minstefradragInntektSjablonBelop,
        ).setScale(0, RoundingMode.HALF_UP)
    }

    override fun beregnSkattetrinnBelop(grunnlag: GrunnlagBeregning): BigDecimal {
        // Legger sammen inntektene
        val inntekt = grunnlag.inntektListe.stream()
            .map(Inntekt::inntektBelop)
            .reduce(BigDecimal.ZERO, BigDecimal::add)

        val sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(
            sjablonListe = grunnlag.sjablonListe.map { it.sjablon }.toList(),
            sjablonNavn = SjablonNavn.TRINNVIS_SKATTESATS,
        )

        var samletSkattetrinnBelop = BigDecimal.ZERO
        var indeks = 1

        // Beregner skattetrinnbeløp
        while (indeks < sortertTrinnvisSkattesatsListe.size) {
            val denneGrense = sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense
            val nesteGrense = sortertTrinnvisSkattesatsListe[indeks].inntektGrense

            if (inntekt > denneGrense) {
                val taxableIncome = minOf(inntekt, nesteGrense) - denneGrense
                val taxRate = sortertTrinnvisSkattesatsListe[indeks - 1].sats

                samletSkattetrinnBelop += (taxableIncome * taxRate).divide(BigDecimal.valueOf(100), MathContext(10, RoundingMode.HALF_UP))
            }
            indeks++
        }

        if (inntekt > sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense) {
            val taxableIncome = inntekt - sortertTrinnvisSkattesatsListe[indeks - 1].inntektGrense
            val taxRate = sortertTrinnvisSkattesatsListe[indeks - 1].sats

            samletSkattetrinnBelop += (taxableIncome * taxRate / BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
        }

        return samletSkattetrinnBelop.setScale(0, RoundingMode.HALF_UP)
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(
        sjablonPeriodeListe: List<SjablonPeriode>,
        bostatusKode: Bostatuskode,
        skatteklasse: Int,
    ): Map<String, BigDecimal> {
        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()
        val sjablonListe = sjablonPeriodeListe.map { it.sjablon }.toList()

        // Sjablontall
        if (skatteklasse == 1) {
            sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn] =
                SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP)
        } else {
            sjablonNavnVerdiMap[SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELØP.navn] =
                SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELØP)
            sjablonNavnVerdiMap[SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP.navn] =
                SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP)
        }
        sjablonNavnVerdiMap[SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.TRYGDEAVGIFT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP)
        sjablonNavnVerdiMap[SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP)
        sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT)
        sjablonNavnVerdiMap[SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn] =
            SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP)

        // Bidragsevne
        val sjablonNokkelVerdi = if ((bostatusKode == Bostatuskode.ALENE)) "EN" else "GS"
        sjablonNavnVerdiMap[SjablonInnholdNavn.BOUTGIFT_BELØP.navn] = SjablonUtil.hentSjablonverdi(
            sjablonListe = sjablonListe,
            sjablonNavn = SjablonNavn.BIDRAGSEVNE,
            sjablonNokkelListe = listOf(SjablonNokkel(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = sjablonNokkelVerdi)),
            sjablonInnholdNavn = SjablonInnholdNavn.BOUTGIFT_BELØP,
        )
        sjablonNavnVerdiMap[SjablonInnholdNavn.UNDERHOLD_BELØP.navn] = SjablonUtil.hentSjablonverdi(
            sjablonListe = sjablonListe,
            sjablonNavn = SjablonNavn.BIDRAGSEVNE,
            sjablonNokkelListe = listOf(SjablonNokkel(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = sjablonNokkelVerdi)),
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
