package no.nav.bidrag.beregn.felles.dto

import java.time.LocalDate

// Felles
data class PeriodeCore(
    val periodeDatoFra: LocalDate,
    val periodeDatoTil: LocalDate?
)

data class SjablonPeriodeCore(
    val sjablonPeriodeDatoFraTil: PeriodeCore,
    val sjablonNavn: String,
    val sjablonNokkelListe: List<SjablonNokkelCore>? = emptyList(),
    val sjablonInnholdListe: List<SjablonInnholdCore>
)

data class SjablonCore(
    val sjablonNavn: String,
    val sjablonNokkelListe: List<SjablonNokkelCore>? = emptyList(),
    val sjablonInnholdListe: List<SjablonInnholdCore>
)

data class AvvikCore(
    val avvikTekst: String,
    val avvikType: String
)

data class SjablonNokkelCore(
    val sjablonNokkelNavn: String,
    val sjablonNokkelVerdi: String
)

data class SjablonInnholdCore(
    val sjablonInnholdNavn: String,
    val sjablonInnholdVerdi: Double
)