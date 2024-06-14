package no.nav.bidrag.beregn.core.felles.bo

import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats

data class SjablonListe(
    var sjablontallResponse: List<Sjablontall> = emptyList(),
    var sjablonSamvaersfradragResponse: List<Samværsfradrag> = emptyList(),
    var sjablonBidragsevneResponse: List<Bidragsevne> = emptyList(),
    var sjablonTrinnvisSkattesatsResponse: List<TrinnvisSkattesats> = emptyList(),
)
