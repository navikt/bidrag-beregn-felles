package no.nav.bidrag.beregn.felles.dto

import java.math.BigDecimal
import java.time.LocalDate

// Felles
data class PeriodeCore(
  val datoFom: LocalDate,
  val datoTil: LocalDate?
)

data class SjablonPeriodeCore(
  val referanse: String,
  val periode: PeriodeCore,
  val navn: String,
  val nokkelListe: List<SjablonNokkelCore>? = emptyList(),
  val innholdListe: List<SjablonInnholdCore>
)

data class SjablonCore(
  val navn: String,
  val nokkelListe: List<SjablonNokkelCore>? = emptyList(),
  val innholdListe: List<SjablonInnholdCore>
)

data class SjablonNokkelCore(
  val navn: String,
  val verdi: String
)

data class SjablonInnholdCore(
  val navn: String,
  val verdi: BigDecimal
)

data class SjablonNavnVerdiCore(
  val navn: String,
  val verdi: BigDecimal
)

data class AvvikCore(
  val avvikTekst: String,
  val avvikType: String
)
