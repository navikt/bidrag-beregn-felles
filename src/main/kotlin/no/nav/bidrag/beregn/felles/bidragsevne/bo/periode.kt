package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import no.nav.bidrag.beregn.felles.bo.SjablonNy
import no.nav.bidrag.beregn.felles.enums.BostatusKode
import no.nav.bidrag.beregn.felles.enums.InntektType
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode

data class SjablonPeriode(
    val sjablonDatoFraTil: Periode,
    val sjablonnavn: String,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double? ) : PeriodisertGrunnlag {
  constructor(sjablonPeriode: SjablonPeriode) : this(sjablonPeriode.sjablonDatoFraTil.justerDatoer(), sjablonPeriode.sjablonnavn,
      sjablonPeriode.sjablonVerdi1, sjablonPeriode.sjablonVerdi2)
  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}

data class SjablonPeriodeNy(
    val sjablonDatoFraTil: Periode,
    val sjablonNy: SjablonNy) : PeriodisertGrunnlag {
  constructor(sjablonPeriodeNy: SjablonPeriodeNy) : this(sjablonPeriodeNy.sjablonDatoFraTil.justerDatoer(), sjablonPeriodeNy.sjablonNy)
  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}

data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val inntektType: InntektType,
    val skatteklasse: Int,
    val inntektBelop: Double) : PeriodisertGrunnlag {
  constructor(inntektPeriode: InntektPeriode) : this(inntektPeriode.inntektDatoFraTil.justerDatoer(), inntektPeriode.inntektType,
      inntektPeriode.skatteklasse, inntektPeriode.inntektBelop)
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