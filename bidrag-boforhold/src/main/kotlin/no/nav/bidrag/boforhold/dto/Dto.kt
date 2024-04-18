package no.nav.bidrag.boforhold.dto

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.Bostatuskode
import java.time.LocalDate

data class BoforholdRequest(
//   Personid til relatert person. Dette er husstandsmedlem eller barn av BM/BP
    val relatertPersonPersonId: String?,
    val fødselsdato: LocalDate,
//   Angir om den relaterte personen er barn av BM/BP
    val erBarnAvBmBp: Boolean,
//   Periodisert liste over en persons bostatus, relatert til BM/BP
    val bostatusListe: List<Bostatus>,
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
