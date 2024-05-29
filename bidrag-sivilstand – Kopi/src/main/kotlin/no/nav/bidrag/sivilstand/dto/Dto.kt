package no.nav.bidrag.sivilstand.dto

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

data class SivilstandRequest(
    // Data som er hentet fra PDL. Disse dataene brukes til å beregne offentlige perioder.
    val offentligePerioder: List<SivilstandGrunnlagDto>,
    // Manuelle perioder.
    val manuellePerioder: List<Sivilstand>,
)

data class Sivilstand(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val sivilstandskode: Sivilstandskode,
    val kilde: Kilde,
)
