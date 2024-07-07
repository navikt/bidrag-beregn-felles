package no.nav.bidrag.boforhold.dto

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.diverse.TypeEndring
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Familierelasjon
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

data class BoforholdBarnRequestV3(
    // Personid til barn/husstandsmedlem
    val gjelderPersonId: String?,
    val fødselsdato: LocalDate,
    // Angir relasjon mellom gjelderPerson og BM/BP
    val relasjon: Familierelasjon,
    // Periodisert liste med offentlige bostatus-opplysninger hentet fra PDL
    val innhentedeOffentligeOpplysninger: List<Bostatus>,
    // Behandlede bostatusopplysninger
    val behandledeBostatusopplysninger: List<Bostatus>,
    // Endret bostatus
    val endreBostatus: EndreBostatus?,
)

data class BoforholdVoksneRequest(
    val boforholdOffentligeOpplysninger: List<BoforholdOffentligeOpplysninger>,
    // Behandlede bostatusopplysninger
    val behandledeBostatusopplysninger: List<Bostatus>,
    // Endret bostatus
    val endreBostatus: EndreBostatus?,
)

data class BoforholdOffentligeOpplysninger(
    // Personid til husstandsmedlem
    val gjelderPersonId: String?,
    val fødselsdato: LocalDate,
    // Angir relasjon mellom gjelderPerson og BM/BP
    val relasjon: Familierelasjon,
    // Periodisert liste med offentlige bostatus-opplysninger hentet fra PDL
    val innhentedeOffentligeOpplysninger: List<Bostatus>,
)

data class Bostatus(val periodeFom: LocalDate?, val periodeTom: LocalDate?, val bostatusKode: Bostatuskode?, val kilde: Kilde)

data class EndreBostatus(
    val typeEndring: TypeEndring,
    // Periode etter endring endret/ny periode. Er null ved slett.
    val nyBostatus: Bostatus?,
    // Periode som har blitt endret/slettet.
    val originalBostatus: Bostatus?,
)

data class BoforholdResponse(
    val relatertPersonPersonId: String?,
    val fødselsdato: LocalDate,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val kilde: Kilde = Kilde.OFFENTLIG,
)

data class BoforholdResponseV2(
    val gjelderPersonId: String?,
    val fødselsdato: LocalDate,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val kilde: Kilde = Kilde.OFFENTLIG,
)

data class BoforholdVoksneResponse(
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val kilde: Kilde = Kilde.OFFENTLIG,
)
