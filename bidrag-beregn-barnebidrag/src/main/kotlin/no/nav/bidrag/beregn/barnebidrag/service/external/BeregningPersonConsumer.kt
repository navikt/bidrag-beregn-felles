package no.nav.bidrag.beregn.barnebidrag.service.external

import no.nav.bidrag.domene.ident.Personident
import java.time.LocalDate

interface BeregningPersonConsumer {
    fun hentFødselsdatoForPerson(kravhaver: Personident): LocalDate?
}
