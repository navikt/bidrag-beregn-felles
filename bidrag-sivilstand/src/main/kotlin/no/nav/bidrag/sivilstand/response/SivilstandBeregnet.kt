package no.nav.bidrag.sivilstand.response

import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate
import java.time.LocalDateTime

fun SivilstandGrunnlagDto.tilBeregningGrunnlagDto(grunnlagreferanse: String? = null) = SivilstandBeregningGrunnlagDto(
    personId = personId,
    type = type,
    gyldigFom = gyldigFom,
    bekreftelsesdato = bekreftelsesdato,
    master = master,
    registrert = registrert,
    historisk = historisk,
    grunnlagsreferanse = grunnlagreferanse,
)
data class SivilstandBeregningGrunnlagDto(
    val personId: String?,
    val type: SivilstandskodePDL?,
    val gyldigFom: LocalDate?,
    val bekreftelsesdato: LocalDate?,
    val master: String?,
    val registrert: LocalDateTime?,
    val historisk: Boolean?,
    val grunnlagsreferanse: Grunnlagsreferanse? = null,
)
data class SivilstandBo(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskodePDL: SivilstandskodePDL?,
    val grunnlagsreferanse: Grunnlagsreferanse? = null,
)

data class SivilstandBeregnet(
    val status: Status,
    val sivilstandListe: List<SivilstandBeregnetPeriode>,
)

data class SivilstandBeregnetPeriode(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskode: Sivilstandskode,
    val grunnlagsreferanser: List<Grunnlagsreferanse> = emptyList(),
)

enum class Status {
    OK,
    MANGLENDE_DATOINFORMASJON,
    LOGISK_FEIL_I_TIDSLINJE,
    ALLE_FOREKOMSTER_ER_HISTORISKE,
    SIVILSTANDSTYPE_MANGLER,
}
