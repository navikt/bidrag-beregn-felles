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
    // Manuelle bostatusopplysninger
    val manuelleBostatusopplysninger: List<Bostatus>,
)

data class Bostatus(
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode?,
    val kilde: Kilde,
)

data class BoforholdResponse(
    val relatertPersonPersonId: String?,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val fødselsdato: LocalDate,
    val kilde: Kilde = Kilde.OFFENTLIG,
)
