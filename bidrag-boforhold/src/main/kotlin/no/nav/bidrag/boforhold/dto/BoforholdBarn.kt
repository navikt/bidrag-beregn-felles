package no.nav.bidrag.boforhold.dto

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate

data class BoforholdBarnRequest(
    // Personid til barn av BM
    val relatertPersonPersonId: String?,
    val fødselsdato: LocalDate,
    // Angir om den relaterte personen er barn av BM/BP
    val erBarnAvBmBp: Boolean,
    // Periodisert liste med offentlige bostatus-opplysninger hentet fra PDL
    val innhentedeOffentligeOpplysninger: List<Bostatus>,
    // Behandlede bostatusopplysninger
    val behandledeBostatusopplysninger: List<Bostatus>,
    // Endret bostatus
    val endreBostatus: EndreBostatus?,
)

data class Bostatus(
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    val bostatusKode: Bostatuskode?,
    val kilde: Kilde,
)

data class EndreBostatus(
    val typeEndring: TypeEndring,
    // Periode etter endring endret/ny periode. Er null ved slett.
    val nyBostatus: Bostatus?,
    // Periode som har blitt endret/slettet.
    val originalBostatus: Bostatus?,
)

enum class TypeEndring {
    NY,
    ENDRET,
    SLETTET,
}

data class BoforholdResponse(
    val relatertPersonPersonId: String?,
    val fødselsdato: LocalDate,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatuskode: Bostatuskode,
    val kilde: Kilde = Kilde.OFFENTLIG,
)
