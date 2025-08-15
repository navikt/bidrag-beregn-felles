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
import no.nav.bidrag.beregn.barnebidrag.utils.KlageOrkestratorHelpers
import no.nav.bidrag.beregn.barnebidrag.utils.toYearMonth
import no.nav.bidrag.beregn.barnebidrag.utils.vedtaksidAutomatiskJobb
import no.nav.bidrag.beregn.barnebidrag.utils.vedtaksidBeregnetBeløpshistorikk
import no.nav.bidrag.beregn.core.mapper.tilDto
import no.nav.bidrag.beregn.core.service.VedtakService
import no.nav.bidrag.beregn.core.util.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.core.util.justerVedtakstidspunkt
import no.nav.bidrag.beregn.core.util.justerVedtakstidspunktVedtak
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
import no.nav.bidrag.indeksregulering.service.IndeksreguleringOrkestrator
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
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
import no.nav.bidrag.transport.behandling.felles.grunnlag.PrivatAvtaleGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.ResultatFraVedtakGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningIndeksregulering
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.erResultatEndringUnderGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåEgenReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.søknadsbarn
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.erIndeksEllerAldersjustering
import no.nav.bidrag.transport.behandling.vedtak.response.finnAldersjusteringDetaljerGrunnlag
import no.nav.bidrag.transport.behandling.vedtak.response.finnStønadsendring
import no.nav.bidrag.transport.behandling.vedtak.response.virkningstidspunkt
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth

fun VedtakDto.erAldersjustering() = type == Vedtakstype.ALDERSJUSTERING
fun VedtakDto.erIndeksregulering() = type == Vedtakstype.INDEKSREGULERING

internal data class KlageOrkestratorContext(
    val stønad: Stønadsid,
    val klageperiode: ÅrMånedsperiode,
    val løpendeStønad: StønadDto,
    val påklagetVedtakVirkningstidspunkt: YearMonth,
    val klageresultat: BeregnetBarnebidragResultat,
    val beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    val klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
    val opphørsdato: YearMonth?,
    val nyVirkningErEtterOpprinneligVirkning: Boolean,
    val vedtakslisteRelatertTilPåklagetVedtak: Set<Int>,
) {
    val påklagetVedtakId: Int = klageOrkestratorGrunnlag.påklagetVedtakId
}

internal data class BeregnetBarnebidragResultatInternal(
    val resultat: BeregnetBarnebidragResultat,
    val vedtakstype: Vedtakstype,
    val beregnetFraDato: LocalDate,
    val klagevedtak: Boolean = false,
    val beregnet: Boolean = false,
)

internal data class ByggetBeløpshistorikk(
    val nesteIndeksår: Int,
    val grunnlagsliste: List<GrunnlagDto>,
    val stønadDto: StønadDto,
    val grunnlagBeløpshistorikk: GrunnlagDto,
)

internal data class BeløpshistorikkPeriodeInternal(
    val periode: ÅrMånedsperiode,
    val beløp: BigDecimal?,
    val resultatkode: String? = null,
    val vedtaksid: Int? = null,
    val aldersjuster: Boolean = false,
    val indeksreguler: Boolean = false,
    val klagevedtak: Boolean = false,
)

data class EtterfølgendeVedtakSomOverlapper(val vedtaksid: Int, val virkningstidspunkt: YearMonth)

fun klageFeilet(begrunnelse: String): Nothing = throw KlageberegningFeiletFunksjonelt(begrunnelse)
fun klageFeiletTeknisk(begrunnelse: String, throwable: Throwable): Nothing = throw KlageberegningFeiletTeknisk(begrunnelse, throwable)
fun finnesEtterfølgendeVedtak(vedtak: List<EtterfølgendeVedtakSomOverlapper>): Nothing =
    throw FinnesEtterfølgendeVedtakMedVirkningstidspunktFørPåklagetVedtak(vedtak)

class KlageberegningFeiletFunksjonelt(feilmelding: String) : RuntimeException(feilmelding)
class KlageberegningFeiletTeknisk(feilmelding: String, throwable: Throwable) : RuntimeException(feilmelding, throwable)

class FinnesEtterfølgendeVedtakMedVirkningstidspunktFørPåklagetVedtak(val vedtak: List<EtterfølgendeVedtakSomOverlapper>) :
    RuntimeException("Det finnes etterfølgende vedtak $vedtak")

@Service
@Import(
    VedtakService::class,
    AldersjusteringOrchestrator::class,
    IndeksreguleringOrkestrator::class,
    IdentUtils::class,
    KlageOrkestratorHelpers::class,
)
class KlageOrkestrator(
    private val vedtakService: VedtakService,
    private val aldersjusteringOrchestrator: AldersjusteringOrchestrator,
    private val indeksreguleringOrkestrator: IndeksreguleringOrkestrator,
    private val klageOrkestratorHelpers: KlageOrkestratorHelpers,
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
            val påklagetVedtakVirkningstidspunkt = påklagetVedtak.virkningstidspunkt?.toYearMonth()
                ?: klageFeilet("Påklaget vedtak med id $påklagetVedtakId har ikke virkningstidspunkt")
            val løpendeStønadGjeldende = vedtakService.hentLøpendeStønad(stønad)
                ?: klageFeilet("Fant ikke løpende stønad for $stønad")
            val grunnlagsliste = klageberegningGrunnlag.grunnlagListe

            validerEtterfølgendeVedtakIkkeOverlapper(
                stønad = stønad,
                påklagetVedtak = påklagetVedtak,
                klageperiode = klageperiode,
            )

            val personobjekter =
                listOf(
                    grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi) ?: klageFeilet("Fant ikke søknadsbarn/kravhaver i grunnlaget"),
                    klageberegningGrunnlag.grunnlagListe.bidragsmottaker ?: klageFeilet("Fant ikke bidragsmottaker i grunnlaget"),
                    klageberegningGrunnlag.grunnlagListe.bidragspliktig ?: klageFeilet("Fant ikke bidragspliktig i grunnlaget"),
                ) as List<GrunnlagDto>

            val beløpshistorikkFørPåklagetVedtak = klageOrkestratorHelpers.finnBeløpshistorikk(påklagetVedtak, stønad, personobjekter)

            // TODO Sjekk om nytt virkningstidspunkt kan være tidligere enn originalt virkningstidspunkt
            val nyVirkningErEtterOpprinneligVirkning = klageperiode.fom.isAfter(påklagetVedtakVirkningstidspunkt)

            val vedtakslisteRelatertTilPåklagetVedtak = vedtakService.hentAlleVedtaksiderRelatertTilPåklagetVedtak(stønad, påklagetVedtakId)
            val context = KlageOrkestratorContext(
                nyVirkningErEtterOpprinneligVirkning = nyVirkningErEtterOpprinneligVirkning,
                klageresultat = klageberegningResultat,
                klageperiode = klageperiode,
                opphørsdato = klageberegningGrunnlag.opphørsdato,
                løpendeStønad = løpendeStønadGjeldende,
                påklagetVedtakVirkningstidspunkt = påklagetVedtakVirkningstidspunkt,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
                stønad = stønad,
                vedtakslisteRelatertTilPåklagetVedtak = vedtakslisteRelatertTilPåklagetVedtak,
            )

            val foreløpigVedtak = byggVedtak(context)

            val (_, _, _, beløpshistorikkFørPåklagetVedtakEtterOrkestrering) = klageOrkestratorHelpers.byggBeløpshistorikk(
                foreløpigVedtak.map { it.resultat },
                stønad,
                klageperiode.fom,
                beløpshistorikkFørPåklagetVedtak,
            )

            // Sjekk klageberegningen mot minimumsgrense for endring (aka 12%-regel)
            val klageberegningResultatEtterSjekkMinGrenseForEndring = sjekkMotMinimumsgrenseForEndring(
                klageberegningResultat = klageberegningResultat,
                klageperiode = klageperiode,
                beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtakEtterOrkestrering,
                stønadstype = stønad.type,
                klageberegningGrunnlag = klageberegningGrunnlag,
            )

            return byggVedtak(
                context.copy(
                    klageresultat = klageberegningResultatEtterSjekkMinGrenseForEndring,
                ),
            )
        } catch (e: Exception) {
            if (e is FinnesEtterfølgendeVedtakMedVirkningstidspunktFørPåklagetVedtak || e is KlageberegningFeiletFunksjonelt) {
                throw e
            }
            klageFeiletTeknisk("Feil under klageberegning: ${e.message}.", e)
        }
    }

    private fun validerEtterfølgendeVedtakIkkeOverlapper(stønad: Stønadsid, påklagetVedtak: VedtakDto, klageperiode: ÅrMånedsperiode) {
        val påklagetVedtakVirkningstidspunkt = påklagetVedtak.virkningstidspunkt?.toYearMonth()
            ?: klageFeilet("Påklaget vedtak med id ${påklagetVedtak.vedtaksid} har ikke virkningstidspunkt")
        val påklagetVedtakVedtakstidspunkt = påklagetVedtak.justerVedtakstidspunktVedtak().vedtakstidspunkt
        val vedtaksliste = vedtakService.hentAlleVedtakForStønad(
            stønadsid = stønad,
            fraPeriode = påklagetVedtakVirkningstidspunkt,
            ignorerVedtaksid = påklagetVedtak.vedtaksid,
        )

        val etterfølgendeVedtakMedPeriodeFørKlageperiode = vedtaksliste.filter {
            it.justerVedtakstidspunkt().vedtakstidspunkt >
                påklagetVedtakVedtakstidspunkt &&
                !it.type.erIndeksEllerAldersjustering
        }
            .filter {
                it.virkningstidspunkt!! < klageperiode.fom
            }
        if (etterfølgendeVedtakMedPeriodeFørKlageperiode.isNotEmpty()) {
            finnesEtterfølgendeVedtak(
                etterfølgendeVedtakMedPeriodeFørKlageperiode.map {
                    EtterfølgendeVedtakSomOverlapper(it.vedtaksid, it.virkningstidspunkt!!)
                },
            )
        }
    }

    private fun byggVedtak(context: KlageOrkestratorContext): List<ResultatVedtak> = when {
        // Scenario 1: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og
        // kjør evt ny indeksregulering/aldersjustering
        !context.nyVirkningErEtterOpprinneligVirkning ->
            klageScenarioVirkningFørEllerLikOpprinneligVirkning(context)

        // Scenario 2: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
        // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles
        // ut.
        context.nyVirkningErEtterOpprinneligVirkning ->
            klageScenarioVirkningEtterOpprinneligVirkning(context)

        else -> emptyList()
    }

    // Scenario 2: Klagevedtak dekker opprinnelig beregningsperiode for det påklagede vedtaket - legg til evt etterfølgende vedtak og kjør
    // evt ny indeksregulering/aldersjustering
    private fun klageScenarioVirkningFørEllerLikOpprinneligVirkning(context: KlageOrkestratorContext): List<ResultatVedtak> {
        val (
            _,
            klageperiode,
            _,
            påklagetVedtakVirkningstidspunkt,
            klageberegningResultat,
            beløpshistorikkFørPåklagetVedtak,
        ) = context
        val etterfølgendeVedtakListe =
            hentEtterfølgendeVedtakslisteFraVedtak(context)
        val periodeSomSkalOpphøres =
            finnPeriodeSomSkalOpphøres(klageperiode, påklagetVedtakVirkningstidspunkt, beløpshistorikkFørPåklagetVedtak)

        val delvedtakListe = buildList {
            periodeSomSkalOpphøres?.let {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(it),
                        delvedtak = true,
                        klagevedtak = false,
                        beregnet = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            add(
                ResultatVedtak(
                    resultat = klageberegningResultat,
                    delvedtak = true,
                    beregnet = true,
                    klagevedtak = true,
                    vedtakstype = Vedtakstype.KLAGE,
                ),
            )
            addAll(
                opprettDelvedtakFraVedtakslisten(
                    context,
                    beløpshistorikk = etterfølgendeVedtakListe,
                    delvedtak = this,
                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            vedtakstype = it.vedtakstype,
                            beregnet = it.beregnet,
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
    private fun klageScenarioVirkningEtterOpprinneligVirkning(context: KlageOrkestratorContext): List<ResultatVedtak> {
        val vedtakIderMellomPåklagetVirkningOgNyVirkning =
            finnVedtakIderMellomPåklagetVirkningOgNyVirkning(context)
        val periodeSomSkalOpphøres = finnPeriodeSomSkalOpphøres(
            context.klageperiode,
            context.påklagetVedtakVirkningstidspunkt,
            context.beløpshistorikkFørPåklagetVedtak,
        )

        val delvedtakListeFør = buildList {
            if (periodeSomSkalOpphøres != null) {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(periodeSomSkalOpphøres),
                        delvedtak = true,
                        klagevedtak = false,
                        beregnet = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            addAll(
                opprettDelvedtakFraVedtakslisten(
                    context,
                    beløpshistorikk = vedtakIderMellomPåklagetVirkningOgNyVirkning,
                    delvedtak = this,
                    gjenopprettetBeløpshistorikk = true,

                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            beregnet = it.beregnet,
                            vedtakstype = it.vedtakstype,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }

        val etterfølgendeVedtakListe =
            hentEtterfølgendeVedtakslisteFraVedtak(context)
        val delvedtakListe = buildList {
            addAll(delvedtakListeFør)
            add(
                ResultatVedtak(
                    resultat = context.klageresultat,
                    delvedtak = true,
                    beregnet = true,
                    klagevedtak = true,
                    vedtakstype = Vedtakstype.KLAGE,
                ),
            )
            addAll(
                opprettDelvedtakFraVedtakslisten(
                    context,
                    beløpshistorikk = etterfølgendeVedtakListe,
                    delvedtak = this,
                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            klagevedtak = false,
                            beregnet = it.beregnet,
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

    // Lager BeregnetBarnebidragResultat (simulert resultat fra beregningen) for alle (eksisterende) vedtak i vedtakListe
    private fun opprettDelvedtakFraVedtakslisten(
        context: KlageOrkestratorContext,
        beløpshistorikk: List<BeløpshistorikkPeriodeInternal>,
        delvedtak: List<ResultatVedtak>,
        gjenopprettetBeløpshistorikk: Boolean = false,
    ): List<BeregnetBarnebidragResultatInternal> {
        val stønad = context.stønad
        val historikk = delvedtak.map {
            BeregnetBarnebidragResultatInternal(it.resultat, it.vedtakstype, it.beregnetFraDato, it.klagevedtak)
        }.toMutableList()
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultatInternal>()
        beløpshistorikk.groupBy { it.vedtaksid }.entries.sortedWith(compareBy(nullsLast()) { it.key }).forEach { (vedtaksid, it) ->
            val komplettVedtak = vedtaksid?.let { vedtakService.hentVedtak(vedtaksid) }
            val forrigeVedtakErKlagevedtak = historikk.maxByOrNull { it.beregnetFraDato }?.klagevedtak == true
            val erAldersjusteringBasertPåPåklagetVedtak = if (komplettVedtak?.type == Vedtakstype.ALDERSJUSTERING) {
                val aldersjusteringGrunnlag = komplettVedtak.finnAldersjusteringDetaljerGrunnlag(komplettVedtak.finnStønadsendring(stønad)!!)
                val grunnlagFraVedtak = aldersjusteringGrunnlag?.innhold?.grunnlagFraVedtak
                grunnlagFraVedtak != null && context.vedtakslisteRelatertTilPåklagetVedtak.contains(grunnlagFraVedtak)
            } else {
                false
            }
            val resultat = when {
                vedtaksid == null && it.any { it.aldersjuster } -> {
                    it.filter { it.aldersjuster }.forEach { periode ->
                        utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                            context.klageresultat.tilVedtakDto(stønad),
                            historikk,
                            stønad,
                            context.klageOrkestratorGrunnlag,
                            context.beløpshistorikkFørPåklagetVedtak,
                            periode.periode.fom.year,
                            periode.indeksreguler,
                        )?.let {
                            beregnetBarnebidragResultatListe.add(it)
                            historikk.add(it)
                        }
                    }
                    null
                }

                komplettVedtak != null && komplettVedtak.erAldersjustering() &&
                    (forrigeVedtakErKlagevedtak || erAldersjusteringBasertPåPåklagetVedtak) &&
                    historikk.isNotEmpty() && !gjenopprettetBeløpshistorikk -> {
                    utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                        context.klageresultat.tilVedtakDto(stønad),
                        historikk,
                        stønad,
                        context.klageOrkestratorGrunnlag,
                        context.beløpshistorikkFørPåklagetVedtak,
                        komplettVedtak.vedtakstidspunkt!!.year,
                        true,
                    )
                }

                komplettVedtak != null && komplettVedtak.erIndeksregulering() && historikk.isNotEmpty() && !gjenopprettetBeløpshistorikk ->
                    utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                        context.klageresultat.tilVedtakDto(stønad),
                        historikk,
                        stønad,
                        context.klageOrkestratorGrunnlag,
                        context.beløpshistorikkFørPåklagetVedtak,
                        komplettVedtak.vedtakstidspunkt!!.year,
                        true,
                    )

                komplettVedtak != null -> {
                    val referanse = "resultatFraVedtak_${komplettVedtak.vedtaksid}"
                    val perioder = it.map {
                        ResultatPeriode(
                            it.periode,
                            ResultatBeregning(it.beløp),
                            listOf(referanse),
                        )
                    }.sortedBy { it.periode.fom }
                    BeregnetBarnebidragResultatInternal(
                        resultat = BeregnetBarnebidragResultat(
                            beregnetBarnebidragPeriodeListe = perioder,
                            grunnlagListe = listOf(
                                GrunnlagDto(
                                    referanse = referanse,
                                    type = Grunnlagstype.RESULTAT_FRA_VEDTAK,
                                    innhold = POJONode(
                                        ResultatFraVedtakGrunnlag(
                                            vedtaksid = komplettVedtak.vedtaksid,
                                            beregnet = false,
                                            vedtakstidspunkt = komplettVedtak.vedtakstidspunkt,
                                        ),
                                    ),
                                ),
                            ),
                        ),
                        vedtakstype = komplettVedtak.type,
                        beregnet = false,
                        beregnetFraDato = perioder.minOf { it.periode.fom }.atDay(1),
                    )
                }

                else -> null
            }
            resultat?.let {
                beregnetBarnebidragResultatListe.add(resultat)
                historikk.add(resultat)
            }
        }
        return beregnetBarnebidragResultatListe
    }

    private fun List<BeløpshistorikkPeriodeInternal>.finnFørsteIndeksår(
        stønadsid: Stønadsid,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): Int {
        val førsteVedtak = groupBy { it.vedtaksid }.minBy { (_, perioder) -> perioder.minOf { it.periode.fom } }.value
        val sistePeriode = førsteVedtak.maxBy { it.periode.til?.year ?: it.periode.fom.year }
        val årstallSistePeriode = sistePeriode.periode.til?.year ?: sistePeriode.periode.fom.year
        if (sistePeriode.vedtaksid == null && sistePeriode.resultatkode == null ||
            sistePeriode.vedtaksid == null
        ) {
            val periodErIngenEndringUnderGrense = sistePeriode.resultatkode != null &&
                Resultatkode.fraKode(sistePeriode.resultatkode) == Resultatkode.INGEN_ENDRING_UNDER_GRENSE
            if (periodErIngenEndringUnderGrense) {
                val sistePeriodeFraHistorikk = beløpshistorikkFørPåklagetVedtak.beløpshistorikk.maxBy { it.periode.til?.year ?: it.periode.fom.year }
                val vedtak = vedtakService.hentVedtak(sistePeriodeFraHistorikk.vedtaksid!!)!!
                return vedtak.finnStønadsendring(stønadsid)?.førsteIndeksreguleringsår ?: (årstallSistePeriode + 1)
            }
            return årstallSistePeriode + 1
        }
        val vedtak = vedtakService.hentVedtak(sistePeriode.vedtaksid)!!
        return vedtak.finnStønadsendring(stønadsid)?.førsteIndeksreguleringsår ?: (årstallSistePeriode + 1)
    }

    private fun List<BeløpshistorikkPeriodeInternal>.fyllPåPerioderForAldersjusteringEllerIndeksregulering(
        klageperiode: ÅrMånedsperiode,
        påklagetVedtakVirkningstidspunkt: YearMonth? = null,
        beregnForPerioderEtterKlage: Boolean = false,
        klageberegningResultat: BeregnetBarnebidragResultat,
        stønadsid: Stønadsid,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): List<BeløpshistorikkPeriodeInternal> {
        if (!beregnForPerioderEtterKlage && this.isEmpty()) return emptyList()

        val beløshistorikkKlage = if (beregnForPerioderEtterKlage) {
            klageberegningResultat.beregnetBarnebidragPeriodeListe.map {
                val søknadsbarn = klageberegningResultat.grunnlagListe.hentPersonMedIdent(stønadsid.kravhaver.verdi)!!
                val erResultatIngenEndring = klageberegningResultat.grunnlagListe.erResultatEndringUnderGrense(søknadsbarn.referanse)
                BeløpshistorikkPeriodeInternal(
                    it.periode,
                    it.resultat.beløp,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    klagevedtak = true,
                )
            }
        } else {
            listOf(
                BeløpshistorikkPeriodeInternal(
                    påklagetVedtakVirkningstidspunkt?.let { ÅrMånedsperiode(påklagetVedtakVirkningstidspunkt, klageperiode.fom) } ?: klageperiode,
                    BigDecimal.ZERO,
                    klagevedtak = true,
                ),
            )
        }

        val beløshistorikkMedKlage = (this + beløshistorikkKlage).sortedBy { it.periode.fom }

        val mutableList = beløshistorikkMedKlage.toMutableList()
        val førsteIndeksår = beløshistorikkMedKlage.finnFørsteIndeksår(stønadsid, beløpshistorikkFørPåklagetVedtak)
        val minYear = minOf(førsteIndeksår, beløshistorikkMedKlage.minOf { it.periode.fom.year })
        val sistePeriode = beløshistorikkMedKlage.maxBy { it.periode.fom }
        val sistePeriodeErOpphør = sistePeriode.beløp == null
        val sistePeriodeTil = if (sistePeriodeErOpphør) sistePeriode.periode.fom else sistePeriode.periode.til
        val maksÅrJustert = if (sistePeriodeTil != null && sistePeriodeTil.month.value < 7) {
            sistePeriodeTil.year - 1
        } else if (!beregnForPerioderEtterKlage && sistePeriodeTil != null) {
            sistePeriodeTil.year
        } else {
            sistePeriode.periode.fom.year
        }
        val maxYear = when {
            beregnForPerioderEtterKlage && sistePeriodeErOpphør -> maksÅrJustert
            beregnForPerioderEtterKlage -> YearMonth.now().year
            else -> maksÅrJustert
        }

        for (year in minYear..maxYear) {
            val julyFirst = YearMonth.of(year, 7)
            val hasPeriodStartingInJuly = beløshistorikkMedKlage.any {
                it.periode.fom == julyFirst &&
                    (!it.klagevedtak || it.resultatkode != Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name)
            }

            if (!hasPeriodStartingInJuly) {
                val periodBeforeJuly = beløshistorikkMedKlage.filter { it.periode.fom.isBefore(julyFirst) }.maxByOrNull { it.periode.fom }
                if (periodBeforeJuly != null) {
                    mutableList.add(
                        BeløpshistorikkPeriodeInternal(
                            periode = ÅrMånedsperiode(fom = julyFirst, til = null),
                            beløp = null,
                            vedtaksid = null,
                            aldersjuster = true,
                            indeksreguler = year >= førsteIndeksår,
                        ),
                    )
                }
            }
        }
        return mutableList.sortedBy { it.periode.fom }
    }

    private fun finnVedtakIderMellomPåklagetVirkningOgNyVirkning(context: KlageOrkestratorContext): List<BeløpshistorikkPeriodeInternal> {
        val (
            stønad,
            klageperiode,
            løpendeStønad,
            påklagetVedtakVirkningstidspunkt,
            klageberegningResultat,
            beløpshistorikkFørPåklagetVedtak,
        ) = context
        val nyBeløpshistorikk = if (klageperiode.fom > påklagetVedtakVirkningstidspunkt) {
            hentHistorikkMellomOpprinneligOgNyVirkning(
                beløpshistorikkFørPåklagetVedtak,
                påklagetVedtakVirkningstidspunkt,
                klageperiode,
            )
        } else if (løpendeStønad.periodeListe.isEmpty()) {
            emptyList()
        } else {
            løpendeStønad.periodeListe
                .filter {
                    it.vedtaksid != context.påklagetVedtakId &&
                        it.periode.fom >= påklagetVedtakVirkningstidspunkt
                    it.periode.til != null && it.periode.til!!.isBefore(klageperiode.til!!)
                }
                .map {
                    BeløpshistorikkPeriodeInternal(
                        periode = it.periode,
                        beløp = it.beløp,
                        vedtaksid = it.vedtaksid,
                        resultatkode = it.resultatkode,
                    )
                }
                .distinct()
        }
        return nyBeløpshistorikk.fyllPåPerioderForAldersjusteringEllerIndeksregulering(
            klageperiode,
            påklagetVedtakVirkningstidspunkt,
            stønadsid = stønad,
            klageberegningResultat = klageberegningResultat,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
    }

    private fun hentHistorikkMellomOpprinneligOgNyVirkning(
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        påklagetVedtakVirkningstidspunkt: YearMonth,
        klageperiode: ÅrMånedsperiode,
    ): List<BeløpshistorikkPeriodeInternal> {
        val vedtakMellom = beløpshistorikkFørPåklagetVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(klageperiode.fom) &&
                (
                    it.periode.fom.isAfter(påklagetVedtakVirkningstidspunkt) ||
                        it.periode.til != null && it.periode.til!!.isAfter(påklagetVedtakVirkningstidspunkt)
                    )
        }.map {
            BeløpshistorikkPeriodeInternal(
                periode = ÅrMånedsperiode(maxOf(påklagetVedtakVirkningstidspunkt, it.periode.fom), it.periode.til),
                beløp = it.beløp,
                vedtaksid = it.vedtaksid,

            )
        }.distinct()
            .ifEmpty {
                beløpshistorikkFørPåklagetVedtak.beløpshistorikk
                    .filter { it.periode.fom.isBefore(klageperiode.fom) }
                    .maxByOrNull { it.periode.fom }?.let { listOf(it) }?.map {
                        BeløpshistorikkPeriodeInternal(
                            periode = ÅrMånedsperiode(maxOf(påklagetVedtakVirkningstidspunkt, it.periode.fom), it.periode.til),
                            beløp = it.beløp,
                            vedtaksid = it.vedtaksid,
                        )
                    } ?: emptyList()
            }

        return vedtakMellom.mapIndexed { index, periode ->
            if (index == vedtakMellom.lastIndex && (periode.periode.til != null && periode.periode.til!! > klageperiode.fom)) {
                periode.copy(periode = periode.periode.copy(til = klageperiode.fom))
            } else {
                periode
            }
        }
    }

    private fun finnPeriodeSomSkalOpphøres(
        klageperiode: ÅrMånedsperiode,
        påklagetVedtakVirkningstidspunkt: YearMonth,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
    ): ÅrMånedsperiode? {
        val vedtakMellomPåklagetVirkningOgNyVirkning = beløpshistorikkFørPåklagetVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(klageperiode.fom)
        }

        val tidligstePeriodeFom = vedtakMellomPåklagetVirkningOgNyVirkning.minOfOrNull { it.periode.fom }

        return if (tidligstePeriodeFom == null && påklagetVedtakVirkningstidspunkt < klageperiode.fom) {
            ÅrMånedsperiode(
                fom = YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
                til = klageperiode.fom,
            )
        } else if (tidligstePeriodeFom != null && tidligstePeriodeFom > påklagetVedtakVirkningstidspunkt) {
            ÅrMånedsperiode(
                fom = YearMonth.of(påklagetVedtakVirkningstidspunkt.year, påklagetVedtakVirkningstidspunkt.monthValue),
                til = tidligstePeriodeFom,
            )
        } else {
            null
        }
    }

    private fun utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
        klageVedtak: VedtakDto,
        historikk: MutableList<BeregnetBarnebidragResultatInternal>,
        stønad: Stønadsid,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        aldersjusteresIndeksreguleresForÅr: Int,
        indeksreguler: Boolean,
    ): BeregnetBarnebidragResultatInternal? {
        val (_, _, stønadDto) = klageOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
        val løpendePeriode = stønadDto.periodeListe.hentSisteLøpendePeriode() ?: return null
        val erKlagevedtak = klageVedtak.finnStønadsendring(stønad)!!.periodeListe.any { it.periode.fom == løpendePeriode.periode.fom }

        val klageVedtakLøpendePeriode = klageVedtak.finnStønadsendring(stønad)!!.periodeListe.hentSisteLøpendePeriode()
        val erKlageVedtakOpphør = klageVedtakLøpendePeriode == null || klageVedtakLøpendePeriode.beløp == null
        val klagevedtakResultat = historikk.find { it.klagevedtak }
        val vedtakFraGrunnlag = if (erKlagevedtak) {
            BeregnBasertPåVedtak(vedtakDto = klageVedtak)
        } else {
            val klageBeregnFraDato = klagevedtakResultat?.beregnetFraDato?.toYearMonth()
            val historikkUtenAutojobb = stønadDto.periodeListe.filter {
                (it.vedtaksid != vedtaksidBeregnetBeløpshistorikk || it.periode.fom == klageBeregnFraDato) &&
                    it.vedtaksid != vedtaksidAutomatiskJobb
            }
            val sistePeriode = historikkUtenAutojobb.maxByOrNull { it.periode.fom }
            val erSistePeriodeKlagevedtak = sistePeriode?.periode?.fom == klageBeregnFraDato
            BeregnBasertPåVedtak(
                if (!erSistePeriodeKlagevedtak) sistePeriode?.vedtaksid else null,
                if (erSistePeriodeKlagevedtak) klageVedtak else null,
            )
        }
        if (erKlagevedtak && erKlageVedtakOpphør) return null
        val resultatAldersjustering =
            utførAldersjustering(
                vedtakFraGrunnlag,
                aldersjusteresIndeksreguleresForÅr,
                historikk,
                klageOrkestratorGrunnlag,
                beløpshistorikkFørPåklagetVedtak,
            )
        val detaljer = resultatAldersjustering.resultat.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<AldersjusteringDetaljerGrunnlag>(
            Grunnlagstype.ALDERSJUSTERING_DETALJER,
        ).first()
        return if (!detaljer.innhold.aldersjustert && !detaljer.innhold.aldersjusteresManuelt) {
            if (indeksreguler) {
                utførIndeksregulering(
                    stønad,
                    historikk,
                    beløpshistorikkFørPåklagetVedtak,
                    aldersjusteresIndeksreguleresForÅr,
                )
            } else {
                null
            }
        } else {
            resultatAldersjustering
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
        // Fjern perioder fra klagevedtaket som overlapper med indeksregulering eller aldersjustering som kommer etter
        val perioderJustertForIndeksOgAldersjustering = resultatPeriodeListe.groupBy { it.periode.fom }.map { (_, periods) ->
            if (periods.size > 1) {
                periods.find { p ->
                    grunnlagListe.filtrerBasertPåEgenReferanser(
                        Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
                        p.grunnlagsreferanseListe,
                    ).ifEmpty {
                        grunnlagListe.filtrerBasertPåEgenReferanser(
                            Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG_ALDERSJUSTERING,
                            p.grunnlagsreferanseListe,
                        )
                    }.isNotEmpty()
                } ?: periods.first()
            } else {
                periods.first()
            }
        }
        return BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = sorterOgJusterPerioder(perioderJustertForIndeksOgAldersjustering),
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

    private fun hentEtterfølgendeVedtakslisteFraVedtak(context: KlageOrkestratorContext): List<BeløpshistorikkPeriodeInternal> {
        val stønad = context.stønad
        val klageperiode = context.klageperiode
        val opphørsdato = context.opphørsdato
        val vedtaksliste = vedtakService.hentAlleVedtakForStønad(
            stønadsid = stønad,
            fraPeriode = klageperiode.til,
            ignorerVedtaksid = context.påklagetVedtakId,

        )
        val vedtakEtterPåklagetVedtak = vedtaksliste.sortedBy {
            it.vedtakstidspunkt
        }.filter {
            val sistePeriodeFom = it.stønadsendring.periodeListe.maxOf { it.periode.fom }
            sistePeriodeFom >= klageperiode.til && (opphørsdato == null || sistePeriodeFom.isBefore(opphørsdato))
        }
            .flatMap { v ->
                v.stønadsendring.periodeListe.map {
                    BeløpshistorikkPeriodeInternal(
                        periode = it.periode,
                        beløp = it.beløp,
                        vedtaksid = v.vedtaksid,
                        resultatkode = it.resultatkode,
                    )
                }
            }.fyllPåPerioderForAldersjusteringEllerIndeksregulering(
                klageperiode,
                beregnForPerioderEtterKlage = true,
                stønadsid = stønad,
                klageberegningResultat = context.klageresultat,
                beløpshistorikkFørPåklagetVedtak = context.beløpshistorikkFørPåklagetVedtak,
            )
        return vedtakEtterPåklagetVedtak
    }

    private fun utførIndeksregulering(
        stønad: Stønadsid,
        historikk: List<BeregnetBarnebidragResultatInternal>,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        nesteIndeksår: Int,
    ): BeregnetBarnebidragResultatInternal? {
        val (_, grunnlagsliste, stønadDto) = klageOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )
        val førsteIndeksår = stønadDto.periodeListe.minOf { it.periode.fom.year } + 1
        val løpendeBeløp = stønadDto.periodeListe
            .firstOrNull { it.periode.inneholder(YearMonth.of(nesteIndeksår, 7)) }
        if (førsteIndeksår > nesteIndeksår || løpendeBeløp?.beløp == BigDecimal.ZERO) return null
        val indeksresultat = indeksreguleringOrkestrator.utførIndeksreguleringBarnebidrag(
            indeksreguleresForÅr = Year.of(nesteIndeksår),
            stønad = stønad,
            grunnlagListe = grunnlagsliste,
        )

        val sluttberegning = indeksresultat.filtrerOgKonverterBasertPåEgenReferanse<SluttberegningIndeksregulering>(
            Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
        ).first()
        return BeregnetBarnebidragResultatInternal(
            BeregnetBarnebidragResultat(
                grunnlagListe = (indeksresultat + grunnlagsliste).toSet().toList(),
                beregnetBarnebidragPeriodeListe = listOf(
                    ResultatPeriode(
                        periode = sluttberegning.innhold.periode,
                        resultat = ResultatBeregning(sluttberegning.innhold.beløp.verdi),
                        grunnlagsreferanseListe = listOf(sluttberegning.referanse),
                    ),
                ),
            ),
            beregnet = true,
            vedtakstype = Vedtakstype.INDEKSREGULERING,
            beregnetFraDato = LocalDate.of(nesteIndeksår, 7, 1),
        )
    }

    private fun utførAldersjustering(
        beregnBasertPåVedtak: BeregnBasertPåVedtak?,
        aldersjusteresForÅr: Int,
        historikk: MutableList<BeregnetBarnebidragResultatInternal>,
        klageOrkestratorGrunnlag: KlageOrkestratorGrunnlag,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
        personobjekter: List<GrunnlagDto> = emptyList(),
    ): BeregnetBarnebidragResultatInternal {
        val (stønad) = klageOrkestratorGrunnlag
        val (_, _, stønadDto) = klageOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørPåklagetVedtak = beløpshistorikkFørPåklagetVedtak,
        )

        val søknadsbarn =
            beregnBasertPåVedtak?.vedtakDto?.grunnlagListe?.søknadsbarn?.firstOrNull()?.tilDto()
                ?: klageOrkestratorHelpers.opprettPersonGrunnlag(
                    stønad.kravhaver,
                    Rolletype.BARN,
                )

        val manuellAldersjustering = klageOrkestratorGrunnlag.manuellAldersjustering.find { it.aldersjusteringForÅr == aldersjusteresForÅr }
        try {
            val aldersjustering = aldersjusteringOrchestrator.utførAldersjustering(
                stønad,
                aldersjusteresForÅr,
                (
                    manuellAldersjustering?.grunnlagFraVedtak?.let {
                        BeregnBasertPåVedtak(
                            it,
                        )
                    } ?: beregnBasertPåVedtak
                    )?.takeIf { it.vedtaksid != null || it.vedtakDto != null },
                beløpshistorikkStønad = stønadDto,
                personobjekter = personobjekter,
            )
            val aldersjusteringGrunnlag = opprettAldersjusteringDetaljerGrunnlag(
                søknadsbarnReferanse = søknadsbarn.referanse,
                aldersjusteresForÅr = aldersjusteresForÅr,
                aldersjusteresManuelt = false,
                aldersjustert = true,
                stønad = stønad,
            )
            return BeregnetBarnebidragResultatInternal(
                resultat = BeregnetBarnebidragResultat(
                    beregnetBarnebidragPeriodeListe = aldersjustering.beregning.beregnetBarnebidragPeriodeListe,
                    grunnlagListe = aldersjustering.beregning.grunnlagListe + listOf(aldersjusteringGrunnlag),
                ),
                beregnet = true,
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
            return BeregnetBarnebidragResultatInternal(
                resultat = BeregnetBarnebidragResultat(
                    grunnlagListe = listOf(søknadsbarn, aldersjusteringGrunnlag),
                    beregnetBarnebidragPeriodeListe = emptyList(),
                ),
                vedtakstype = Vedtakstype.ALDERSJUSTERING,
                beregnet = true,
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
//                    aldersjusteresManuelt = !listOf(
//                        SkalAldersjusteresManueltBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_HAR_RESULTAT_DELT_BOSTED_MED_BELØP_0,
//                    ).contains(e.begrunnelse),
                    vedtaksidBeregning = null,
                    begrunnelser = listOf(e.begrunnelse.name),
                )
            return BeregnetBarnebidragResultatInternal(
                resultat = BeregnetBarnebidragResultat(
                    grunnlagListe = listOf(søknadsbarn, aldersjusteringGrunnlag),
                    beregnetBarnebidragPeriodeListe = listOf(
                        // Lag dummy periode for at det skal vises som en midlertidlig periode i endelig vedtaket
                        ResultatPeriode(
                            ÅrMånedsperiode(YearMonth.of(aldersjusteresForÅr, 7), null),
                            ResultatBeregning(beløp = null),
                            emptyList(),
                        ),
                    ),
                ),
                beregnet = true,
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
}
