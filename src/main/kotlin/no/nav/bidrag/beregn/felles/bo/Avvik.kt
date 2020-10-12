package no.nav.bidrag.beregn.felles.bo

import no.nav.bidrag.beregn.felles.enums.AvvikType

data class Avvik(
    val avvikTekst: String,
    val avvikType: AvvikType
)