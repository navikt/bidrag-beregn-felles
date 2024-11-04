package no.nav.bidrag.beregn.barnebidrag.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.Delberegning
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagPeriodeInnhold
import java.math.BigDecimal

data class EndeligBidragPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val bidragsevneDelberegningPeriodeGrunnlagListe: List<BidragsevneDelberegningPeriodeGrunnlag>,
    val underholdskostnadDelberegningPeriodeGrunnlagListe: List<UnderholdskostnadDelberegningPeriodeGrunnlag>,
    val bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe: List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag>,
    val samværsfradragDelberegningPeriodeGrunnlagListe: List<SamværsfradragDelberegningPeriodeGrunnlag>,
    val deltBostedPeriodeGrunnlagListe: List<DeltBostedPeriodeGrunnlag>,
    val barnetilleggBPPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
    val barnetilleggBMPeriodeGrunnlagListe: List<BarnetilleggPeriodeGrunnlag>,
)

//TODO Bør flyttes til bidrag-felles og kalles noe annet?
data class BidragsevneDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bidragsevnePeriode: BidragsevnePeriode,
)

//TODO Bør flyttes til bidrag-felles
@Schema(description = "Bidragsevne for person")
data class BidragsevnePeriode(
    override val periode: ÅrMånedsperiode,
    val beløp: BigDecimal,
    //TODO Sjekk om det gir mening å ha denne her
    val tjuefemProsentInntekt: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

//TODO Bør flyttes til bidrag-felles og kalles noe annet?
data class BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag(
    val referanse: String,
    val bpAndelUnderholdskostnadPeriode: BpAndelUnderholdskostnadPeriode,
)

//TODO Bør flyttes til bidrag-felles
@Schema(description = "BPs andel underholdskostnad for person")
data class BpAndelUnderholdskostnadPeriode(
    override val periode: ÅrMånedsperiode,
    val andelBeløp: BigDecimal,
    val andelFaktor: BigDecimal,
    val barnetErSelvforsørget: Boolean,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

//TODO Bør flyttes til bidrag-felles og kalles noe annet?
data class SamværsfradragDelberegningPeriodeGrunnlag(
    val referanse: String,
    val samværsfradragPeriode: SamværsfradragPeriode,
)

//TODO Bør flyttes til bidrag-felles
@Schema(description = "Samværsfradrag for person")
data class SamværsfradragPeriode(
    override val periode: ÅrMånedsperiode,
    val beløp: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

//TODO Bør flyttes til bidrag-felles og kalles noe annet?
data class DeltBostedPeriodeGrunnlag(
    val referanse: String,
    val deltBostedPeriode: DeltBostedPeriode,
)

//TODO Bør flyttes til bidrag-felles
@Schema(description = "Delt bosted for person")
data class DeltBostedPeriode(
    override val periode: ÅrMånedsperiode,
    val deltBosted: Boolean,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

//TODO Bør flyttes til bidrag-felles og kalles noe annet?
data class BarnetilleggPeriodeGrunnlag(
    val referanse: String,
    val barnetilleggPeriode: BarnetilleggPeriode,
)

//TODO Bør flyttes til bidrag-felles
@Schema(description = "Barnetillegg for person")
data class BarnetilleggPeriode(
    override val periode: ÅrMånedsperiode,
    val beløp: BigDecimal,
    val skattFaktor: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold

data class EndeligBidragPeriodeResultat(
    val periode: ÅrMånedsperiode,
    val resultat: EndeligBidragBeregningResultat
)

data class EndeligBidragBeregningGrunnlag(
    val bidragsevneBeregningGrunnlag: BidragsevneDelberegningBeregningGrunnlag,
    val underholdskostnadBeregningGrunnlag: UnderholdskostnadDelberegningBeregningGrunnlag,
    val bpAndelUnderholdskostnadBeregningGrunnlag: BpAndelUnderholdskostnadDelberegningBeregningGrunnlag,
    val samværsfradragBeregningGrunnlag: SamværsfradragDelberegningBeregningGrunnlag,
    val deltBostedBeregningGrunnlag: DeltBostedBeregningGrunnlag,
    val barnetilleggBPBeregningGrunnlag: BarnetilleggBeregningGrunnlag,
    val barnetilleggBMBeregningGrunnlag: BarnetilleggBeregningGrunnlag,
)

data class BidragsevneDelberegningBeregningGrunnlag(
    val referanse: String,
    val beløp: BigDecimal,
    //TODO Sjekk om det gir mening å ha denne her
    val tjuefemProsentInntekt: BigDecimal
)

data class UnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val beløp: BigDecimal,
)

data class BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
    val referanse: String,
    val andelBeløp: BigDecimal,
    val andelFaktor: BigDecimal,
    val barnetErSelvforsørget: Boolean
)

data class SamværsfradragDelberegningBeregningGrunnlag(
    val referanse: String,
    val beløp: BigDecimal
)

data class DeltBostedBeregningGrunnlag(
    val referanse: String,
    val deltBosted: Boolean
)

data class BarnetilleggBeregningGrunnlag(
    val referanse: String,
    val beløp: BigDecimal,
    val skattFaktor: BigDecimal
)

data class EndeligBidragBeregningResultat(
    val beregnetBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val resultatKode: Resultatkode,
    val resultatBeløp: BigDecimal = BigDecimal.ZERO.avrundetMedNullDesimaler,
    val kostnadsberegnetBidrag: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBarnetilleggBP: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val nettoBarnetilleggBM: BigDecimal = BigDecimal.ZERO.avrundetMedToDesimaler,
    val justertNedTilEvne: Boolean = false,
    val justertNedTil25ProsentAvInntekt: Boolean = false,
    val justertForNettoBarnetilleggBP: Boolean = false,
    val justertForNettoBarnetilleggBM: Boolean = false,
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

//TODO Flytte til bidrag-felles
data class DelberegningEndeligBidrag(
    override val periode: ÅrMånedsperiode,
    val beregnetBeløp: BigDecimal,
    val resultatKode: Resultatkode,
    val resultatBeløp: BigDecimal,
    val kostnadsberegnetBidrag: BigDecimal,
    val nettoBarnetilleggBP: BigDecimal,
    val nettoBarnetilleggBM: BigDecimal,
    val justertNedTilEvne: Boolean,
    val justertNedTil25ProsentAvInntekt: Boolean,
    val justertForNettoBarnetilleggBP: Boolean,
    val justertForNettoBarnetilleggBM: Boolean,
) : Delberegning
