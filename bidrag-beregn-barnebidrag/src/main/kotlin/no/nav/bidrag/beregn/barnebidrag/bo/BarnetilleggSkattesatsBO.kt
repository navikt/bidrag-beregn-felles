package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import java.math.BigDecimal

data class BarnetilleggSkattesatsPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val sumInntektBeregningGrunnlag: List<SumInntektDelberegningPeriodeGrunnlag>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
    var sjablonTrinnvisSkattesatsPeriodeGrunnlagListe: List<SjablonTrinnvisSkattesatsPeriodeGrunnlag>,
)

data class SumInntektDelberegningPeriodeGrunnlag(val referanse: String, val sumInntektPeriode: DelberegningSumInntekt)

data class BarnetilleggSkattesatsPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: BarnetilleggSkattesatsBeregningResultat)

data class BarnetilleggSkattesatsBeregningGrunnlag(
    val inntektBeregningGrunnlag: InntektBeregningGrunnlag,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
    val sjablonTrinnvisSkattesatsBeregningGrunnlag: SjablonTrinnvisSkattesatsBeregningGrunnlag,
)

data class BarnetilleggSkattesatsBeregningResultat(
    val skattFaktor: BigDecimal,
    val minstefradrag: BigDecimal,
    val skattAlminneligInntekt: BigDecimal,
    val trygdeavgift: BigDecimal,
    val trinnskatt: BigDecimal,
    val sumSkatt: BigDecimal,
    val sumInntekt: BigDecimal,
    val grunnlagsreferanseListe: List<String>
)
