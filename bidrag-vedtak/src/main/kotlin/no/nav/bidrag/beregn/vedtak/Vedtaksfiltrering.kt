package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId
import org.springframework.stereotype.Service

@Service
class Vedtaksfiltrering {
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

    /**
     * Finner vedtak som skal benyttes i evnevurdering fra en samling vedtak knyttet til en bestemt stønad. Returnerer null dersom metoden
     * ikke finner relevant vedtak. Dette kan ansees som en unntaktstilstand.
     *
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @param personidentSøknadsbarn personidenSøknadsbarn typisk fødselsnummer til søknadsbarnet stønaden og vedtakene gjelder for
     * @return vedtak for evnevurdering for stønaden
     */
    fun finneVedtakForEvnevurderingNy(vedtak: Collection<VedtakForStønad>, personidentSøknadsbarn: Personident): VedtakForStønad? =
        finneSisteManuelleVedtak(vedtak)

    /**
     * Finner vedtak siste manuelle vedtak for stønadstype
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @return Siste manuelle vedtak
     */
    fun finneSisteManuelleVedtak(vedtak: Collection<VedtakForStønad>): VedtakForStønad? {
        val iterator =
            Vedtaksiterator(vedtak.filter { it.filtrereBortIrrelevanteVedtak() }.sortedByDescending { it.vedtakstidspunkt }.tilVedtaksdetaljer())

        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()

            // Håndtere resultat fra annet vedtak
            if (vedtaksdetaljer.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksdetaljer.periode.beløp)
                if (!iterator.hasNext()) return null
                continue
            }

            // Hopp over indeksregulering
            if (vedtaksdetaljer.vedtak.kilde == Vedtakskilde.AUTOMATISK) {
                continue
            }

            return vedtaksdetaljer.vedtak
        }

        secureLogger.warn { "Fant ikke tidligere vedtak for barn med personident ${vedtak.firstOrNull()?.stønadsendring?.kravhaver?.verdi}" }
        return null
    }

    private fun VedtakForStønad.filtrereBortIrrelevanteVedtak(): Boolean = erInnkreving() && !erIkkeRelevant()
}
