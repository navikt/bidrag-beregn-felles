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
    // Beregner netto tilsynsutgift for alle barn under 13 år med faktiske utgifter. Hvis samlede faktiske utgifter er større enn
    // sjablon maks tilsynsbeløp, skal beløpene justeres forholdsmessig.

    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {
        val antallBarnIPerioden = grunnlag.faktiskUtgiftListe.size

        val sjablonSkattAlminneligInntektProsent =
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .firstOrNull { it.type == SjablonTallNavn.SKATT_ALMINNELIG_INNTEKT_PROSENT.navn }
                ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for skatt alminnelig inntekt.")

        val sjablonMaksTilsynsbeløp = grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.maxBeløpTilsyn

        val sjablonMaksFradragsbeløp = grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.maxBeløpFradrag

        // lag samletFaktiskUtgiftBeløp
        val totaltFaktiskUtgiftBeløp = grunnlag.faktiskUtgiftListe.sumOf { it.beregnetBeløp }

        val skattefradragsbeløpPerBarn =
            beregnFradragsbeløpPerBarn(
                antallBarnIPerioden = antallBarnIPerioden,
                totaltFaktiskUtgiftBeløp = minOf(totaltFaktiskUtgiftBeløp, sjablonMaksTilsynsbeløp),
                sjablonSkattesatsAlminneligInntektProsent = sjablonSkattAlminneligInntektProsent.verdi.toBigDecimal(),
                sjablonMaksFradragsbeløp = sjablonMaksFradragsbeløp,
            )

        val tilsynsutgiftBarnListe = mutableListOf<TilsynsutgiftBarn>()

        // Finner prosentandel av totalbeløp og beregner så andel av maks tilsynsbeløp
        grunnlag.faktiskUtgiftListe.forEach {
            val bruttoTilsynsutgift =
                if (totaltFaktiskUtgiftBeløp > sjablonMaksTilsynsbeløp) {
                    it.beregnetBeløp.divide(totaltFaktiskUtgiftBeløp, MathContext(10, RoundingMode.HALF_UP)) * sjablonMaksTilsynsbeløp
                } else {
                    it.beregnetBeløp
                }

            val tilleggsstønadBeløp = grunnlag.tilleggsstønadListe
                .filter { ts -> ts.gjelderBarn == it.gjelderBarn }
                .sumOf { ts -> ts.beregnetBeløp }

            tilsynsutgiftBarnListe.add(
                TilsynsutgiftBarn(
                    gjelderBarn = it.gjelderBarn,
                    sumFaktiskeUtgifter = it.beregnetBeløp.avrundetMedToDesimaler,
                    endeligSumFaktiskeUtgifter = bruttoTilsynsutgift.avrundetMedToDesimaler,
                    skattefradragsbeløpPerBarn = skattefradragsbeløpPerBarn.avrundetMedToDesimaler,
                    tilleggsstønad = tilleggsstønadBeløp.avrundetMedToDesimaler,
                    nettoTilsynsutgift = (bruttoTilsynsutgift - skattefradragsbeløpPerBarn - tilleggsstønadBeløp).avrundetMedToDesimaler,
                ),
            )
        }

        val resultat = NettoTilsynsutgiftBeregningResultat(
            totaltFaktiskUtgiftBeløp = totaltFaktiskUtgiftBeløp.avrundetMedToDesimaler,
            tilsynsutgiftBarnListe = tilsynsutgiftBarnListe,
            grunnlagsreferanseListe =
            grunnlag.faktiskUtgiftListe.map { it.referanse } +
                grunnlag.tilleggsstønadListe.map { it.referanse } +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse } +
                grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.referanse +
                grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.referanse,

        )
        return resultat
    }

    private fun beregnFradragsbeløpPerBarn(
        antallBarnIPerioden: Int,
        totaltFaktiskUtgiftBeløp: BigDecimal,
        sjablonSkattesatsAlminneligInntektProsent: BigDecimal,
        sjablonMaksFradragsbeløp: BigDecimal,
    ): BigDecimal {
        val skatteSatsOmregnet = sjablonSkattesatsAlminneligInntektProsent.divide(BigDecimal(100)).avrundetMedTiDesimaler
        val maksFradragsbeløp = sjablonMaksFradragsbeløp * skatteSatsOmregnet
        val fradragsbeløp = minOf((totaltFaktiskUtgiftBeløp * skatteSatsOmregnet), maksFradragsbeløp)

        return fradragsbeløp.divide(antallBarnIPerioden.toBigDecimal(), MathContext(10, RoundingMode.HALF_UP))
    }

//    private fun beregnNettoTilsynsutgiftBeløp(
//        bruttoTilsynsutgift: BigDecimal,
//        skattefradragsbeløpPerBarn: BigDecimal,
//        tilleggsstønadBeløp: BigDecimal,
//    ): BigDecimal {
//        val nettoTilsynsutgift = (bruttoTilsynsutgift - skattefradragsbeløpPerBarn - tilleggsstønadBeløp).multiply(BigDecimal(11))
//            .divide(BigDecimal(12), 10, RoundingMode.HALF_UP)
//        return nettoTilsynsutgift.avrundetMedToDesimaler.coerceAtLeast(BigDecimal.ZERO)
//    }
}
