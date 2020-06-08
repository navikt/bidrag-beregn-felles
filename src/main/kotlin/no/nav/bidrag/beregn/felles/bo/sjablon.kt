package no.nav.bidrag.beregn.felles.bo

// Nye sjablonklasser
data class SjablonNy(
    val sjablonNavn: String,
    val sjablonNokkelListe: List<SjablonNokkelNy>,
    val sjablonInnholdListe: List<SjablonInnholdNy>
)

data class SjablonNokkelNy(
    val sjablonNokkelNavn: String,
    val sjablonNokkelVerdi: String
)

data class SjablonInnholdNy(
    val sjablonInnholdNavn: String,
    val sjablonInnholdVerdi: Double
)

data class SjablonSingelNokkelNy(
    val sjablonNavn: String,
    val sjablonNokkelVerdi: String,
    val sjablonInnholdListe: List<SjablonInnholdNy>
)

data class SjablonSingelNokkelSingelInnholdNy(
    val sjablonNavn: String,
    val sjablonNokkelVerdi: String,
    val sjablonInnholdVerdi: Double
)

data class TrinnvisSkattesatsNy(
    val inntektGrense: Double,
    val sats: Double
)

