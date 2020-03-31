package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode
import no.nav.bidrag.beregn.felles.bidragsevne.periode.PeriodisertGrunnlag
import java.time.LocalDate
import java.util.function.Predicate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAlt(
    var beregnDatoFra: LocalDate,
    var beregnDatoTil: LocalDate,
    var sjablonPeriodeListe: List<SjablonPeriode>,
    var inntektPeriodeListe: List<InntektPeriode>,
    var bostatusPeriodeListe: List<BostatusPeriode>,
    var antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>
)

data class SjablonPeriode(
    val sjablonPeriodeDatoFraTil: Periode,
    val sjablonnavn: String,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double? ) : PeriodisertGrunnlag {
  override fun getDatoFraTil(): Periode {
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

//data class SjablonPeriodeVerdi(
//    val sjablonDatoFraTil: Periode,
//    val sjablonVerdi1: Double,
//    val sjablonVerdi2: Double) : PeriodisertGrunnlag {
//  override fun getDatoFraTil(): Periode {
//    return sjablonDatoFraTil
//  }
//}


// Resultat periode
data class BeregnBidragsevneResultat(
    val resultatPeriodeListe: List<ResultatPeriode>
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning
)


// Grunnlag beregning
data class BeregnBidragsevneGrunnlagPeriodisert(
    var inntektBelop: Double,
    var skatteklasse: Int,
    var borAlene: Boolean,
    var antallEgneBarnIHusstand: Int,
    var sjablonPeriodeListe: List<SjablonPeriode>) {
  fun hentSjablon(sjablonnavn: String?): SjablonPeriode? {
    return sjablonPeriodeListe.stream()
        .filter(Predicate { (sjablonnavn) -> sjablonnavn == sjablonnavn })
        .findAny()
        .orElse(null)
  }
}




