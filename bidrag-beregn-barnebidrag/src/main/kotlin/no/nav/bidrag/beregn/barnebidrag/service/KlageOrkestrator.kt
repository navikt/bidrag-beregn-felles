package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.virkningstidspunkt
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
@Import(VedtakService::class)
class KlageOrkestrator(private val vedtakService: VedtakService) {

    fun utførKlageEndelig(grunnlag: KlageOrkestratorGrunnlag): List<ResultatVedtak> {
        try {
            val stønad = grunnlag.stønad
            val klageberegningResultat = grunnlag.klageberegningResultat
            val klageperiode = grunnlag.klageperiode
            val påklagetVedtakId = grunnlag.påklagetVedtakId

            secureLogger.info { "Komplett klageberegning kjøres for stønad $stønad og påklaget vedtak $påklagetVedtakId" }

            val løpendeStønad = vedtakService.hentLøpendeStønad(stønad)
            val påklagetVedtak = vedtakService.hentVedtak(påklagetVedtakId)
                ?: throw IllegalArgumentException("Fant ikke påklaget vedtak med id $påklagetVedtakId")
            val påklagetVedtakVirkningstidspunkt = påklagetVedtak.virkningstidspunkt
                ?: throw IllegalArgumentException("Påklaget vedtak med id $påklagetVedtakId har ikke virkningstidspunkt")
            val påklagetVedtakVedtakstidspunkt = påklagetVedtak.vedtakstidspunkt
                ?: throw IllegalArgumentException("Påklaget vedtak med id $påklagetVedtakId har ikke vedtakstidspunkt")

            // TODO Sjekk om nytt virkningstidspunkt kan være tidligere enn originalt virkningstidspunkt
            val nyVirkningErEtterOpprinneligVirkning = klageperiode.fom.isAfter(
                YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
            )

            val klageperiodeTilErLikOpprinneligVedtakstidspunkt = klageperiode.til!!.minusMonths(1) ==
                YearMonth.of(påklagetVedtakVedtakstidspunkt.year, påklagetVedtakVedtakstidspunkt.monthValue)

            val klageperiodeTilErLikInneværendePeriode = klageperiode.til!!.minusMonths(1) == YearMonth.now()

            // Scenario 1: Klagevedtak dekker perioden fra opprinnelig virkningstidspunkt til inneværende periode - skal overstyre alt
            if (!nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikInneværendePeriode) {
                return listOf(
                    ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true),
                    ResultatVedtak(resultat = klageberegningResultat, delvedtak = false, klagevedtak = false),
                ).sortedByDescending { it.delvedtak }
            }

            // Scenario 2: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og kjør
            // evt ny indeksregulering/aldersjustering
            if (!nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikOpprinneligVedtakstidspunkt) {
                val etterfølgendeVedtakListe: List<Int> =
                    if (løpendeStønad == null || løpendeStønad.periodeListe.isEmpty()) {
                        emptyList()
                    } else {
                        løpendeStønad.periodeListe
                            .filter { !it.periode.fom.isBefore(klageperiode.til!!.minusMonths(1)) }
                            .map { it.vedtaksid }
                    }
                // TODO Sjekke om det må kjøres ny indeksregulering/aldersjustering
                val delvedtakListe = buildList {
                    add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true))
                    addAll(
                        lagBeregnetBarnebidragResultatFraVedtak(vedtakListe = etterfølgendeVedtakListe, stønadstype = stønad.type)
                            .map { ResultatVedtak(resultat = it, delvedtak = true, klagevedtak = false) },
                    )
                }

                val sammenslåttVedtak = ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false)

                return (delvedtakListe + sammenslåttVedtak).sortedByDescending { it.delvedtak }
            }

            return emptyList()
        } catch (e: Exception) {
            // TODO
            throw e
        }
    }

    // Lager BeregnetBarnebidragResultat (simulert resultat fra beregningen) for alle vedtak i vedtakListe
    private fun lagBeregnetBarnebidragResultatFraVedtak(vedtakListe: List<Int>, stønadstype: Stønadstype): List<BeregnetBarnebidragResultat> {
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultat>()
        vedtakListe.forEach {
            val komplettVedtak = vedtakService.hentVedtak(it)
            if (komplettVedtak != null) {
                beregnetBarnebidragResultatListe.add(
                    // TODO Filtrere grunnlagene
                    BeregnetBarnebidragResultat(komplettVedtak.hentBeregningsperioder(stønadstype), komplettVedtak.grunnlagListe),
                )
            }
        }
        return beregnetBarnebidragResultatListe
    }

    // Extension fuction for å hente beregningsperioder for et vedtak med en gitt stønadstype
    private fun VedtakDto.hentBeregningsperioder(stønadstype: Stønadstype): List<ResultatPeriode> = stønadsendringListe
        .first { it.type == stønadstype }
        .periodeListe
        .map {
            ResultatPeriode(
                periode = it.periode,
                resultat = ResultatBeregning(it.beløp),
                grunnlagsreferanseListe = it.grunnlagReferanseListe,
            )
        }

    // Slår sammen alle vedtak i en liste til ett (teknisk) vedtak
    private fun slåSammenVedtak(vedtakListe: List<ResultatVedtak>): BeregnetBarnebidragResultat {
        val resultatPeriodeListe = mutableListOf<ResultatPeriode>()
        val grunnlagListe = mutableListOf<GrunnlagDto>()
        vedtakListe.forEach {
            resultatPeriodeListe.addAll(it.resultat.beregnetBarnebidragPeriodeListe)
            grunnlagListe.addAll(it.resultat.grunnlagListe)
        }
        return BeregnetBarnebidragResultat(
            // TODO Sjekk at det ikke er duplikater og overlappende perioder eller hull i periodene
            beregnetBarnebidragPeriodeListe = sorterOgJusterPerioder(resultatPeriodeListe),
            // TODO Sjekk om grunnlagslisten blir riktig
            grunnlagListe = grunnlagListe.distinctBy { it.referanse },
        )
    }

    // Sorterer ResultatPeriode basert på periode-fom og erstatter åpen sluttperiode med fom-dato på neste forekomst (hvis den finnes)
    private fun sorterOgJusterPerioder(perioder: List<ResultatPeriode>): List<ResultatPeriode> {
        val sortert = perioder.sortedBy { it.periode.fom }

        return sortert.mapIndexed { indeks, resultatPeriode ->
            val nesteFom = sortert.getOrNull(indeks + 1)?.periode?.fom
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.fom, til = nesteFom ?: resultatPeriode.periode.til),
                resultat = resultatPeriode.resultat,
                grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe,
            )
        }
    }
}
