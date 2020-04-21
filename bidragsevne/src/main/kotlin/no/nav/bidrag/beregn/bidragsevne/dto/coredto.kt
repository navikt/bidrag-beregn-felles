package no.nav.bidrag.beregn.bidragsevne.dto

import no.nav.bidrag.beregn.bidragsevne.bo.BostatusKode
import no.nav.bidrag.beregn.bidragsevne.bo.SaerfradragKode
import no.nav.bidrag.beregn.bidragsevne.periode.Periode
import java.time.LocalDate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAltCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val sjablonPeriodeListe: List<SjablonPeriodeCore>,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriodeCore>,
    val saerfradragPeriodeListe: List<SaerfradragPeriodeCore>
)

data class SjablonPeriodeCore(
    val sjablonPeriodeDatoFraTil: PeriodeCore?,
    val sjablonnavn: String?,
    val sjablonVerdi1: Double?,
    val sjablonVerdi2: Double?
)

data class InntektPeriodeCore(
    val inntektDatoFraTil: Periode,
    val skatteklasse: Int,
    val inntektBelop: Double
)

data class BostatusPeriodeCore(
    val bostatusPeriodeDatoFraTil: Periode,
    val bostatusKode: BostatusKode
)

data class AntallBarnIEgetHusholdPeriodeCore(
    val antallBarnIEgetHusholdPeriodeDatoFraTil: Periode,
    val antallBarn: Int
)

data class SaerfradragPeriodeCore(
    val saerfradragPeriodeDatoFraTil: Periode,
    val saerfradragKode: SaerfradragKode
)



// Resultat
data class BeregnBidragsevneResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val avviksListe: List<AvvikCore>
)

data class ResultatPeriodeCore(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning
)

data class ResultatBeregningCore(
    val resultatEvne: Double,
    val resultatKode: String,
    val resultatBeskrivelse: String
)

data class AvvikCore(
    val avvikTekst: String,
    val avvikType: String
)

  // Felles
  data class PeriodeCore(
      val periodeDatoFra: LocalDate,
      val periodeDatoTil: LocalDate?
  )