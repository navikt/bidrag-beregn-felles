package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.PrivatAvtaleIndeksregulertPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.delberegningEndringSjekkGrensePeriode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.erOverMinimumsgrenseForEndring
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrenseService.delberegningEndringSjekkGrense
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils.opprettAldersjusteringDetaljerGrunnlag
import no.nav.bidrag.beregn.barnebidrag.utils.OmgjøringOrkestratorHelpers
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.barnebidrag.utils.opprettStønad
import no.nav.bidrag.beregn.barnebidrag.utils.tilDto
import no.nav.bidrag.beregn.barnebidrag.utils.toYearMonth
import no.nav.bidrag.beregn.barnebidrag.utils.vedtaksidAutomatiskJobb
import no.nav.bidrag.beregn.barnebidrag.utils.vedtaksidBeregnetBeløpshistorikk
import no.nav.bidrag.beregn.barnebidrag.utils.vedtaksidPrivatavtale
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
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.indeksregulering.bo.BeregnIndeksreguleringGrunnlag
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.OmgjøringOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.AldersjusteringDetaljerGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
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
import no.nav.bidrag.transport.behandling.vedtak.response.erOrkestrertVedtak
import no.nav.bidrag.transport.behandling.vedtak.response.finnAldersjusteringDetaljerGrunnlag
import no.nav.bidrag.transport.behandling.vedtak.response.finnResultatFraAnnenVedtak
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
val List<ResultatVedtak>.omgjøringsvedtaksErEnesteVedtak get() =
    filter { !it.endeligVedtak }.all { it.omgjøringsvedtak }
internal data class OmgjøringeOrkestratorContext(
    val stønad: Stønadsid,
    val omgjøringsperiode: ÅrMånedsperiode,
    val løpendeStønad: StønadDto,
    val omgjørVedtakVirkningstidspunkt: YearMonth,
    val omgjøringsresultat: BeregnetBarnebidragResultat,
    val beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
    val omgjøringOrkestratorGrunnlag: OmgjøringOrkestratorGrunnlag,
    val opphørsdato: YearMonth?,
    val nyVirkningErEtterOpprinneligVirkning: Boolean,
    val vedtakslisteRelatertTilOmgjortVedtak: Set<Int>,
    val omgjørVedtak: VedtakDto,
) {
    val erBeregningsperiodeLøpende get() = omgjøringOrkestratorGrunnlag.erBeregningsperiodeLøpende
    val omgjørVedtakId: Int = omgjøringOrkestratorGrunnlag.omgjørVedtakId
    val omgjøringsvedtakVedtaktstype get() = if (omgjøringOrkestratorGrunnlag.gjelderKlage) Vedtakstype.KLAGE else Vedtakstype.ENDRING
}
internal data class BeregnetBarnebidragResultatInternal(
    val resultat: BeregnetBarnebidragResultat,
    val vedtakstype: Vedtakstype,
    val beregnetFraDato: LocalDate,
    val omgjøringsvedtak: Boolean = false,
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
    val omgjøringsvedtak: Boolean = false,
)

data class EtterfølgendeVedtakSomOverlapper(val vedtaksid: Int, val virkningstidspunkt: YearMonth)
fun omgjøringFeilet(begrunnelse: String): Nothing = throw OmgjøringsberegningFeiletFunksjonelt(begrunnelse)
fun omgjøringFeiletTeknisk(begrunnelse: String, throwable: Throwable): Nothing = throw OmgjøringsberegningFeiletTeknisk(begrunnelse, throwable)
fun finnesEtterfølgendeVedtak(vedtak: List<EtterfølgendeVedtakSomOverlapper>): Nothing =
    throw FinnesEtterfølgendeVedtakMedVirkningstidspunktFørOmgjortVedtak(vedtak)

class OmgjøringsberegningFeiletFunksjonelt(feilmelding: String) : RuntimeException(feilmelding)
class OmgjøringsberegningFeiletTeknisk(feilmelding: String, throwable: Throwable) : RuntimeException(feilmelding, throwable)

class FinnesEtterfølgendeVedtakMedVirkningstidspunktFørOmgjortVedtak(val vedtak: List<EtterfølgendeVedtakSomOverlapper>) :
    RuntimeException("Det finnes etterfølgende vedtak $vedtak")

@Service
@Import(
    VedtakService::class,
    AldersjusteringOrchestrator::class,
    BeregnIndeksreguleringApi::class,
    IdentUtils::class,
    OmgjøringOrkestratorHelpers::class,
)
class OmgjøringOrkestrator(
    private val vedtakService: VedtakService,
    private val aldersjusteringOrchestrator: AldersjusteringOrchestrator,
    private val beregnIndeksreguleringApi: BeregnIndeksreguleringApi,
    private val omgjøringOrkestratorHelpers: OmgjøringOrkestratorHelpers,
) {

    fun utførOmgjøringEndelig(
        omgjøringResultat: BeregnetBarnebidragResultat,
        omgjøringGrunnlag: BeregnGrunnlag,
        omgjøringOrkestratorGrunnlag: OmgjøringOrkestratorGrunnlag,
    ): List<ResultatVedtak> {
        try {
            val stønad = omgjøringOrkestratorGrunnlag.stønad
            val omgjørVedtakId = omgjøringOrkestratorGrunnlag.omgjørVedtakId
            val omgjøringperiode = omgjøringGrunnlag.periode
            secureLogger.debug { "Komplett klageberegning kjøres for stønad $stønad og påklaget vedtak $omgjørVedtakId" }

            val omgjørVedtak = vedtakService.hentVedtak(omgjørVedtakId)
                ?: omgjøringFeilet("Fant ikke omgjort vedtak med id $omgjørVedtakId")
            val omgjørVedtakVirkningstidspunkt = omgjørVedtak.virkningstidspunkt?.toYearMonth()
                ?: omgjøringFeilet("Omgjort vedtak med id $omgjørVedtakId har ikke virkningstidspunkt")
            val løpendeStønadGjeldende = vedtakService.hentLøpendeStønad(stønad) ?: run {
                if (!omgjøringOrkestratorGrunnlag.skalInnkreves) {
                    opprettStønad(stønad).copy(
                        førsteIndeksreguleringsår = null,
                        nesteIndeksreguleringsår = null,
                        innkreving = Innkrevingstype.UTEN_INNKREVING,
                        periodeListe = emptyList(),
                    )
                } else {
                    omgjøringFeilet("Fant ikke løpende stønad for $stønad")
                }
            }

            val grunnlagsliste = omgjøringGrunnlag.grunnlagListe

            if (!omgjøringOrkestratorGrunnlag.gjelderParagraf35c) {
                validerEtterfølgendeVedtakIkkeOverlapper(
                    stønad = stønad,
                    omgjørVedtak = omgjørVedtak,
                    omgjøringsperiode = omgjøringperiode,
                )
            }

            val personobjekter =
                listOf(
                    grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi) ?: omgjøringFeilet("Fant ikke søknadsbarn/kravhaver i grunnlaget"),
                    omgjøringGrunnlag.grunnlagListe.bidragsmottaker ?: omgjøringFeilet("Fant ikke bidragsmottaker i grunnlaget"),
                    omgjøringGrunnlag.grunnlagListe.bidragspliktig ?: omgjøringFeilet("Fant ikke bidragspliktig i grunnlaget"),
                ) as List<GrunnlagDto>

            val beløpshistorikkFørOmgjortVedtak = omgjøringOrkestratorHelpers.finnBeløpshistorikkFørOmgjøringsVedtak(
                omgjørVedtak,
                stønad,
                personobjekter,
                omgjøringGrunnlag,
                omgjørVedtakVirkningstidspunkt,

            )

            // TODO Sjekk om nytt virkningstidspunkt kan være tidligere enn originalt virkningstidspunkt
            val nyVirkningErEtterOpprinneligVirkning = omgjøringperiode.fom.isAfter(omgjørVedtakVirkningstidspunkt)

            val vedtakslisteRelatertTilOmgjortVedtak = vedtakService.hentAlleVedtaksiderRelatertTilOmgjortVedtak(stønad, omgjørVedtakId)
            val context = OmgjøringeOrkestratorContext(
                nyVirkningErEtterOpprinneligVirkning = nyVirkningErEtterOpprinneligVirkning,
                omgjøringsresultat = omgjøringResultat,
                omgjøringsperiode = omgjøringperiode,
                opphørsdato = omgjøringGrunnlag.opphørsdato,
                løpendeStønad = løpendeStønadGjeldende,
                omgjørVedtakVirkningstidspunkt = omgjørVedtakVirkningstidspunkt,
                omgjøringOrkestratorGrunnlag = omgjøringOrkestratorGrunnlag,
                beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtak,
                stønad = stønad,
                vedtakslisteRelatertTilOmgjortVedtak = vedtakslisteRelatertTilOmgjortVedtak,
                omgjørVedtak = omgjørVedtak,
            )

            val foreløpigVedtak = byggVedtak(context)

            val (_, _, _, beløpshistorikkFørOmgjortVedtakEtterOrkestrering) = omgjøringOrkestratorHelpers.byggBeløpshistorikk(
                foreløpigVedtak.map { it.resultat },
                stønad,
                omgjøringperiode.fom,
                beløpshistorikkFørOmgjortVedtak,
            )

            // Sjekk klageberegningen mot minimumsgrense for endring (aka 12%-regel)
            val omgjøringResultatEtterSjekkMinGrenseForEndring = sjekkMotMinimumsgrenseForEndring(
                omgjøringResultat = omgjøringResultat,
                omgjøringperiode = omgjøringperiode,
                beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtakEtterOrkestrering,
                stønadstype = stønad.type,
                omgjøringGrunnlag = omgjøringGrunnlag,
            )

            val resultat = byggVedtak(
                context.copy(
                    omgjøringsresultat = omgjøringResultatEtterSjekkMinGrenseForEndring,
                ),
            )
            return resultat.map {
                if (it.omgjøringsvedtak) {
                    it.copy(
                        resultat = it.resultat.copy(
                            grunnlagListe = (grunnlagsliste + omgjøringResultatEtterSjekkMinGrenseForEndring.grunnlagListe).toSet().toList(),
                        ),
                    )
                } else {
                    it
                }
            }.gjørOmTilÅpenPeriodeHvisEnesteVedtak()
        } catch (e: Exception) {
            if (e is FinnesEtterfølgendeVedtakMedVirkningstidspunktFørOmgjortVedtak || e is OmgjøringsberegningFeiletFunksjonelt) {
                throw e
            }
            omgjøringFeiletTeknisk("Feil under omgjøringsberegning: ${e.message}.", e)
        }
    }

    private fun List<ResultatVedtak>.gjørOmTilÅpenPeriodeHvisEnesteVedtak(): List<ResultatVedtak> = if (omgjøringsvedtaksErEnesteVedtak) {
        map {
            if (it.omgjøringsvedtak || it.endeligVedtak) {
                it.copy(
                    resultat = it.resultat.copy(
                        beregnetBarnebidragPeriodeListe = it.resultat.beregnetBarnebidragPeriodeListe.mapIndexed { i, resultatPeriode ->
                            if (i == it.resultat.beregnetBarnebidragPeriodeListe.size - 1) {
                                resultatPeriode.copy(
                                    periode = resultatPeriode.periode.copy(til = null),
                                )
                            } else {
                                resultatPeriode
                            }
                        },
                    ),
                )
            } else {
                it
            }
        }
    } else {
        this
    }

    private fun validerEtterfølgendeVedtakIkkeOverlapper(stønad: Stønadsid, omgjørVedtak: VedtakDto, omgjøringsperiode: ÅrMånedsperiode) {
        val omgjørVedtakVirkningstidspunkt = omgjørVedtak.virkningstidspunkt?.toYearMonth()
            ?: omgjøringFeilet("Omgjort vedtak med id ${omgjørVedtak.vedtaksid} har ikke virkningstidspunkt")
        val omgjørVedtakVedtakstidspunkt = omgjørVedtak.justerVedtakstidspunktVedtak().vedtakstidspunkt
        val vedtaksliste = vedtakService.hentAlleVedtakForStønad(
            stønadsid = stønad,
            fraPeriode = omgjørVedtakVirkningstidspunkt,
            ignorerVedtaksid = omgjørVedtak.vedtaksid,
        )

        val etterfølgendeVedtakMedPeriodeFørOmgjøringsperiode = vedtaksliste.filter {
            it.vedtakstidspunkt > omgjørVedtakVedtakstidspunkt &&
                !it.type.erIndeksEllerAldersjustering && it.stønadsendring.periodeListe.isNotEmpty()
        }
            .filter {
                val perioderFørOmgjøring = it.stønadsendring.periodeListe.filter { it.periode.fom < omgjøringsperiode.fom }
                perioderFørOmgjøring.any { it.periode.til == null || it.periode.til!! > omgjøringsperiode.fom }
            }
        if (etterfølgendeVedtakMedPeriodeFørOmgjøringsperiode.isNotEmpty()) {
            finnesEtterfølgendeVedtak(
                etterfølgendeVedtakMedPeriodeFørOmgjøringsperiode.map {
                    EtterfølgendeVedtakSomOverlapper(it.vedtaksid, it.virkningstidspunkt!!)
                },
            )
        }
    }

    private fun byggVedtak(context: OmgjøringeOrkestratorContext): List<ResultatVedtak> = when {
        // Scenario 1: Klagevedtak dekker opprinnelig beregningsperiode for det omgjort vedtaket - legg til evt etterfølgende vedtak og
        // kjør evt ny indeksregulering/aldersjustering
        !context.nyVirkningErEtterOpprinneligVirkning ->
            omgjøringScenarioVirkningFørEllerLikOpprinneligVirkning(context)

        // Scenario 2: Fra-perioden i omgjøringsvedtaket er flyttet fram ifht. omgjort vedtak. Perioder mellom forrige og nye virkningstidspunkt må fylles ut med opphør eller gjenopprettet beløpshistorikk
        // legg til evt etterfølgende vedtak for perioder etter beregningsperioden og kjør evt ny indeksregulering/aldersjustering
        context.nyVirkningErEtterOpprinneligVirkning ->
            omgjøringScenarioVirkningEtterOpprinneligVirkning(context)

        else -> emptyList()
    }

    private fun omgjøringScenarioVirkningFørEllerLikOpprinneligVirkning(context: OmgjøringeOrkestratorContext): List<ResultatVedtak> {
        val (
            _,
            omgjøringperiode,
            _,
            omgjørVedtakVirkningstidspunkt,
            omgjøringResultat,
            beløpshistorikkFørOmgjortVedtak,
        ) = context
        val etterfølgendeVedtakListe =
            hentEtterfølgendeVedtakslisteFraVedtak(context)
        val periodeSomSkalOpphøres =
            finnPeriodeSomSkalOpphøres(
                omgjøringperiode,
                omgjørVedtakVirkningstidspunkt,
                beløpshistorikkFørOmgjortVedtak,
                context.omgjøringOrkestratorGrunnlag.skalInnkreves,
            )

        val delvedtakListe = buildList {
            periodeSomSkalOpphøres?.let {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(it),
                        delvedtak = true,
                        omgjøringsvedtak = false,
                        beregnet = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            add(
                ResultatVedtak(
                    resultat = omgjøringResultat,
                    delvedtak = true,
                    beregnet = true,
                    omgjøringsvedtak = true,
                    vedtakstype = context.omgjøringsvedtakVedtaktstype,
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
                            omgjøringsvedtak = false,
                            vedtakstype = it.vedtakstype,
                            beregnet = it.beregnet,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }

        val sammenslåttVedtak =
            ResultatVedtak(
                resultat = slåSammenVedtak(delvedtakListe, context.opphørsdato, context.erBeregningsperiodeLøpende),
                delvedtak = false,
                omgjøringsvedtak = false,
                vedtakstype = context.omgjøringsvedtakVedtaktstype,
            )

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
    }

    // Scenario 3: Fra-perioden i klagevedtaket er flyttet fram ifht. påklaget vedtak. Til-perioden i klagevedtaket er lik inneværende
    // periode. Det eksisterer ingen vedtak før påklaget vedtak. Perioden fra opprinnelig vedtakstidspunkt til ny fra-periode må nulles ut.
    private fun omgjøringScenarioVirkningEtterOpprinneligVirkning(context: OmgjøringeOrkestratorContext): List<ResultatVedtak> {
        val vedtakIderMellomOmgjortVirkningOgNyVirkning =
            finnVedtakIderMellomOmgjortVirkningOgNyVirkning(context)
        val periodeSomSkalOpphøres = finnPeriodeSomSkalOpphøres(
            context.omgjøringsperiode,
            context.omgjørVedtakVirkningstidspunkt,
            context.beløpshistorikkFørOmgjortVedtak,
            context.omgjøringOrkestratorGrunnlag.skalInnkreves,
        )

        val delvedtakListeFør = buildList {
            if (periodeSomSkalOpphøres != null) {
                add(
                    ResultatVedtak(
                        resultat = lagOpphørsvedtak(periodeSomSkalOpphøres),
                        delvedtak = true,
                        omgjøringsvedtak = false,
                        beregnet = false,
                        vedtakstype = Vedtakstype.OPPHØR,
                    ),
                )
            }

            addAll(
                opprettDelvedtakFraVedtakslisten(
                    context,
                    beløpshistorikk = vedtakIderMellomOmgjortVirkningOgNyVirkning,
                    delvedtak = this,
                    gjenopprettetBeløpshistorikk = true,

                )
                    .map {
                        ResultatVedtak(
                            resultat = it.resultat,
                            delvedtak = true,
                            omgjøringsvedtak = false,
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
                    resultat = context.omgjøringsresultat,
                    delvedtak = true,
                    beregnet = true,
                    omgjøringsvedtak = true,
                    vedtakstype = context.omgjøringsvedtakVedtaktstype,
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
                            omgjøringsvedtak = false,
                            beregnet = it.beregnet,
                            vedtakstype = it.vedtakstype,
                            beregnetFraDato = it.beregnetFraDato,
                        )
                    },
            )
        }
        val sammenslåttVedtak =
            ResultatVedtak(
                resultat = slåSammenVedtak(delvedtakListe, context.opphørsdato, context.erBeregningsperiodeLøpende),
                delvedtak = false,
                omgjøringsvedtak = false,
                vedtakstype = context.omgjøringsvedtakVedtaktstype,
            )

        return (delvedtakListe + sammenslåttVedtak).sorterListe()
    }

    // Lager BeregnetBarnebidragResultat (simulert resultat fra beregningen) for alle (eksisterende) vedtak i vedtakListe
    private fun opprettDelvedtakFraVedtakslisten(
        context: OmgjøringeOrkestratorContext,
        beløpshistorikk: List<BeløpshistorikkPeriodeInternal>,
        delvedtak: List<ResultatVedtak>,
        gjenopprettetBeløpshistorikk: Boolean = false,
    ): List<BeregnetBarnebidragResultatInternal> {
        val stønad = context.stønad
        val historikk = delvedtak.map {
            BeregnetBarnebidragResultatInternal(it.resultat, it.vedtakstype, it.beregnetFraDato, it.omgjøringsvedtak)
        }.toMutableList()
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultatInternal>()
        beløpshistorikk.groupBy { it.vedtaksid }.entries.sortedWith(compareBy(nullsLast()) { it.key }).forEach { (vedtaksid, it) ->
            val komplettVedtak = vedtaksid?.let { vedtakService.hentVedtak(vedtaksid) }
            val forrigeVedtakErKlagevedtak = historikk.maxByOrNull { it.beregnetFraDato }?.omgjøringsvedtak == true
            val forrigeVedtakErBeregnet = historikk.maxByOrNull { it.beregnetFraDato }?.beregnet == true
            val erAldersjusteringBasertPåOmgjortVedtak = if (komplettVedtak?.type == Vedtakstype.ALDERSJUSTERING) {
                val aldersjusteringGrunnlag = komplettVedtak.finnAldersjusteringDetaljerGrunnlag(komplettVedtak.finnStønadsendring(stønad)!!)
                val grunnlagFraVedtak = aldersjusteringGrunnlag?.innhold?.grunnlagFraVedtak
                grunnlagFraVedtak != null && context.vedtakslisteRelatertTilOmgjortVedtak.contains(grunnlagFraVedtak)
            } else {
                false
            }
            val resultat = when {
                vedtaksid == null && it.any { it.aldersjuster } -> {
                    it.filter { it.aldersjuster }.forEach { periode ->
                        utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                            historikk,
                            periode.periode.fom.year,
                            periode.indeksreguler,
                            context,
                        )?.let {
                            beregnetBarnebidragResultatListe.add(it)
                            historikk.add(it)
                        }
                    }
                    null
                }

                komplettVedtak != null && komplettVedtak.erAldersjustering() &&
                    (forrigeVedtakErKlagevedtak || erAldersjusteringBasertPåOmgjortVedtak) &&
                    historikk.isNotEmpty() && !gjenopprettetBeløpshistorikk -> {
                    utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                        historikk,
                        komplettVedtak.vedtakstidspunkt!!.year,
                        true,
                        context,
                    )
                }

                komplettVedtak != null && komplettVedtak.erIndeksregulering() && historikk.isNotEmpty() &&
                    !gjenopprettetBeløpshistorikk &&
                    (forrigeVedtakErKlagevedtak || forrigeVedtakErBeregnet) ->
                    utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
                        historikk,
                        komplettVedtak.vedtakstidspunkt!!.year,
                        true,
                        context,
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
                                            vedtakstype = komplettVedtak.type,
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
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        opphørsdato: YearMonth?,
        omgjørVedtakVedtakstidspunkt: LocalDateTime,
        skalInnkreves: Boolean,
    ): Int {
        val førsteVedtak = filter {
            skalInnkreves || !it.omgjøringsvedtak
        }.groupBy { it.vedtaksid }.minBy { (_, perioder) -> perioder.minOf { it.periode.fom } }.value
        val sistePeriode = førsteVedtak.maxBy { it.periode.til?.year ?: it.periode.fom.year }
        val årstallSistePeriode = if (sistePeriode.omgjøringsvedtak) {
            omgjørVedtakVedtakstidspunkt.year
        } else if (sistePeriode.periode.til != null &&
            opphørsdato == sistePeriode.periode.til
        ) {
            sistePeriode.periode.fom.year
        } else {
            sistePeriode.periode.til?.year ?: sistePeriode.periode.fom.year
        }

        if (sistePeriode.vedtaksid == null && sistePeriode.resultatkode == null ||
            sistePeriode.vedtaksid == null
        ) {
            val periodErIngenEndringUnderGrense = sistePeriode.resultatkode != null &&
                Resultatkode.fraKode(sistePeriode.resultatkode) == Resultatkode.INGEN_ENDRING_UNDER_GRENSE
            if (periodErIngenEndringUnderGrense) {
                val sistePeriodeFraHistorikk = beløpshistorikkFørOmgjortVedtak.beløpshistorikk.maxBy { it.periode.til?.year ?: it.periode.fom.year }
                val indeksårFraVedtak =
                    sistePeriodeFraHistorikk.vedtaksid?.let { vedtakService.hentVedtak(it)?.finnStønadsendring(stønadsid)?.førsteIndeksreguleringsår }
                        ?: beløpshistorikkFørOmgjortVedtak.nesteIndeksreguleringsår
                return indeksårFraVedtak ?: (årstallSistePeriode + 1)
            }
            return årstallSistePeriode + 1
        }
        val vedtak = vedtakService.hentVedtak(sistePeriode.vedtaksid)!!
        return vedtak.finnStønadsendring(stønadsid)?.førsteIndeksreguleringsår ?: (årstallSistePeriode + 1)
    }

    private fun List<BeløpshistorikkPeriodeInternal>.fyllPåPerioderForAldersjusteringEllerIndeksregulering(
        omgjøringsperiode: ÅrMånedsperiode,
        omgjørVedtakVirkningstidspunkt: YearMonth? = null,
        beregnForPerioderEtterKlage: Boolean = false,
        omgjøringResultat: BeregnetBarnebidragResultat,
        stønadsid: Stønadsid,
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        opphørsdato: YearMonth?,
        omgjørVedtakVedtakstidspunkt: LocalDateTime,
        skalInnkreves: Boolean,
    ): List<BeløpshistorikkPeriodeInternal> {
        if (!beregnForPerioderEtterKlage && (this.isEmpty() || !skalInnkreves)) return emptyList()

        val beløshistorikkKlage = if (beregnForPerioderEtterKlage) {
            omgjøringResultat.beregnetBarnebidragPeriodeListe.map {
                val søknadsbarn = omgjøringResultat.grunnlagListe.hentPersonMedIdent(stønadsid.kravhaver.verdi)!!
                val erResultatIngenEndring = omgjøringResultat.grunnlagListe.erResultatEndringUnderGrense(søknadsbarn.referanse)
                BeløpshistorikkPeriodeInternal(
                    it.periode,
                    it.resultat.beløp,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    omgjøringsvedtak = true,
                )
            }
        } else {
            listOf(
                BeløpshistorikkPeriodeInternal(
                    omgjørVedtakVirkningstidspunkt?.let { ÅrMånedsperiode(omgjørVedtakVirkningstidspunkt, omgjøringsperiode.fom) }
                        ?: omgjøringsperiode,
                    BigDecimal.ZERO,
                    omgjøringsvedtak = true,
                ),
            )
        }

        val beløshistorikkMedKlage = (this + beløshistorikkKlage).sortedBy { it.periode.fom }

        val mutableList = beløshistorikkMedKlage.toMutableList()
        val førsteIndeksår = beløshistorikkMedKlage.finnFørsteIndeksår(
            stønadsid,
            beløpshistorikkFørOmgjortVedtak,
            opphørsdato,
            omgjørVedtakVedtakstidspunkt,
            skalInnkreves,
        )
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
                    (!it.omgjøringsvedtak || it.resultatkode != Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name)
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

    private fun finnVedtakIderMellomOmgjortVirkningOgNyVirkning(context: OmgjøringeOrkestratorContext): List<BeløpshistorikkPeriodeInternal> {
        val (
            stønad,
            omgjøringsperiode,
            løpendeStønad,
            omgjørVedtakVirkningstidspunkt,
            omgjøringResultat,
            beløpshistorikkFørOmgjortVedtak,
        ) = context
        val nyBeløpshistorikk = if (omgjøringsperiode.fom > omgjørVedtakVirkningstidspunkt) {
            hentHistorikkMellomOpprinneligOgNyVirkning(
                beløpshistorikkFørOmgjortVedtak,
                omgjørVedtakVirkningstidspunkt,
                omgjøringsperiode,
            )
        } else if (løpendeStønad.periodeListe.isEmpty()) {
            emptyList()
        } else {
            løpendeStønad.periodeListe
                .filter {
                    it.vedtaksid != context.omgjørVedtakId &&
                        it.periode.fom >= omgjørVedtakVirkningstidspunkt
                    it.periode.til != null && it.periode.til!!.isBefore(omgjøringsperiode.til!!)
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
            omgjøringsperiode,
            omgjørVedtakVirkningstidspunkt,
            stønadsid = stønad,
            omgjøringResultat = omgjøringResultat,
            beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtak,
            opphørsdato = context.opphørsdato,
            omgjørVedtakVedtakstidspunkt = context.omgjørVedtak.vedtakstidspunkt!!,
            skalInnkreves = context.omgjøringOrkestratorGrunnlag.skalInnkreves,
        )
    }

    private fun hentHistorikkMellomOpprinneligOgNyVirkning(
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        omgjørVedtakVirkningstidspunkt: YearMonth,
        omgjøringsperiode: ÅrMånedsperiode,
    ): List<BeløpshistorikkPeriodeInternal> {
        val vedtakMellom = beløpshistorikkFørOmgjortVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(omgjøringsperiode.fom) &&
                (
                    it.periode.fom.isAfter(omgjørVedtakVirkningstidspunkt) ||
                        it.periode.til != null && it.periode.til!!.isAfter(omgjørVedtakVirkningstidspunkt)
                    )
        }.map {
            BeløpshistorikkPeriodeInternal(
                periode = ÅrMånedsperiode(maxOf(omgjørVedtakVirkningstidspunkt, it.periode.fom), it.periode.til),
                beløp = it.beløp,
                vedtaksid = it.vedtaksid,

            )
        }.distinct()
            .ifEmpty {
                beløpshistorikkFørOmgjortVedtak.beløpshistorikk
                    .filter {
                        it.periode.fom.isBefore(omgjøringsperiode.fom) &&
                            (it.periode.til == null || it.periode.til!!.isAfter(omgjørVedtakVirkningstidspunkt))
                    }
                    .maxByOrNull { it.periode.fom }?.let { listOf(it) }?.map {
                        BeløpshistorikkPeriodeInternal(
                            periode = ÅrMånedsperiode(maxOf(omgjørVedtakVirkningstidspunkt, it.periode.fom), it.periode.til),
                            beløp = it.beløp,
                            vedtaksid = it.vedtaksid,
                        )
                    } ?: emptyList()
            }

        return vedtakMellom.mapIndexed { index, periode ->
            if (index == vedtakMellom.lastIndex && (periode.periode.til != null && periode.periode.til!! > omgjøringsperiode.fom)) {
                periode.copy(periode = periode.periode.copy(til = omgjøringsperiode.fom))
            } else {
                periode
            }
        }
    }

    private fun finnPeriodeSomSkalOpphøres(
        omgjøringsperiode: ÅrMånedsperiode,
        omgjørVedtakVirkningstidspunkt: YearMonth,
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        skalInnkreves: Boolean = true,
    ): ÅrMånedsperiode? {
        if (!skalInnkreves && beløpshistorikkFørOmgjortVedtak.beløpshistorikk.isEmpty()) return null

        val vedtakMellomOmgjortVirkningOgNyVirkning = beløpshistorikkFørOmgjortVedtak.beløpshistorikk.filter {
            it.periode.fom.isBefore(omgjøringsperiode.fom) && it.vedtaksid != null
        }

        val tidligstePeriodeFom = vedtakMellomOmgjortVirkningOgNyVirkning.minOfOrNull { it.periode.fom }
        val sistePeriodeFørVedtak = vedtakMellomOmgjortVirkningOgNyVirkning.maxOfOrNull { it.periode.tilEllerMax() }
            ?.takeIf { it != YearMonth.from(LocalDate.MAX) }

        // Ingen vedtak før omgjort vedtak
        return if (tidligstePeriodeFom == null && omgjørVedtakVirkningstidspunkt < omgjøringsperiode.fom) {
            ÅrMånedsperiode(
                fom = YearMonth.of(omgjørVedtakVirkningstidspunkt.year, omgjørVedtakVirkningstidspunkt.monthValue),
                til = omgjøringsperiode.fom,
            )
            // Finnes vedtak før omgjort vedtak som kommer i mellom forrige og nye virkningstidspunkt
        } else if (tidligstePeriodeFom != null && tidligstePeriodeFom > omgjørVedtakVirkningstidspunkt) {
            ÅrMånedsperiode(
                fom = YearMonth.of(omgjørVedtakVirkningstidspunkt.year, omgjørVedtakVirkningstidspunkt.monthValue),
                til = tidligstePeriodeFom,
            )
            // Det finnes beløpshistorikk før omgjort vedtak men at vedtak før er opphør før påklaget vedtak
        } else if (sistePeriodeFørVedtak != null && sistePeriodeFørVedtak < omgjørVedtakVirkningstidspunkt &&
            omgjørVedtakVirkningstidspunkt < omgjøringsperiode.fom
        ) {
            ÅrMånedsperiode(
                fom = YearMonth.of(omgjørVedtakVirkningstidspunkt.year, omgjørVedtakVirkningstidspunkt.monthValue),
                til = omgjøringsperiode.fom,
            )
        } else {
            null
        }
    }

    private fun utførAldersjusteringEllerIndeksreguleringHvisNødvendig(
        historikk: MutableList<BeregnetBarnebidragResultatInternal>,
        aldersjusteresIndeksreguleresForÅr: Int,
        indeksreguler: Boolean,
        context: OmgjøringeOrkestratorContext,
    ): BeregnetBarnebidragResultatInternal? {
        val stønad = context.stønad
        val omgjøringsvedtak = context.omgjøringsresultat.tilVedtakDto(stønad, context.omgjøringsvedtakVedtaktstype)
        val omgjøringOrkestratorGrunnlag = context.omgjøringOrkestratorGrunnlag
        val beløpshistorikkFørOmgjortVedtak = context.beløpshistorikkFørOmgjortVedtak
        val (_, _, stønadDto) = omgjøringOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtak,
        )
        val periodeBeregning = YearMonth.of(aldersjusteresIndeksreguleresForÅr, 7)
        val løpendePeriode = stønadDto.periodeListe.hentSisteLøpendePeriode(periodeBeregning) ?: return null
        val erKlagevedtak = omgjøringsvedtak.finnStønadsendring(stønad)!!.periodeListe.any { it.periode.fom == løpendePeriode.periode.fom }

        val opphørsdato = finnOmgjøringOpphørsdato(omgjøringsvedtak, stønad, context.opphørsdato)
        // Ikke indeksreguler eller aldersjuster hvis klageberegningen opphører før beregningsperioden
        if (opphørsdato != null && opphørsdato <= periodeBeregning) return null

        val omgjøringsvedtakResultat = historikk.find { it.omgjøringsvedtak }
        val manuellAldersjustering = omgjøringOrkestratorGrunnlag.manuellAldersjustering.find {
            it.aldersjusteringForÅr ==
                aldersjusteresIndeksreguleresForÅr
        }

        val vedtakFraGrunnlag = if (manuellAldersjustering?.grunnlagFraVedtak != null && !manuellAldersjustering.grunnlagFraOmgjøringsvedtak) {
            BeregnBasertPåVedtak(
                manuellAldersjustering.grunnlagFraVedtak,
            )
        } else if (erKlagevedtak || manuellAldersjustering != null && manuellAldersjustering.grunnlagFraOmgjøringsvedtak) {
            BeregnBasertPåVedtak(vedtakDto = omgjøringsvedtak)
        } else {
            val omgjøringBeregnFraDato = omgjøringsvedtakResultat?.beregnetFraDato?.toYearMonth()
            val historikkUtenAutojobb = stønadDto.periodeListe.filter {
                (it.vedtaksid != vedtaksidBeregnetBeløpshistorikk || it.periode.fom == omgjøringBeregnFraDato) &&
                    it.vedtaksid != vedtaksidAutomatiskJobb && it.vedtaksid != vedtaksidPrivatavtale &&
                    it.resultatkode != Resultatkode.INDEKSREGULERING.name
            }
            val sistePeriode = historikkUtenAutojobb.maxByOrNull { it.periode.fom }
            val erSistePeriodeKlagevedtak = sistePeriode?.periode?.fom == omgjøringBeregnFraDato
            BeregnBasertPåVedtak(
                if (!erSistePeriodeKlagevedtak) sistePeriode?.vedtaksid else null,
                if (erSistePeriodeKlagevedtak) omgjøringsvedtak else null,
            )
        }
        // Skal ikke indeksregulere eller aldersjustere hvis påklaget vedtak er uten innkreving
        if (!omgjøringOrkestratorGrunnlag.skalInnkreves && erKlagevedtak) return null
        val resultatAldersjustering =
            utførAldersjustering(
                vedtakFraGrunnlag,
                aldersjusteresIndeksreguleresForÅr,
                historikk,
                omgjøringOrkestratorGrunnlag,
                beløpshistorikkFørOmgjortVedtak,
                opphørsdato = opphørsdato,
            )
        val detaljer = resultatAldersjustering.resultat.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<AldersjusteringDetaljerGrunnlag>(
            Grunnlagstype.ALDERSJUSTERING_DETALJER,
        ).first()
        return if (!detaljer.innhold.aldersjustert && (manuellAldersjustering != null || !detaljer.innhold.aldersjusteresManuelt)) {
            secureLogger.warn {
                "Aldersjustering ble ikke utført for år $aldersjusteresIndeksreguleresForÅr med begrunnelse ${detaljer.innhold.begrunnelserVisningsnavn}"
            }
            if (indeksreguler) {
                utførIndeksregulering(
                    stønad,
                    historikk,
                    beløpshistorikkFørOmgjortVedtak,
                    aldersjusteresIndeksreguleresForÅr,
                    opphørsdato,
                )
            } else {
                null
            }
        } else {
            resultatAldersjustering
        }
    }
    private fun finnOmgjøringOpphørsdato(omgjøringsvedtak: VedtakDto, stønad: Stønadsid, opphørsdato: YearMonth?): YearMonth? {
        val omgjøringsvedtakSistePeriode = omgjøringsvedtak.finnStønadsendring(stønad)!!.periodeListe.maxBy { it.periode.fom }
        val erOmgjøringsvedtakOpphør = omgjøringsvedtakSistePeriode.beløp == null
        // Hvis siste periode i klagevedtaker er opphør pga at barnet er selvforsørget eller at barnet bor hos BP
        // så regnes det også som opphør da skal det ikke indeksreguleres/aldersjusteres etter det. Men det kan også eksplisitt
        // settes opphørsdato i virkningstidspunkt bildet som opphører bidraget og da skal det heller ikke indeksreguleres/aldersjusters
        return if (erOmgjøringsvedtakOpphør) omgjøringsvedtakSistePeriode.periode.fom else opphørsdato
    }

    // Slår sammen alle vedtak i en liste til ett (teknisk) vedtak
    private fun slåSammenVedtak(
        vedtakListe: List<ResultatVedtak>,
        opphørsdato: YearMonth?,
        erBeregningsperiodeLøpende: Boolean,
    ): BeregnetBarnebidragResultat {
        val resultatPeriodeListe = mutableListOf<ResultatPeriode>()
        val grunnlagListe = mutableListOf<GrunnlagDto>()
        vedtakListe.forEach {
            resultatPeriodeListe.addAll(it.resultat.beregnetBarnebidragPeriodeListe)
            grunnlagListe.addAll(it.resultat.grunnlagListe)
        }
        // Fjern perioder fra klagevedtaket som overlapper med indeksregulering eller aldersjustering.
        // Erstatter identiske perioder (feks periode som starter med 2025-07 med indeks/aldeersjustering
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
        }.leggTilOpphørsperiodeMellomOmgjøringOgEtterfølgendeVedtak(opphørsdato)
        return BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = sorterOgJusterPerioder(
                perioderJustertForIndeksOgAldersjustering,
                opphørsdato,
                erBeregningsperiodeLøpende,
            ),
            grunnlagListe = grunnlagListe.distinctBy { it.referanse },
        )
    }
    fun List<ResultatPeriode>.leggTilOpphørsperiodeMellomOmgjøringOgEtterfølgendeVedtak(opphørsdato: YearMonth?): List<ResultatPeriode> {
        if (opphørsdato == null) return this

        val nestePeriodeEtterOpphør = this.filter { it.periode.fom >= opphørsdato }.minByOrNull { it.periode.fom }

        return if (nestePeriodeEtterOpphør != null) {
            val opphørsPeriode = ResultatPeriode(
                periode = ÅrMånedsperiode(fom = opphørsdato, til = nestePeriodeEtterOpphør.periode.fom),
                resultat = ResultatBeregning(
                    beløp = null,
                ),
                grunnlagsreferanseListe = emptyList(),
            )
            (this + opphørsPeriode).sortedBy { it.periode.fom }
        } else {
            this
        }
    }

    // Sorterer ResultatPeriode basert på periode-fom og erstatter åpen sluttperiode med fom-dato på neste forekomst (hvis den finnes)
    private fun sorterOgJusterPerioder(
        perioder: List<ResultatPeriode>,
        opphørsdato: YearMonth?,
        erBeregningsperiodeLøpende: Boolean,
    ): List<ResultatPeriode> {
        val sortert = perioder.sortedBy { it.periode.fom }

        return sortert.mapIndexed { indeks, resultatPeriode ->
            val erSistePeriode = indeks == sortert.size - 1
            val nesteFom = if (!erSistePeriode) sortert.getOrNull(indeks + 1)?.periode?.fom else null
            val tilDato = when {
                erBeregningsperiodeLøpende && erSistePeriode && opphørsdato != null -> opphørsdato
                else -> nesteFom ?: resultatPeriode.periode.til
            }
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.fom, til = tilDato),
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
        omgjøringResultat: BeregnetBarnebidragResultat,
        omgjøringperiode: ÅrMånedsperiode,
        beløpshistorikkFørOmgjortVedtak: GrunnlagDto,
        stønadstype: Stønadstype,
        omgjøringGrunnlag: BeregnGrunnlag,
    ): BeregnetBarnebidragResultat {
        val (_, opphørsdato, _, søknadsbarnReferanse) = omgjøringGrunnlag
        val åpenSluttperiode = opphørsdato == null
        if (omgjøringResultat.erOpphør()) {
            return omgjøringResultat
        }

        val delberegningIndeksreguleringPrivatAvtalePeriodeResultat = omgjøringOrkestratorHelpers.utførDelberegningPrivatAvtalePeriode(
            omgjøringGrunnlag,
        )

        // Kaller delberegning for å sjekke om endring i bidrag er over grense (pr periode)
        val delberegningEndringSjekkGrensePeriodeResultat =
            delberegningEndringSjekkGrensePeriode(
                mottattGrunnlag = BeregnGrunnlag(
                    periode = omgjøringperiode,
                    opphørsdato = opphørsdato,
                    stønadstype = stønadstype,
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    grunnlagListe =
                    omgjøringResultat.grunnlagListe + beløpshistorikkFørOmgjortVedtak +
                        delberegningIndeksreguleringPrivatAvtalePeriodeResultat,
                ),
                åpenSluttperiode = åpenSluttperiode,
            )

        // Kaller delberegning for å sjekke om endring i bidrag er over grense (totalt)
        val delberegningEndringSjekkGrenseResultat = delberegningEndringSjekkGrense(
            mottattGrunnlag = BeregnGrunnlag(
                periode = omgjøringperiode,
                opphørsdato = opphørsdato,
                stønadstype = stønadstype,
                søknadsbarnReferanse = søknadsbarnReferanse,
                grunnlagListe = omgjøringResultat.grunnlagListe + delberegningEndringSjekkGrensePeriodeResultat,
            ),
            åpenSluttperiode = åpenSluttperiode,
        )

        val beregnetBidragErOverMinimumsgrenseForEndring = erOverMinimumsgrenseForEndring(delberegningEndringSjekkGrenseResultat)
        val grunnlagstype = when {
            stønadstype == Stønadstype.BIDRAG18AAR -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
            else -> Grunnlagstype.BELØPSHISTORIKK_BIDRAG
        }
        val resultatPeriodeListe = lagResultatPerioder(
            delberegningEndeligBidragPeriodeResultat = omgjøringResultat.grunnlagListe,
            beregnetBidragErOverMinimumsgrenseForEndring = beregnetBidragErOverMinimumsgrenseForEndring,
            beløpshistorikkGrunnlag = listOf(beløpshistorikkFørOmgjortVedtak),
            beløpshistorikkGrunnlagstype = grunnlagstype,
            delberegningEndringSjekkGrensePeriodeResultat = delberegningEndringSjekkGrensePeriodeResultat,
            delberegningIndeksreguleringPrivatAvtalePeriodeResultat = delberegningIndeksreguleringPrivatAvtalePeriodeResultat,
        )

        return BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = resultatPeriodeListe,
            grunnlagListe = (
                delberegningIndeksreguleringPrivatAvtalePeriodeResultat +
                    omgjøringResultat.grunnlagListe +
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

    private fun hentEtterfølgendeVedtakslisteFraVedtak(context: OmgjøringeOrkestratorContext): List<BeløpshistorikkPeriodeInternal> {
        val stønad = context.stønad
        val erLøpende = context.omgjøringOrkestratorGrunnlag.erBeregningsperiodeLøpende
        val omgjøringsperiode = context.omgjøringsperiode
        if (erLøpende) return emptyList()
        // Beregningsperiode er til inneværende måned
        val opphørsdato = context.opphørsdato
        val omgjørVedtakVedtakstidspunkt = context.omgjørVedtak.justerVedtakstidspunktVedtak().vedtakstidspunkt
        val vedtaksliste = vedtakService.hentAlleVedtakForStønad(
            stønadsid = stønad,
            fraPeriode = omgjøringsperiode.til,
            ignorerVedtaksid = context.omgjørVedtakId,

        )
        val vedtakEtterOmgjøringsVedtakFiltrert = vedtaksliste.filter { it.vedtakstidspunkt > omgjørVedtakVedtakstidspunkt }.sortedBy {
            it.vedtakstidspunkt
        }.filter { it.stønadsendring.beslutning != Beslutningstype.DELVEDTAK }.filter {
            val sistePeriodeFom = it.stønadsendring.periodeListe.maxOf { it.periode.fom }
            val førstePeriodeFom = it.stønadsendring.periodeListe.minOf { it.periode.fom }
            sistePeriodeFom >= omgjøringsperiode.til // &&
            // (opphørsdato == null || førstePeriodeFom.isBefore(opphørsdato))
        }
        val vedtakEtterOmgjøringsVedtak = vedtakEtterOmgjøringsVedtakFiltrert.flatMapIndexed { index, v ->
            val nextVedtak = vedtakEtterOmgjøringsVedtakFiltrert.getOrNull(index + 1)
            val nextVedtakEarliestPeriod = nextVedtak?.stønadsendring?.periodeListe?.minByOrNull { it.periode.fom }?.periode?.fom

            v.stønadsendring.periodeListe
                .filter {
                    (omgjøringsperiode.til == null || it.periode.fom >= omgjøringsperiode.til) // &&
                    // (opphørsdato == null || it.periode.fom.isBefore(opphørsdato))
                }
                .filter { nextVedtakEarliestPeriod == null || it.periode.fom.isBefore(nextVedtakEarliestPeriod) }
                .map {
                    hentFaktiskPeriode(v.vedtaksid, it, context.stønad)
                }
        }.fyllPåPerioderForAldersjusteringEllerIndeksregulering(
            omgjøringsperiode,
            beregnForPerioderEtterKlage = true,
            stønadsid = stønad,
            omgjøringResultat = context.omgjøringsresultat,
            beløpshistorikkFørOmgjortVedtak = context.beløpshistorikkFørOmgjortVedtak,
            opphørsdato = opphørsdato,
            omgjørVedtakVedtakstidspunkt = context.omgjørVedtak.vedtakstidspunkt!!,
            skalInnkreves = context.omgjøringOrkestratorGrunnlag.skalInnkreves,
        )
        return vedtakEtterOmgjøringsVedtak
    }
    private fun hentFaktiskPeriode(vedtakId: Int, periode: VedtakPeriodeDto, stønadsid: Stønadsid): BeløpshistorikkPeriodeInternal {
        val periodeInternal = BeløpshistorikkPeriodeInternal(
            periode = periode.periode,
            beløp = periode.beløp,
            vedtaksid = vedtakId,
            resultatkode = periode.resultatkode,
        )
        val vedtak = vedtakService.hentVedtak(vedtakId) ?: return periodeInternal
        return if (vedtak.erOrkestrertVedtak && vedtak.type == Vedtakstype.INNKREVING) {
            val resultatFraAnnenVedtak = vedtak.grunnlagListe.finnResultatFraAnnenVedtak(periode.grunnlagReferanseListe)!!
            hentFaktiskPeriode(resultatFraAnnenVedtak.vedtaksid!!, periode, stønadsid)
        } else if (vedtak.erOrkestrertVedtak) {
            val resultatFraAnnenVedtak = vedtak.grunnlagListe.finnResultatFraAnnenVedtak(periode.grunnlagReferanseListe) ?: run {
                val periodeFraVedtak = vedtakService.oppdaterIdenterStønadsendringer(vedtak).finnStønadsendring(stønadsid)!!
                    .periodeListe.find { periode.periode.inneholder(it.periode) }
                vedtak.grunnlagListe.finnResultatFraAnnenVedtak(periodeFraVedtak!!.grunnlagReferanseListe)!!
            }
            periodeInternal.copy(
                vedtaksid = resultatFraAnnenVedtak.vedtaksid,
                resultatkode = if (resultatFraAnnenVedtak.vedtaksid == null) Resultatkode.OPPHØR.name else periode.resultatkode,
            )
        } else {
            periodeInternal
        }
    }
    private fun utførIndeksregulering(
        stønad: Stønadsid,
        historikk: List<BeregnetBarnebidragResultatInternal>,
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        nesteIndeksår: Int,
        opphørsdato: YearMonth?,
    ): BeregnetBarnebidragResultatInternal? {
        val (_, grunnlagsliste, stønadDto) = omgjøringOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtak,
        )
        val førsteIndeksår = stønadDto.periodeListe.minOf { it.periode.fom.year } + 1
        val løpendeBeløp = stønadDto.periodeListe
            .firstOrNull { it.periode.inneholder(YearMonth.of(nesteIndeksår, 7)) }
        if (førsteIndeksår > nesteIndeksår || løpendeBeløp?.beløp == BigDecimal.ZERO) return null
        val indeksresultat = beregnIndeksreguleringApi.beregnIndeksregulering(
            BeregnIndeksreguleringGrunnlag(
                indeksregulerÅr = Year.of(nesteIndeksår),
                stønadsid = stønad,
                personobjektListe = grunnlagsliste,
                beløpshistorikkListe = grunnlagsliste,
                opphørsdato = opphørsdato,
            ),
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
        omgjøringOrkestratorGrunnlag: OmgjøringOrkestratorGrunnlag,
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
        personobjekter: List<GrunnlagDto> = emptyList(),
        opphørsdato: YearMonth? = null,
    ): BeregnetBarnebidragResultatInternal {
        val (stønad) = omgjøringOrkestratorGrunnlag
        val (_, _, stønadDto) = omgjøringOrkestratorHelpers.byggBeløpshistorikk(
            historikk.map { it.resultat },
            stønad,
            beløpshistorikkFørOmgjortVedtak = beløpshistorikkFørOmgjortVedtak,
        )

        val søknadsbarn =
            beregnBasertPåVedtak?.vedtakDto?.grunnlagListe?.søknadsbarn?.firstOrNull()?.tilDto()
                ?: omgjøringOrkestratorHelpers.opprettPersonGrunnlag(
                    stønad.kravhaver,
                    Rolletype.BARN,
                )

        try {
            val aldersjustering = aldersjusteringOrchestrator.utførAldersjustering(
                stønad,
                aldersjusteresForÅr,
                beregnBasertPåVedtak?.takeIf { it.vedtaksid != null || it.vedtakDto != null },
                opphørsdato = opphørsdato,
                beløpshistorikkStønad = stønadDto,
                personobjekter = personobjekter,
            )
            val aldersjusteringGrunnlag = opprettAldersjusteringDetaljerGrunnlag(
                søknadsbarnReferanse = søknadsbarn.referanse,
                aldersjusteresForÅr = aldersjusteresForÅr,
                aldersjusteresManuelt = false,
                aldersjustert = true,
                stønad = stønad,
                vedtaksidBeregning = beregnBasertPåVedtak?.vedtaksid,
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
                    vedtaksidBeregning = beregnBasertPåVedtak?.vedtaksid,
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
                    vedtaksidBeregning = beregnBasertPåVedtak?.vedtaksid,
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
    private fun BeregnetBarnebidragResultat.tilVedtakDto(stønad: Stønadsid, vedtakstype: Vedtakstype) = VedtakDto(
        type = vedtakstype,
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
