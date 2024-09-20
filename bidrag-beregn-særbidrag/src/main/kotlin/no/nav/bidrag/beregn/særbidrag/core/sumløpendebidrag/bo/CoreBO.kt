package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore
import java.math.BigDecimal

// Grunnlag
// data class GrunnlagBeregning(
//    val beregnDatoFra: LocalDate,
//    val beregnDatoTil: LocalDate,
//    val løpendeBidragCoreListe: List<LøpendeBidragCore>,
//    val sjablonPeriodeListe: List<SjablonPeriode>,
// )

// Resultatperiode

data class BeregnSumLøpendeBidragResultat(val resultatPeriodeListe: List<ResultatPeriode>)

// data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: GrunnlagBeregning)
data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: LøpendeBidragGrunnlagCore)

data class ResultatBeregning(val sum: BigDecimal, val sjablonListe: List<SjablonPeriodeNavnVerdi>)
