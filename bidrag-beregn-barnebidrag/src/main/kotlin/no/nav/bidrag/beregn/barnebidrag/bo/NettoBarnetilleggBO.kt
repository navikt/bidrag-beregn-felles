package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.enums.barnetillegg.Barnetilleggstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import java.math.BigDecimal

data class NettoBarnetilleggPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val barnetilleggPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
    val barnetilleggSkattesatsListe: List<BarnetilleggSkattesatsDelberegningPeriodeGrunnlag>,
)

data class BarnetilleggSkattesatsDelberegningPeriodeGrunnlag(
    val referanse: String,
    val barnetilleggSkattesatsPeriode: DelberegningBarnetilleggSkattesats,
)

data class NettoBarnetilleggBeregningGrunnlag(
    val skattFaktor: SkattFaktorBeregningsgrunnlag,
    val barnetilleggBeregningGrunnlagListe: List<BarnetilleggBeregningsgrunnlag>,
)

data class SkattFaktorBeregningsgrunnlag(val referanse: String, val skattFaktor: BigDecimal)
data class BarnetilleggBeregningsgrunnlag(val referanse: String, val barnetilleggstype: Barnetilleggstype, val beløp: BigDecimal)

data class NettoBarnetilleggBeregningResultat(
    val summertBruttoBarnetillegg: BigDecimal,
    val summertNettoBarnetillegg: BigDecimal,
    val barnetilleggTypeListe: List<Barnetillegg>,
)
