package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilleggPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import java.math.BigDecimal
import java.time.LocalDate

data class SøknadsbarnPeriodeGrunnlag(val referanse: String, val fødselsdato: LocalDate)

data class SjablonSjablontallPeriodeGrunnlag(val referanse: String, val sjablonSjablontallPeriode: SjablonSjablontallPeriode)

data class InntektBeregningGrunnlag(val referanse: String, val sumInntekt: BigDecimal)

data class SjablonSjablontallBeregningGrunnlag(val referanse: String, val type: String, val verdi: Double)

data class BarnetilleggPeriodeGrunnlag(val referanse: String, val barnetilleggPeriode: BarnetilleggPeriode)
