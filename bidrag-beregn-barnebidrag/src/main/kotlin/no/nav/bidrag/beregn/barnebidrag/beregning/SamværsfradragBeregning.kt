package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragBeregningResultat
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import java.math.BigDecimal
import java.math.RoundingMode

internal object SamværsfradragBeregning {
    fun beregn(grunnlag: SamværsfradragBeregningGrunnlag): SamværsfradragBeregningResultat {
        val samværsklasse = grunnlag.samværsklasseBeregningGrunnlag.samværsklasse
        val alder = grunnlag.søknadsbarn.alder

        // Samværsklasse DELT_BOSTED gir ingen fradrag og finnes ikke som sjablonverdi
        if (samværsklasse == Samværsklasse.DELT_BOSTED) {
            return SamværsfradragBeregningResultat(
                beløpFradrag = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                grunnlagsreferanseListe = listOf(
                    grunnlag.søknadsbarn.referanse,
                    grunnlag.samværsklasseBeregningGrunnlag.referanse,
                ),
            )
        }

        val sjablon = grunnlag.sjablonSamværsfradragBeregningGrunnlagListe
            .firstOrNull {
                it.samværsklasse == samværsklasse && it.alderTom == alder
            } ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for samværsklasse $samværsklasse og alder $alder.")

        return SamværsfradragBeregningResultat(
            beløpFradrag = sjablon.beløpFradrag.setScale(2, RoundingMode.HALF_UP),
            grunnlagsreferanseListe = listOf(
                grunnlag.søknadsbarn.referanse,
                grunnlag.samværsklasseBeregningGrunnlag.referanse,
                sjablon.referanse,
            ),
        )
    }
}
