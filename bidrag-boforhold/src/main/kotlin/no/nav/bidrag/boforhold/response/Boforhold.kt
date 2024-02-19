package no.nav.bidrag.boforhold.response

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
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
    val borISammeHusstandDtoListe: List<BorISammeHusstandBeregningDto>,
    val grunnlagsreferanse: Grunnlagsreferanse? = null,
)

data class BorISammeHusstandBeregningDto(
    @Schema(description = "Personen bor i samme husstand som BM/BP fra- og med måned")
    val periodeFra: LocalDate?,
    @Schema(description = "Personen bor i samme husstand som BM/BP til- og med måned")
    val periodeTil: LocalDate?,
    val grunnlagsreferanse: Grunnlagsreferanse? = null,
)

fun BorISammeHusstandDto.tilBeregningDto(grunnlagsreferanse: Grunnlagsreferanse? = null) = BorISammeHusstandBeregningDto(
    periodeFra = periodeFra,
    periodeTil = periodeTil,
    grunnlagsreferanse = grunnlagsreferanse,
)
data class BoforholdBeregnet(
    val relatertPersonPersonId: String?,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?,
    val bostatus: Bostatuskode,
    val grunnlagsreferanseListe: MutableSet<Grunnlagsreferanse>,
)

enum class Bostatus {
    MED_FORELDER,
    IKKE_MED_FORELDER,
    REGNES_IKKE_SOM_BARN,
}
