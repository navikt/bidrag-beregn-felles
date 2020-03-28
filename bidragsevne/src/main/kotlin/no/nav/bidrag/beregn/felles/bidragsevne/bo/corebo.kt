package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode
import no.nav.bidrag.beregn.felles.bidragsevne.periode.PeriodisertGrunnlag
import java.math.BigDecimal
import java.time.LocalDate


// Grunnlag periode
data class BeregnBidragsevneGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>
)

data class SjablonPeriode(
    val sjablonPeriodeDatoFraTil: Periode,
    val sjablonnavn: String,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double
)

data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val inntektBelop: BigDecimal) : PeriodisertGrunnlag {
  override fun getDatoFraTil(): Periode {
    return inntektDatoFraTil
  }
}

data class BostatusPeriode(
    val bostatusPeriodeDatoFraTil: Periode,
    val borAlene: Boolean) : PeriodisertGrunnlag {

  override fun getDatoFraTil(): Periode {
    return bostatusPeriodeDatoFraTil
  }
}

data class AntallBarnIEgetHusholdPeriode(
    val antallBarnIEgetHusholdPeriodeDatoFraTil: Periode,
    val antallBarn: Int) : PeriodisertGrunnlag {
  override fun getDatoFraTil(): Periode {
    return antallBarnIEgetHusholdPeriodeDatoFraTil
  }

}

data class SjablonPeriodeVerdi(
    val sjablonDatoFraTil: Periode,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double) : PeriodisertGrunnlag {
  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}

data class AlderPeriode(
    val alderDatoFraTil: Periode,
    val alder: Int) : PeriodisertGrunnlag {

  override fun getDatoFraTil(): Periode {
    return alderDatoFraTil
  }
}


// Resultat periode
data class BeregnForskuddResultat(
    val resultatPeriodeListe: List<ResultatPeriode>
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning
)


// Grunnlag beregning
data class GrunnlagBeregning(
    val inntektBelop: Double,
    val skatteklasse: Int,
    val borAlene: Boolean,
    val antallEgneBarnIHusstand: Int,
    val sjablonPeriodeListe: List<SjablonPeriode>
)



