package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import java.math.BigDecimal

//TODO Bør endres til å bruke delberegning-objektene for inntekt? Men problemet er at delberegningene produseres av denne servicen også
data class BpAndelUnderholdskostnadPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val underholdskostnadDelberegningPeriodeGrunnlagListe: List<UnderholdskostnadDelberegningPeriodeGrunnlag>,
    val inntektBPPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    val inntektBMPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    val inntektSBPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
)

data class UnderholdskostnadDelberegningPeriodeGrunnlag(val referanse: String, val underholdskostnadPeriode: DelberegningUnderholdskostnad)

data class BpAndelUnderholdskostnadPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: BpAndelUnderholdskostnadBeregningResultat)

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
