package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import java.math.BigDecimal

data class EndeligBidragPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragsevneDelberegningPeriodeGrunnlagListe: List<BidragsevneDelberegningPeriodeGrunnlag>,
    val underholdskostnadDelberegningPeriodeGrunnlagListe: List<UnderholdskostnadDelberegningPeriodeGrunnlag>,
    val bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe: List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
    val samværsklassePeriodeGrunnlagListe: List<SamværsklassePeriodeGrunnlag>,
    val barnetilleggBPPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
    val barnetilleggBMPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
)

data class BidragsevneDelberegningPeriodeGrunnlag(val referanse: String, val bidragsevnePeriode: DelberegningBidragsevne)

data class BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bpAndelUnderholdskostnadPeriode: DelberegningBidragspliktigesAndel,
)

data class SamværsfradragDelberegningPeriodeGrunnlag(val referanse: String, val samværsfradragPeriode: DelberegningSamværsfradrag)

data class EndeligBidragPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: EndeligBidragBeregningResultat)

data class EndeligBidragBeregningGrunnlag(
    val bidragsevneBeregningGrunnlag: BidragsevneDelberegningBeregningGrunnlag,
    val underholdskostnadBeregningGrunnlag: UnderholdskostnadDelberegningBeregningGrunnlag,
    val bpAndelUnderholdskostnadBeregningGrunnlag: BpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
    val deltBostedBeregningGrunnlag: DeltBostedBeregningGrunnlag,
    val barnetilleggBPBeregningGrunnlag: BarnetilleggBeregningGrunnlag?,
    val barnetilleggBMBeregningGrunnlag: BarnetilleggBeregningGrunnlag?,
)

data class BidragsevneDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal, val sumInntekt25Prosent: BigDecimal)

data class UnderholdskostnadDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val andelBeløp: BigDecimal,
    val andelFaktor: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)

data class SamværsfradragDelberegningBeregningGrunnlag(val referanse: String, val beløp: BigDecimal)

data class DeltBostedBeregningGrunnlag(val referanse: String, val deltBosted: Boolean)

// TODO Ikke nødvendig med liste av referanser når delberegning netto barnetillegg er klar
data class BarnetilleggBeregningGrunnlag(val referanse: List<String>, val beløp: BigDecimal, val skattFaktor: BigDecimal)

data class EndeligBidragBeregningResultat(
    val beregnetBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val resultatBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedNullDesimaler,
    val uMinusNettoBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragEtterBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBidragEtterBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragJustertForEvneOg25Prosent: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bruttoBidragEtterBarnetilleggBP: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBidragEtterSamværsfradrag: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val bpAndelAvUVedDeltBostedFaktor: BigDecimal = BigDecimal.ZERO.avrundetMedTiDesimaler,
    val bpAndelAvUVedDeltBostedBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val ingenEndringUnderGrense: Boolean = false,
    val barnetErSelvforsørget: Boolean = false,
    val bidragJustertForDeltBosted: Boolean = false,
    val bidragJustertForNettoBarnetilleggBP: Boolean = false,
    val bidragJustertForNettoBarnetilleggBM: Boolean = false,
    val bidragJustertNedTilEvne: Boolean = false,
    val bidragJustertNedTil25ProsentAvInntekt: Boolean = false,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)
