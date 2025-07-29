package no.nav.bidrag.beregn.core.util

import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.collections.map

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

fun VedtakDto.justerVedtakstidspunkt(): VedtakDto = if (this.kildeapplikasjon == "bisys") {
    this.copy(
        // Når Bisys oppretter vedtak så sender den vedtakstidspunkt som er 2 timer tidligere enn det som er faktiske vedtakstidspunkt. Kompenserer for dette her.
        vedtakstidspunkt = this.vedtakstidspunkt!!.plusHours(2),
    )
} else {
    this
}

fun VedtakForStønad.justerVedtakstidspunkt(): VedtakForStønad = if (this.kildeapplikasjon == "bisys") {
    this.copy(
        // Når Bisys oppretter vedtak så sender den vedtakstidspunkt som er 2 timer tidligere enn det som er faktiske vedtakstidspunkt. Kompenserer for dette her.
        vedtakstidspunkt = this.vedtakstidspunkt.plusHours(2),
    )
} else {
    this
}
