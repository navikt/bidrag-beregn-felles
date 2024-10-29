package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BruttoTilsynsutgiftBarn
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal object NettoTilsynsutgiftBeregning {
    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {
        // Summerer faktisk utgift pr barn
//        val faktiskUtgiftListeSummertPerBarn = grunnlag.faktiskUtgiftListe
//            .filter { it.beregnetBeløp > BigDecimal.ZERO }
//            .groupBy { it.gjelderBarn }
//            .mapValues { it -> it.value.sumOf { it.beregnetBeløp } }
//
//        // Summerer tilleggsstønad pr barn
//        val tilleggsstønadSummertPerBarn = grunnlag.tilleggsstønadListe
//            .filter { it.beregnetBeløp > BigDecimal.ZERO }
//            .groupBy { it.gjelderBarn }
//            .mapValues { it -> it.value.sumOf { it.beregnetBeløp } }

        val antallBarnIPerioden = grunnlag.faktiskUtgiftListe.size

        val sjablonSkattAlminneligInntektProsent =
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .firstOrNull { it.type == SjablonTallNavn.SKATT_ALMINNELIG_INNTEKT_PROSENT.navn }
                ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for skatt alminnelig inntekt.")

        val sjablonMaksTilsynsbeløp = grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.maxBeløpTilsyn

        val sjablonMaksFradragsbeløp = grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.maxBeløpFradrag

        // lag samletFaktiskUtgiftBeløp
        val samletFaktiskUtgiftBeløp = grunnlag.faktiskUtgiftListe.sumOf { it.beregnetBeløp }

        // lag samletTilleggstønadBeløp
        val samletTilleggstønadBeløp = grunnlag.tilleggsstønadListe.sumOf { it.beregnetBeløp }

        val skattefradragsbeløpPerBarn =
            beregnFradragsbeløpPerBarn(
                antallBarnIPerioden = antallBarnIPerioden,
                samletFaktiskUtgiftBeløp = minOf(samletFaktiskUtgiftBeløp, sjablonMaksTilsynsbeløp),
                sjablonSkattesatsAlminneligInntektProsent = sjablonSkattAlminneligInntektProsent.verdi.toBigDecimal(),
                sjablonMaksFradragsbeløp = sjablonMaksFradragsbeløp,
            )

        var bruttoTilsynsutgift = BigDecimal.ZERO

        val bruttoTilsynsutgiftBarnListe = mutableListOf<BruttoTilsynsutgiftBarn>()

        // Finner prosentandel av totalbeløp og beregner så andel av maks tilsynsbeløp
        grunnlag.faktiskUtgiftListe.forEach {
            bruttoTilsynsutgift =
                if (samletFaktiskUtgiftBeløp > sjablonMaksTilsynsbeløp) {
                    it.beregnetBeløp.divide(samletFaktiskUtgiftBeløp, MathContext(2, RoundingMode.HALF_UP)) * sjablonMaksTilsynsbeløp
                } else {
                    it.beregnetBeløp
                }

            bruttoTilsynsutgiftBarnListe.add(
                BruttoTilsynsutgiftBarn(
                    it.gjelderBarn,
                    bruttoTilsynsutgift,
                    bruttoTilsynsutgift - skattefradragsbeløpPerBarn,
                ),
            )

            // Trekker fra beregnet fradragsbeløp
            bruttoTilsynsutgift -= skattefradragsbeløpPerBarn
        }

        val resultat = NettoTilsynsutgiftBeregningResultat(
            nettoTilsynsutgiftBeløp = (bruttoTilsynsutgift - samletTilleggstønadBeløp).coerceAtLeast(BigDecimal.ZERO),
            samletFaktiskUtgiftBeløp = samletFaktiskUtgiftBeløp,
            samletTilleggstønadBeløp = samletTilleggstønadBeløp,
            skattefradragsbeløpPerBarn = skattefradragsbeløpPerBarn,
            bruttoTilsynsutgiftBarnListe = bruttoTilsynsutgiftBarnListe,
            grunnlagsreferanseListe =
            grunnlag.faktiskUtgiftListe.map { it.referanse } +
                grunnlag.tilleggsstønadListe.map { it.referanse } +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse } +
                grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.referanse +
                grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.referanse,

        )
        return resultat
    }

//    private fun finnMaksTilsynsbeløp(
//        antallBarnIPerioden: Int,
//        sjablonMaksTilsynsbeløpBeregningGrunnlag: <SjablonMaksTilsynsbeløpBeregningGrunnlag,
//    ): BigDecimal = sjablonMaksTilsynsbeløpBeregningGrunnlag
//        .filter { it.antallBarnTom >= antallBarnIPerioden }
//        .sortedBy { it.antallBarnTom }
//        .map { it.maxBeløpTilsyn }
//        .firstOrNull() ?: BigDecimal.ZERO
//
//    private fun finnMaksFradragsbeløp(
//        antallBarnIPerioden: Int,
//        sjablonMaksFradragsbeløpBeregningGrunnlag: SjablonMaksFradragsbeløpBeregningGrunnlag,
//    ): BigDecimal = sjablonMaksFradragsbeløpBeregningGrunnlag
//        .filter { it.antallBarnTom >= antallBarnIPerioden }
//        .sortedBy { it.antallBarnTom }
//        .map { it.maxBeløpFradrag }
//        .firstOrNull() ?: BigDecimal.ZERO

    private fun beregnFradragsbeløpPerBarn(
        antallBarnIPerioden: Int,
        samletFaktiskUtgiftBeløp: BigDecimal,
        sjablonSkattesatsAlminneligInntektProsent: BigDecimal,
        sjablonMaksFradragsbeløp: BigDecimal,
    ): BigDecimal {
        val skatteSatsOmregnet = sjablonSkattesatsAlminneligInntektProsent.divide(BigDecimal(100)).avrundetMedTiDesimaler
        val maksFradragsbeløp = sjablonMaksFradragsbeløp * skatteSatsOmregnet
        val fradragsbeløp = minOf((samletFaktiskUtgiftBeløp * skatteSatsOmregnet), maksFradragsbeløp)

        return fradragsbeløp.divide(antallBarnIPerioden.toBigDecimal()).avrundetMedTiDesimaler
    }
}
