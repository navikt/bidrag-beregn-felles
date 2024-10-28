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
    val faktiskUtgiftPeriodeCoreListe: List<FaktiskUtgiftPeriodeCore>,
    val tilleggsstønadPeriodeCoreListe: List<TilleggsstønadPeriodeCore>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
    var sjablonMaksTilsynsbeløpPeriodeGrunnlagListe: List<SjablonMaksTilsynsbeløpPeriodeGrunnlag>,
    var sjablonMaksFradragsbeløpPeriodeGrunnlagListe: List<SjablonMaksFradragsbeløpPeriodeGrunnlag>,
)

data class SjablonMaksTilsynsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksTilsynsbeløpPeriode: SjablonMaksTilsynPeriode)

data class SjablonMaksFradragsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksFradragsbeløpPeriode: SjablonMaksFradragPeriode)

data class NettoTilsynsutgiftPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: NettoTilsynsutgiftBeregningResultat)

data class NettoTilsynsutgiftBeregningGrunnlag(
    val faktiskUtgiftListe: List<FaktiskUtgift>,
    val tilleggsstønadListe: List<Tilleggsstønad>,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
    val sjablonMaksTilsynsbeløpBeregningGrunnlag: SjablonMaksTilsynsbeløpBeregningGrunnlag,
    val sjablonMaksFradragsbeløpBeregningGrunnlag: SjablonMaksFradragsbeløpBeregningGrunnlag,
)

data class SjablonMaksTilsynsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpTilsyn: BigDecimal)
data class SjablonMaksFradragsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpFradrag: BigDecimal)

data class NettoTilsynsutgiftBeregningResultat(
    val nettoTilsynsutgiftBeløp: BigDecimal,
    val samletFaktiskUtgiftBeløp: BigDecimal,
    val samletTilleggstønadBeløp: BigDecimal,
    val skattefradragsbeløpPerBarn: BigDecimal,
    val bruttoTilsynsutgiftBarnListe: List<BruttoTilsynsutgiftBarn>,
    val grunnlagsreferanseListe: List<String>,
)

data class BruttoTilsynsutgiftBarn(val gjelderBarn: Grunnlagsreferanse, val bruttoTilsynsutgift: BigDecimal, val nettoTilsynsutgift: BigDecimal)

data class FaktiskUtgift(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetBeløp: BigDecimal)
data class Tilleggsstønad(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetBeløp: BigDecimal)

data class FaktiskUtgiftPeriode(
    override val periode: ÅrMånedsperiode,
    @Schema(description = "Referanse til barnet utgiften gjelder")
    val gjelderBarn: Grunnlagsreferanse,
    val fødselsdatoBarn: LocalDate,
    val faktiskUtgiftBeløp: BigDecimal,
    val kostpengerBeløp: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

data class TilleggsstønadPeriode(
    override val periode: ÅrMånedsperiode,
    @Schema(description = "Referanse til barnet stønaden mottas for")
    val gjelderBarn: Grunnlagsreferanse,
    val beløpDagsats: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

data class DelberegningFaktiskTilsynsutgift(override val periode: ÅrMånedsperiode, val beregnetBeløp: BigDecimal) : Delberegning

data class DelberegningTilleggsstønad(override val periode: ÅrMånedsperiode, val beregnetBeløp: BigDecimal) : Delberegning

data class DelberegningNettoTilsynsutgift(
    override val periode: ÅrMånedsperiode,
    val nettoTilsynsutgiftBeløp: BigDecimal,
    val samletFaktiskUtgiftBeløp: BigDecimal,
    val samletTilleggstønadBeløp: BigDecimal,
    val skattefradragsbeløpPerBarn: BigDecimal,
    val bruttoTilsynsutgiftBarnListe: List<BruttoTilsynsutgiftBarn>,
) : Delberegning
