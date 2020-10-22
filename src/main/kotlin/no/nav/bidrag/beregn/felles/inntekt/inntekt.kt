package no.nav.bidrag.beregn.felles.inntekt

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.enums.InntektType
import java.math.BigDecimal

data class InntektGrunnlag(
    val inntektDatoFraTil: Periode,
    val inntektType: InntektType,
    val inntektBelop: BigDecimal
)