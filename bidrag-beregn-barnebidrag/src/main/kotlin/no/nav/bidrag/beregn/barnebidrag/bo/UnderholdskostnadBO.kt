package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilsynMedStønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBarnetilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonForbruksutgifterPeriode
import java.math.BigDecimal

data class UnderholdskostnadPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val søknadsbarnPeriodeGrunnlag: SøknadsbarnPeriodeGrunnlag,
    val barnetilsynMedStønadPeriodeGrunnlagListe: List<BarnetilsynMedStønadPeriodeGrunnlag>,
    val nettoTilsynsutgiftPeriodeGrunnlagListe: List<NettoTilsynsutgiftPeriodeGrunnlagDto>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
    var sjablonBarnetilsynPeriodeGrunnlagListe: List<SjablonBarnetilsynPeriodeGrunnlag>,
    var sjablonForbruksutgifterPeriodeGrunnlagListe: List<SjablonForbruksutgifterPeriodeGrunnlag>,
)

data class BarnetilsynMedStønadPeriodeGrunnlag(val referanse: String, val barnetilsynMedStønadPeriode: BarnetilsynMedStønadPeriode)
data class BarnetilsynMedStønad(val referanse: String, val tilsynstype: Tilsynstype, val skolealder: Skolealder)

data class NettoTilsynsutgiftPeriodeGrunnlagDto(val referanse: String, val nettoTilsynsutgiftPeriodeGrunnlag: NettoTilsynsutgiftPeriode)
data class NettoTilsynsutgiftPeriode(val referanse: String, val periode: ÅrMånedsperiode, val nettoTilsynsutgift: BigDecimal)
data class NettoTilsynsutgift(val referanse: String, val nettoTilsynsutgift: BigDecimal)

data class SjablonBarnetilsynPeriodeGrunnlag(val referanse: String, val sjablonBarnetilsynPeriode: SjablonBarnetilsynPeriode)

data class SjablonForbruksutgifterPeriodeGrunnlag(val referanse: String, val sjablonForbruksutgifterPeriode: SjablonForbruksutgifterPeriode)

data class UnderholdskostnadPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: UnderholdskostnadBeregningResultat)

data class UnderholdskostnadBeregningGrunnlag(
    val søknadsbarn: SøknadsbarnBeregningGrunnlag,
    val barnetilsynMedStønad: BarnetilsynMedStønad?,
    val nettoTilsynsutgiftBeregningGrunnlag: NettoTilsynsutgift?,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
    val sjablonBarnetilsynBeregningGrunnlag: SjablonBarnetilsynBeregningGrunnlag,
    val sjablonForbruksutgifterBeregningGrunnlag: SjablonForbruksutgifterBeregningGrunnlag,
)

data class SjablonBarnetilsynBeregningGrunnlag(
    val referanse: String,
    val typeStønad: String,
    val typeTilsyn: String,
    val beløpBarnetilsyn: BigDecimal,
)

data class SjablonForbruksutgifterBeregningGrunnlag(val referanse: String, val alderTom: Int, val beløpForbrukTotalt: BigDecimal)

data class UnderholdskostnadBeregningResultat(
    val forbruksutgift: BigDecimal,
    val boutgift: BigDecimal,
    val barnetilsynMedStønad: BigDecimal,
    val nettoTilsynsutgift: BigDecimal,
    val barnetrygd: BigDecimal,
    val underholdskostnad: BigDecimal,
    val grunnlagsreferanseListe: List<String>,
)
