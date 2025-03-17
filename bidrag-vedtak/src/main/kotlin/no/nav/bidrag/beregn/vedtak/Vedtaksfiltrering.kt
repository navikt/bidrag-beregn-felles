package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.søknadKlageRefId
import org.springframework.stereotype.Service

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
        val iterator = Vedtaksiterator(vedtak.filter { it.filtrereBortIrrelevanteVedtakBidrag() }.tilVedtaksdetaljer())

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
     * Finner vedtak siste manuelle vedtak for stønadstype
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @param personidentSøknadsbarn personidenSøknadsbarn typisk fødselsnummer til søknadsbarnet stønaden og vedtakene gjelder for
     * @return Siste manuelle vedtak
     */
    fun finneSisteManuelleVedtak(
        vedtak: Collection<VedtakForStønad>,
        personidentSøknadsbarn: Personident,
        stønadstype: Stønadstype,
    ): VedtakForStønad? {
        val iterator = Vedtaksiterator(vedtak.filter { it.filtrereBortIrrelevanteVedtak(stønadstype) }.tilVedtaksdetaljer())

        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()

            // Håndtere resultat fra annet vedtak
            if (vedtaksdetaljer.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksdetaljer.periode.beløp)
                if (!iterator.hasNext()) return null
                continue
            }

            // Hopp over indeksregulering
            if (listOf(Vedtakstype.INDEKSREGULERING, Vedtakstype.ALDERSJUSTERING).contains(vedtaksdetaljer.vedtak.type)) {
                continue
            }

            return vedtaksdetaljer.vedtak
        }

        secureLogger.warn { "Fant ikke tidligere vedtak for barn med personident $personidentSøknadsbarn" }
        return null
    }

    private fun VedtakForStønad.filtrereBortIrrelevanteVedtak(forStønad: Stønadstype): Boolean =
        erInnkreving() && !erIkkeRelevant() && (stønadsendring.type == forStønad)

    private fun VedtakForStønad.filtrereBortIrrelevanteVedtakBidrag(): Boolean =
        erInnkreving() && !erIkkeRelevant() && (erBidrag() || er18årsbidrag() || erOppfostringsbidrag())
}
