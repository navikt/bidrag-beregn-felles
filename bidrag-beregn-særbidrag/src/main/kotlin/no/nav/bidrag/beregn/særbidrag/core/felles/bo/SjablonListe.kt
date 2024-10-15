package no.nav.bidrag.beregn.særbidrag.core.felles.bo

import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats
import java.util.Collections.emptyList

data class SjablonListe(
    var sjablonSjablontallResponse: List<Sjablontall> = emptyList(),
    var sjablonBidragsevneResponse: List<Bidragsevne> = emptyList(),
    var sjablonTrinnvisSkattesatsResponse: List<TrinnvisSkattesats> = emptyList(),
    var sjablonSamværsfradragResponse: List<Samværsfradrag> = emptyList(),
)
