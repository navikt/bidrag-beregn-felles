package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import java.math.BigDecimal
import java.time.LocalDate

data class SøknadsbarnPeriodeGrunnlag(val referanse: String, val fødselsdato: LocalDate)

data class InntektBeregningGrunnlag(val referanse: String, val sumInntekt: BigDecimal)

data class BeløpshistorikkPeriodeGrunnlag(val referanse: String, val beløpshistorikkPeriode: BeløpshistorikkGrunnlag)
