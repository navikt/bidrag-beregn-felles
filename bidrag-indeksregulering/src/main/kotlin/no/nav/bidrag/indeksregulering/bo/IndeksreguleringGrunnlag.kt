package no.nav.bidrag.indeksregulering.bo

import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.service.Beløpshistorikk
import no.nav.bidrag.indeksregulering.service.Periode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import java.math.BigDecimal

data class IndeksreguleringGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val periodeSkalIndeksreguleres: Boolean,
    val referanseTilRolle: Grunnlagsreferanse,
    val gjelderBarnReferanse: Grunnlagsreferanse,
    val beløpshistorikk: Beløpshistorikk,
    val periode: Periode,
    var sjablonIndeksreguleringFaktor: SjablonSjablontallBeregningGrunnlag? = null,
    val beløpFraForrigeDelberegning: BigDecimal? = null,
    val referanseliste: List<String> = emptyList(),
)
