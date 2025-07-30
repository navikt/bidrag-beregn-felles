package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.PrivatAvtaleIndeksregulertPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.delberegningEndringSjekkGrensePeriode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.erOverMinimumsgrenseForEndring
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrenseService.delberegningEndringSjekkGrense
import no.nav.bidrag.beregn.barnebidrag.service.BeregnIndeksreguleringPrivatAvtaleService.delberegningPrivatAvtalePeriode
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils.opprettAldersjusteringDetaljerGrunnlag
import no.nav.bidrag.beregn.barnebidrag.utils.beregnetFraDato
import no.nav.bidrag.beregn.barnebidrag.utils.hentPersonForNyesteIdent
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.barnebidrag.utils.tilDto
import no.nav.bidrag.beregn.barnebidrag.utils.tilGrunnlag
import no.nav.bidrag.beregn.barnebidrag.utils.toYearMonth
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.AldersjusteringDetaljerGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.PrivatAvtaleGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.ResultatFraVedtakGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.søknadsbarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.tilGrunnlagstype
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.erResultatEndringUnderGrense
import no.nav.bidrag.transport.behandling.vedtak.response.finnStønadsendring
import no.nav.bidrag.transport.behandling.vedtak.response.virkningstidspunkt
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

fun VedtakDto.erAldersjustering() = type == Vedtakstype.ALDERSJUSTERING
fun VedtakDto.erIndeksregulering() = type == Vedtakstype.INDEKSREGULERING
internal data class BeregnetBarnebidragResultatIntern(
    val resultat: BeregnetBarnebidragResultat,
    val vedtakstype: Vedtakstype,
    val beregnetFraDato: LocalDate,
    val klagevedtak: Boolean = false,
)

internal data class ByggetBeløpshistorikk(
    val nesteIndeksår: Int,
    val grunnlagsliste: List<GrunnlagDto>,
    val stønadDto: StønadDto,
    val grunnlagBeløpshistorikk: GrunnlagDto,
)

@Service
@Import(VedtakService::class, AldersjusteringOrchestrator::class, BeregnIndeksreguleringApi::class, IdentUtils::class)
class KlageOrkestrator(
    private val vedtakService: VedtakService,
    private val aldersjusteringOrchestrator: AldersjusteringOrchestrator,
    private val beregnIndeksreguleringApi: BeregnIndeksreguleringApi,
    private val identUtils: IdentUtils,
) {

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
                påklagetVedtak.finnBeløpshistorikkGrunnlag(stønad)
                    ?: vedtakService.hentBeløpshistorikkTilGrunnlag(
                        stønadsid = stønad,
                        personer = personobjekter,
                        tidspunkt = påklagetVedtakVedtakstidspunkt.minusSeconds(1),
                    ).innholdTilObjekt<BeløpshistorikkGrunnlag>()

            // TODO Sjekk om nytt virkningstidspunkt kan være tidligere enn originalt virkningstidspunkt
            val nyVirkningErEtterOpprinneligVirkning = klageperiode.fom.isAfter(påklagetVedtakVirkningstidspunkt.toYearMonth())

            val foreløpigVedtak = byggVedtak(
                nyVirkningErEtterOpprinneligVirkning,
                klageberegningResultat,
                klageperiode,
                klageberegningGrunnlag.opphørsdato,
                løpendeStønadGjeldende,
                påklagetVedtakVirkningstidspunkt,
                klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak,
            )

            val (_, _, _, beløpshistorikkFørPåklagetVedtak2) = foreløpigVedtak.map {
                it.resultat
            }.byggBeløpshistorikk(stønad, klageperiode.fom, beløpshistorikkFørPåklagetVedtak)

            // Sjekk klageberegningen mot minimumsgrense for endring (aka 12%-regel)
            val klageberegningResultatEtterSjekkMinGrenseForEndring = sjekkMotMinimumsgrenseForEndring(
                klageberegningResultat = klageberegningResultat,
                klageperiode = klageperiode,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak2,
                stønadstype = stønad.type,
                klageberegningGrunnlag = klageberegningGrunnlag,
            )

            return byggVedtak(
                nyVirkningErEtterOpprinneligVirkning,
                klageberegningResultatEtterSjekkMinGrenseForEndring,
                klageperiode,
                klageberegningGrunnlag.opphørsdato,
                løpendeStønadGjeldende,
                påklagetVedtakVirkningstidspunkt,
                klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak,
            )
        } catch (e: Exception) {
            // TODO
            throw e
        }
    }

    private fun VedtakDto.finnBeløpshistorikkGrunnlag(stønad: Stønadsid): BeløpshistorikkGrunnlag? {
        val grunnlagstype = when (stønad.type) {
            Stønadstype.BIDRAG18AAR -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
            Stønadstype.FORSKUDD -> Grunnlagstype.BELØPSHISTORIKK_FORSKUDD
            else -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG
        }
        val søknadsbarn = grunnlagListe.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)!!
        return grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(grunnlagType = grunnlagstype)
            .firstOrNull { it.gjelderBarnReferanse == søknadsbarn.referanse }?.innhold
    }

    private fun byggVedtak(
        nyVirkningErEtterOpprinneligVirkning: Boolean,
        klageberegningResultatEtterSjekkMinGrenseForEndring: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        opphørsdato: YearMonth?,
        løpendeStønadGjeldende: StønadDto,
        påklagetVedtakVirkningstidspunkt: LocalDate,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): List<ResultatVedtak> = when {
        // Scenario 1: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og
        // kjør evt ny indeksregulering/aldersjustering
        !nyVirkningErEtterOpprinneligVirkning ->
            klageScenarioVirkningFørEllerLikOpprinneligVirkning(
                klageberegningResultat = klageberegningResultatEtterSjekkMinGrenseForEndring,
                klageperiode = klageperiode,
                opphørsdato = opphørsdato,
                løpendeStønad = løpendeStønadGjeldende,
                påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
            )

        // Scenario 2: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
        // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles
        // ut.
        nyVirkningErEtterOpprinneligVirkning ->
            klageScenarioVirkningEtterOpprinneligVirkning(
                klageberegningResultat = klageberegningResultatEtterSjekkMinGrenseForEndring,
                klageperiode = klageperiode,
                opphørsdato = opphørsdato,
                løpendeStønad = løpendeStønadGjeldende,
                påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,

            )

        else -> emptyList()
    }

    // Scenario 2: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og kjør
    // evt ny indeksregulering/aldersjustering
    private fun klageScenarioVirkningFørEllerLikOpprinneligVirkning(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        løpendeStønad: StønadDto?,
        påklagetVedtakVirkningstidspunkt: LocalDate,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        opphørsdato: YearMonth?,
    ): List<ResultatVedtak> {
        val (_, påklagetVedtakId) = klageOrkestratorGrunnlag
        val etterfølgendeVedtakListe: List<Int> = løpendeStønad.hentEtterfølgendeVedtaksliste(klageperiode, påklagetVedtakId, opphørsdato)
        val periodeSomSkalOpphøres =
            finnPeriodeSomSkalOpphøres(klageperiode, påklagetVedtakVirkningstidspunkt, beløpshistorikkFørPåklagetVedtak)

        val delvedtakListe = buildList {
            periodeSomSkalOpphøres?.let {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(it),
                        delvedtak = true,
                        klagevedtak = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true, vedtakstype = Vedtakstype.KLAGE))
            addAll(
                lagBeregnetBarnebidragResultatFraEksisterendeVedtak(
                    vedtakListe = etterfølgendeVedtakListe,
                    klageResultat = klageberegningResultat,
                    delvedtak = this,
                    klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                    beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                    påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,

                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            vedtakstype = it.vedtakstype,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }

        val sammenslåttVedtak =
            ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false, vedtakstype = Vedtakstype.KLAGE)

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
    }

    // Scenario 3: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
    // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles ut.
    private fun klageScenarioVirkningEtterOpprinneligVirkning(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        løpendeStønad: StønadDto?,
        påklagetVedtakVirkningstidspunkt: LocalDate,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        opphørsdato: YearMonth?,
    ): List<ResultatVedtak> {
        val (_, påklagetVedtakId) = klageOrkestratorGrunnlag
        val vedtakIderMellomPåklagetVirkningOgNyVirkning =
            finnVedtakIderMellomPåklagetVirkningOgNyVirkning(
                klageperiode,
                påklagetVedtakVirkningstidspunkt,
                påklagetVedtakId,
                løpendeStønad,
                beløpshistorikkFørPåklagetVedtak,
            )
        val periodeSomSkalOpphøres = finnPeriodeSomSkalOpphøres(
            klageperiode,
            påklagetVedtakVirkningstidspunkt,
            beløpshistorikkFørPåklagetVedtak,
        )

        val delvedtakListeFør = buildList {
            if (periodeSomSkalOpphøres != null) {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(periodeSomSkalOpphøres),
                        delvedtak = true,
                        klagevedtak = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            addAll(
                lagBeregnetBarnebidragResultatFraEksisterendeVedtak(
                    vedtakListe = vedtakIderMellomPåklagetVirkningOgNyVirkning,
                    klageResultat = klageberegningResultat,
                    delvedtak = this,
                    klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                    beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                    påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            vedtakstype = it.vedtakstype,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }

        val etterfølgendeVedtakListe: List<Int> = løpendeStønad.hentEtterfølgendeVedtaksliste(klageperiode, påklagetVedtakId, opphørsdato)
        val delvedtakListe = buildList {
            addAll(delvedtakListeFør)
            add(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true, vedtakstype = Vedtakstype.KLAGE))
            addAll(
                lagBeregnetBarnebidragResultatFraEksisterendeVedtak(
                    vedtakListe = etterfølgendeVedtakListe,
                    klageResultat = klageberegningResultat,
                    delvedtak = this,
                    klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                    beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                    påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            vedtakstype = it.vedtakstype,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }
        val sammenslåttVedtak =
            ResultatVedtak(resultat = slåSammenVedtak(delvedtakListe), delvedtak = false, klagevedtak = false, vedtakstype = Vedtakstype.KLAGE)

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
    }

    private fun finnVedtakIderMellomPåklagetVirkningOgNyVirkning(
        klageperiode: ÅrMånedsperiode,
        påklagetVedtakVirkningstidspunkt: LocalDate,
        påklagetVedtakId: Int,
        løpendeStønad: StønadDto?,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): List<Int> = if (klageperiode.fom > påklagetVedtakVirkningstidspunkt.toYearMonth()) {
        val vedtakMellom = beløpshistorikkFørPåklagetVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(klageperiode.fom) &&
                it.periode.fom.isAfter(påklagetVedtakVirkningstidspunkt.toYearMonth())
        }.mapNotNull { it.vedtaksid }.distinct()

        vedtakMellom.ifEmpty {
            beløpshistorikkFørPåklagetVedtak.beløpshistorikk
                .filter { it.periode.fom.isBefore(klageperiode.fom) }
                .maxByOrNull { it.periode.fom }
                ?.vedtaksid?.let { listOf(it) } ?: emptyList()
        }
    } else if (løpendeStønad == null || løpendeStønad.periodeListe.isEmpty()) {
        emptyList()
    } else {
        løpendeStønad.periodeListe
            .filter {
                it.vedtaksid != påklagetVedtakId &&
                    it.periode.fom >= påklagetVedtakVirkningstidspunkt.toYearMonth()
                it.periode.til != null && it.periode.til!!.isBefore(klageperiode.til!!)
            }
            .map { it.vedtaksid }
            .distinct()
    }

    private fun finnPeriodeSomSkalOpphøres(
        klageperiode: ÅrMånedsperiode,
        påklagetVedtakVirkningstidspunkt: LocalDate,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): ÅrMånedsperiode? {
        val vedtakMellomPåklagetVirkningOgNyVirkning = beløpshistorikkFørPåklagetVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(klageperiode.fom)
        }

        val tidligstePeriodeFom = vedtakMellomPåklagetVirkningOgNyVirkning.minOfOrNull { it.periode.fom }

        return if (tidligstePeriodeFom == null && påklagetVedtakVirkningstidspunkt.toYearMonth() < klageperiode.fom) {
            ÅrMånedsperiode(
                fom = YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
                til = klageperiode.fom,
            )
        } else if (tidligstePeriodeFom != null && tidligstePeriodeFom > påklagetVedtakVirkningstidspunkt.toYearMonth()) {
            ÅrMånedsperiode(
                fom = YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
                til = tidligstePeriodeFom,
            )
        } else {
            null
        }
    }

    // Lager BeregnetBarnebidragResultat (simulert resultat fra beregningen) for alle (eksisterende) vedtak i vedtakListe
    private fun lagBeregnetBarnebidragResultatFraEksisterendeVedtak(
        vedtakListe: List<Int>,
        klageResultat: BeregnetBarnebidragResultat,
        delvedtak: List<ResultatVedtak>,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        påklagetVedtakVirkningstidspunkt: LocalDate,
    ): List<BeregnetBarnebidragResultatIntern> {
        val (stønad) = klageOrkestratorGrunnlag
        val historikk = delvedtak.map {
            BeregnetBarnebidragResultatIntern(it.resultat, it.vedtakstype, it.beregnetFraDato, it.klagevedtak)
        }.toMutableList()
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultatIntern>()
        vedtakListe.forEachIndexed { index, it ->
            val komplettVedtak = vedtakService.hentVedtak(it)
            if (komplettVedtak != null) {
                val forrigeVedtakErKlagevedtak = historikk.maxByOrNull { it.beregnetFraDato }?.klagevedtak == true
                val resultat = when {
                    komplettVedtak.erAldersjustering() && forrigeVedtakErKlagevedtak && historikk.isNotEmpty() -> {
                        utførAldersjustering(
                            klageResultat.tilVedtakDto(stønad),
                            komplettVedtak.vedtakstidspunkt!!.year,
                            historikk,
                            klageOrkestratorGrunnlag,
                            beløpshistorikkFørPåklagetVedtak,
                        )
                    }
                    komplettVedtak.erIndeksregulering() && historikk.isNotEmpty() ->
                        utførIndeksregulering(stønad, historikk, beløpshistorikkFørPåklagetVedtak)
                    else -> {
                        utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                            komplettVedtak,
                            historikk,
                            stønad,
                            klageOrkestratorGrunnlag,
                            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                        )?.let {
                            beregnetBarnebidragResultatListe.add(it)
                            historikk.add(it)
                        }
                        val referanse = "resultatFraVedtak_${komplettVedtak.vedtaksid}"
                        val perioder = komplettVedtak.hentBeregningsperioder(stønad.type, referanse, klageResultat, påklagetVedtakVirkningstidspunkt)
                        BeregnetBarnebidragResultatIntern(
                            resultat = BeregnetBarnebidragResultat(
                                beregnetBarnebidragPeriodeListe = perioder,
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
                            vedtakstype = komplettVedtak.type,
                            beregnetFraDato = perioder.minOf { it.periode.fom }.atDay(1),
                        )
                    }
                }
                beregnetBarnebidragResultatListe.add(resultat)
                historikk.add(resultat)
            }
            utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                klageResultat.tilVedtakDto(stønad),
                historikk,
                stønad,
                klageOrkestratorGrunnlag,
                true,
                beløpshistorikkFørPåklagetVedtak,
            )?.let {
                beregnetBarnebidragResultatListe.add(it)
                historikk.add(it)
            }
        }
        return beregnetBarnebidragResultatListe
    }

    private fun utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
        vedtak: VedtakDto,
        historikk: MutableList<BeregnetBarnebidragResultatIntern>,
        stønad: Stønadsid,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        erKlagevedtak: Boolean = false,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): BeregnetBarnebidragResultatIntern? {
        val (_, _, stønadDto) = historikk.map { it.resultat }.byggBeløpshistorikk(
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
        val løpendePeriode = stønadDto.periodeListe.hentSisteLøpendePeriode() ?: return null
        val vedtakSistePeriode = vedtak.finnStønadsendring(stønad)!!.periodeListe.maxByOrNull { it.periode.fom }
        val vedtakFørstePeriode = vedtak.finnStønadsendring(stønad)!!.periodeListe.minByOrNull { it.periode.fom } ?: return null
        val vedtakSistePeriodeTil = vedtakSistePeriode?.periode?.til
        val datoAldersjusteringEllerIndeksregulering = if (vedtakFørstePeriode.periode.fom.year > løpendePeriode.periode.fom.year) {
            YearMonth.of(løpendePeriode.periode.fom.year + 1, 7)
        } else {
            YearMonth.of(løpendePeriode.periode.fom.year, 7)
        }

        val erKlageVedtakLøpende =
            vedtakSistePeriode?.beløp != null && vedtakFørstePeriode.periode.fom.isBefore(datoAldersjusteringEllerIndeksregulering) &&
                vedtakSistePeriodeTil == null &&
                erKlagevedtak
        val skalAldersjusteresEllerIndeksreguleres =
            (vedtakFørstePeriode.periode.fom.isAfter(datoAldersjusteringEllerIndeksregulering) || erKlageVedtakLøpende) &&
                løpendePeriode.periode.fom.isBefore(datoAldersjusteringEllerIndeksregulering)
        if (skalAldersjusteresEllerIndeksreguleres) {
            val resultatAldersjustering =
                utførAldersjustering(
                    vedtak,
                    datoAldersjusteringEllerIndeksregulering.year,
                    historikk,
                    klageOrkestratorGrunnlag,
                    beløpshistorikkFørPåklagetVedtak,
                )
            val detaljer = resultatAldersjustering.resultat.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<AldersjusteringDetaljerGrunnlag>(
                Grunnlagstype.ALDERSJUSTERING_DETALJER,
            ).first()
            return if (!detaljer.innhold.aldersjustert && !detaljer.innhold.aldersjusteresManuelt) {
                null // utførIndeksregulering(stønad, historikk)
            } else {
                resultatAldersjustering
            }
        }
        return null
    }

    // Extension fuction for å hente beregningsperioder for et vedtak med en gitt stønadstype
    private fun VedtakDto.hentBeregningsperioder(
        stønadstype: Stønadstype,
        referanse: String? = null,
        klageResultat: BeregnetBarnebidragResultat,
        påklagetVedtakVirkningstidspunkt: LocalDate,
    ): List<ResultatPeriode> {
        val førstePeriode = stønadsendringListe.first { it.type == stønadstype }.periodeListe.minOf { it.periode.fom }
        val vedtakKommerFørKlagevedtak = førstePeriode.isBefore(klageResultat.beregnetFraDato.toYearMonth())
        return stønadsendringListe
            .first { it.type == stønadstype }
            .periodeListe
            .filter {
                !vedtakKommerFørKlagevedtak ||
                    it.periode.fom <= klageResultat.beregnetFraDato.toYearMonth() &&
                    it.periode.fom >= påklagetVedtakVirkningstidspunkt.toYearMonth()
            }
            .map {
                ResultatPeriode(
                    periode = it.periode.copy(
                        til = when {
                            // Juster slik at periode til ikke er etter klageperiode
                            vedtakKommerFørKlagevedtak &&
                                it.periode.til != null &&
                                it.periode.til!!.isAfter(klageResultat.beregnetFraDato.toYearMonth())
                            -> klageResultat.beregnetFraDato.toYearMonth()
                            else -> it.periode.til
                        },
                    ),
                    resultat = ResultatBeregning(it.beløp),
                    grunnlagsreferanseListe = if (referanse != null) listOf(referanse) else it.grunnlagReferanseListe,
                )
            }
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
            val nesteFom = if (indeks == sortert.size - 1) null else sortert.getOrNull(indeks + 1)?.periode?.fom
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.fom, til = nesteFom ?: resultatPeriode.periode.til),
                resultat = resultatPeriode.resultat,
                grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe,
            )
        }
    }

    private fun List<StønadPeriodeDto>.sorterOgJusterPerioder2(): List<StønadPeriodeDto> {
        val sortert = sortedBy { it.periode.fom }

        return sortert.mapIndexed { indeks, resultatPeriode ->
            val nesteFom = sortert.getOrNull(indeks + 1)?.periode?.fom
            resultatPeriode.copy(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.fom, til = nesteFom ?: resultatPeriode.periode.til),
            )
        }
    }

    private fun lagOpphørsvedtak(periode: ÅrMånedsperiode): BeregnetBarnebidragResultat = BeregnetBarnebidragResultat(
        beregnetBarnebidragPeriodeListe = listOf(
            ResultatPeriode(
                periode = periode,
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
                it.beregnetFraDato
            },
    )

    private fun BeregnetBarnebidragResultat.erOpphør() = this.beregnetBarnebidragPeriodeListe.all { it.resultat.beløp == null }

    // Sjekker klageberegning mot minimumsgrense for endring (aka 12%-regelen)
    private fun sjekkMotMinimumsgrenseForEndring(
        klageberegningResultat: BeregnetBarnebidragResultat,
        klageperiode: ÅrMånedsperiode,
        beløpshistorikkFørPåklagetVedtak: GrunnlagDto,
        stønadstype: Stønadstype,
        klageberegningGrunnlag: BeregnGrunnlag,
    ): BeregnetBarnebidragResultat {
        val (_, opphørsdato, _, søknadsbarnReferanse) = klageberegningGrunnlag
        val åpenSluttperiode = opphørsdato == null || opphørsdato!!.isAfter(YearMonth.now())
        if (klageberegningResultat.erOpphør()) {
            return klageberegningResultat
        }

        val delberegningIndeksreguleringPrivatAvtalePeriodeResultat = utførDelberegningPrivatAvtalePeriode(klageberegningGrunnlag)

        // Kaller delberegning for å sjekke om endring i bidrag er over grense (pr periode)
        val delberegningEndringSjekkGrensePeriodeResultat =
            delberegningEndringSjekkGrensePeriode(
                mottattGrunnlag = BeregnGrunnlag(
                    periode = klageperiode,
                    opphørsdato = opphørsdato,
                    stønadstype = stønadstype,
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    grunnlagListe =
                    klageberegningResultat.grunnlagListe + beløpshistorikkFørPåklagetVedtak +
                        delberegningIndeksreguleringPrivatAvtalePeriodeResultat,
                ),
                åpenSluttperiode = åpenSluttperiode,
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
            åpenSluttperiode = åpenSluttperiode,
        )

        val beregnetBidragErOverMinimumsgrenseForEndring = erOverMinimumsgrenseForEndring(delberegningEndringSjekkGrenseResultat)
        val grunnlagstype = when {
            stønadstype == Stønadstype.BIDRAG18AAR -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
            else -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG
        }
        val resultatPeriodeListe = lagResultatPerioder(
            delberegningEndeligBidragPeriodeResultat = klageberegningResultat.grunnlagListe,
            beregnetBidragErOverMinimumsgrenseForEndring = beregnetBidragErOverMinimumsgrenseForEndring,
            beløpshistorikkGrunnlag = listOf(beløpshistorikkFørPåklagetVedtak),
            beløpshistorikkGrunnlagstype = grunnlagstype,
            delberegningEndringSjekkGrensePeriodeResultat = delberegningEndringSjekkGrensePeriodeResultat,
            delberegningIndeksreguleringPrivatAvtalePeriodeResultat = delberegningIndeksreguleringPrivatAvtalePeriodeResultat,
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

    private fun utførDelberegningPrivatAvtalePeriode(klageberegningGrunnlag: BeregnGrunnlag): List<GrunnlagDto> =
        if (klageberegningGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<PrivatAvtaleGrunnlag>(Grunnlagstype.PRIVAT_AVTALE_GRUNNLAG)
                .none { it.gjelderBarnReferanse == klageberegningGrunnlag.søknadsbarnReferanse }
        ) {
            emptyList()
        } else {
            delberegningPrivatAvtalePeriode(klageberegningGrunnlag)
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

    private fun StønadDto?.hentEtterfølgendeVedtaksliste(klageperiode: ÅrMånedsperiode, påklagetVedtakId: Int, opphørsdato: YearMonth?) =
        if (this == null || this.periodeListe.isEmpty()) {
            emptyList()
        } else {
            this.periodeListe
                .filter {
                    it.vedtaksid != påklagetVedtakId &&
                        (
                            it.periode.fom.isAfter(klageperiode.til) &&
                                (
                                    it.periode.til == null && opphørsdato == null ||
                                        opphørsdato != null && it.periode.til != null && it.periode.til!!.isAfter(opphørsdato)
                                    )
                            )
                }
                .map { it.vedtaksid }
                .distinct()
        }

    private fun utførIndeksregulering(
        stønad: Stønadsid,
        historikk: List<BeregnetBarnebidragResultatIntern>,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): BeregnetBarnebidragResultatIntern {
        val (nesteIndeksår, grunnlagsliste) = historikk.map { it.resultat }.byggBeløpshistorikk(
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
        val indeksresultat = beregnIndeksreguleringApi.beregnIndeksregulering(
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(
                    fom = YearMonth.of(nesteIndeksår - 1, 7),
                    til = null,
                ),
                stønadstype = stønad.type,
                søknadsbarnReferanse = grunnlagsliste.søknadsbarn.hentPersonMedIdent(stønad.kravhaver.verdi)!!.referanse,
                grunnlagListe = grunnlagsliste,
            ),
        )
        val privatavtale = indeksresultat.filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtale>(
            Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE,
        ).first()
        val førstePeriode = privatavtale.innhold.perioder.minBy { it.periode.fom }
        return BeregnetBarnebidragResultatIntern(
            BeregnetBarnebidragResultat(
                grunnlagListe = (indeksresultat + grunnlagsliste).toSet().toList(),
                beregnetBarnebidragPeriodeListe = listOf(
                    ResultatPeriode(
                        periode = førstePeriode.periode,
                        resultat = ResultatBeregning(førstePeriode.beløp),
                        grunnlagsreferanseListe = listOf(privatavtale.referanse),
                    ),
                ),
            ),
            vedtakstype = Vedtakstype.INDEKSREGULERING,
            beregnetFraDato = LocalDate.of(nesteIndeksår, 7, 1),
        )
    }
    private fun utførAldersjustering(
        beregnBasertPåVedtak: VedtakDto,
        aldersjusteresForÅr: Int,
        historikk: MutableList<BeregnetBarnebidragResultatIntern>,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): BeregnetBarnebidragResultatIntern {
        val (stønad) = klageOrkestratorGrunnlag
        val (_, _, stønadDto) = historikk.map { it.resultat }.byggBeløpshistorikk(
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
        val søknadsbarn = beregnBasertPåVedtak.grunnlagListe.søknadsbarn.first().tilDto()

        val manuellAldersjustering = klageOrkestratorGrunnlag.manuellAldersjustering.find { it.aldersjusteringForÅr == aldersjusteresForÅr }
        try {
            val aldersjustering = aldersjusteringOrchestrator.utførAldersjustering(
                stønad,
                aldersjusteresForÅr,
                BeregnBasertPåVedtak(
                    manuellAldersjustering?.grunnlagFraVedtak,
                    if (manuellAldersjustering?.grunnlagFraVedtak != null) null else beregnBasertPåVedtak,
                ),
                beløpshistorikkStønad = stønadDto,
            )
            val aldersjusteringGrunnlag = opprettAldersjusteringDetaljerGrunnlag(
                søknadsbarnReferanse = søknadsbarn.referanse,
                aldersjusteresForÅr = aldersjusteresForÅr,
                aldersjusteresManuelt = false,
                aldersjustert = true,
                stønad = stønad,
            )
            return BeregnetBarnebidragResultatIntern(
                resultat = BeregnetBarnebidragResultat(
                    beregnetBarnebidragPeriodeListe = aldersjustering.beregning.beregnetBarnebidragPeriodeListe,
                    grunnlagListe = aldersjustering.beregning.grunnlagListe + listOf(aldersjusteringGrunnlag),
                ),
                vedtakstype = Vedtakstype.ALDERSJUSTERING,
                beregnetFraDato = LocalDate.of(aldersjusteresForÅr, 7, 1),
            )
        } catch (e: SkalIkkeAldersjusteresException) {
            val aldersjusteringGrunnlag =
                opprettAldersjusteringDetaljerGrunnlag(
                    søknadsbarnReferanse = søknadsbarn.referanse,
                    aldersjusteresForÅr = aldersjusteresForÅr,
                    stønad = stønad,
                    aldersjustert = false,
                    begrunnelser = e.begrunnelser.map { it.name },
                    vedtaksidBeregning = null,
                )
            return BeregnetBarnebidragResultatIntern(
                resultat = BeregnetBarnebidragResultat(
                    grunnlagListe = listOf(søknadsbarn, aldersjusteringGrunnlag),
                    beregnetBarnebidragPeriodeListe = emptyList(),
                ),
                vedtakstype = Vedtakstype.ALDERSJUSTERING,
                beregnetFraDato = LocalDate.of(aldersjusteresForÅr, 7, 1),
            )
        } catch (e: AldersjusteresManueltException) {
            val aldersjusteringGrunnlag =
                opprettAldersjusteringDetaljerGrunnlag(
                    søknadsbarnReferanse = søknadsbarn.referanse,
                    aldersjusteresForÅr = aldersjusteresForÅr,
                    stønad = stønad,
                    aldersjustert = false,
                    aldersjusteresManuelt = true,
                    vedtaksidBeregning = null,
                    begrunnelser = listOf(e.begrunnelse.name),
                )
            return BeregnetBarnebidragResultatIntern(
                resultat = BeregnetBarnebidragResultat(
                    grunnlagListe = listOf(søknadsbarn, aldersjusteringGrunnlag),
                    beregnetBarnebidragPeriodeListe = emptyList(),
                ),
                vedtakstype = Vedtakstype.ALDERSJUSTERING,
                beregnetFraDato = LocalDate.of(aldersjusteresForÅr, 7, 1),
            )
        }
    }
    private fun BeregnetBarnebidragResultat.tilVedtakDto(stønad: Stønadsid) = VedtakDto(
        type = Vedtakstype.KLAGE,
        kilde = Vedtakskilde.AUTOMATISK,
        opprettetAv = "",
        vedtakstidspunkt = LocalDateTime.now(),
        opprettetTidspunkt = LocalDateTime.now(),
        opprettetAvNavn = "",
        vedtaksid = -1,
        unikReferanse = "",
        kildeapplikasjon = "",
        enhetsnummer = null,
        innkrevingUtsattTilDato = null,
        fastsattILand = null,
        grunnlagListe = grunnlagListe,
        engangsbeløpListe = emptyList(),
        stønadsendringListe = listOf(
            StønadsendringDto(
                periodeListe = beregnetBarnebidragPeriodeListe.map {
                    VedtakPeriodeDto(
                        periode = it.periode,
                        resultatkode = Resultatkode.KOSTNADSBEREGNET_BIDRAG.name,
                        beløp = it.resultat.beløp,
                        valutakode = "NOK",
                        delytelseId = null,
                        grunnlagReferanseListe = it.grunnlagsreferanseListe,
                    )
                },
                sak = stønad.sak,
                kravhaver = stønad.kravhaver,
                skyldner = stønad.skyldner,
                beslutning = Beslutningstype.ENDRING,
                type = stønad.type,
                mottaker = Personident(""),
                sisteVedtaksid = null,
                førsteIndeksreguleringsår = null,
                innkreving = Innkrevingstype.MED_INNKREVING,
                omgjørVedtakId = null,
                eksternReferanse = null,
                grunnlagReferanseListe = emptyList(),
            ),
        ),
        behandlingsreferanseListe = emptyList(),
    )

    private fun List<GrunnlagDto>.erResultatUnderGrense(grunnlagsreferanseListe: List<Grunnlagsreferanse>): Boolean {
        val søknadsbarn = finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<Person>(
            Grunnlagstype.PERSON_SØKNADSBARN,
            grunnlagsreferanseListe,
        ).firstOrNull() ?: run {
            val refererTil = grunnlagsreferanseListe.mapNotNull { gr -> find { it.referanse == gr }?.gjelderBarnReferanse }
            filtrerOgKonverterBasertPåEgenReferanse<Person>(
                Grunnlagstype.PERSON_SØKNADSBARN,
                referanse = refererTil.firstOrNull() ?: "",
            ).firstOrNull()
        }
        return søknadsbarn?.let { erResultatEndringUnderGrense(søknadsbarn.referanse) } ?: false
    }
    private fun List<BeregnetBarnebidragResultat>.byggBeløpshistorikk(
        stønad: Stønadsid,
        førPeriode: YearMonth? = null,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): ByggetBeløpshistorikk {
        val personer = this.flatMap { it.grunnlagListe.hentAllePersoner() }.map { it.tilDto() }.toMutableList()

        val perioder = this.filter { it.beregnetBarnebidragPeriodeListe.isNotEmpty() }.sortedBy { it.beregnetFraDato }.flatMap {
            val grunnlagsliste = it.grunnlagListe
            it.beregnetBarnebidragPeriodeListe.map {
                val erResultatIngenEndring = grunnlagsliste.erResultatUnderGrense(it.grunnlagsreferanseListe)
                StønadPeriodeDto(
                    periodeid = 1,
                    periode = it.periode,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    beløp = it.resultat.beløp,
                    stønadsid = 1,
                    valutakode = "NOK",
                    vedtaksid = 1,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        }.sortedBy { it.periode.fom }.sorterOgJusterPerioder2()
            .filter { førPeriode == null || it.periode.fom.isBefore(førPeriode) }
            .justerSistePeriodeTilÅBliLøpende()

        val førstePeriode = perioder.minOfOrNull { it.periode.fom }
        val perioderFørFraBeløpshistorikk = beløpshistorikkFørPåklagetVedtak.beløpshistorikk
            .filter { førstePeriode == null || it.periode.fom.isBefore(førstePeriode) }
            .map {
                val vedtak = vedtakService.hentVedtak(it.vedtaksid!!)!!
                val periode = vedtak.finnStønadsendring(stønad)!!.periodeListe.find { vp -> vp.periode.fom == it.periode.fom }!!
                val erResultatIngenEndring = vedtak.grunnlagListe.erResultatUnderGrense(periode.grunnlagReferanseListe)

                StønadPeriodeDto(
                    periodeid = 1,
                    periode = it.periode,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    beløp = it.beløp,
                    stønadsid = 1,
                    valutakode = "NOK",
                    vedtaksid = 1,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        val nesteIndeksår = perioder.fold(LocalDate.now().plusYears(1).year) { acc, dto ->
            if (dto.resultatkode == Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name) {
                acc
            } else {
                dto.periode.fom.year + 1
            }
        }
        val stønadDto = StønadDto(
            stønadsid = -1,
            type = stønad.type,
            kravhaver = stønad.kravhaver,
            skyldner = stønad.skyldner,
            sak = stønad.sak,
            mottaker = Personident(""),
            førsteIndeksreguleringsår = nesteIndeksår,
            nesteIndeksreguleringsår = nesteIndeksår,
            innkreving = Innkrevingstype.MED_INNKREVING,
            opprettetAv = "",
            opprettetTidspunkt = LocalDateTime.now(),
            endretAv = "",
            endretTidspunkt = LocalDateTime.now(),
            periodeListe = (perioderFørFraBeløpshistorikk + perioder).sorterOgJusterPerioder2(),
        )
        personer.hentPersonMedIdent(stønad.kravhaver.verdi) ?: personer.hentPersonForNyesteIdent(identUtils, stønad.kravhaver) ?: run {
            val grunnlag = opprettPersonGrunnlag(stønad.kravhaver, Rolletype.BARN)
            personer.add(grunnlag)
        }

        personer.hentPersonMedIdent(stønad.skyldner.verdi) ?: personer.hentPersonForNyesteIdent(identUtils, stønad.skyldner) ?: run {
            val grunnlag = opprettPersonGrunnlag(stønad.skyldner, Rolletype.BIDRAGSPLIKTIG)
            personer.add(grunnlag)
        }
        val grunnlagBeløpshistorikk = stønadDto.tilGrunnlag(personer.toMutableList(), stønad, identUtils)
        val grunnlagsliste = (listOf(grunnlagBeløpshistorikk) + personer).toSet().toList()

        return ByggetBeløpshistorikk(nesteIndeksår, grunnlagsliste, stønadDto, grunnlagBeløpshistorikk)
    }

    fun List<StønadPeriodeDto>.justerSistePeriodeTilÅBliLøpende() = mapIndexed { index, periode ->
        if (index == this.size - 1) {
            periode.copy(periode = periode.periode.copy(til = null))
        } else {
            periode
        }
    }

    private fun opprettPersonGrunnlag(ident: Personident, rolle: Rolletype): GrunnlagDto = GrunnlagDto(
        referanse = "person_${rolle.name}_$ident",
        type = rolle.tilGrunnlagstype(),
        innhold = POJONode(
            Person(
                ident = ident,
                navn = "",
                fødselsdato = LocalDate.now(),
            ),
        ),
    )
}
