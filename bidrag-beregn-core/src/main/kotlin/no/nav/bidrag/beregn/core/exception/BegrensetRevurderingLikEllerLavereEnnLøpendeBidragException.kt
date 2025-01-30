package no.nav.bidrag.beregn.core.exception

import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

class BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException(val melding: String, val data: List<GrunnlagDto>) : RuntimeException(melding)
