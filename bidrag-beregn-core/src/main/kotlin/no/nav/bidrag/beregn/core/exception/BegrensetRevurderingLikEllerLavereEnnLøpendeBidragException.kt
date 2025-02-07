package no.nav.bidrag.beregn.core.exception

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat

class BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException(
    val melding: String,
    val periodeListe: List<ÅrMånedsperiode>,
    val data: BeregnetBarnebidragResultat,
) : RuntimeException(melding)
