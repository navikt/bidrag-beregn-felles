package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.UnderholdskostnadPeriode
import java.math.BigDecimal

data class BpAndelUnderholdskostnadPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val underholdskostnadPeriodeGrunnlagListe: List<UnderholdskostnadPeriodeGrunnlag>,
    val inntektBPPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    val inntektBMPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    val inntektSBPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
)

data class UnderholdskostnadPeriodeGrunnlag(
    val referanse: String,
    val underholdskostnadPeriode: UnderholdskostnadPeriode,
)

data class BpAndelUnderholdskostnadPeriodeResultat(
    val periode: ÅrMånedsperiode,
    val resultat: BpAndelUnderholdskostnadBeregningResultat
)

data class BpAndelUnderholdskostnadBeregningGrunnlag(
    val underholdskostnadBeregningGrunnlag: UnderholdskostnadBeregningGrunnlag,
    val inntektBPBeregningGrunnlag: InntektBeregningGrunnlag,
    val inntektBMBeregningGrunnlag: InntektBeregningGrunnlag,
    val inntektSBBeregningGrunnlag: InntektBeregningGrunnlag,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
)

data class UnderholdskostnadBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class BpAndelUnderholdskostnadBeregningResultat(
    val endeligAndelFaktor: BigDecimal,
    val andelBeløp: BigDecimal,
    val beregnetAndelFaktor: BigDecimal,
    val barnEndeligInntekt: BigDecimal,
    val barnetErSelvforsørget: Boolean,
    val grunnlagsreferanseListe: List<String>,
)
