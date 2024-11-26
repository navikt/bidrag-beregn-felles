package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.enums.barnetillegg.Barnetilleggstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Barnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagPeriodeInnhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import java.math.BigDecimal

data class NettoBarnetilleggPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val barnetilleggSkattesatsListe: List<BarnetilleggSkattesatsDelberegningPeriodeGrunnlag>,
    val barnetilleggPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag2>,
)

data class BarnetilleggSkattesatsDelberegningPeriodeGrunnlag(
    val referanse: String,
    val gjelderReferanse: String,
    val barnetilleggSkattesatsPeriode: DelberegningBarnetilleggSkattesats,
)

data class NettoBarnetilleggBeregningGrunnlag(
    val skattFaktorGrunnlag: SkattFaktorBeregningsgrunnlag,
    val barnetilleggBeregningGrunnlagListe: List<BarnetilleggBeregningsgrunnlag>,
)

data class BarnetilleggBeregningsgrunnlag(val referanse: String, val barnetilleggstype: Barnetilleggstype, val bruttoBarnetillegg: BigDecimal)

data class SkattFaktorBeregningsgrunnlag(val referanse: String, val skattFaktor: BigDecimal)

data class NettoBarnetilleggPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: NettoBarnetilleggBeregningResultat)

data class NettoBarnetilleggBeregningResultat(
    val summertBruttoBarnetillegg: BigDecimal,
    val summertNettoBarnetillegg: BigDecimal,
    val barnetilleggTypeListe: List<Barnetillegg>,
    val grunnlagsreferanseListe: List<String>,
)

// Skal erstattes av BarnetilleggPeriode etter endring av denne. Fjern skattFaktor og legg til Barnetilleggstype
data class BarnetilleggPeriode2(
    override val periode: ÅrMånedsperiode,
    val gjelderBarn: Grunnlagsreferanse,
    val type: Barnetilleggstype,
    val beløp: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

// Fjernes også
data class BarnetilleggPeriodeGrunnlag2(val referanse: String, val gjelderReferanse: String, val barnetilleggPeriode: BarnetilleggPeriode2)
