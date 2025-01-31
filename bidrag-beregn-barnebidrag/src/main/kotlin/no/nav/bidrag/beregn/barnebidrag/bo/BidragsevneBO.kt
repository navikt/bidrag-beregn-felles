package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import java.math.BigDecimal

// TODO Bør endres til å bruke delberegning-objektene? Men problemet er at delberegningene produseres av denne servicen også
data class BidragsevnePeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val inntektBPPeriodeGrunnlagListe: List<InntektPeriodeCore>,
    val barnIHusstandenPeriodeGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
    val voksneIHusstandenPeriodeGrunnlagListe: List<VoksneIHusstandenPeriodeCore>,
    val boforholdPeriodeGrunnlagListe: List<BoforholdPeriodeCore>,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
    var sjablonBidragsevnePeriodeGrunnlagListe: List<SjablonBidragsevnePeriodeGrunnlag>,
    var sjablonTrinnvisSkattesatsPeriodeGrunnlagListe: List<SjablonTrinnvisSkattesatsPeriodeGrunnlag>,
)

data class SjablonBidragsevnePeriodeGrunnlag(val referanse: String, val sjablonBidragsevnePeriode: SjablonBidragsevnePeriode)

data class SjablonTrinnvisSkattesatsPeriodeGrunnlag(val referanse: String, val sjablonTrinnvisSkattesatsPeriode: SjablonTrinnvisSkattesatsPeriode)

data class BidragsevnePeriodeResultat(val periode: ÅrMånedsperiode, val resultat: BidragsevneBeregningResultat)

data class BidragsevneBeregningGrunnlag(
    val inntektBPBeregningGrunnlag: InntektBeregningGrunnlag,
    val barnIHusstandenBeregningGrunnlag: BarnIHusstandenBeregningGrunnlag,
    val voksneIHusstandenBeregningGrunnlag: VoksneIHusstandenBeregningGrunnlag,
    val sjablonSjablontallBeregningGrunnlagListe: List<SjablonSjablontallBeregningGrunnlag>,
    val sjablonBidragsevneBeregningGrunnlagListe: List<SjablonBidragsevneBeregningGrunnlag>,
    val sjablonTrinnvisSkattesatsBeregningGrunnlag: SjablonTrinnvisSkattesatsBeregningGrunnlag,
)

data class BarnIHusstandenBeregningGrunnlag(val referanse: String, val antallBarn: Double)

data class VoksneIHusstandenBeregningGrunnlag(val referanse: String, val borMedAndre: Boolean)

data class SjablonBidragsevneBeregningGrunnlag(val referanse: String, val bostatus: String, val boutgift: BigDecimal, val underhold: BigDecimal)

data class SjablonTrinnvisSkattesatsBeregningGrunnlag(val referanse: String, val trinnliste: List<SjablonTrinnvisSkattesats>)

data class BidragsevneBeregningResultat(
    val bidragsevne: BigDecimal,
    val minstefradrag: BigDecimal,
    val skattAlminneligInntekt: BigDecimal,
    val trygdeavgift: BigDecimal,
    val trinnskatt: BigDecimal,
    val sumSkatt: BigDecimal,
    val sumSkattFaktor: BigDecimal,
    val underholdBarnEgenHusstand: BigDecimal,
    val sumInntekt25Prosent: BigDecimal,
    val grunnlagsreferanseListe: List<String>,
)
