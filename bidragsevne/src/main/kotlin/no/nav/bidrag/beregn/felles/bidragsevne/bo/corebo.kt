package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode
import no.nav.bidrag.beregn.felles.bidragsevne.periode.PeriodisertGrunnlag
import java.time.LocalDate
import java.util.function.Predicate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAlt(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>
)

data class SjablonPeriode(
    val sjablonPeriodeDatoFraTil: Periode?,
    val sjablonnavn: String?,
    val sjablonVerdi1: Double?,
    val sjablonVerdi2: Double? ) : PeriodisertGrunnlag {
  override fun getDatoFraTil(): Periode? {
    return sjablonPeriodeDatoFraTil
  }
}


data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val skatteklasse: Int,
    val inntektBelop: Double) : PeriodisertGrunnlag {
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


// Resultat periode
data class BeregnBidragsevneResultat(
    val resultatPeriodeListe: List<ResultatPeriode>
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning
)


data class SjablonPeriodeVerdi(
    val sjablonnavn: String,
    val sjablonVerdi: Int)

// Grunnlag beregning
data class BeregnBidragsevneGrunnlagPeriodisert(
    val inntektBelop: Double,
    val skatteklasse: Int,
    val borAlene: Boolean,
    val antallEgneBarnIHusstand: Int,
    val sjablonPeriodeListe: List<SjablonPeriode>) {
  fun hentSjablon(sjablonnavn: String?): SjablonPeriode? = sjablonPeriodeListe.first() { it.sjablonnavn == sjablonnavn }
}




