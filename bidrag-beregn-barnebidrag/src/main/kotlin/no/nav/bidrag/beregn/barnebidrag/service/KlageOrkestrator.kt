package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.PrivatAvtaleIndeksregulertPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.delberegningEndringSjekkGrensePeriode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.erOverMinimumsgrenseForEndring
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrenseService.delberegningEndringSjekkGrense
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.ResultatFraVedtakGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.virkningstidspunkt
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Service
@Import(VedtakService::class)
class KlageOrkestrator(private val vedtakService: VedtakService) {

    fun utførKlageEndelig(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageberegningGrunnlag: BeregnGrunnlag,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
    ): List<ResultatVedtak> {
        try {
            val stønad = klageOrkestratorGrunnlag.stønad
            val påklagetVedtakId = klageOrkestratorGrunnlag.påklagetVedtakId
            val klageperiode = klageberegningGrunnlag.periode

            secureLogger.info { "Komplett klageberegning kjøres for stønad $stønad og påklaget vedtak $påklagetVedtakId" }

            val påklagetVedtak = vedtakService.hentVedtak(påklagetVedtakId)
                ?: klageFeilet("Fant ikke påklaget vedtak med id $påklagetVedtakId")
            val påklagetVedtakVirkningstidspunkt = påklagetVedtak.virkningstidspunkt
                ?: klageFeilet("Påklaget vedtak med id $påklagetVedtakId har ikke virkningstidspunkt")
            val påklagetVedtakVedtakstidspunkt = påklagetVedtak.vedtakstidspunkt
                ?: klageFeilet("Påklaget vedtak med id $påklagetVedtakId har ikke vedtakstidspunkt")
            val løpendeStønadGjeldende = vedtakService.hentLøpendeStønad(stønad)
                ?: klageFeilet("Fant ikke løpende stønad for $stønad")
            val grunnlagsliste = klageberegningGrunnlag.grunnlagListe

            val personobjekter =
                listOf(
                    grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi) ?: klageFeilet("Fant ikke søknadsbarn/kravhaver i grunnlaget"),
                    klageberegningGrunnlag.grunnlagListe.bidragsmottaker ?: klageFeilet("Fant ikke bidragsmottaker i grunnlaget"),
                    klageberegningGrunnlag.grunnlagListe.bidragspliktig ?: klageFeilet("Fant ikke bidragspliktig i grunnlaget"),
                ) as List<GrunnlagDto>

            val beløpshistorikkFørPåklagetVedtak =
                vedtakService.hentBeløpshistorikkTilGrunnlag(
                    stønadsid = stønad,
                    personer = personobjekter,
                    tidspunkt = påklagetVedtakVedtakstidspunkt.minusSeconds(1),
                )

            // Sjekk klageberegningen mot minimumsgrense for endring (aka 12%-regel)
            val klageberegningResultatEtterSjekkMinGrenseForEndring = sjekkMotMinimumsgrenseForEndring(
                klageberegningResultat = klageberegningResultat,
                klageperiode = klageperiode,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                stønadstype = stønad.type,
                opphørsdato = klageberegningGrunnlag.opphørsdato,
                søknadsbarnReferanse = klageberegningGrunnlag.søknadsbarnReferanse,
            )

            // TODO Sjekk om nytt virkningstidspunkt kan være tidligere enn originalt virkningstidspunkt
            val nyVirkningErEtterOpprinneligVirkning = klageperiode.fom.isAfter(
                YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
            )

            val klageperiodeTilErLikOpprinneligVedtakstidspunkt = klageperiode.til!!.minusMonths(1) ==
                YearMonth.of(påklagetVedtakVedtakstidspunkt.year, påklagetVedtakVedtakstidspunkt.monthValue)

            val klageperiodeTilErLikInneværendePeriode = klageperiode.til!!.minusMonths(1) == YearMonth.now()

            val foreløpigVedtak = when {
                // Scenario 1: Klagevedtak dekker perioden fra opprinnelig virkningstidspunkt til inneværende periode - skal overstyre alt
                !nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikInneværendePeriode ->
                    klageScenario1(klageberegningResultat = klageberegningResultatEtterSjekkMinGrenseForEndring)

                // Scenario 2: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og
                // kjør evt ny indeksregulering/aldersjustering
                !nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikOpprinneligVedtakstidspunkt ->
                    klageScenario2(
                        klageberegningResultat = klageberegningResultatEtterSjekkMinGrenseForEndring,
                        klageperiode = klageperiode,
                        løpendeStønad = løpendeStønadGjeldende,
                        påklagetVedtakId = påklagetVedtakId,
                        stønadstype = stønad.type,
                    )

                // Scenario 3: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
                // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles
                // ut.
                nyVirkningErEtterOpprinneligVirkning && klageperiodeTilErLikInneværendePeriode ->
                    klageScenario3(
                        klageberegningResultat = klageberegningResultatEtterSjekkMinGrenseForEndring,
                        klageperiode = klageperiode,
                        påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                    )

                else -> emptyList()
            }

            return foreløpigVedtak
        } catch (e: Exception) {
            // TODO
            throw e
        }
    }

    // Scenario 1: Klagevedtak dekker perioden fra opprinnelig virkningstidspunkt til inneværende periode - skal overstyre alt
    private fun klageScenario1(klageberegningResultat: BeregnetBarnebidragResultat): List<ResultatVedtak> = listOf(
        ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true),
        ResultatVedtak(resultat = klageberegningResultat, delvedtak = false, klagevedtak = false),
    ).sortedByDescending { it.delvedtak }

    // Scenario 2: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og kjør
    // evt ny indeksregulering/aldersjustering
    private fun klageScenario2(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        løpendeStønad: StønadDto?,
        påklagetVedtakId: Int,
        stønadstype: Stønadstype,
    ): List<ResultatVedtak> {
        val etterfølgendeVedtakListe: List<Int> =
            if (løpendeStønad == null || løpendeStønad.periodeListe.isEmpty()) {
                emptyList()
            } else {
                løpendeStønad.periodeListe
                    .filter { it.vedtaksid != påklagetVedtakId && (it.periode.til == null || it.periode.til!!.isAfter(klageperiode.til!!)) }
                    .map { it.vedtaksid }
                    .distinct()
            }

        val delvedtakListe = buildList {
            add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true))
            addAll(
                lagBeregnetBarnebidragResultatFraEksisterendeVedtak(vedtakListe = etterfølgendeVedtakListe, stønadstype = stønadstype)
                    .map { ResultatVedtak(resultat = it, delvedtak = true, klagevedtak = false) },
            )
        }

        val sammenslåttVedtak = ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false)

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
    }

    // Scenario 3: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
    // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles ut.
    private fun klageScenario3(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        påklagetVedtakVirkningstidspunkt: LocalDate,
    ): List<ResultatVedtak> {
        val delvedtakListe = buildList {
            add(
                ResultatVedtak(
                    resultat = lagOpphørsvedtak(klageperiode = klageperiode, påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt),
                    delvedtak = true,
                    klagevedtak = false,
                ),
            )
            add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true))
        }

        val sammenslåttVedtak = ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false)

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
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

    // Sjekker klageberegning mot minimumsgrense for endring (aka 12%-regelen)
    private fun sjekkMotMinimumsgrenseForEndring(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        beløpshistorikkFørPåklagetVedtak: GrunnlagDto,
        stønadstype: Stønadstype,
        opphørsdato: YearMonth?,
        søknadsbarnReferanse: String,
    ): BeregnetBarnebidragResultat {
        val er18ÅrsBidrag = stønadstype == Stønadstype.BIDRAG18AAR
//
//        // Filtrerer ut beløpshistorikk. Hvis det er 18-års-bidrag benyttes egen beløpshistorikk.
//        val beløpshistorikkGrunnlag = if (er18ÅrsBidrag) {
//            filtrerBeløpshistorikk18ÅrGrunnlag(mottattGrunnlag)
//        } else {
//            filtrerBeløpshistorikkGrunnlag(mottattGrunnlag)
//        }

        // Kaller delberegning for indeksregulering av privat avtale
        // TODO
//        val delberegningIndeksreguleringPrivatAvtalePeriodeResultat = utførDelberegningPrivatAvtalePeriode(mottattGrunnlag)

        // Kaller delberegning for å sjekke om endring i bidrag er over grense (pr periode)
        val delberegningEndringSjekkGrensePeriodeResultat =
            delberegningEndringSjekkGrensePeriode(
                mottattGrunnlag = BeregnGrunnlag(
                    periode = klageperiode,
                    opphørsdato = opphørsdato,
                    stønadstype = stønadstype,
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    grunnlagListe = klageberegningResultat.grunnlagListe + beløpshistorikkFørPåklagetVedtak,
                ),
                åpenSluttperiode = true,
            )

        // Kaller delberegning for å sjekke om endring i bidrag er over grense (totalt)
        val delberegningEndringSjekkGrenseResultat = delberegningEndringSjekkGrense(
            mottattGrunnlag = BeregnGrunnlag(
                periode = klageperiode,
                opphørsdato = opphørsdato,
                stønadstype = stønadstype,
                søknadsbarnReferanse = søknadsbarnReferanse,
                grunnlagListe = klageberegningResultat.grunnlagListe + delberegningEndringSjekkGrensePeriodeResultat,
            ),
            åpenSluttperiode = true,
        )

        val beregnetBidragErOverMinimumsgrenseForEndring = erOverMinimumsgrenseForEndring(delberegningEndringSjekkGrenseResultat)
        val grunnlagstype = if (er18ÅrsBidrag) Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR else Grunnlagstype.BELØPSHISTORIKK_BIDRAG

        val resultatPeriodeListe = lagResultatPerioder(
            delberegningEndeligBidragPeriodeResultat = klageberegningResultat.grunnlagListe,
            beregnetBidragErOverMinimumsgrenseForEndring = beregnetBidragErOverMinimumsgrenseForEndring,
            beløpshistorikkGrunnlag = listOf(beløpshistorikkFørPåklagetVedtak),
            beløpshistorikkGrunnlagstype = grunnlagstype,
            delberegningEndringSjekkGrensePeriodeResultat = delberegningEndringSjekkGrensePeriodeResultat,
            delberegningIndeksreguleringPrivatAvtalePeriodeResultat = emptyList(),
        )

        return BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = resultatPeriodeListe,
            grunnlagListe = (
                klageberegningResultat.grunnlagListe +
                    delberegningEndringSjekkGrenseResultat +
                    delberegningEndringSjekkGrensePeriodeResultat
                ).distinctBy { it.referanse }.sortedBy { it.referanse },
        )
    }

    // Lager resultatperioder basert på beløpshistorikk hvis beregnet bidrag ikke er over minimumsgrense for endring
    // TODO Kopiert fra BeregnBarnebidragService. Bør flyttes til et felles sted. BeregnFelles?
    fun lagResultatPerioder(
        delberegningEndeligBidragPeriodeResultat: List<GrunnlagDto>,
        beregnetBidragErOverMinimumsgrenseForEndring: Boolean,
        beløpshistorikkGrunnlag: List<GrunnlagDto>,
        beløpshistorikkGrunnlagstype: Grunnlagstype,
        delberegningEndringSjekkGrensePeriodeResultat: List<GrunnlagDto>,
        delberegningIndeksreguleringPrivatAvtalePeriodeResultat: List<GrunnlagDto>,
    ): List<ResultatPeriode> {
        // Henter beløpshistorikk (det finnes kun en forekomst, som dekker hele perioden)
        val beløpshistorikkPeriodeGrunnlag = beløpshistorikkGrunnlag
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(beløpshistorikkGrunnlagstype)
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()

        // Henter resultat av sluttberegning
        val sluttberegningPeriodeGrunnlagListe = delberegningEndeligBidragPeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningPeriodeGrunnlag(
                    referanse = it.referanse,
                    sluttberegningPeriode = it.innhold,
                )
            }

        // Henter resultat av delberegning endring-sjekk-grense-periode
        val delberegningEndringSjekkGrensePeriodeGrunnlagListe = delberegningEndringSjekkGrensePeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag(
                    referanse = it.referanse,
                    endringSjekkGrensePeriodePeriode = it.innhold,
                    referanseListe = it.grunnlag.grunnlagsreferanseListe,
                )
            }

        // Henter resultat av delberegning privat-avtale-periode
        val delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe = delberegningIndeksreguleringPrivatAvtalePeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtale>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE)
            .firstOrNull()?.let { dpa ->
                dpa.innhold.perioder.map {
                    PrivatAvtaleIndeksregulertPeriodeGrunnlag(
                        referanse = dpa.referanse,
                        privatAvtaleIndeksregulertPeriode = it,
                    )
                }
            } ?: emptyList()

        // Bruker grunnlagslisten fra delberegning endring-sjekk-grense-periode som utgangspunkt for å lage resultatperioder
        return delberegningEndringSjekkGrensePeriodeGrunnlagListe
            .map {
                ResultatPeriode(
                    periode = it.endringSjekkGrensePeriodePeriode.periode,
                    resultat = ResultatBeregning(
                        beløp = hentEndeligBeløp(
                            beregnetBidragErOverMinimumsgrenseForEndring = beregnetBidragErOverMinimumsgrenseForEndring,
                            referanseListe = it.referanseListe,
                            periode = it.endringSjekkGrensePeriodePeriode.periode,
                            sluttberegningPeriodeGrunnlagListe = sluttberegningPeriodeGrunnlagListe,
                            beløpshistorikkPeriodeGrunnlag = beløpshistorikkPeriodeGrunnlag,
                            delberegningIndeksregPrivatAvtalePeriodeGrunnlagListe = delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe,
                        ),
                    ),
                    grunnlagsreferanseListe = lagReferanseliste(
                        referanseListe = it.referanseListe,
                        periode = it.endringSjekkGrensePeriodePeriode.periode,
                        beløpshistorikkPeriodeGrunnlag = beløpshistorikkPeriodeGrunnlag,
                        delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe = delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe,
                    ),
                )
            }
    }

    // Hvis beregnet bidrag er over minimumsgrense for endring skal beløp hentes fra sluttberegning (matcher på referanse); hvis ikke skal beløp
    // hentes fra privat avtale (matcher på referanse) eller beløpshistorikk (matcher på periode)
    // TODO Kopiert fra BeregnBarnebidragService. Bør flyttes til et felles sted. BeregnFelles?
    private fun hentEndeligBeløp(
        beregnetBidragErOverMinimumsgrenseForEndring: Boolean,
        referanseListe: List<String>,
        periode: ÅrMånedsperiode,
        sluttberegningPeriodeGrunnlagListe: List<SluttberegningPeriodeGrunnlag>,
        beløpshistorikkPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
        delberegningIndeksregPrivatAvtalePeriodeGrunnlagListe: List<PrivatAvtaleIndeksregulertPeriodeGrunnlag>,
    ): BigDecimal? {
        val privatAvtaleBeløp = delberegningIndeksregPrivatAvtalePeriodeGrunnlagListe
            .filter {
                (periode.til == null || it.privatAvtaleIndeksregulertPeriode.periode.fom < periode.til) &&
                    (it.privatAvtaleIndeksregulertPeriode.periode.til == null || it.privatAvtaleIndeksregulertPeriode.periode.til!! > periode.fom)
            }
            .map { it.privatAvtaleIndeksregulertPeriode.beløp }
            .firstOrNull()
        val beløpshistorikkBeløp = beløpshistorikkPeriodeGrunnlag?.beløpshistorikkPeriode?.beløpshistorikk
            ?.filter { (periode.til == null || it.periode.fom < periode.til) && (it.periode.til == null || it.periode.til!! > periode.fom) }
            ?.map { it.beløp }
            ?.firstOrNull()
        return if (beregnetBidragErOverMinimumsgrenseForEndring) {
            sluttberegningPeriodeGrunnlagListe
                .filter { it.referanse in referanseListe }
                .map { it.sluttberegningPeriode.resultatBeløp }
                .firstOrNull()
        } else {
            beløpshistorikkBeløp ?: privatAvtaleBeløp
        }
    }

    // Fjerner referanser som skal legges ut i resultatperioden i enkelte tilfeller (privat avtale og sjablon)
    // TODO Kopiert fra BeregnBarnebidragService. Bør flyttes til et felles sted. BeregnFelles?
    private fun lagReferanseliste(
        referanseListe: List<String>,
        periode: ÅrMånedsperiode,
        beløpshistorikkPeriodeGrunnlag: BeløpshistorikkPeriodeGrunnlag?,
        delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe: List<PrivatAvtaleIndeksregulertPeriodeGrunnlag>,
    ): List<String> {
        val privatAvtaleReferanse = delberegningIndeksreguleringPrivatAvtalePeriodeGrunnlagListe
            .filter { it.referanse in referanseListe }
            .map { it.referanse }
            .firstOrNull() ?: ""
        val sjablonReferanse = referanseListe
            .firstOrNull { it.startsWith("sjablon") || it.startsWith("Sjablon") || it.startsWith("SJABLON") } ?: ""
        val beløpshistorikkReferanse = beløpshistorikkPeriodeGrunnlag?.beløpshistorikkPeriode?.beløpshistorikk
            ?.filter { (periode.til == null || it.periode.fom < periode.til) && (it.periode.til == null || it.periode.til!! > periode.fom) }
            ?.map { beløpshistorikkPeriodeGrunnlag.referanse }
            ?.firstOrNull() ?: ""
        // beløpshistorikkReferanse trumfer privatAvtaleReferanse hvis begge finnes
        return if (privatAvtaleReferanse.isNotEmpty() && beløpshistorikkReferanse.isNotEmpty()) {
            referanseListe.minus(privatAvtaleReferanse).minus(sjablonReferanse)
        } else {
            referanseListe.minus(sjablonReferanse)
        }
    }

    private fun klageFeilet(begrunnelse: String): Nothing = throw RuntimeException(begrunnelse)
}
