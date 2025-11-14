package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetrygdType
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal

internal object UnderholdskostnadBeregning {

    fun beregn(grunnlag: UnderholdskostnadBeregningGrunnlag, barnetrygdType: BarnetrygdType): UnderholdskostnadBeregningResultat {
        // Henter sjablonverdier
        val sjablonverdier = hentSjablonverdier(grunnlag)

        val barnetrygdBeløp: BigDecimal

        // Setter riktig barnetrygdbeløp og Fjerner sjabloner som ikke skal brukes
        when (barnetrygdType) {
            BarnetrygdType.INGEN -> {
                barnetrygdBeløp = BigDecimal.ZERO
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
            }

            BarnetrygdType.ORDINÆR -> {
                barnetrygdBeløp = sjablonverdier.ordinærBarnetrygd
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
            }

            BarnetrygdType.FORHØYET -> {
                barnetrygdBeløp = sjablonverdier.forhøyetBarnetrygd
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.removeIf { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
            }
        }

        val beregnetUnderholdskostnad = (
            sjablonverdier.forbruksutgifterBeløp.add(sjablonverdier.boutgifterBidragsbarn).add(sjablonverdier.barnetilsynBeløp ?: BigDecimal.ZERO)
                .add(
                    grunnlag.nettoTilsynsutgiftBeregningGrunnlag?.nettoTilsynsutgift ?: BigDecimal.ZERO,
                ).subtract(barnetrygdBeløp)
            ).coerceAtLeast(BigDecimal.ZERO)

        val underholdskostnadBeregningResultat = UnderholdskostnadBeregningResultat(
            forbruksutgift = sjablonverdier.forbruksutgifterBeløp.avrundetMedToDesimaler,
            boutgift = sjablonverdier.boutgifterBidragsbarn.avrundetMedToDesimaler,
            barnetilsynMedStønad = sjablonverdier.barnetilsynBeløp?.avrundetMedToDesimaler,
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

    private fun hentSjablonverdier(grunnlag: UnderholdskostnadBeregningGrunnlag): Sjablonverdier {
        val boutgifterBidragsbarn = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            ).toBigDecimal()

        val ordinærBarnetrygd = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            ).toBigDecimal()

        val forhøyetBarnetrygd = (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.FORHØYET_BARNETRYGD_BELØP.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            ).toBigDecimal()

        // Hvis alderjustering hentes beløp fra siste vedtak og ikke fra sjablon
        val barnetilsynBeløp = grunnlag.barnetilsynMedStønad?.beløp ?: grunnlag.sjablonBarnetilsynBeregningGrunnlag?.beløpBarnetilsyn

        val forbruksutgifterBeløp = grunnlag.sjablonForbruksutgifterBeregningGrunnlag.beløpForbrukTotalt

        return Sjablonverdier(
            boutgifterBidragsbarn = boutgifterBidragsbarn,
            ordinærBarnetrygd = ordinærBarnetrygd,
            forhøyetBarnetrygd = forhøyetBarnetrygd,
            barnetilsynBeløp = barnetilsynBeløp,
            forbruksutgifterBeløp = forbruksutgifterBeløp,
        )
    }

    private data class Sjablonverdier(
        val boutgifterBidragsbarn: BigDecimal,
        val ordinærBarnetrygd: BigDecimal,
        val forhøyetBarnetrygd: BigDecimal,
        val barnetilsynBeløp: BigDecimal?,
        val forbruksutgifterBeløp: BigDecimal,
    )
}
