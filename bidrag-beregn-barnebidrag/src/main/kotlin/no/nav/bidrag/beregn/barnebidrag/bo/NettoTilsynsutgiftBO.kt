package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.TilsynsutgiftBarn
import java.math.BigDecimal

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
    val totaltFaktiskUtgiftBeløp: BigDecimal,
    val tilsynsutgiftBarnListe: List<TilsynsutgiftBarn>,
    val grunnlagsreferanseListe: List<String>,
)

data class FaktiskUtgift(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetBeløp: BigDecimal)
data class Tilleggsstønad(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetBeløp: BigDecimal)
