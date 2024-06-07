package no.nav.bidrag.beregn.samvaersfradrag.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriodeNavnVerdi
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnSamvaersfradragGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val samvaersfradragGrunnlagPeriodeListe: List<SamvaersfradragGrunnlagPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
)

// Resultatperiode
data class BeregnSamvaersfradragResultat(
    val resultatPeriodeListe: List<ResultatPeriode>,
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregningListe: List<ResultatBeregning>,
    val resultatGrunnlag: GrunnlagBeregningPeriodisert,
)

data class ResultatBeregning(
    val barnPersonId: Int,
    val belop: BigDecimal,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)

// Grunnlag beregning
data class GrunnlagBeregningPeriodisert(
    val samvaersfradragGrunnlagPerBarnListe: List<SamvaersfradragGrunnlagPerBarn>,
    val sjablonListe: List<SjablonPeriode>,
)

data class SamvaersfradragGrunnlagPerBarn(
    val referanse: String,
    val barnPersonId: Int,
    val barnAlder: Int,
    val samvaersklasse: String,
)

// Hjelpeklasser
data class BeregnSamvaersfradragListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var justertSamvaersfradragPeriodeListe: List<SamvaersfradragGrunnlagPeriode> = listOf(),
    var justertSjablonPeriodeListe: List<SjablonPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
