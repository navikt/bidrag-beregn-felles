package no.nav.bidrag.beregn.felles.bo

import no.nav.bidrag.domene.enums.Avvikstype

data class Avvik(
    val avvikTekst: String,
    val avvikType: Avvikstype
)
