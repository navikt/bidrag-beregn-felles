package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetrygdType
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal

internal object UnderholdskostnadBeregning {
    var sjablonverdiBoutgifterBidragsbarn = BigDecimal.ZERO
    var sjablonverdiOrdinærBarnetrygd = BigDecimal.ZERO
    var sjablonverdiForhøyetBarnetrygd = BigDecimal.ZERO
    var sjablonverdiBarnetilsynBeløp: BigDecimal? = BigDecimal.ZERO
    var sjablonverdiForbruksutgifterBeløp = BigDecimal.ZERO

    fun beregn(grunnlag: UnderholdskostnadBeregningGrunnlag, barnetrygdType: BarnetrygdType): UnderholdskostnadBeregningResultat {
        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val barnetrygdBeløp = when (barnetrygdType) {
            BarnetrygdType.INGEN -> BigDecimal.ZERO
            BarnetrygdType.ORDINÆR -> sjablonverdiOrdinærBarnetrygd
            BarnetrygdType.FORHØYET -> sjablonverdiForhøyetBarnetrygd
        }

        val beregnetUnderholdskostnad = (
            sjablonverdiForbruksutgifterBeløp.add(sjablonverdiBoutgifterBidragsbarn).add(sjablonverdiBarnetilsynBeløp ?: BigDecimal.ZERO).add(
                grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.nettoTilsynsutgift ?: BigDecimal.ZERO,
            ).subtract(barnetrygdBeløp)
            ).coerceAtLeast(BigDecimal.ZERO)

        val underholdskostnadBeregningResultat = UnderholdskostnadBeregningResultat(
            forbruksutgift = sjablonverdiForbruksutgifterBeløp.avrundetMedToDesimaler,
            boutgift = sjablonverdiBoutgifterBidragsbarn.avrundetMedToDesimaler,
            barnetilsynMedStønad = sjablonverdiBarnetilsynBeløp?.avrundetMedToDesimaler,
            nettoTilsynsutgift = grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.nettoTilsynsutgift?.avrundetMedToDesimaler,
            barnetrygd = barnetrygdBeløp.avrundetMedToDesimaler,
            underholdskostnad = beregnetUnderholdskostnad.avrundetMedToDesimaler,
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

        sjablonverdiOrdinærBarnetrygd = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()

        sjablonverdiForhøyetBarnetrygd = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()

        sjablonverdiBarnetilsynBeløp = grunnlag.sjablonBarnetilsynBeregningGrunnlag?.beløpBarnetilsyn
        sjablonverdiForbruksutgifterBeløp = grunnlag.sjablonForbruksutgifterBeregningGrunnlag.beløpForbrukTotalt
    }
}
