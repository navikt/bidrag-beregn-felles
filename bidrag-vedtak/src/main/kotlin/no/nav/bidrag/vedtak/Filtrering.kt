package no.nav.bidrag.vedtak

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.søknadId
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class Vedtaksfiltrering {

    fun finneManueltVedtakTilEvnevurdering(vedtak: Collection<VedtakDto>, personidentSøknadsbarn: Personident): VedtakDto? {

        val eksludereVedtakstyper = setOf(Vedtakstype.INDEKSREGULERING)

        val iterator = Vedtaksiterator(vedtak.filter { !eksludereVedtakstyper.contains(it.type) }
            .filter { !it.erKlage() }
            .filter { it.filtrereBortIrrelevanteVedtak() }.tilVedtaksperioder())

        while (iterator.hasNext()) {
            val vedtaksperiode = iterator.next()
            // Dersom resultatet er Ingen endring 12% skal vedtaket hoppes over.
            if (vedtaksperiode.vedtak.erIngenEndringPga12Prosentregel()) {
                // Dersom dette er resultatet av en klage skal det hoppes til det påklagde vedtaket.
                if (vedtaksperiode.vedtak.erKlage()) {
                    // Hopp til påklaget vedtak
                    iterator.hoppeTilPåklagetVedtak(vedtaksperiode.vedtak.søknadKlageRefId!!)
                    // Hopp over dette vedtaket, ettersom dette enten er eller skulle vært Ingen endring 12%
                    iterator.next()
                } else if (vedtaksperiode.vedtak.erOmgjøring()) {
                    // Hopp til påklaget vedtak
                    iterator.hoppeTilOmgjortVedtak(vedtaksperiode.vedtak.idTilOmgjortVedtak()!!)
                    // Hopp over dette vedtaket, ettersom dette enten er eller skulle vært Ingen endring 12%
                    iterator.next()
                }
                continue
            }

            // Håndtere resultat fra annet vedtak
            if (vedtaksperiode.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksperiode.periode.beløp)
                require(iterator.hasNext(), { "Fant ikke tidligere manuelt vedtak i vedtak ${vedtaksperiode.vedtak.id}" })
                return iterator.next().vedtak
            }

            return vedtaksperiode.vedtak
        }

        secureLogger.error { "Fant ikke tidligere vedtak for barn med personident $personidentSøknadsbarn" }
        throw IllegalStateException("Fant ikke tidligere vedtak")
    }

    private fun VedtakDto.filtrereBortIrrelevanteVedtak(): Boolean {
        if (this.erKlage() || this.erAutomatiskVedtak()) return false
        val bidragMedInnkreving = this.stønadsendringListe.filter { Stønadstype.BIDRAG == it.type && Innkrevingstype.MED_INNKREVING == it.innkreving }
        return !bidragMedInnkreving.omgjørVedtak() && bidragMedInnkreving.erEndring()
    }
}

data class Vedtaksperiode(
    val vedtak: VedtakDto,
    val periode: VedtakPeriodeDto,
)

class Vedtaksiterator(vedtakssamling: Collection<Vedtaksperiode>) : Iterator<Vedtaksperiode> {

    private val iterator: Iterator<Vedtaksperiode> = vedtakssamling.asSequence().sortedByDescending { it.periode.delytelseId }.iterator()
    private var nesteVedtak: Vedtaksperiode? = null

    init {
        forberedeNeste()
    }

    override fun hasNext(): Boolean {
        return nesteVedtak != null
    }

    override fun next(): Vedtaksperiode {
        if (!hasNext()) {
            throw NoSuchElementException("Har ikke flere vedtak å iterere over.")
        }
        return nesteVedtak!!
    }

    private fun forberedeNeste() {
        if (iterator.hasNext()) {
            nesteVedtak = iterator.next()
        } else {
            nesteVedtak = null
        }
    }

    fun hoppeTilPåklagetVedtak(referanseTilPåklagetVedtak: Long) {
        while (nesteVedtak != null && nesteVedtak!!.vedtak.søknadId != referanseTilPåklagetVedtak) {
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
        while (nesteVedtak != null && nesteVedtak!!.vedtak.id != idTilOmgjortVedtak) {
            forberedeNeste()
        }
    }
}



