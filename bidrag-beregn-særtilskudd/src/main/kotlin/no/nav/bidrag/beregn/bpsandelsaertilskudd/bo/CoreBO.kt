package no.nav.bidrag.beregn.bpsandelsaertilskudd.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriodeNavnVerdi
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag
data class BeregnBPsAndelSaertilskuddGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val nettoSaertilskuddPeriodeListe: List<NettoSaertilskuddPeriode>,
    val inntektBPPeriodeListe: List<InntektPeriode>,
    val inntektBMPeriodeListe: List<InntektPeriode>,
    val inntektBBPeriodeListe: List<InntektPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
)

// Resultatperiode
data class BeregnBPsAndelSaertilskuddResultat(
    val resultatPeriodeListe: List<ResultatPeriode>,
)

// Resultat
data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning,
    val resultatGrunnlagBeregning: GrunnlagBeregning,
)

data class ResultatBeregning(
    val resultatAndelProsent: BigDecimal,
    val resultatAndelBelop: BigDecimal,
    val barnetErSelvforsorget: Boolean,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)

// Grunnlag beregning
data class GrunnlagBeregning(
    val nettoSaertilskuddBelop: BigDecimal,
    val inntektBPListe: List<Inntekt>,
    val inntektBMListe: List<Inntekt>,
    val inntektBBListe: List<Inntekt>,
    val sjablonListe: List<SjablonPeriode>,
)

data class Inntekt(
    val referanse: String,
    val inntektType: String,
    val inntektBelop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean,
)

// Hjelpeklasser
data class BeregnBPsAndelSaertilskuddListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var justertNettoSaertilskuddPeriodeListe: List<NettoSaertilskuddPeriode> = listOf(),
    var justertInntektBPPeriodeListe: List<InntektPeriode> = listOf(),
    var justertInntektBMPeriodeListe: List<InntektPeriode> = listOf(),
    var justertInntektBBPeriodeListe: List<InntektPeriode> = listOf(),
    var justertSjablonPeriodeListe: List<SjablonPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
