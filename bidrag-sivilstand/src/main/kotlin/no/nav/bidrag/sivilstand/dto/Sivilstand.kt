package no.nav.bidrag.sivilstand.dto

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.transport.behandling.grunnlag.response.SivilstandGrunnlagDto
import java.time.LocalDate

data class SivilstandRequest(
    val fødselsdatoBM: LocalDate,
    // Data som er hentet fra PDL. Disse dataene brukes til å beregne offentlige perioder.
    val innhentedeOffentligeOpplysninger: List<SivilstandGrunnlagDto>,
    // Behandlede sivilstandsopplysninger. Dette vil være resultatperioder fra en tidligere beregning.
    val behandledeSivilstandsopplysninger: List<Sivilstand>,
    // Endret sivilstand. Endring, sletting og ny sivilstandsperiode legges inn her.
    val endreSivilstand: EndreSivilstand?,
)

data class Sivilstand(val periodeFom: LocalDate, val periodeTom: LocalDate?, val sivilstandskode: Sivilstandskode, val kilde: Kilde)

data class EndreSivilstand(
    val typeEndring: TypeEndring,
    // Periode etter endring endret/ny periode. Er null ved slett.
    val nySivilstand: Sivilstand?,
    // Periode som har blitt endret/slettet.
    val originalSivilstand: Sivilstand?,
)
