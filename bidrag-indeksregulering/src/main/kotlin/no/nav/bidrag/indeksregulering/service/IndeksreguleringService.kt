package no.nav.bidrag.indeksregulering.service

import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

internal class IndeksreguleringService : BeregnService() {

    fun beregn(grunnlag: BeregnGrunnlag): List<GrunnlagDto> = emptyList()
}
