package no.nav.bidrag.sivilstand.response

import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import java.time.LocalDate

data class SivilstandBo(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskodePDL: SivilstandskodePDL?,
)

data class SivilstandBeregnet(
    val status: Status,
    val sivilstandListe: List<Sivilstand>,
)

data class Sivilstand(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskode: Sivilstandskode,
)

enum class Status {
    OK,
    MANGLENDE_DATOINFORMASJON,
    LOGISK_FEIL_I_TIDSLINJE,
    ALLE_FOREKOMSTER_ER_HISTORISKE,
    SIVILSTANDSTYPE_MANGLER,
}
