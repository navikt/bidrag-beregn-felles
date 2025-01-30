package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningResultat
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import java.math.BigDecimal
import java.math.RoundingMode

internal object NettoBarnetilleggBeregning {
    // Beregner netto barnetillegg for alle barn  med barnetillegg.

    val bigDecimal12 = BigDecimal.valueOf(12)

    fun beregn(grunnlag: NettoBarnetilleggBeregningGrunnlag): NettoBarnetilleggBeregningResultat {

        // Barnetillegg tiltakspenger er skattefritt (nettoverdi). Filtreres bort fra sum brutto og legges til i sum netto.
        val barnetilleggTiltakspenger =
            grunnlag.barnetilleggBeregningGrunnlagListe
                .filter { it.barnetilleggstype == Inntektstype.BARNETILLEGG_TILTAKSPENGER }
                .sumOf { it.bruttoBarnetillegg }.divide(bigDecimal12, 10, RoundingMode.HALF_UP)

        val summertBruttoBarnetillegg =
            grunnlag.barnetilleggBeregningGrunnlagListe
                .filter { it.barnetilleggstype != Inntektstype.BARNETILLEGG_TILTAKSPENGER }
                .sumOf { it.bruttoBarnetillegg }.divide(bigDecimal12, 10, RoundingMode.HALF_UP)

        val summertNettoBarnetillegg =
            summertBruttoBarnetillegg
                .minus(beregnSkattefradrag(beløp = summertBruttoBarnetillegg, skattFaktor = grunnlag.skattFaktorGrunnlag.skattFaktor))
                .plus(barnetilleggTiltakspenger)

        val barnetilleggTypeListe = grunnlag.barnetilleggBeregningGrunnlagListe.map {
            Barnetillegg(
                barnetilleggType = it.barnetilleggstype,
                bruttoBarnetillegg =
                    if (it.barnetilleggstype != Inntektstype.BARNETILLEGG_TILTAKSPENGER) {
                        it.bruttoBarnetillegg.divide(bigDecimal12, 10, RoundingMode.HALF_UP)
                    } else {
                        BigDecimal.ZERO
                    }.avrundetMedToDesimaler,
                nettoBarnetillegg =
                    if (it.barnetilleggstype != Inntektstype.BARNETILLEGG_TILTAKSPENGER) {
                        it.bruttoBarnetillegg.minus(
                            beregnSkattefradrag(
                                beløp = it.bruttoBarnetillegg,
                                skattFaktor = grunnlag.skattFaktorGrunnlag.skattFaktor,
                            )
                        ).divide(bigDecimal12, 10, RoundingMode.HALF_UP)
                    } else {
                        it.bruttoBarnetillegg.divide(bigDecimal12, 10, RoundingMode.HALF_UP)
                    }.avrundetMedToDesimaler,
            )
        }

        val grunnlagsreferanseListe = grunnlag.barnetilleggBeregningGrunnlagListe.map { it.referanse } +
            grunnlag.skattFaktorGrunnlag.referanse

        return NettoBarnetilleggBeregningResultat(
            summertBruttoBarnetillegg = summertBruttoBarnetillegg.avrundetMedToDesimaler,
            summertNettoBarnetillegg = summertNettoBarnetillegg.avrundetMedToDesimaler,
            barnetilleggTypeListe = barnetilleggTypeListe,
            grunnlagsreferanseListe = grunnlagsreferanseListe,
        )
    }

    private fun beregnSkattefradrag(beløp: BigDecimal, skattFaktor: BigDecimal): BigDecimal = beløp.multiply(skattFaktor)
}
