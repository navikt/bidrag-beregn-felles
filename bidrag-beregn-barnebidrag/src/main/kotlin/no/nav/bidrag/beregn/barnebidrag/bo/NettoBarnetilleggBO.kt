package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilleggPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import java.math.BigDecimal

data class NettoBarnetilleggPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val barnetilleggSkattesatsListe: List<BarnetilleggSkattesatsDelberegningPeriodeGrunnlag>,
    val barnetilleggPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
)

data class BarnetilleggSkattesatsDelberegningPeriodeGrunnlag(
    val referanse: String,
    val gjelderReferanse: String,
    val barnetilleggSkattesatsPeriode: DelberegningBarnetilleggSkattesats,
)

data class BarnetilleggPeriodeGrunnlag(val referanse: String, val barnetilleggPeriode: BarnetilleggPeriode)

data class NettoBarnetilleggPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: NettoBarnetilleggBeregningResultat)

data class NettoBarnetilleggBeregningGrunnlag(
    val skattFaktorGrunnlag: SkattFaktorBeregningGrunnlag,
    val barnetilleggBeregningGrunnlagListe: List<BarnetilleggBeregningGrunnlag>,
)

data class SkattFaktorBeregningGrunnlag(val referanse: String, val skattFaktor: BigDecimal)

data class BarnetilleggBeregningGrunnlag(val referanse: String, val barnetilleggstype: Inntektstype, val bruttoBarnetillegg: BigDecimal)

data class NettoBarnetilleggBeregningResultat(
    val summertBruttoBarnetillegg: BigDecimal,
    val summertNettoBarnetillegg: BigDecimal,
    val barnetilleggTypeListe: List<Barnetillegg>,
    val grunnlagsreferanseListe: List<String>,
)
