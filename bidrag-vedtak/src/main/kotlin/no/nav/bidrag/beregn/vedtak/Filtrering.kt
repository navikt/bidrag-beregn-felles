package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId
import no.nav.bidrag.transport.behandling.vedtak.response.søknadsid
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class Vedtaksfiltrering {

    /**
     * Finner vedtak som skal benyttes i evnevurdering fra en samling vedtak knyttet til en bestemt stønad. Returnerer null dersom metoden
     * ikke finner relevant vedtak. Dette kan ansees som en unntaktstilstand.
     *
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @param personidentSøknadsbarn personidenSøknadsbarn typisk fødselsnummer til søknadsbarnet stønaden og vedtakene gjelder for
     * @return vedtak for evnevurdering for stønaden
     */
    fun finneVedtakForEvnevurdering(vedtak: Collection<VedtakForStønad>, personidentSøknadsbarn: Personident): VedtakForStønad? {
        val iterator = Vedtaksiterator(vedtak.filter { it.filtrereBortIrrelevanteVedtak() }.tilVedtaksdetaljer())

        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()

            // Hopp over dersom vedtaket er omgjort.
            if (vedtaksdetaljer.erOmgjort) {
                continue
            }

            // Dersom vedtaket gjelder klage, skal det hoppes til det påklagde vedtaket.
            if (vedtaksdetaljer.vedtak.erKlage()) {
                // Hopp til påklaget vedtak
                vedtaksdetaljer.vedtak.omgjørVedtaksid()?.let { iterator.hoppeTilOmgjortVedtak(it.toLong()) }
                    ?: iterator.hoppeTilPåklagetVedtak(vedtaksdetaljer.vedtak.søknadKlageRefId!!)
                continue
            } else if (vedtaksdetaljer.vedtak.erOmgjøring()) {
                // Hopp til omgjort vedtak
                iterator.hoppeTilOmgjortVedtak(vedtaksdetaljer.vedtak.idTilOmgjortVedtak()!!)
                continue
            }

            // Håndtere resultat fra annet vedtak
            if (vedtaksdetaljer.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksdetaljer.periode.beløp)
                if (!iterator.hasNext()) return null
                continue
            }

            // Hopp over indeksregulering
            if (Vedtakstype.INDEKSREGULERING == vedtaksdetaljer.vedtak.type) {
                continue
            }

            return vedtaksdetaljer.vedtak
        }

        secureLogger.warn { "Fant ikke tidligere vedtak for barn med personident $personidentSøknadsbarn" }
        return null
    }

    private fun VedtakForStønad.filtrereBortIrrelevanteVedtak(): Boolean =
        erInnkreving() && !erIkkeRelevant() && (erBidrag() || er18årsbidrag() || erOppfostringsbidrag())
}

data class Vedtaksdetaljer(var erOmgjort: Boolean = false, val vedtak: VedtakForStønad, val periode: VedtakPeriodeDto)

class Vedtaksiterator(vedtakssamling: Collection<Vedtaksdetaljer>) : Iterator<Vedtaksdetaljer> {

    private val iterator: Iterator<Vedtaksdetaljer> = vedtakssamling.asSequence().sortedByDescending { it.periode.delytelseId }.iterator()
    private var nesteVedtak: Vedtaksdetaljer? = null
    private var omgjorteVedtak = emptySet<Long>()

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

    fun hoppeTilOmgjortVedtak(idTilOmgjortVedtak: Long) {
        while (nesteVedtak != null && nesteVedtak!!.vedtak.vedtaksid != idTilOmgjortVedtak) {
            forberedeNeste()
        }
    }
}
