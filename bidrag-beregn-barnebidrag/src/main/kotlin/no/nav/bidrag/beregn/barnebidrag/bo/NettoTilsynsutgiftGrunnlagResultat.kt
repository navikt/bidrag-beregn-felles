package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import java.math.BigDecimal

data class NettoTilsynsutgiftPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val faktiskTilsynsutgiftListe: List<FaktiskTilsynsutgift>,
    val kostpengerListe: List<Kostpenger>,
    val tilleggsstønadListe: List<Tilleggstønad>,
    var sjablonMaksTilsynsbeløpPeriodeGrunnlagListe: List<SjablonMaksTilsynsbeløpPeriodeGrunnlag>,
    var sjablonMaksFradragsbeløpPeriodeGrunnlagListe: List<SjablonMaksTilsynsbeløpPeriodeGrunnlag>,
)

data class FaktiskTilsynsutgift(val referanseBarn: String, val beløp: BigDecimal)
data class Kostpenger(val referanseBarn: String, val beløp: BigDecimal)
data class Tilleggstønad(val referanseBarn: String, val beløp: BigDecimal)

data class SjablonMaksTilsynsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksTilsynsbeløpPeriode: SjablonMaksTilsynPeriode)

data class SjablonMaksFradragsbeløpPeriodeGrunnlag(val referanse: String, val sjablonMaksFradragsbeløpPeriode: SjablonMaksFradragPeriode)

data class NettoTilsynsutgiftPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: NettoTilsynsutgiftBeregningResultat)

data class NettoTilsynsutgiftBeregningGrunnlag(
    val søknadsbarn: SøknadsbarnBeregningGrunnlag,
    val samværsklasseBeregningGrunnlag: SamværsklasseBeregningGrunnlag,
    val sjablonSamværsfradragBeregningGrunnlagListe: List<SjablonSamværsfradragBeregningGrunnlag>,
)

// data class SamværsklasseBeregningGrunnlag(val referanse: String, val samværsklasse: Samværsklasse)

data class SjablonMaksTilsynsbeløpBeregningGrunnlag(
    val referanse: String,
    val samværsklasse: Samværsklasse,
    val alderTom: Int,
    val beløpFradrag: BigDecimal,
)

data class SjablonMaksFradragsbeløpBeregningGrunnlag(
    val referanse: String,
    val samværsklasse: Samværsklasse,
    val alderTom: Int,
    val beløpFradrag: BigDecimal,
)

data class NettoTilsynsutgiftBeregningResultat(val beløpFradrag: BigDecimal, val grunnlagsreferanseListe: List<String>)
