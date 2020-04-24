package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import no.nav.bidrag.beregn.felles.enums.AvvikType
import no.nav.bidrag.beregn.felles.enums.BostatusKode
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode
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
    val sjablonDatoFraTil: Periode,
    val sjablonnavn: String?,
    val sjablonVerdi1: Double?,
    val sjablonVerdi2: Double? ) : PeriodisertGrunnlag {
  constructor(sjablonPeriode: SjablonPeriode) : this(sjablonPeriode.sjablonDatoFraTil.justerDatoer(), sjablonPeriode.sjablonnavn,
      sjablonPeriode.sjablonVerdi1, sjablonPeriode.sjablonVerdi2)
  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}


data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val skatteklasse: Int,
    val inntektBelop: Double) : PeriodisertGrunnlag {
  constructor(inntektPeriode: InntektPeriode) : this(inntektPeriode.inntektDatoFraTil.justerDatoer(), inntektPeriode.skatteklasse,
      inntektPeriode.inntektBelop)
  override fun getDatoFraTil(): Periode {
    return inntektDatoFraTil
  }
}

data class BostatusPeriode(
    val bostatusDatoFraTil: Periode,
    val bostatusKode: BostatusKode) : PeriodisertGrunnlag {
  constructor(bostatusPeriode: BostatusPeriode) : this(bostatusPeriode.bostatusDatoFraTil.justerDatoer(), bostatusPeriode.bostatusKode)
  override fun getDatoFraTil(): Periode {
    return bostatusDatoFraTil
  }
}

data class AntallBarnIEgetHusholdPeriode(
    val antallBarnIEgetHusholdDatoFraTil: Periode,
    val antallBarn: Int) : PeriodisertGrunnlag {
  constructor(antallBarnIEgetHusholdPeriode: AntallBarnIEgetHusholdPeriode) : this(antallBarnIEgetHusholdPeriode.antallBarnIEgetHusholdDatoFraTil.justerDatoer(),
      antallBarnIEgetHusholdPeriode.antallBarn)
  override fun getDatoFraTil(): Periode {
    return antallBarnIEgetHusholdDatoFraTil
  }
}

data class SaerfradragPeriode(
    val saerfradragDatoFraTil: Periode,
    val saerfradragKode: SaerfradragKode) : PeriodisertGrunnlag {
  constructor(saerfradragPeriode: SaerfradragPeriode) : this(saerfradragPeriode.saerfradragDatoFraTil.justerDatoer(),
      saerfradragPeriode.saerfradragKode)
  override fun getDatoFraTil(): Periode {
    return saerfradragDatoFraTil
  }
}

// Resultat periode
data class BeregnBidragsevneResultat(
    val resultatPeriodeListe: List<ResultatPeriode>
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning,
    val resultatGrunnlag: BeregnBidragsevneGrunnlagPeriodisert
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

