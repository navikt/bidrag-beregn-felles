package no.nav.bidrag.beregn.core.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun BigDecimal.årsbeløpTilMåndesbeløp() = divide(BigDecimal(12), MathContext(10, RoundingMode.HALF_UP))
fun BigDecimal.avrund(antallDesimaler: Int) = setScale(antallDesimaler, RoundingMode.HALF_UP)
val BigDecimal.nærmesteTier get() = divide(
    BigDecimal.TEN,
    0,
    RoundingMode.HALF_UP,
).multiply(
    BigDecimal.TEN,
)
val BigDecimal.avrundetTilToDesimaler get() = avrund(2)
