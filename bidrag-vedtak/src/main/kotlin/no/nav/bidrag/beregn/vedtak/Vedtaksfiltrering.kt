package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.beregn.core.util.justerVedtakstidspunkt
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
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
    fun finneVedtakForEvnevurderingNy(vedtak: Collection<VedtakForStønad>, personidentSøknadsbarn: Personident): VedtakForStønad? =
        finneSisteManuelleVedtak(vedtak)

    /**
     * Finner vedtak siste manuelle vedtak for stønadstype
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @return Siste manuelle vedtak
     */
    fun finneSisteManuelleVedtak(vedtak: Collection<VedtakForStønad>): VedtakForStønad? {
        val iterator =
            Vedtaksiterator(
                vedtak.filter { it.filtrereBortIrrelevanteVedtak() }
                    .map(VedtakForStønad::justerVedtakstidspunkt)
                    .sortedByDescending { it.vedtakstidspunkt }.tilVedtaksdetaljer(),
            )

        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()

            // Håndtere resultat fra annet vedtak
            if (vedtaksdetaljer.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksdetaljer.periode.beløp)
                if (!iterator.hasNext()) return null
                continue
            }

            // Hopp over indeksregulering
            if (vedtaksdetaljer.vedtak.erOpprettetAvBatchEllerAldersjusteringIndeksregulering()) {
                continue
            }

            return vedtaksdetaljer.vedtak
        }

        secureLogger.warn { "Fant ikke tidligere vedtak for barn med personident ${vedtak.firstOrNull()?.stønadsendring?.kravhaver?.verdi}" }
        return null
    }

    /**
     * Finner alle manuelle vedtak for en stønad
     * @param vedtak samling vedtak for stønad som sendes inn til metoden
     * @return Alle manuelle vedtak
     */
    fun finneAlleManuelleVedtak(vedtak: Collection<VedtakForStønad>): List<VedtakForStønad> {
        val iterator =
            Vedtaksiterator(
                vedtak.filter { it.filtrereBortIrrelevanteVedtak() }
                    .map(VedtakForStønad::justerVedtakstidspunkt)
                    .sortedByDescending { it.vedtakstidspunkt }.tilVedtaksdetaljer(),
            )

        val vedtaksdetaljerListe = mutableListOf<VedtakForStønad>()

        while (iterator.hasNext()) {
            val vedtaksdetaljer = iterator.next()

            // Håndtere resultat fra annet vedtak
            if (vedtaksdetaljer.vedtak.erResultatFraAnnetVedtak()) {
                iterator.hoppeTilBeløp(vedtaksdetaljer.periode.beløp)
                if (!iterator.hasNext()) return emptyList()
                continue
            }

            // Hopp over indeksregulering
            if (vedtaksdetaljer.vedtak.erOpprettetAvBatchEllerAldersjusteringIndeksregulering()) {
                continue
            }

            vedtaksdetaljerListe.add(vedtaksdetaljer.vedtak)
        }

        if (vedtaksdetaljerListe.isEmpty()) {
            secureLogger.warn { "Fant ingen tidligere vedtak for barn med personident ${vedtak.firstOrNull()?.stønadsendring?.kravhaver?.verdi}" }
        }

        return vedtaksdetaljerListe
    }

    private fun VedtakForStønad.erOpprettetAvBatchEllerAldersjusteringIndeksregulering(): Boolean =
        kilde == Vedtakskilde.AUTOMATISK || type == Vedtakstype.INDEKSREGULERING || type == Vedtakstype.ALDERSJUSTERING

    private fun VedtakForStønad.filtrereBortIrrelevanteVedtak(): Boolean = erInnkreving() && !erIkkeRelevant()
}
