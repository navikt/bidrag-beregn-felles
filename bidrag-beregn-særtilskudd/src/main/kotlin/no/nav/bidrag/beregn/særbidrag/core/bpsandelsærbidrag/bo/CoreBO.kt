package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag
data class BeregnBPsAndelSærbidragGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val utgiftPeriodeListe: List<UtgiftPeriode>,
    val inntektBPPeriodeListe: List<InntektPeriode>,
    val inntektBMPeriodeListe: List<InntektPeriode>,
    val inntektSBPeriodeListe: List<InntektPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
)

// Resultatperiode
data class BeregnBPsAndelSærbidragResultat(
    val resultatPeriodeListe: List<ResultatPeriode>,
)

// Resultat
data class ResultatPeriode(
    val periode: Periode,
    val resultat: ResultatBeregning,
    val grunnlag: GrunnlagBeregning,
)

data class ResultatBeregning(
    val resultatAndelProsent: BigDecimal,
    val resultatAndelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)

// Grunnlag beregning
data class GrunnlagBeregning(
    val utgift: Utgift,
    val inntektBPListe: List<Inntekt>,
    val inntektBMListe: List<Inntekt>,
    val inntektSBListe: List<Inntekt>,
    val sjablonListe: List<SjablonPeriode>,
)

data class Utgift(
    val referanse: String,
    val beløp: BigDecimal,
)

data class Inntekt(
    val referanse: String,
    val inntektType: String,
    val inntektBeløp: BigDecimal,
)

// Hjelpeklasser
data class BeregnBPsAndelSærbidragListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var utgiftPeriodeListe: List<UtgiftPeriode> = listOf(),
    var inntektBPPeriodeListe: List<InntektPeriode> = listOf(),
    var inntektBMPeriodeListe: List<InntektPeriode> = listOf(),
    var inntektSBPeriodeListe: List<InntektPeriode> = listOf(),
    var sjablonPeriodeListe: List<SjablonPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
