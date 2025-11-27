package no.nav.bidrag.beregn.core.exception

import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponseV2

class IkkeFullBidragsevneOgUfullstendigeGrunnlagException(val melding: String, val data: BidragsberegningOrkestratorResponseV2) :
    RuntimeException(melding)
