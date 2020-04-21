package no.nav.bidrag.beregn.bidragsevne.bo

import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

interface PeriodisertGrunnlag {
  fun getDatoFraTil(): Periode
}

data class Periode(
    var datoFra: LocalDate,
    val datoTil: LocalDate?
) : PeriodisertGrunnlag {
  companion object {
    // Juster dato til den første i neste måned (hvis ikke dato er den første i inneværende måned)
    internal fun justerDato(dato: LocalDate?): LocalDate? {
      return if (dato == null || dato.dayOfMonth == 1) dato else dato.with(TemporalAdjusters.firstDayOfNextMonth())
    }
  }

  constructor(periode: Periode) : this(justerDato(periode.datoFra) ?: periode.datoFra, justerDato(periode.datoTil))

  override fun getDatoFraTil(): Periode {
    return this
  }

  // Sjekker at en denne perioden overlapper med annenPeriode (intersect)
  fun overlapperMed(annenPeriode: Periode): Boolean {
    return ((annenPeriode.datoTil == null || datoFra.isBefore(annenPeriode.datoTil))
        && (datoTil == null || datoTil.isAfter(annenPeriode.datoFra)))
  }

  // Sjekk om perioden overlapper (datoFra i denne perioden kommer tidligere enn datoTil i forrige periode)
  // Hvis forrige periode er null, er dette den første perioden. Ingen kontroll nødvendig
  fun overlapper(forrigePeriode: Periode?): Boolean {
    if (forrigePeriode?.datoTil == null) return false

    return datoFra.isBefore(forrigePeriode.datoTil)
  }

  // Sjekk om det er opphold (gap) mellom periodene (datoFra i denne perioden kommer senere enn datoTil i forrige periode)
  // Hvis forrige periode er null, er dette den første perioden. Ingen kontroll nødvendig
  fun harOpphold(forrigePeriode: Periode?): Boolean {
    if (forrigePeriode == null) return false

    return forrigePeriode.datoTil != null && datoFra.isAfter(forrigePeriode.datoTil)
  }

  // Sjekk om datoFra er tidligere eller lik datoTil
  fun datoTilErEtterDatoFra(): Boolean {
    return datoTil == null || datoTil.isAfter(datoFra)
  }

  // Juster datoer i perioden
  internal fun justerDatoer(): Periode {
    val fraDato = justerDato(datoFra)
    val tilDato = justerDato(datoTil)

    return Periode(fraDato as LocalDate, tilDato)
  }
}

data class BostatusPeriode(
    val bostatusDatoFraTil: Periode,
    val bostatusKode: BostatusKode
) : PeriodisertGrunnlag {
  constructor(bostatusPeriode: BostatusPeriode) : this(bostatusPeriode.bostatusDatoFraTil.justerDatoer(), bostatusPeriode.bostatusKode)

  override fun getDatoFraTil(): Periode {
    return bostatusDatoFraTil
  }
}

data class InntektPeriode(
    val inntektDatoFraTil: Periode,
    val inntektBelop: BigDecimal
) : PeriodisertGrunnlag {
  constructor(inntektPeriode: InntektPeriode) : this(inntektPeriode.inntektDatoFraTil.justerDatoer(), inntektPeriode.inntektBelop)

  override fun getDatoFraTil(): Periode {
    return inntektDatoFraTil
  }
}

data class SivilstandPeriode(
    val sivilstandDatoFraTil: Periode,
    val sivilstandKode: SivilstandKode
) : PeriodisertGrunnlag {
  constructor(sivilstandPeriode: SivilstandPeriode) : this(sivilstandPeriode.sivilstandDatoFraTil.justerDatoer(), sivilstandPeriode.sivilstandKode)

  override fun getDatoFraTil(): Periode {
    return sivilstandDatoFraTil
  }
}

data class SjablonPeriode(
    val sjablonDatoFraTil: Periode,
    val sjablonType: String,
    val sjablonVerdi: Int
)

data class SjablonPeriodeVerdi(
    val sjablonDatoFraTil: Periode,
    val sjablonVerdi: Int
) : PeriodisertGrunnlag {
  constructor(sjablonPeriode: SjablonPeriode) : this(sjablonPeriode.sjablonDatoFraTil.justerDatoer(), sjablonPeriode.sjablonVerdi)

  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}

data class AlderPeriode(
    val alderDatoFraTil: Periode,
    val alder: Int
) : PeriodisertGrunnlag {

  override fun getDatoFraTil(): Periode {
    return alderDatoFraTil
  }
}
