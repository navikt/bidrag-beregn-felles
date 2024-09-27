package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.data.SamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.data.SamværsfradragBeregningResultat

internal object SamværsfradragBeregning {
    fun beregn(grunnlag: SamværsfradragBeregningGrunnlag): SamværsfradragBeregningResultat {
        val samværsklasse = grunnlag.samværsklasseBeregningGrunnlag.samværsklasse
        val alder = grunnlag.søknadsbarn.alder

        val sjablon = grunnlag.sjablonSamværsfradragBeregningGrunnlagListe
            .firstOrNull {
                it.samværsklasse == samværsklasse && it.alderTom == alder
            } ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for samværsklasse $samværsklasse og alder $alder.")

        return SamværsfradragBeregningResultat(
            beløpFradrag = sjablon.beløpFradrag,
            grunnlagsreferanseListe = listOf(
                grunnlag.søknadsbarn.referanse,
                grunnlag.samværsklasseBeregningGrunnlag.referanse,
                sjablon.referanse,
            ),
        )
    }
}
