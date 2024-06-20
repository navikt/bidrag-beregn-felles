package no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag beregning
data class BeregnSaertilskuddGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val soknadsbarnPersonId: Int,
    val bidragsevnePeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode>,
    val bPsAndelSaertilskuddPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode>,
    val lopendeBidragPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode>,
    val samvaersfradragGrunnlagPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,

)

// Resultat
data class BeregnSaertilskuddResultat(val resultatPeriodeListe: List<ResultatPeriode>)

data class ResultatPeriode(val periode: Periode, val soknadsbarnPersonId: Int, val resultat: ResultatBeregning, val grunnlag: GrunnlagBeregning)

data class ResultatBeregning(val resultatBelop: BigDecimal, val resultatkode: Resultatkode)

// Grunnlag beregning
data class GrunnlagBeregning(
    val bidragsevne: Bidragsevne,
    val bPsAndelSaertilskudd: BPsAndelSaertilskudd,
    val lopendeBidragListe: List<LopendeBidrag>,
    val samvaersfradragGrunnlagListe: List<SamvaersfradragGrunnlag>,
)

data class Bidragsevne(val referanse: String, val bidragsevneBelop: BigDecimal)

data class BPsAndelSaertilskudd(
    val referanse: String,
    val bPsAndelSaertilskuddProsent: BigDecimal,
    val bPsAndelSaertilskuddBelop: BigDecimal,
    val barnetErSelvforsorget: Boolean,
)

data class LopendeBidrag(
    val referanse: String,
    val barnPersonId: Int,
    val lopendeBidragBelop: BigDecimal,
    val opprinneligBPsAndelUnderholdskostnadBelop: BigDecimal,
    val opprinneligBidragBelop: BigDecimal,
    val opprinneligSamvaersfradragBelop: BigDecimal,
)

data class SamvaersfradragGrunnlag(val referanse: String, val barnPersonId: Int, val samvaersfradragBelop: BigDecimal)

// Hjelpeklasser
data class BeregnSaertilskuddListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var justertBidragsevnePeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode> = listOf(),
    var justertBPsAndelSaertilskuddPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode> = listOf(),
    var justertLopendeBidragPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode> = listOf(),
    var justertSamvaersfradragPeriodeListe: List<no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
