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

        var harBruktLøpendeBidrag = false
        var harBruktPrivatAvtale = false
        var faktiskEndringFaktor: BigDecimal? = null

        // Beregner faktisk endring faktor. Sjekker først om det finnes løpende bidrag å sammenligne mot. Hvis ikke sjekkes det om det finnes privat
        // avtale å sammenligne mot. Hvis ingen av delene finnes eller beregnet bidrag er null settes faktisk endring faktor til null.
        if (grunnlag.beregnetBidragBeregningGrunnlag.beløp != null) {
            if (grunnlag.løpendeBidragBeregningGrunnlag?.beløp != null) {
                faktiskEndringFaktor = grunnlag.beregnetBidragBeregningGrunnlag.beløp
                    .divide(grunnlag.løpendeBidragBeregningGrunnlag.beløp, 10, RoundingMode.HALF_UP)
                    .minus(BigDecimal(1))
                    .abs()
                harBruktLøpendeBidrag = true
            } else if (grunnlag.privatAvtaleBeregningGrunnlag?.beløp != null) {
                faktiskEndringFaktor = grunnlag.beregnetBidragBeregningGrunnlag.beløp
                    .divide(grunnlag.privatAvtaleBeregningGrunnlag.beløp, 10, RoundingMode.HALF_UP)
                    .minus(BigDecimal(1))
                    .abs()
                harBruktPrivatAvtale = true
            }
        }

        // Sjekker om endring er over grense. true hvis:
        // - faktisk endring > sjablonverdi for endringsgrense (normalcase)
        // - faktisk endring er null og beregnet bidragsbeløp ikke er null (beløpshistorikk mangler)
        val endringErOverGrense = ((faktiskEndringFaktor != null) && (faktiskEndringFaktor > endringsgrenseFaktor)) ||
            (faktiskEndringFaktor == null && grunnlag.beregnetBidragBeregningGrunnlag.beløp != null)

        return EndringSjekkGrensePeriodeBeregningResultat(
            faktiskEndringFaktor = faktiskEndringFaktor?.avrundetMedTiDesimaler,
            endringErOverGrense = endringErOverGrense,
            harBruktLøpendeBidrag = harBruktLøpendeBidrag,
            harBruktPrivatAvtale = harBruktPrivatAvtale,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.beregnetBidragBeregningGrunnlag.referanse,
                grunnlag.løpendeBidragBeregningGrunnlag?.referanse,
                grunnlag.privatAvtaleBeregningGrunnlag?.referanse,
            ) +
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse },
        )
    }

    private fun hentSjablonverdi(grunnlag: EndringSjekkGrensePeriodeBeregningGrunnlag): BigDecimal = (
        grunnlag.sjablonSjablontallBeregningGrunnlagListe
            .filter { it.type == SjablonTallNavn.ENDRING_BIDRAG_GRENSE_PROSENT.navn }
            .map { it.verdi }
            .firstOrNull() ?: 0.0
        )
        .toBigDecimal()
}
