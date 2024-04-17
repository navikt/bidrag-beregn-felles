package no.nav.bidrag.sivilstand.dto

import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

data class SivilstandRequest(
    val offentligePerioder: List<SivilstandGrunnlagDto>,
    val manuellePerioder: List<Sivilstand>,
)

data class Sivilstand(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskode: Sivilstandskode,
    val kilde: Kilde,
)

data class SivilstandPDLBo(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskodePDL: SivilstandskodePDL,
    val kilde: Kilde,
)

enum class Kilde {
    MANUELL,
    OFFENTLIG,
}
