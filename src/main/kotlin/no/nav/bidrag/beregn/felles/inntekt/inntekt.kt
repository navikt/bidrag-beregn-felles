package no.nav.bidrag.beregn.felles.inntekt

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import no.nav.bidrag.beregn.felles.enums.InntektType
import java.math.BigDecimal

data class InntektPeriodeGrunnlag(
    val inntektDatoFraTil: Periode,
    val inntektType: InntektType,
    val inntektBelop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean) : PeriodisertGrunnlag {

  constructor(inntektPeriodeGrunnlag: InntektPeriodeGrunnlag)
      : this(inntektPeriodeGrunnlag.inntektDatoFraTil.justerDatoer(),
      inntektPeriodeGrunnlag.inntektType,
      inntektPeriodeGrunnlag.inntektBelop,
      inntektPeriodeGrunnlag.deltFordel,
      inntektPeriodeGrunnlag.skatteklasse2)

  override fun getDatoFraTil(): Periode {
    return inntektDatoFraTil
  }
}

data class PeriodisertInntekt(
    val inntektDatoFraTil: Periode,
    val inntektSummertBelop: BigDecimal,
    var inntektFordelSaerfradragBelop: BigDecimal,
    val sjablon0004FordelSkatteklasse2Belop: BigDecimal,
    val sjablon0030OvreInntektsgrenseBelop: BigDecimal,
    val sjablon0031NedreInntektsgrenseBelop: BigDecimal,
    val sjablon0039FordelSaerfradragBelop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean
)
