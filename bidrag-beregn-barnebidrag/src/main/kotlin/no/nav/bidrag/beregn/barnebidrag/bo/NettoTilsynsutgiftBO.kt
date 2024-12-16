package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.TilsynsutgiftBarn
import java.math.BigDecimal
import java.time.LocalDate

data class NettoTilsynsutgiftPeriodeGrunnlag(
    val søknadsbarnReferanse: String,
    val beregningsperiode: ÅrMånedsperiode,
    val barnBMListe: List<BarnBM>,
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
    val søknadsbarnReferanse: String,
    val barnBMListe: List<BarnBM>,
    val barnBMListeUnderTolvÅr: List<BarnBM>,
    val faktiskUtgiftListe: List<FaktiskUtgift>,
    val tilleggsstønad: Tilleggsstønad?,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
    val sjablonMaksTilsynsbeløpBeregningGrunnlag: SjablonMaksTilsynsbeløpBeregningGrunnlag,
    val sjablonMaksFradragsbeløpBeregningGrunnlag: SjablonMaksFradragsbeløpBeregningGrunnlag,
)

data class BarnBM(val referanse: String, val fødselsdato: LocalDate)

data class SjablonMaksTilsynsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpTilsyn: BigDecimal)
data class SjablonMaksFradragsbeløpBeregningGrunnlag(val referanse: String, val antallBarnTom: Int, val maxBeløpFradrag: BigDecimal)

data class NettoTilsynsutgiftBeregningResultat(
    val erBegrensetAvMaksTilsyn: Boolean,
    val totalTilsynsutgift: BigDecimal,
    val sjablonMaksTilsynsutgift: BigDecimal,
    val bruttoTilsynsutgift: BigDecimal,
    val justertBruttoTilsynsutgift: BigDecimal,
    val andelTilsynsutgiftFaktor: BigDecimal,
    val antallBarn: Int,
    val skattefradrag: BigDecimal,
    val skattefradragPerBarn: BigDecimal,
    val skattefradragTotalTilsynsutgift: BigDecimal = BigDecimal.ZERO,
    val skattefradragMaksfradrag: BigDecimal = BigDecimal.ZERO,
    val nettoTilsynsutgift: BigDecimal,
    val tilsynsutgiftBarnListe: List<TilsynsutgiftBarn>,
    val grunnlagsreferanseListe: List<String>,
)

data class FaktiskUtgift(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetMånedsbeløp: BigDecimal)
data class Tilleggsstønad(val referanse: String, val gjelderBarn: Grunnlagsreferanse, val beregnetMånedsbeløp: BigDecimal)
