package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import java.math.BigDecimal
import java.math.RoundingMode

internal object EndringSjekkGrensePeriodeBeregning {

    private val bigDecimal100 = BigDecimal.valueOf(100)

    fun beregn(grunnlag: EndringSjekkGrensePeriodeBeregningGrunnlag): EndringSjekkGrensePeriodeBeregningResultat {
        // Henter sjablonverdi
        val sjablonverdiEndringBidragGrenseProsent = hentSjablonverdi(grunnlag)
        val endringsgrenseFaktor = sjablonverdiEndringBidragGrenseProsent.divide(bigDecimal100, 10, RoundingMode.HALF_UP)

        var løpendeBidragFraPrivatAvtale = false
        var løpendeBidragBeløp: BigDecimal? = grunnlag.løpendeBidragBeregningGrunnlag?.beløp?.avrundetMedNullDesimaler
        val beregnetBidragBeløp = grunnlag.beregnetBidragBeregningGrunnlag.beløp?.avrundetMedToDesimaler
        val faktiskEndringFaktor: BigDecimal?

        // Beregner faktisk endring faktor. Sjekker først om det finnes løpende bidrag å sammenligne mot. Hvis ikke sjekkes det om det finnes privat
        // avtale å sammenligne mot.
        if (grunnlag.løpendeBidragBeregningGrunnlag?.beløp == null && grunnlag.privatAvtaleBeregningGrunnlag?.beløp != null) {
            løpendeBidragFraPrivatAvtale = true
            løpendeBidragBeløp = grunnlag.privatAvtaleBeregningGrunnlag.beløp
        }

        when {
            // Hvis både løpende bidrag og beregnet bidrag er null settes faktor til null
            løpendeBidragBeløp == null && beregnetBidragBeløp == null -> {
                faktiskEndringFaktor = null
            }
            // Hvis løpende bidrag er null settes faktor til 1 hvis beregnet bidrag er ulik 0, ellers null
            løpendeBidragBeløp == null -> {
                faktiskEndringFaktor = if (beregnetBidragBeløp == BigDecimal.ZERO.avrundetMedToDesimaler) {
                    null
                } else {
                    BigDecimal.ONE
                }
            }
            // Hvis beregnet bidrag er null settes faktor til 1 hvis løpende bidrag er ulik 0, ellers null
            beregnetBidragBeløp == null -> {
                faktiskEndringFaktor = if (løpendeBidragBeløp == BigDecimal.ZERO) {
                    null
                } else {
                    BigDecimal.ONE
                }
            }
            // Hvis både løpende bidrag og beregnet bidrag er 0 settes faktor til 0
            løpendeBidragBeløp == BigDecimal.ZERO && beregnetBidragBeløp == BigDecimal.ZERO.avrundetMedToDesimaler -> {
                faktiskEndringFaktor = BigDecimal.ZERO
            }
            // Hvis både løpende bidrag eller beregnet bidrag er 0 (og underforstått at det andre beløpet er større enn 0) settes faktor til 0
            løpendeBidragBeløp == BigDecimal.ZERO || beregnetBidragBeløp == BigDecimal.ZERO.avrundetMedToDesimaler -> {
                faktiskEndringFaktor = BigDecimal.ONE
            }
            else -> {
                // Hvis begge beløp er ulik 0 beregnes faktor
                faktiskEndringFaktor = beregnetBidragBeløp.divide(løpendeBidragBeløp, 10, RoundingMode.HALF_UP).minus(BigDecimal(1)).abs()
            }
        }

        // Endring er over grense hvis fakktisk endring ikke er null og faktisk endring > sjablonverdi for endringsgrense
        val endringErOverGrense = faktiskEndringFaktor != null && faktiskEndringFaktor > endringsgrenseFaktor

        return EndringSjekkGrensePeriodeBeregningResultat(
            løpendeBidragBeløp = løpendeBidragBeløp?.avrundetMedNullDesimaler,
            løpendeBidragFraPrivatAvtale = løpendeBidragFraPrivatAvtale,
            beregnetBidragBeløp = beregnetBidragBeløp?.avrundetMedToDesimaler,
            faktiskEndringFaktor = faktiskEndringFaktor?.avrundetMedTiDesimaler,
            endringErOverGrense = endringErOverGrense,
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
