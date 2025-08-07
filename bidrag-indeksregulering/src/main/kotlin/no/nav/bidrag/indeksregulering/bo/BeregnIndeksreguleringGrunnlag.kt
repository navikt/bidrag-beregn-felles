package no.nav.bidrag.indeksregulering.bo

import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.time.Year

data class BeregnIndeksreguleringGrunnlag(
    val indeksregulerÅr: Year,
    val stønadsid: Stønadsid,
    val personobjektListe: List<GrunnlagDto>,
    val beløpshistorikkListe: List<GrunnlagDto>,
)
