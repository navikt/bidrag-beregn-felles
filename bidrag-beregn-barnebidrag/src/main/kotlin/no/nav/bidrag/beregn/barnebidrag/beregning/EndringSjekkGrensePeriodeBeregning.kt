package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

internal object EndringSjekkGrensePeriodeBeregning {

    private val bigDecimal100 = BigDecimal.valueOf(100)

    fun beregn(grunnlag: EndringSjekkGrensePeriodeBeregningGrunnlag): EndringSjekkGrensePeriodeBeregningResultat {

        // Henter sjablonverdi
        val sjablonverdiEndringBidragGrenseProsent = hentSjablonverdi(grunnlag)
        val endringsgrenseFaktor = sjablonverdiEndringBidragGrenseProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP)

        // Beregner faktisk endring. Hvis det ikke er noe løpende bidrag eller beregnet bidrag er null, settes faktisk endring til null.
        val faktiskEndringFaktor =
            if (grunnlag.løpendeBidragBeregningGrunnlag?.beløp != null && grunnlag.beregnetBidragBeregningGrunnlag.beløp != null) {
                grunnlag.beregnetBidragBeregningGrunnlag.beløp
                    .divide(grunnlag.løpendeBidragBeregningGrunnlag.beløp, 10, RoundingMode.HALF_UP)
                    .minus(BigDecimal(1))
                    .abs()
            } else {
                null
            }

        // Sjekker om endring er over grense (true hvis faktisk endring > sjablonverdi for endringsgrense eller faktisk endring er null)
        val endringErOverGrense = (faktiskEndringFaktor == null) || (faktiskEndringFaktor > endringsgrenseFaktor)

        return EndringSjekkGrensePeriodeBeregningResultat(
            faktiskEndringFaktor = faktiskEndringFaktor?.avrundetMedTiDesimaler,
            endringErOverGrense = endringErOverGrense,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.beregnetBidragBeregningGrunnlag.referanse,
                grunnlag.løpendeBidragBeregningGrunnlag?.referanse
            )
                + grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse },
        )
    }

    private fun hentSjablonverdi(grunnlag: EndringSjekkGrensePeriodeBeregningGrunnlag): BigDecimal =
        (
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .filter { it.type == SjablonTallNavn.ENDRING_BIDRAG_GRENSE_PROSENT.navn }
                .map { it.verdi }
                .firstOrNull() ?: 0.0
            )
            .toBigDecimal()
}
