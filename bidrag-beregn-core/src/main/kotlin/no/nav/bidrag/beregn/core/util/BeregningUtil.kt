package no.nav.bidrag.beregn.core.util

import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
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

fun VedtakDto.justerVedtakstidspunkt2(): VedtakDto = if (this.kildeapplikasjon == "bisys") {
    this.copy(
        // Når Bisys oppretter vedtak så sender den vedtakstidspunkt som er 2 timer tidligere enn det som er faktiske vedtakstidspunkt. Kompenserer for dette her.
        vedtakstidspunkt = this.vedtakstidspunkt!!.plusHours(2),
    )
} else {
    this
}

fun VedtakForStønad.justerVedtakstidspunkt(): VedtakForStønad = if (this.kildeapplikasjon == "bisys") {
    val osloZoneId = java.time.ZoneId.of("Europe/Oslo")
    val zonedDateTime = this.vedtakstidspunkt.atZone(osloZoneId)
    val erSommertid = osloZoneId.rules.isDaylightSavings(zonedDateTime.toInstant())

    val justerMedAntallTimer = if (erSommertid) 2L else 1L

    this.copy(
        // Når Bisys oppretter vedtak så sender den vedtakstidspunkt som er 1 eller 2 timer tidligere enn det som er faktiske vedtakstidspunkt. Kompenserer for dette her.
        vedtakstidspunkt = this.vedtakstidspunkt.plusHours(justerMedAntallTimer),
    )
} else {
    this
}
