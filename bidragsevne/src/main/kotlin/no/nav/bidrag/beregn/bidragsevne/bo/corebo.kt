package no.nav.bidrag.beregn.bidragsevne.bo

import no.nav.bidrag.beregn.bidragsevne.periode.Periode
import java.time.LocalDate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAlt(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>,
    val saerfradragPeriodeListe: List<SaerfradragPeriode>
)

data class SjablonPeriode(
    val sjablonPeriodeDatoFraTil: Periode?,
    val sjablonnavn: String?,
    val sjablonVerdi1: Double?,
    val sjablonVerdi2: Double? ) {
  override fun getDatoFraTil(): Periode? {
    return sjablonPeriodeDatoFraTil
  }
}


data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val skatteklasse: Int,
    val inntektBelop: Double) {
  override fun getDatoFraTil(): Periode {
    return inntektDatoFraTil
  }
}

data class BostatusPeriode(
    val bostatusPeriodeDatoFraTil: Periode,
    val bostatusKode: BostatusKode) {
  override fun getDatoFraTil(): Periode {
    return bostatusPeriodeDatoFraTil
  }
}

data class AntallBarnIEgetHusholdPeriode(
    val antallBarnIEgetHusholdPeriodeDatoFraTil: Periode,
    val antallBarn: Int) {
  override fun getDatoFraTil(): Periode {
    return antallBarnIEgetHusholdPeriodeDatoFraTil
  }
}

data class SaerfradragPeriode(
    val saerfradragPeriodeDatoFraTil: Periode,
    val saerfradragKode: SaerfradragKode) {
  override fun getDatoFraTil(): Periode {
    return saerfradragPeriodeDatoFraTil
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


// Avvik periode
data class Avvik(
    val avvikTekst: String,
    val avvikType: AvvikType
)


// Grunnlag beregning
data class BeregnBidragsevneGrunnlagPeriodisert(
    val inntektBelop: Double,
    val skatteklasse: Int,
    val bostatusKode: BostatusKode,
    val antallEgneBarnIHusstand: Int,
    val saerfradragkode: SaerfradragKode,
    val sjablonPeriodeListe: List<SjablonPeriode>) {
  fun hentSjablon(sjablonnavn: String?): SjablonPeriode? = sjablonPeriodeListe.first() { it.sjablonnavn == sjablonnavn }
}

// ENUMs
enum class BostatusKode {
  ALENE,
  MED_ANDRE
}
enum class SaerfradragKode {
  INGEN,
  HALVT,
  HELT
}

enum class AvvikType {
  PERIODER_OVERLAPPER,
  PERIODER_HAR_OPPHOLD,
  NULL_VERDI_I_DATO,
  DATO_FRA_ETTER_DATO_TIL,
  INPUT_DATA_MANGLER
}

