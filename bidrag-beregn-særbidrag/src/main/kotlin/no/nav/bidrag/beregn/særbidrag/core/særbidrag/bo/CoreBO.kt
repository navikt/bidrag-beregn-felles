package no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag beregning
data class BeregnSærbidragGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val søknadsbarnPersonId: String,
    val betaltAvBpPeriodeListe: List<BetaltAvBpPeriode>,
    val bidragsevnePeriodeListe: List<BidragsevnePeriode>,
    val sumLøpendeBidrag: SumLøpendeBidragPeriode,
    val bPsAndelSærbidragPeriodeListe: List<BPsAndelSærbidragPeriode>,

)

// Resultat
data class BeregnSærbidragResultat(val resultatPeriodeListe: List<ResultatPeriode>)

data class ResultatPeriode(val periode: Periode, val søknadsbarnPersonId: String, val resultat: ResultatBeregning, val grunnlag: GrunnlagBeregning)

data class ResultatBeregning(val beregnetBeløp: BigDecimal, val resultatKode: Resultatkode, val resultatBeløp: BigDecimal?)

// Grunnlag beregning
data class GrunnlagBeregning(
    val betaltAvBp: BetaltAvBp,
    val bidragsevne: Bidragsevne,
    val bPsAndelSærbidrag: BPsAndelSærbidrag,
    val sumLøpendeBidrag: SumLøpendeBidragPeriode,
)

data class BetaltAvBp(val referanse: String, val beløp: BigDecimal)

data class Bidragsevne(val referanse: String, val beløp: BigDecimal)

data class BPsAndelSærbidrag(val referanse: String, val andelFaktor: BigDecimal, val andelBeløp: BigDecimal, val barnetErSelvforsørget: Boolean)

// Hjelpeklasser
data class BeregnSærbidragListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var betaltAvBpPeriodeListe: List<BetaltAvBpPeriode> = listOf(),
    var bidragsevnePeriodeListe: List<BidragsevnePeriode> = listOf(),
    var sumLøpendeBidragPeriode: SumLøpendeBidragPeriode = SumLøpendeBidragPeriode("", Periode(LocalDate.MIN, LocalDate.MIN), BigDecimal.ZERO),
    var bPsAndelSærbidragPeriodeListe: List<BPsAndelSærbidragPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
