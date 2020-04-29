package no.nav.bidrag.beregn.felles.bidragsevne.dto

import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.enums.BostatusKode
import no.nav.bidrag.beregn.felles.enums.InntektType
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode
import java.math.BigDecimal
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
    val inntektType: String,
    val skatteklasse: Int,
    val inntektBelop: Double
)

data class BostatusPeriodeCore(
    val bostatusPeriodeDatoFraTil: Periode,
    val bostatusKode: String
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
    val resultatDatoFraTil: PeriodeCore,
    val resultatBeregning: ResultatBeregningCore,
    val resultatGrunnlag: ResultatGrunnlagCore
)

data class ResultatBeregningCore(
    val resultatEvne: Double
)

data class ResultatGrunnlagCore(
    val inntektListe: List<InntektCore>,
    val skatteklasse: Int,
    val bostatusKode: String,
    val antallEgneBarnIHusstand: Int,
    val saerfradragkode: String,
    val sjablonPeriodeListe: List<SjablonPeriode>
)


data class InntektCore(
    val inntektType: String,
    val inntektBelop: Double
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