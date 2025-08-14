package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningSakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils.finnBarnAlder
import no.nav.bidrag.beregn.barnebidrag.utils.aldersjusteringAldersgrupper
import no.nav.bidrag.beregn.core.exception.AldersjusteringLavereEnnEllerLikLøpendeBidragException
import no.nav.bidrag.beregn.core.mapper.tilGrunnlag
import no.nav.bidrag.beregn.core.service.SisteManuelleVedtak
import no.nav.bidrag.beregn.core.service.VedtakService
import no.nav.bidrag.beregn.core.util.hentPersonForNyesteIdent
import no.nav.bidrag.beregn.core.util.hentSisteLøpendePeriode
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.tilBisysResultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sak.Sakskategori
import no.nav.bidrag.domene.enums.vedtak.VirkningstidspunktÅrsakstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.VirkningstidspunktGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.sak.BidragssakDto
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

private val log = KotlinLogging.logger {}

data class AldersjusteringResultat(
    val vedtaksid: Int,
    val løpendeBeløp: BigDecimal?,
    val beregning: BeregnetBarnebidragResultat,
    val resultat: String? = null,
)
enum class SkalIkkeAldersjusteresBegrunnelse {
    INGEN_LØPENDE_PERIODE,
    BIDRAGET_HAR_OPPHØRT,
    LØPENDE_PERIODE_FRA_OG_MED_DATO_ER_LIK_ELLER_ETTER_ALDERSJUSTERING,
    IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING,
    BARN_MANGLER_FØDSELSDATO,
    BIDRAG_LØPER_MED_UTENLANDSK_VALUTA,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_FOR_BARNETILLEGG_BM,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_FOR_BARNETILLEGG_BP,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_NED_TIL_EVNE,

    VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_NED_TIL_25_PROSENT_AV_INNTEKT,
    VEDTAK_GRUNNLAG_HENTES_FRA_INNEHOLDER_UNDERHOLDSKOSTNAD_MED_FORPLEINING,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_BEGRENSET_REVURDERING_JUSTERT_OPP_TIL_FORSKUDDSATS,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_SKJØNNSFASTSATT_AV_UTLAND,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_PRIVAT_AVTALE,
    ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
    SAKEN_TILHØRER_UTLAND,
}

enum class SkalAldersjusteresManueltBegrunnelse {
    FANT_INGEN_MANUELL_VEDTAK,
    VEDTAK_GRUNNLAG_HENTES_FRA_HAR_PERIODE_MED_FOM_DATO_ETTER_ALDERSJUSTERINGEN,
    VEDTAK_GRUNNLAG_HENTES_FRA_HAR_RESULTAT_DELT_BOSTED_MED_BELØP_0,
    VEDTAK_GRUNNLAG_HENTES_FRA_ER_INNVILGET_VEDTAK,
}

class SkalIkkeAldersjusteresException(
    vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse,
    val resultat: String? = null,
    val vedtaksid: Int? = null,
) : RuntimeException("Skal ikke aldersjusteres med begrunnelse ${begrunnelse.joinToString(",")}") {
    val begrunnelser: List<SkalIkkeAldersjusteresBegrunnelse> = begrunnelse.toList()
}

class AldersjusteresManueltException(begrunnelse: SkalAldersjusteresManueltBegrunnelse, val resultat: String? = null, val vedtaksid: Int? = null) :
    RuntimeException("Skal aldersjusteres manuelt med begrunnelse $begrunnelse") {
    val begrunnelse: SkalAldersjusteresManueltBegrunnelse = begrunnelse
}

fun aldersjusteringFeilet(begrunnelse: String): Nothing = throw RuntimeException(begrunnelse)

fun aldersjusteresManuelt(begrunnelse: SkalAldersjusteresManueltBegrunnelse, resultat: String? = null, vedtaksid: Int? = null): Nothing =
    throw AldersjusteresManueltException(begrunnelse, resultat = resultat, vedtaksid = vedtaksid)

fun skalIkkeAldersjusteres(vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse, resultat: String? = null, vedtaksid: Int? = null): Nothing =
    throw SkalIkkeAldersjusteresException(*begrunnelse, resultat = resultat, vedtaksid = vedtaksid)

data class BeregnBasertPåVedtak(val vedtaksid: Int? = null, val vedtakDto: VedtakDto? = null)

@Service
@Import(BeregnBarnebidragApi::class, VedtakService::class)
class AldersjusteringOrchestrator(
    private val vedtakService: VedtakService,
    private val sakConsumer: BeregningSakConsumer,
    private val barnebidragApi: BeregnBarnebidragApi,
    private val personConsumer: BeregningPersonConsumer,
    private val identUtils: IdentUtils,
) {
    fun utførAldersjustering(
        stønad: Stønadsid,
        aldersjusteresForÅr: Int = YearMonth.now().year,
        beregnBasertPåVedtak: BeregnBasertPåVedtak? = null,
        opphørsdato: YearMonth? = null,
        beløpshistorikkStønad: StønadDto? = null,
        personobjekter: List<GrunnlagDto> = emptyList(),
    ): AldersjusteringResultat {
        try {
            log.info { "Aldersjustering kjøres for stønadstype ${stønad.type} og sak ${stønad.sak} for årstall $aldersjusteresForÅr" }
            secureLogger.info { "Aldersjustering kjøres for stønad $stønad og årstall $aldersjusteresForÅr" }
            // Antar at hvis beregnBasertPåVedtak er satt så er det utført manuell justering. Endre på dette hvis det ikke stemmer lenger
            val erManuellJustering = beregnBasertPåVedtak != null
            val sak = sakConsumer.hentSak(stønad.sak.verdi)
            val fødselsdatoBarn = personConsumer.hentFødselsdatoForPerson(stønad.kravhaver) ?: skalIkkeAldersjusteres(
                SkalIkkeAldersjusteresBegrunnelse.BARN_MANGLER_FØDSELSDATO,
            )

            if (!AldersjusteringUtils.skalAldersjusteres(fødselsdatoBarn, aldersjusteresForÅr)) {
                log.warn {
                    "Barn som er født $fødselsdatoBarn med alder ${finnBarnAlder(
                        fødselsdatoBarn,
                        aldersjusteresForÅr,
                    )} skal ikke alderjusteres for år $aldersjusteresForÅr. " +
                        "Aldersgruppene for aldersjustering er $aldersjusteringAldersgrupper"
                }
                skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING)
            }

            val beløpshistorikk = beløpshistorikkStønad?.periodeListe?.hentSisteLøpendePeriode() ?: vedtakService
                .hentBeløpshistorikkSistePeriode(stønad, LocalDateTime.now().withYear(aldersjusteresForÅr))

            beløpshistorikk.validerSkalAldersjusteres(sak)

            val sisteManuelleVedtak =
                beregnBasertPåVedtak?.let { SisteManuelleVedtak(it.vedtaksid ?: -1, it.vedtakDto ?: vedtakService.hentVedtak(it.vedtaksid!!)!!) }
                    ?: vedtakService.finnSisteManuelleVedtak(stønad)
                    ?: aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.FANT_INGEN_MANUELL_VEDTAK)

            sisteManuelleVedtak.validerSkalAldersjusteres(stønad, erManuellJustering = erManuellJustering)

            return sisteManuelleVedtak.utførOgBeregn(stønad, aldersjusteresForÅr, opphørsdato, beløpshistorikkStønad, personobjekter)
        } catch (e: Exception) {
            if (e is SkalIkkeAldersjusteresException || e is AldersjusteresManueltException) {
                throw e
            }
            e.loggOgKastFeil(stønad, aldersjusteresForÅr)
        }
    }

    internal fun SisteManuelleVedtak.utførOgBeregn(
        stønad: Stønadsid,
        aldersjusteresForÅr: Int = YearMonth.now().year,
        opphørPåDato: YearMonth?,
        beløpshistorikkStønad: StønadDto?,
        personobjekter: List<GrunnlagDto> = emptyList(),
    ): AldersjusteringResultat {
        val beregningInput = byggGrunnlagForBeregning(
            stønad,
            aldersjusteresForÅr,
            beløpshistorikkStønad,
            personobjekter,
        )
        val søknadsbarn =
            personobjekter.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)
                ?: vedtak.grunnlagListe.hentPersonMedIdent(stønad.kravhaver.verdi)
                ?: vedtak.grunnlagListe.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)
                ?: aldersjusteringFeilet("Fant ikke person ${stønad.kravhaver.verdi} i grunnlaget")
        val stønadsendring = finnStønadsendring(stønad)
        val løpendeStønad = vedtakService
            .hentBeløpshistorikkSistePeriode(stønad, LocalDateTime.now().withYear(aldersjusteresForÅr))
        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!
        val sluttberegningSistePeriode = vedtak.grunnlagListe.finnSluttberegningIReferanser(sistePeriode.grunnlagReferanseListe)
            ?.innholdTilObjekt<SluttberegningBarnebidrag>()
        val resultatSistePeriode = when (Resultatkode.fraKode(sistePeriode.resultatkode) == Resultatkode.INGEN_ENDRING_UNDER_GRENSE) {
            true -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.visningsnavn.intern
            false -> sluttberegningSistePeriode?.resultatVisningsnavn?.intern
        }
        return try {
            barnebidragApi
                .beregnAldersjustering(beregningInput).let {
                    val beregnetPeriode = it.beregnetBarnebidragPeriodeListe.first()
                    val opphørsdato = opphørPåDato ?: løpendeStønad?.periode?.til
                    if (opphørsdato != null && beregnetPeriode.periode.fom.isAfter(opphørsdato)) {
                        skalIkkeAldersjusteres(
                            SkalIkkeAldersjusteresBegrunnelse.BIDRAGET_HAR_OPPHØRT,
                            resultat = resultatSistePeriode,
                            vedtaksid = vedtaksId,
                        )
                    }
                    val resultatMedGrunnlag = it.copy(
                        beregnetBarnebidragPeriodeListe = listOf(
                            beregnetPeriode.copy(
                                periode = ÅrMånedsperiode(beregnetPeriode.periode.fom, opphørsdato),
                            ),
                        ),
                        grunnlagListe = it.grunnlagListe + listOf(
                            opprettVirkningstidspunktGrunnlag(
                                søknadsbarn.referanse,
                                it.beregnetBarnebidragPeriodeListe.first().periode.fom.atDay(1),
                                opphørsdato,
                            ),
                        ),
                    )
                    secureLogger.info {
                        "Resultat av beregning av aldersjustering for stønad $stønad og år $aldersjusteresForÅr: ${resultatMedGrunnlag.beregnetBarnebidragPeriodeListe}"
                    }
                    AldersjusteringResultat(
                        vedtaksId,
                        løpendeStønad?.beløp,
                        resultatMedGrunnlag,
                        resultatSistePeriode,
                    )
                }
        } catch (e: AldersjusteringLavereEnnEllerLikLøpendeBidragException) {
            SkalIkkeAldersjusteresException(
                SkalIkkeAldersjusteresBegrunnelse.ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
                vedtaksid = vedtaksId,
                resultat = resultatSistePeriode,
            ).loggOgKastFeil(stønad, aldersjusteresForÅr)
        }
    }

    internal fun opprettVirkningstidspunktGrunnlag(referanseBarn: String, virkningstidspunkt: LocalDate, opphørsdato: YearMonth?): GrunnlagDto =
        GrunnlagDto(
            referanse = "virkningstidspunkt_$referanseBarn",
            type = Grunnlagstype.VIRKNINGSTIDSPUNKT,
            gjelderBarnReferanse = referanseBarn,
            innhold = POJONode(
                VirkningstidspunktGrunnlag(
                    virkningstidspunkt = virkningstidspunkt,
                    opphørsdato = opphørsdato?.atDay(1),
                    årsak = VirkningstidspunktÅrsakstype.AUTOMATISK_JUSTERING,
                ),
            ),
        )

    private fun Exception.loggOgKastFeil(stønad: Stønadsid, aldersjusteresForÅr: Int): Nothing {
        when (this) {
            is SkalIkkeAldersjusteresException,
            is AldersjusteresManueltException,
            -> secureLogger.warn {
                "Utførte ikke aldersjustering av stønad $stønad for år $aldersjusteresForÅr: $message"
            }
            else -> secureLogger.error(this) {
                "Det skjedde en feil ved aldersjustering av stønad $stønad for år $aldersjusteresForÅr"
            }
        }
        throw this
    }
    private fun SisteManuelleVedtak.finnStønadsendring(stønad: Stønadsid) = vedtak.stønadsendringListe.find {
        it.type == stønad.type &&
            identUtils.hentNyesteIdent(it.kravhaver) == identUtils.hentNyesteIdent(stønad.kravhaver)
    }!!
    private fun SisteManuelleVedtak.validerSkalAldersjusteres(
        stønad: Stønadsid,
        aldersjusteresForÅr: Int = YearMonth.now().year,
        erManuellJustering: Boolean = false,
    ) {
        val aldersjusteringDato = YearMonth.of(aldersjusteresForÅr, 7)
        if (this.vedtak.grunnlagListe.isEmpty()) aldersjusteringFeilet("Aldersjustering kunne ikke utføres fordi vedtak $vedtaksId mangler grunnlag")
        val begrunnelser: MutableSet<SkalIkkeAldersjusteresBegrunnelse> = mutableSetOf()
        val stønadsendring = finnStønadsendring(stønad)

        if (stønadsendring.periodeListe.isEmpty()) {
            skalIkkeAldersjusteres(
                SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE,
                vedtaksid = vedtaksId,
            )
        }

        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode() ?: skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE,
            vedtaksid = vedtaksId,
        )

        val sluttberegningSistePeriode = vedtak.grunnlagListe.finnSluttberegningIReferanser(sistePeriode.grunnlagReferanseListe)
            ?.innholdTilObjekt<SluttberegningBarnebidrag>()

        val underholdskostnad = vedtak.grunnlagListe.finnDelberegningUnderholdskostnad(sistePeriode.grunnlagReferanseListe)

        val resultatSistePeriode =
            when (Resultatkode.fraKode(sistePeriode.resultatkode)) {
                Resultatkode.INGEN_ENDRING_UNDER_GRENSE,
                Resultatkode.LAVERE_ENN_INNTEKTSEVNE_BEGGE_PARTER,
                Resultatkode.LAVERE_ENN_INNTEKTSEVNE_BIDRAGSPLIKTIG,
                Resultatkode.LAVERE_ENN_INNTEKTSEVNE_BIDRAGSMOTTAKER,
                Resultatkode.MANGLER_DOKUMENTASJON_AV_INNTEKT_BEGGE_PARTER,
                Resultatkode.MANGLER_DOKUMENTASJON_AV_INNTEKT_BIDRAGSMOTTAKER,
                Resultatkode.MANGLER_DOKUMENTASJON_AV_INNTEKT_BIDRAGSPLIKTIG,
                Resultatkode.INNTIL_1_ÅR_TILBAKE,
                Resultatkode.INNVILGET_VEDTAK,
                -> Resultatkode.fraKode(sistePeriode.resultatkode)!!.visningsnavn.intern
                else ->
                    sluttberegningSistePeriode?.resultatVisningsnavn?.intern
                        ?: Resultatkode.fraKode(sistePeriode.resultatkode)?.visningsnavn?.intern
                        ?: sistePeriode.resultatkode
            }

        sistePeriode.resultatkode.validerResultatkkode(resultatSistePeriode, vedtaksId)

        if (sistePeriode.periode.fom.isAfter(aldersjusteringDato)) {
            aldersjusteresManuelt(
                SkalAldersjusteresManueltBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_HAR_PERIODE_MED_FOM_DATO_ETTER_ALDERSJUSTERINGEN,
                resultat = resultatSistePeriode,
                vedtaksid = vedtaksId,
            )
        }

        val sluttberegning =
            vedtak.grunnlagListe
                .finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(
                    Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                    sistePeriode.grunnlagReferanseListe,
                ).firstOrNull()
                ?: aldersjusteringFeilet("Fant ingen sluttberegning i vedtak $vedtaksId for siste periode")

        val beløpSistePeriode = sistePeriode.beløp?.setScale(0)
            ?: run {
                if (sistePeriode.periode.fom.isBefore(aldersjusteringDato)) {
                    skalIkkeAldersjusteres(
                        SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE,
                        resultat = resultatSistePeriode,
                        vedtaksid = vedtaksId,
                    )
                } else {
                    null
                }
            }

        val forpleining = underholdskostnad?.forpleining?.setScale(0) ?: BigDecimal.ZERO
        if (forpleining > BigDecimal.ZERO) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_INNEHOLDER_UNDERHOLDSKOSTNAD_MED_FORPLEINING)
        }

        if (sistePeriode.periode.fom >= aldersjusteringDato && sistePeriode.beløp != null && !erManuellJustering) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.LØPENDE_PERIODE_FRA_OG_MED_DATO_ER_LIK_ELLER_ETTER_ALDERSJUSTERING)
        }

        if (sluttberegning.innhold.bidragJustertForDeltBosted && BigDecimal.ZERO.equals(beløpSistePeriode)) {
            aldersjusteresManuelt(
                SkalAldersjusteresManueltBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_HAR_RESULTAT_DELT_BOSTED_MED_BELØP_0,
                resultat = resultatSistePeriode,
                vedtaksid = vedtaksId,
            )
        }

        if (sluttberegning.innhold.begrensetRevurderingUtført && sluttberegning.innhold.bidragJustertTilForskuddssats) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_BEGRENSET_REVURDERING_JUSTERT_OPP_TIL_FORSKUDDSATS)
        }

        if (sluttberegning.innhold.bidragJustertForNettoBarnetilleggBM) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_FOR_BARNETILLEGG_BM)
        }

        if (sluttberegning.innhold.bidragJustertForNettoBarnetilleggBP) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_FOR_BARNETILLEGG_BP)
        }

        if (sluttberegning.innhold.bidragJustertNedTilEvne &&
            sistePeriode.resultatkode != Resultatkode.MAKS_25_PROSENT_AV_INNTEKT.tilBisysResultatkode()
        ) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_NED_TIL_EVNE)
        }

        if (sluttberegning.innhold.bidragJustertNedTil25ProsentAvInntekt) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_JUSTERT_NED_TIL_25_PROSENT_AV_INNTEKT)
        }

        if (begrunnelser.isNotEmpty()) {
            secureLogger.warn { "Stønad ${stønad.toReferanse()} skal ikke aldersjusteres $begrunnelser" }
            skalIkkeAldersjusteres(*begrunnelser.toTypedArray(), resultat = resultatSistePeriode, vedtaksid = vedtaksId)
        }
    }

    private fun String.validerResultatkkode(resultatSistePeriode: String?, vedtaksId: Int?) = when (this) {
        Resultatkode.INNTIL_1_ÅR_TILBAKE.tilBisysResultatkode(), Resultatkode.INNVILGET_VEDTAK.tilBisysResultatkode() -> aldersjusteresManuelt(
            SkalAldersjusteresManueltBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_INNVILGET_VEDTAK,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )
        Resultatkode.SKJØNN_UTLANDET.bisysKode.first().resultatKode -> skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_SKJØNNSFASTSATT_AV_UTLAND,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )
        Resultatkode.PRIVAT_AVTALE.bisysKode.first().resultatKode -> skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.VEDTAK_GRUNNLAG_HENTES_FRA_ER_PRIVAT_AVTALE,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )
        Resultatkode.OPPHØR.name, Resultatkode.OPPHØR.bisysKode.firstOrNull()?.resultatKode,
        -> skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.BIDRAGET_HAR_OPPHØRT,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )

        else -> null
    }

    private fun SisteManuelleVedtak.byggGrunnlagForBeregning(
        stønad: Stønadsid,
        aldersjusteresForÅr: Int,
        beløpshistorikkStønad: StønadDto? = null,
        personobjekter: List<GrunnlagDto> = emptyList(),
    ): BeregnGrunnlagAldersjustering {
        val grunnlagsliste = vedtak.grunnlagListe

        val søknadsbarn =
            personobjekter.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)
                ?: grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi)
                ?: grunnlagsliste.hentPersonForNyesteIdent(identUtils, stønad.kravhaver)
                ?: aldersjusteringFeilet("Fant ikke person ${stønad.kravhaver.verdi} i grunnlaget")

        val personobjekter =
            listOf(
                søknadsbarn,
                grunnlagsliste.bidragsmottaker ?: aldersjusteringFeilet("Fant ikke bidragsmottaker i grunnlaget"),
                grunnlagsliste.bidragspliktig ?: aldersjusteringFeilet("Fant ikke bidragspliktig i grunnlaget"),
            ) as List<GrunnlagDto>
        return BeregnGrunnlagAldersjustering(
            periode = ÅrMånedsperiode(YearMonth.of(aldersjusteresForÅr, 7), YearMonth.of(aldersjusteresForÅr, 8)),
            personObjektListe = personobjekter,
            vedtakListe =
            listOf(
                BeregnGrunnlagVedtak(
                    gjelderBarnReferanse = søknadsbarn.referanse,
                    vedtakId = vedtaksId,
                    vedtakInnhold = vedtak,
                ),
            ),
            beløpshistorikkListe = listOf(
                beløpshistorikkStønad?.tilGrunnlag(personobjekter, stønad, identUtils)
                    ?: vedtakService.hentBeløpshistorikkTilGrunnlag(stønad, personobjekter, LocalDateTime.now().withYear(aldersjusteresForÅr)),
            ),
        )
    }

    private fun StønadPeriodeDto?.validerSkalAldersjusteres(sak: BidragssakDto) {
        if (this == null) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
        if (valutakode != "NOK") skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.BIDRAG_LØPER_MED_UTENLANDSK_VALUTA)
        if (sak.kategori == Sakskategori.U) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.SAKEN_TILHØRER_UTLAND)
    }

    fun List<GrunnlagDto>.finnDelberegningUnderholdskostnad(grunnlagsreferanseListe: List<Grunnlagsreferanse>): DelberegningUnderholdskostnad? {
        val sluttberegning = finnSluttberegningIReferanser(grunnlagsreferanseListe) ?: return null
        val delberegningUnderholdskostnad =
            find {
                it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD &&
                    sluttberegning.grunnlagsreferanseListe.contains(
                        it.referanse,
                    )
            } ?: return null
        return delberegningUnderholdskostnad.innholdTilObjekt<DelberegningUnderholdskostnad>()
    }
}
