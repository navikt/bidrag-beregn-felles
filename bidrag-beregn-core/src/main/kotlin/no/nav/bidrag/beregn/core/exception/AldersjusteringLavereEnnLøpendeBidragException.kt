package no.nav.bidrag.beregn.core.exception

import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat

class AldersjusteringLavereEnnLøpendeBidragException(
    val melding: String,
    val data: BeregnetBarnebidragResultat,
) : RuntimeException(melding)
