package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningResultat
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import java.math.BigDecimal

internal object NettoBarnetilleggBeregning {
    // Beregner netto barnetillegg for alle barn  med barnetillegg.

    fun beregn(grunnlag: NettoBarnetilleggBeregningGrunnlag): NettoBarnetilleggBeregningResultat {
        val summertBruttoBarnetillegg = grunnlag.barnetilleggBeregningGrunnlagListe.sumOf { it.bruttoBarnetillegg }

        val summertNettoBarnetillegg =
            summertBruttoBarnetillegg.minus(
                beregnSkattefradrag(
                    summertBruttoBarnetillegg,
                    grunnlag.skattFaktorGrunnlag.skattFaktor,
                ),
            ).avrundetMedToDesimaler

        val barnetilleggTypeListe = grunnlag.barnetilleggBeregningGrunnlagListe.map {
            Barnetillegg(
                barnetilleggType = it.barnetilleggstype,
                bruttoBarnetillegg = it.bruttoBarnetillegg,
                nettoBarnetillegg = it.bruttoBarnetillegg.minus(
                    beregnSkattefradrag(
                        it.bruttoBarnetillegg,
                        grunnlag.skattFaktorGrunnlag.skattFaktor,
                    ),
                ).avrundetMedToDesimaler,
            )
        }
        val grunnlagsreferanseListe = grunnlag.barnetilleggBeregningGrunnlagListe.map { it.referanse } +
            grunnlag.skattFaktorGrunnlag.referanse

        return NettoBarnetilleggBeregningResultat(
            summertBruttoBarnetillegg = summertBruttoBarnetillegg,
            summertNettoBarnetillegg = summertNettoBarnetillegg,
            barnetilleggTypeListe = barnetilleggTypeListe,
            grunnlagsreferanseListe = grunnlagsreferanseListe,
        )
    }

    private fun beregnSkattefradrag(beløp: BigDecimal, skattFaktor: BigDecimal): BigDecimal = beløp.multiply(skattFaktor)
}
