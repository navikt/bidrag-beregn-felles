package no.nav.bidrag.indeksregulering.bo

import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.domene.beløp.Beløp
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse

data class IndeksregulerPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val gjelderBarnReferanse: Grunnlagsreferanse,
    val beløp: Beløp,
    var sjablonIndeksreguleringFaktor: SjablonSjablontallBeregningGrunnlag,
    val referanseliste: List<String> = emptyList(),
)
