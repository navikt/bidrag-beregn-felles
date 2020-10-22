package no.nav.bidrag.beregn.felles.inntekt

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.enums.InntektType

data class InntektGrunnlag(
    val inntektDatoFraTil: Periode,
    val inntektType: InntektType,
    val inntektBelop: Double
)