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

