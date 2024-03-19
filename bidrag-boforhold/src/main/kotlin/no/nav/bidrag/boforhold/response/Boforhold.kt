package no.nav.bidrag.boforhold.response

import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.transport.behandling.grunnlag.response.BorISammeHusstandDto
import java.time.LocalDate

data class RelatertPerson(
//   Personid til relatert person. Dette er husstandsmedlem eller barn av BM/BP
    val relatertPersonPersonId: String?,
//   Den relaterte personens fødselsdato
    val fødselsdato: LocalDate?,
//   Angir om den relaterte personen er barn av BM/BP
    val erBarnAvBmBp: Boolean,
//   Liste over perioder personen bor i samme husstand som BM/BP
    val borISammeHusstandDtoListe: List<BorISammeHusstandDto>,
)

data class BoforholdBeregnet(
    val relatertPersonPersonId: String?,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val fødselsdato: LocalDate,
)
