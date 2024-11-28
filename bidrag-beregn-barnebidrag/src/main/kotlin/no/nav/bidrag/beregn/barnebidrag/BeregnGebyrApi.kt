package no.nav.bidrag.beregn.barnebidrag

import no.nav.bidrag.beregn.barnebidrag.beregning.BeregnGebyrService
import no.nav.bidrag.commons.service.sjablon.SjablonService
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import org.springframework.stereotype.Service

@Service
class BeregnGebyrApi(sjablonService: SjablonService) {

    private val service = BeregnGebyrService(sjablonService)

    fun beregnGebyr(grunnlagsliste: List<GrunnlagDto>, referanseTilRolle: Grunnlagsreferanse) = service.beregnGebyr(grunnlagsliste, referanseTilRolle)
}
