package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.TilsynsutgiftBarn
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal object NettoTilsynsutgiftBeregning {
    // Beregner netto tilsynsutgift for alle barn  med faktiske utgifter. Hvis samlede faktiske utgifter er større enn
    // sjablon maks tilsynsbeløp, skal beløpene justeres forholdsmessig.

    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {

        // Henter antall barn i perioden. Dette skal være antall BMs barn under 12 år. i første versjon så må det legges inn perioder med faktiske
        // utgifter for alle barn. I neste versjon så skal alle BMs barn ligge i grunnlaget (grunnlag.barnBMListe), og antall barn telles derfra.
        val antallBarnIPerioden = maxOf(grunnlag.faktiskUtgiftListe.distinctBy { it.gjelderBarn }.size, grunnlag.barnBMListe.size)

        val sjablonSkattAlminneligInntektProsent =
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .firstOrNull { it.type == SjablonTallNavn.SKATT_ALMINNELIG_INNTEKT_PROSENT.navn }
                ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for skatt alminnelig inntekt.")

        val sjablonMaksTilsynsutgift = grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.maxBeløpTilsyn

        val sjablonMaksFradragsbeløp = grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.maxBeløpFradrag

        // lag samletFaktiskUtgiftBeløp
        val totaltFaktiskUtgiftBeløp = grunnlag.faktiskUtgiftListe.sumOf { it.beregnetBeløp }

        // Beregner bruttobeløp. Dette er faktiske utgifter minus kostpenger minus tilleggsstønad (omregnet fra dagsats til månedsbeløp)
        var totalTilsynsutgift = totaltFaktiskUtgiftBeløp.minus(grunnlag.tilleggsstønad.beregnetBeløp).multiply(BigDecimal.valueOf(11))
            .divide(BigDecimal.valueOf(12), MathContext(10, RoundingMode.HALF_UP)).avrundetMedToDesimaler.coerceAtLeast(BigDecimal.ZERO)

        val skattefradrag =
            beregnFradragsbeløpPerBarn(
                antallBarnIPerioden = antallBarnIPerioden,
                beregnetBruttobeløp = minOf(totalTilsynsutgift, sjablonMaksTilsynsutgift),
                sjablonSkattesatsAlminneligInntektProsent = sjablonSkattAlminneligInntektProsent.verdi.toBigDecimal(),
                sjablonMaksFradragsbeløp = sjablonMaksFradragsbeløp,
            )

        val tilsynsutgiftBarnListe = mutableListOf<TilsynsutgiftBarn>()

        var andelTilsynsutgiftBeløp = BigDecimal.ZERO
        var andelTilsynsutgiftFaktor = BigDecimal.ZERO

        // Finner prosentandel av totalbeløp og beregner så andel av maks tilsynsbeløp
        grunnlag.faktiskUtgiftListe.forEach {
            val bruttoTilsynsutgift =
                if (totalTilsynsutgift > sjablonMaksTilsynsutgift) {
                    it.beregnetBeløp.divide(totalTilsynsutgift, MathContext(10, RoundingMode.HALF_UP)) * sjablonMaksTilsynsutgift
                } else {
                    it.beregnetBeløp
                }

            tilsynsutgiftBarnListe.add(
                TilsynsutgiftBarn(
                    gjelderBarn = it.gjelderBarn,
                    sumTilsynsutgifter = it.beregnetBeløp.avrundetMedToDesimaler,
                    endeligSumTilsynsutgifter = bruttoTilsynsutgift.avrundetMedToDesimaler,

                    ),
            )

            if (it.referanse == grunnlag.søknadsbarnReferanse) {
                andelTilsynsutgiftFaktor = it.beregnetBeløp.divide(totalTilsynsutgift, MathContext(10, RoundingMode.HALF_UP))
                    .avrundetMedTiDesimaler

                if (totalTilsynsutgift > sjablonMaksTilsynsutgift) {
                    andelTilsynsutgiftBeløp = andelTilsynsutgiftFaktor * sjablonMaksTilsynsutgift
                } else {
                    andelTilsynsutgiftBeløp = it.beregnetBeløp
                }
            }
        }

        // Sjekker og justerer mot sjablonverdi maks tilsynsbeløp
        totalTilsynsutgift = minOf(totalTilsynsutgift, sjablonMaksTilsynsutgift)

        val resultat = NettoTilsynsutgiftBeregningResultat(
            totalTilsynsutgift = totaltFaktiskUtgiftBeløp.avrundetMedToDesimaler,
            sjablonMaksTilsynsutgift = sjablonMaksTilsynsutgift,
            andelTilsynsutgiftBeløp = andelTilsynsutgiftBeløp.avrundetMedToDesimaler,
            andelTilsynsutgiftFaktor = andelTilsynsutgiftFaktor.avrundetMedToDesimaler,
            skattefradrag = skattefradrag,
            nettoTilsynsutgift = (andelTilsynsutgiftBeløp - skattefradrag).avrundetMedToDesimaler
                .coerceAtLeast(BigDecimal.ZERO),
            tilsynsutgiftBarnListe = tilsynsutgiftBarnListe,
            grunnlagsreferanseListe =
                grunnlag.faktiskUtgiftListe.map { it.referanse } +
                    grunnlag.tilleggsstønad.referanse +
                    grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse } +
                    grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.referanse +
                    grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.referanse,

            )
        return resultat
    }

    private fun beregnFradragsbeløpPerBarn(
        antallBarnIPerioden: Int,
        beregnetBruttobeløp: BigDecimal,
        sjablonSkattesatsAlminneligInntektProsent: BigDecimal,
        sjablonMaksFradragsbeløp: BigDecimal,
    ): BigDecimal {
        val skatteSatsOmregnet = sjablonSkattesatsAlminneligInntektProsent.divide(BigDecimal(100)).avrundetMedTiDesimaler
        val maksFradragsbeløp = sjablonMaksFradragsbeløp * skatteSatsOmregnet
        val fradragsbeløp = minOf((beregnetBruttobeløp * skatteSatsOmregnet), maksFradragsbeløp)

        return fradragsbeløp.divide(antallBarnIPerioden.toBigDecimal(), MathContext(10, RoundingMode.HALF_UP))
    }
}
