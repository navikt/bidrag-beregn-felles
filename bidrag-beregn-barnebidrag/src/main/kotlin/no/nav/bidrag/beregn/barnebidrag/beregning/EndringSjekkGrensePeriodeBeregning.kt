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
        var løpendeBidragReferanse = grunnlag.løpendeBidragBeregningGrunnlag?.referanse
        val beregnetBidragBeløp = grunnlag.beregnetBidragBeregningGrunnlag.beløp?.avrundetMedToDesimaler
        val faktiskEndringFaktor: BigDecimal?

        // Beregner faktisk endring faktor. Sjekker først om det finnes løpende bidrag å sammenligne mot. Hvis ikke sjekkes det om det finnes privat
        // avtale å sammenligne mot.
        if (grunnlag.løpendeBidragBeregningGrunnlag?.beløp == null && grunnlag.privatAvtaleBeregningGrunnlag?.beløp != null) {
            løpendeBidragFraPrivatAvtale = true
            løpendeBidragBeløp = grunnlag.privatAvtaleBeregningGrunnlag.beløp
            løpendeBidragReferanse = grunnlag.privatAvtaleBeregningGrunnlag.referanse
        }

        // Beregnet bidrag er null hvis barnet er selvforsørget eller søknadsbarnet bor hos BP (= avslag/opphør i perioden)
        // Løpende bidrag er null hvis det ikke finnes løpende bidrag eller privat avtale å sammenligne mot eller at det er avslag/opphør i perioden
        // Beregnet/løpende bidrag er 0 f.eks. hvis BP har manglende evne

        faktiskEndringFaktor = when {
            // Hvis både løpende bidrag og beregnet bidrag er null settes faktor til null
            løpendeBidragBeløp == null && beregnetBidragBeløp == null -> null

            // Hvis løpende bidrag eller beregnet bidrag er null (og underforstått at det andre beløpet ikke er null) settes faktor til 1
            løpendeBidragBeløp == null || beregnetBidragBeløp == null -> BigDecimal.ONE

            // Hvis både løpende bidrag og beregnet bidrag er 0 settes faktor til 0
            løpendeBidragBeløp == BigDecimal.ZERO && beregnetBidragBeløp == BigDecimal.ZERO.avrundetMedToDesimaler -> BigDecimal.ZERO

            // Hvis løpende bidrag eller beregnet bidrag er 0 (og underforstått at det andre beløpet er større enn 0) settes faktor til 1
            løpendeBidragBeløp == BigDecimal.ZERO || beregnetBidragBeløp == BigDecimal.ZERO.avrundetMedToDesimaler -> BigDecimal.ONE

            // Hvis begge beløp er ulik 0 beregnes faktor
            else -> beregnetBidragBeløp.divide(løpendeBidragBeløp, 10, RoundingMode.HALF_UP).minus(BigDecimal(1)).abs()
        }

        // Endring er over grense hvis faktisk endring ikke er null og faktisk endring > sjablonverdi for endringsgrense
        val endringErOverGrense = faktiskEndringFaktor != null && faktiskEndringFaktor > endringsgrenseFaktor

        return EndringSjekkGrensePeriodeBeregningResultat(
            løpendeBidragBeløp = løpendeBidragBeløp?.avrundetMedNullDesimaler,
            løpendeBidragFraPrivatAvtale = løpendeBidragFraPrivatAvtale,
            beregnetBidragBeløp = beregnetBidragBeløp?.avrundetMedToDesimaler,
            faktiskEndringFaktor = faktiskEndringFaktor?.avrundetMedTiDesimaler,
            endringErOverGrense = endringErOverGrense,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.beregnetBidragBeregningGrunnlag.referanse,
                løpendeBidragReferanse,
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
