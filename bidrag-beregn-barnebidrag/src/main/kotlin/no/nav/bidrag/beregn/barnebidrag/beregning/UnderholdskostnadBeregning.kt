package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal

internal object UnderholdskostnadBeregning {
    var sjablonverdiBoutgifterBidragsbarn = BigDecimal.ZERO
    var sjablonverdiBarnetrygd = BigDecimal.ZERO
    var sjablonverdiBarnetilsynBeløp: BigDecimal? = BigDecimal.ZERO
    var sjablonverdiForbruksutgifterBeløp = BigDecimal.ZERO

    fun beregn(grunnlag: UnderholdskostnadBeregningGrunnlag): UnderholdskostnadBeregningResultat {
        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val beregnetUnderholdskostnad = (
            sjablonverdiForbruksutgifterBeløp.add(sjablonverdiBoutgifterBidragsbarn).add(sjablonverdiBarnetilsynBeløp).add(
                grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.nettoTilsynsutgift,
            ).subtract(sjablonverdiBarnetrygd)
            )

        val underholdskostnadBeregningResultat = UnderholdskostnadBeregningResultat(
            forbruksutgift = sjablonverdiForbruksutgifterBeløp,
            boutgift = sjablonverdiBoutgifterBidragsbarn,
            barnetilsynMedStønad = sjablonverdiBarnetilsynBeløp,
            nettoTilsynsutgift = grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.nettoTilsynsutgift,
            barnetrygd = sjablonverdiBarnetrygd,
            underholdskostnad = beregnetUnderholdskostnad,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.barnetilsynMedStønad?.referanse,
                grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.referanse,
                grunnlag.sjablonBarnetilsynBeregningGrunnlag?.referanse,
                grunnlag.sjablonForbruksutgifterBeregningGrunnlag.referanse,
            ).plus(
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse },
            ),
        )

        return underholdskostnadBeregningResultat
    }

    private fun hentSjablonverdier(grunnlag: UnderholdskostnadBeregningGrunnlag) {
        sjablonverdiBoutgifterBidragsbarn = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()

        sjablonverdiBarnetrygd = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()

        sjablonverdiBarnetilsynBeløp = grunnlag.sjablonBarnetilsynBeregningGrunnlag?.beløpBarnetilsyn
        sjablonverdiForbruksutgifterBeløp = grunnlag.sjablonForbruksutgifterBeregningGrunnlag.beløpForbrukTotalt
    }
}
