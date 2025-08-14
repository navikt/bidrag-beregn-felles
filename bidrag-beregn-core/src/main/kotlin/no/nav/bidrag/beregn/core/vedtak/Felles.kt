package no.nav.bidrag.beregn.core.vedtak

import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.søknadsid
import java.math.BigDecimal

data class Vedtaksdetaljer(var erOmgjort: Boolean = false, val vedtak: VedtakForStønad, val periode: VedtakPeriodeDto)

class Vedtaksiterator(vedtakssamling: Collection<Vedtaksdetaljer>) : Iterator<Vedtaksdetaljer> {

    private val iterator: Iterator<Vedtaksdetaljer> = vedtakssamling.asSequence().sortedByDescending { it.vedtak.vedtakstidspunkt }.iterator()
    private var nesteVedtak: Vedtaksdetaljer? = null
    private var omgjorteVedtak = emptySet<Int>()

    init {
        forberedeNeste()
    }

    override fun hasNext(): Boolean = nesteVedtak != null

    override fun next(): Vedtaksdetaljer {
        if (!hasNext()) {
            throw NoSuchElementException("Har ikke flere vedtak å iterere over.")
        }
        val neste = nesteVedtak
        forberedeNeste()
        return neste!!
    }

    private fun forberedeNeste() {
        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()
            if (vedtaksdetaljer.vedtak.stønadsendring.erEndring() && vedtaksdetaljer.vedtak.erKlage()) {
                omgjorteVedtak.plus(vedtaksdetaljer.vedtak.idTilOmgjortVedtak())
            }
            vedtaksdetaljer.erOmgjort = omgjorteVedtak.contains(vedtaksdetaljer.vedtak.idTilOmgjortVedtak())
            nesteVedtak = vedtaksdetaljer
            return
        }

        nesteVedtak = null
    }

    fun hoppeTilPåklagetVedtak(referanseTilPåklagetVedtak: Long) {
        while (nesteVedtak != null && nesteVedtak!!.vedtak.søknadsid != referanseTilPåklagetVedtak) {
            forberedeNeste()
        }
    }

    fun hoppeTilBeløp(beløp: BigDecimal?) {
        while (nesteVedtak != null && !erSammeBeløp(nesteVedtak!!.periode.beløp, beløp)) {
            forberedeNeste()
        }
    }

    fun erSammeBeløp(beløpA: BigDecimal?, beløpB: BigDecimal?): Boolean {
        if (beløpA == null) return beløpB == null
        return beløpB != null && beløpA.compareTo(beløpB) == 0
    }

    fun hoppeTilOmgjortVedtak(idTilOmgjortVedtak: Int) {
        while (nesteVedtak != null && nesteVedtak!!.vedtak.vedtaksid != idTilOmgjortVedtak) {
            forberedeNeste()
        }
    }
}
