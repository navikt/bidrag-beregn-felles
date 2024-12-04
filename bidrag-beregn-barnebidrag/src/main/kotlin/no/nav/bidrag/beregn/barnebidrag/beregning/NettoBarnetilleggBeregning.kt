package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningResultat
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import java.math.BigDecimal
import java.math.RoundingMode

internal object NettoBarnetilleggBeregning {
    // Beregner netto barnetillegg for alle barn  med barnetillegg.

    val bigDecimal12 = BigDecimal.valueOf(12)

    fun beregn(grunnlag: NettoBarnetilleggBeregningGrunnlag): NettoBarnetilleggBeregningResultat {
        val summertBruttoBarnetillegg =
            grunnlag.barnetilleggBeregningGrunnlagListe.sumOf { it.bruttoBarnetillegg }.divide(bigDecimal12, 10, RoundingMode.HALF_UP)

        val summertNettoBarnetillegg =
            summertBruttoBarnetillegg.minus(
                beregnSkattefradrag(
                    summertBruttoBarnetillegg,
                    grunnlag.skattFaktorGrunnlag.skattFaktor,
                ),
            )

        val barnetilleggTypeListe = grunnlag.barnetilleggBeregningGrunnlagListe.map {
            Barnetillegg(
                barnetilleggType = it.barnetilleggstype,
                bruttoBarnetillegg = it.bruttoBarnetillegg.divide(bigDecimal12, 10, RoundingMode.HALF_UP).avrundetMedToDesimaler,
                nettoBarnetillegg = it.bruttoBarnetillegg.minus(
                    beregnSkattefradrag(
                        it.bruttoBarnetillegg,
                        grunnlag.skattFaktorGrunnlag.skattFaktor,
                    ),
                ).divide(bigDecimal12, 10, RoundingMode.HALF_UP).avrundetMedToDesimaler,
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
