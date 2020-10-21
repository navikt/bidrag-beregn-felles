package no.nav.bidrag.beregn.felles.bo

// Nye sjablonklasser
data class Sjablon(
    val sjablonNavn: String,
    val sjablonNokkelListe: List<SjablonNokkel>? = emptyList(),
    val sjablonInnholdListe: List<SjablonInnhold>
)

data class SjablonNokkel(
    val sjablonNokkelNavn: String,
    val sjablonNokkelVerdi: String
)

data class SjablonInnhold(
    val sjablonInnholdNavn: String,
    val sjablonInnholdVerdi: Double
)

data class SjablonSingelNokkel(
    val sjablonNavn: String,
    val sjablonNokkelVerdi: String,
    val sjablonInnholdListe: List<SjablonInnhold>
)

data class SjablonSingelNokkelSingelInnhold(
    val sjablonNavn: String,
    val sjablonNokkelVerdi: String,
    val sjablonInnholdVerdi: Double
)

data class TrinnvisSkattesats(
    val inntektGrense: Double,
    val sats: Double
)

data class SjablonPeriode(
    val sjablonDatoFraTil: Periode,
    val sjablon: Sjablon) : PeriodisertGrunnlag {
  constructor(sjablonPeriode: SjablonPeriode) : this(sjablonPeriode.sjablonDatoFraTil.justerDatoer(), sjablonPeriode.sjablon)
  override fun getDatoFraTil(): Periode {
    return sjablonDatoFraTil
  }
}

data class SjablonNavnVerdi(
    val sjablonNavn: String,
    val sjablonVerdi: Double
)
