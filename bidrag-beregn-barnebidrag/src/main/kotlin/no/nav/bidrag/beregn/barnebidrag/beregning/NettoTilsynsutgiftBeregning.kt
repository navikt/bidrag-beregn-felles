package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningResultat

internal object NettoTilsynsutgiftBeregning {
    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {
        val samværsklasse = grunnlag.samværsklasseBeregningGrunnlag.samværsklasse
        val alder = grunnlag.søknadsbarn.alder

        val sjablon = grunnlag.sjablonNettoTilsynsutgiftBeregningGrunnlagListe
            .firstOrNull {
                it.samværsklasse == samværsklasse && it.alderTom == alder
            } ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for samværsklasse $samværsklasse og alder $alder.")

        return NettoTilsynsutgiftBeregningResultat(
            beløpFradrag = sjablon.beløpFradrag,
            grunnlagsreferanseListe = listOf(
                grunnlag.søknadsbarn.referanse,
                grunnlag.samværsklasseBeregningGrunnlag.referanse,
                sjablon.referanse,
            ),
        )
    }
}
