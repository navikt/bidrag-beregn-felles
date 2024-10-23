package no.nav.bidrag.beregn.barnebidrag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Delberegning
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagPeriodeInnhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import java.math.BigDecimal
import java.time.LocalDate

data class NettoTilsynsutgiftPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
//    val fødselsdatoBarn: LocalDate,
    val faktiskUtgiftPeriodeCoreListe: List<FaktiskUtgiftPeriodeCore>,
    val tilleggsstønadPeriodeCoreListe: List<TilleggsstønadPeriodeCore>,
    var sjablonMaksTilsynsbeløpPeriodeGrunnlagListe: List<SjablonMaksTilsynsbeløpPeriodeGrunnlag>,
    var sjablonMaksFradragsbeløpPeriodeGrunnlagListe: List<SjablonMaksFradragsbeløpPeriodeGrunnlag>,
)

data class FaktiskTilsynsutgift(val referanseBarn: String, val beløp: BigDecimal)

data class Kostpenger(val referanseBarn: String, val beløp: BigDecimal)

data class Tilleggstønad(val referanseBarn: String, val beløp: BigDecimal)

data class SjablonMaksTilsynsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksTilsynsbeløpPeriode: SjablonMaksTilsynPeriode)

data class SjablonMaksFradragsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksFradragsbeløpPeriode: SjablonMaksFradragPeriode)

data class NettoTilsynsutgiftPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: NettoTilsynsutgiftBeregningResultat)

data class NettoTilsynsutgiftBeregningGrunnlag(
    val faktiskUtgiftListe: List<FaktiskUtgift>,
    val sjablonMaksTilsynsbeløpBeregningGrunnlag: List<SjablonMaksTilsynsbeløpBeregningGrunnlag>,
    val sjablonMaksFradragsbeløpBeregningGrunnlag: List<SjablonMaksFradragsbeløpBeregningGrunnlag>,
)

data class SjablonMaksTilsynsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpTilsyn: BigDecimal)
data class SjablonMaksFradragsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpFradrag: BigDecimal)

data class NettoTilsynsutgiftBeregningResultat(val beløp: BigDecimal, val grunnlagsreferanseListe: List<String>)

data class FaktiskUtgift(val referanseBarn: String, val beregnetBeløp: BigDecimal)

data class FaktiskUtgiftPeriode(
    val referanse: String,
    override val periode: ÅrMånedsperiode,
    @Schema(description = "Referanse til barnet utgiften gjelder")
    val referanseBarn: Grunnlagsreferanse,
    val fødselsdatoBarn: LocalDate,
    val faktiskUtgiftBeløp: BigDecimal,
    val kostpengerBeløp: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

data class TilleggsstønadPeriode(
    val referanse: String,
    override val periode: ÅrMånedsperiode,
    val beløpDagsats: BigDecimal,
    @Schema(description = "Referanse til barnet stønaden mottas for")
    val referanseBarn: Grunnlagsreferanse,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

data class DelberegningFaktiskTilsynsutgift(override val periode: ÅrMånedsperiode, val beregnetBeløp: BigDecimal) : Delberegning

data class DelberegningTilleggsstønad(override val periode: ÅrMånedsperiode, val beregnetBeløp: BigDecimal) : Delberegning
