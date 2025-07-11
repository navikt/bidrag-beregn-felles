package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.ResultatFraVedtakGrunnlag
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.virkningstidspunkt
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
@Import(VedtakService::class)
class KlageOrkestrator(private val vedtakService: VedtakService) {

    fun utførKlageEndelig(klageberegningResultat: BeregnetBarnebidragResultat, grunnlag: KlageOrkestratorGrunnlag): List<ResultatVedtak> {
        try {
            val stønad = grunnlag.stønad
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
                        lagBeregnetBarnebidragResultatFraEksisterendeVedtak(vedtakListe = etterfølgendeVedtakListe, stønadstype = stønad.type)
                            .map { ResultatVedtak(resultat = it, delvedtak = true, klagevedtak = false) },
                    )
                }

                val sammenslåttVedtak = ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false)

                return (delvedtakListe + sammenslåttVedtak).sorterListe()
            }

            // Scenario 3: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
            // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles ut.
            if (nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikInneværendePeriode) {
                val delvedtakListe = buildList {
                    add(
                        ResultatVedtak(
                            resultat = lagOpphørsvedtak(
                                klageperiode = klageperiode,
                                påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                            ),
                            delvedtak = true,
                            klagevedtak = false,
                        ),
                    )
                    add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true))
                }

                val sammenslåttVedtak = ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false)

                return (delvedtakListe + sammenslåttVedtak).sorterListe()
            }

            return emptyList()
        } catch (e: Exception) {
            // TODO
            throw e
        }
    }

    // Lager BeregnetBarnebidragResultat (simulert resultat fra beregningen) for alle (eksisterende) vedtak i vedtakListe
    private fun lagBeregnetBarnebidragResultatFraEksisterendeVedtak(
        vedtakListe: List<Int>,
        stønadstype: Stønadstype,
    ): List<BeregnetBarnebidragResultat> {
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultat>()
        vedtakListe.forEach {
            val komplettVedtak = vedtakService.hentVedtak(it)
            if (komplettVedtak != null) {
                val referanse = "resultatFraVedtak_${komplettVedtak.vedtaksid}"
                beregnetBarnebidragResultatListe.add(
                    BeregnetBarnebidragResultat(
                        beregnetBarnebidragPeriodeListe = komplettVedtak.hentBeregningsperioder(stønadstype, referanse),
                        grunnlagListe = listOf(
                            GrunnlagDto(
                                referanse = referanse,
                                type = Grunnlagstype.RESULTAT_FRA_VEDTAK,
                                innhold = POJONode(
                                    ResultatFraVedtakGrunnlag(
                                        vedtaksid = komplettVedtak.vedtaksid,
                                    ),
                                ),
                            ),
                        ),
                    ),
                )
            }
        }
        return beregnetBarnebidragResultatListe
    }

    // Extension fuction for å hente beregningsperioder for et vedtak med en gitt stønadstype
    private fun VedtakDto.hentBeregningsperioder(stønadstype: Stønadstype, referanse: String? = null): List<ResultatPeriode> = stønadsendringListe
        .first { it.type == stønadstype }
        .periodeListe
        .map {
            ResultatPeriode(
                periode = it.periode,
                resultat = ResultatBeregning(it.beløp),
                grunnlagsreferanseListe = if (referanse != null) listOf(referanse) else it.grunnlagReferanseListe,
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

    // Lager opphørsvedtak, dvs. et vedtak med null i beløp og ingen tilknyttede grunnlag
    private fun lagOpphørsvedtak(klageperiode: ÅrMånedsperiode, påklagetVedtakVirkningstidspunkt: LocalDate): BeregnetBarnebidragResultat =
        BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = listOf(
                ResultatPeriode(
                    periode = ÅrMånedsperiode(
                        fom = YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
                        til = klageperiode.fom,
                    ),
                    resultat = ResultatBeregning(beløp = null),
                    grunnlagsreferanseListe = emptyList(),
                ),
            ),
            grunnlagListe = emptyList(),
        )

    // Extension function for å sortere på delvedtak (delvedtak = false kommer før delvedtak = true) og deretter periode.fom
    private fun List<ResultatVedtak>.sorterListe(): List<ResultatVedtak> = this.sortedWith(
        compareBy<ResultatVedtak> { !it.delvedtak }
            .thenBy {
                it.resultat.beregnetBarnebidragPeriodeListe.minOfOrNull { periode -> periode.periode.fom }
            },
    )
}
