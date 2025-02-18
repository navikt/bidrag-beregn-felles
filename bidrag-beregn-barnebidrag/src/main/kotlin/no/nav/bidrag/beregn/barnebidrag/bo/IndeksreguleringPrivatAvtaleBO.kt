package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.barnebidrag.service.PrivatAvtalePeriode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import java.math.BigDecimal

//
data class IndeksreguleringPrivatAvtaleGrunnlag(
    val bruddperiode: ÅrMånedsperiode,
    val referanseTilRolle: Grunnlagsreferanse,
    val søknadsbarnReferanse: Grunnlagsreferanse,
    val periodeSkalIndeksreguleres: Boolean,
    val privatAvtalePeriode: PrivatAvtalePeriode,
    var sjablonIndeksreguleringFaktor: SjablonSjablontallBeregningGrunnlag? = null,
    val beløpFraForrigeDelberegning: BigDecimal? = null,
    val referanseliste: List<String> = emptyList(),
)
