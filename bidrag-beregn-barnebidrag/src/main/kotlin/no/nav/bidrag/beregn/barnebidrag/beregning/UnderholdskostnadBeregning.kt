package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetrygdType
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal

internal object UnderholdskostnadBeregning {
    private var sjablonverdiBoutgifterBidragsbarn = BigDecimal.ZERO
    private var sjablonverdiOrdinærBarnetrygd = BigDecimal.ZERO
    private var sjablonverdiForhøyetBarnetrygd = BigDecimal.ZERO
    private var sjablonverdiBarnetilsynBeløp: BigDecimal? = BigDecimal.ZERO
    private var sjablonverdiForbruksutgifterBeløp = BigDecimal.ZERO

    fun beregn(grunnlag: UnderholdskostnadBeregningGrunnlag, barnetrygdType: BarnetrygdType): UnderholdskostnadBeregningResultat {
        // Henter sjablonverdier
        hentSjablonverdier(grunnlag)

        val barnetrygdBeløp: BigDecimal

        // Setter riktig barnetrygdbeløp og Fjerner sjabloner som ikke skal brukes
        when (barnetrygdType) {
            BarnetrygdType.INGEN -> {
                barnetrygdBeløp = BigDecimal.ZERO
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
            }

            BarnetrygdType.ORDINÆR -> {
                barnetrygdBeløp = sjablonverdiOrdinærBarnetrygd
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
            }

            BarnetrygdType.FORHØYET -> {
                barnetrygdBeløp = sjablonverdiForhøyetBarnetrygd
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
            }
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
