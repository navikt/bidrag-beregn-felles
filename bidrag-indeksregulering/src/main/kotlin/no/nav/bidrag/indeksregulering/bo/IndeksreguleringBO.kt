package no.nav.bidrag.sivilstand.bo

import no.nav.bidrag.domene.enums.diverse.Kilde
import no.nav.bidrag.domene.enums.person.SivilstandskodePDL
import java.time.LocalDate

data class IndeksreguleringBO(val periodeFom: LocalDate, val periodeTom: LocalDate?, val sivilstandskodePDL: SivilstandskodePDL, val kilde: Kilde)
