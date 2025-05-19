package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningSakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils
import no.nav.bidrag.beregn.barnebidrag.utils.aldersjusteringAldersgrupper
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
import no.nav.bidrag.beregn.core.exception.AldersjusteringLavereEnnEllerLikLøpendeBidragException
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sak.Sakskategori
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.VirkningstidspunktGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
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
    LØPER_MED_UTENLANDSK_VALUTA,
    JUSTERT_FOR_BARNETILLEGG_BM,
    JUSTERT_FOR_BARNETILLEGG_BP,
    JUSTERT_PÅ_GRUNN_AV_EVNE,
    ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
    JUSTERT_PÅ_GRUNN_AV_25_PROSENT,
    SISTE_VEDTAK_ER_BEGRENSET_REVURDERING_JUSTERT_OPP_TIL_FORSKUDDSATS,
    SISTE_VEDTAK_ER_SKJØNNSFASTSATT_AV_UTLAND,
    UTENLANDSSAK,
    SISTE_VEDTAK_ER_PRIVAT_AVTALE,
}

enum class SkalAldersjusteresManueltBegrunnelse {
    FANT_INGEN_MANUELL_VEDTAK,
    SISTE_VEDTAK_HAR_RESULTAT_DELT_BOSTED_MED_BELØP_0,
    SISTE_VEDTAK_ER_INNVILGET_VEDTAK,
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

@Service
@Import(BeregnBarnebidragApi::class, VedtakService::class)
class AldersjusteringOrchestrator(
    private val vedtakService: VedtakService,
    private val sakConsumer: BeregningSakConsumer,
    private val barnebidragApi: BeregnBarnebidragApi,
    private val personConsumer: BeregningPersonConsumer,
) {
    fun utførAldersjustering(stønad: Stønadsid, aldersjusteresForÅr: Int = YearMonth.now().year): AldersjusteringResultat {
        try {
            log.info { "Aldersjustering kjøres for stønadstype ${stønad.type} og sak ${stønad.sak} for årstall $aldersjusteresForÅr" }
            secureLogger.info { "Aldersjustering kjøres for stønad $stønad og årstall $aldersjusteresForÅr" }
            val sak = sakConsumer.hentSak(stønad.sak.verdi)
            val fødselsdatoBarn = personConsumer.hentFødselsdatoForPerson(stønad.kravhaver) ?: skalIkkeAldersjusteres(
                SkalIkkeAldersjusteresBegrunnelse.BARN_MANGLER_FØDSELSDATO,
            )

            if (!AldersjusteringUtils.skalAldersjusteres(fødselsdatoBarn, aldersjusteresForÅr)) {
                log.warn {
                    "Barn som er født $fødselsdatoBarn skal ikke alderjusteres for år $aldersjusteresForÅr. " +
                        "Aldersgruppene for aldersjustering er $aldersjusteringAldersgrupper"
                }
                skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING)
            }

            vedtakService
                .hentLøpendeStønad(stønad, LocalDateTime.now().withYear(aldersjusteresForÅr))
                .validerSkalAldersjusteres(sak)

            val sisteManuelleVedtak =
                vedtakService.finnSisteManuelleVedtak(stønad)
                    ?: aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.FANT_INGEN_MANUELL_VEDTAK)

            sisteManuelleVedtak.validerSkalAldersjusteres(stønad)

            return sisteManuelleVedtak.utførOgBeregn(stønad, aldersjusteresForÅr)
        } catch (e: Exception) {
            if (e is SkalIkkeAldersjusteresException || e is AldersjusteresManueltException) {
                throw e
            }
            e.loggOgKastFeil(stønad, aldersjusteresForÅr)
        }
    }

    internal fun SisteManuelleVedtak.utførOgBeregn(stønad: Stønadsid, aldersjusteresForÅr: Int = YearMonth.now().year): AldersjusteringResultat {
        val beregningInput = byggGrunnlagForBeregning(
            stønad,
            aldersjusteresForÅr,
        )
        val søknadsbarn = vedtak.grunnlagListe.hentPersonMedIdent(stønad.kravhaver.verdi)!!
        val stønadsendring = finnStønadsendring(stønad)
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
                    val resultatMedGrunnlag = it.copy(
                        grunnlagListe = it.grunnlagListe + listOf(
                            opprettVirkningstidspunktGrunnlag(søknadsbarn.referanse, it.beregnetBarnebidragPeriodeListe.first().periode.fom.atDay(1)),
                        ),
                    )
                    secureLogger.info {
                        "Resultat av beregning av aldersjustering for stønad $stønad og år $aldersjusteresForÅr: ${resultatMedGrunnlag.beregnetBarnebidragPeriodeListe}"
                    }
                    AldersjusteringResultat(
                        vedtaksId,
                        sistePeriode.beløp,
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

    internal fun opprettVirkningstidspunktGrunnlag(referanseBarn: String, virkningstidspunkt: LocalDate): GrunnlagDto = GrunnlagDto(
        referanse = "virkningstidspunkt_$referanseBarn",
        type = Grunnlagstype.VIRKNINGSTIDSPUNKT,
        innhold = POJONode(
            VirkningstidspunktGrunnlag(
                virkningstidspunkt = virkningstidspunkt,
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
            it.kravhaver == stønad.kravhaver
    }!!
    private fun SisteManuelleVedtak.validerSkalAldersjusteres(stønad: Stønadsid, aldersjusteresForÅr: Int = YearMonth.now().year) {
        if (this.vedtak.grunnlagListe.isEmpty()) aldersjusteringFeilet("Aldersjustering kunne ikke utføres fordi vedtak $vedtaksId mangler grunnlag")
        val begrunnelser: MutableSet<SkalIkkeAldersjusteresBegrunnelse> = mutableSetOf()
        val stønadsendring = finnStønadsendring(stønad)

        if (stønadsendring.periodeListe.isEmpty()) {
            skalIkkeAldersjusteres(
                SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE,
                vedtaksid = vedtaksId,
            )
        }

        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!
        val sluttberegningSistePeriode = vedtak.grunnlagListe.finnSluttberegningIReferanser(sistePeriode.grunnlagReferanseListe)
            ?.innholdTilObjekt<SluttberegningBarnebidrag>()
        val resultatSistePeriode = when {
            Resultatkode.fraKode(sistePeriode.resultatkode)
                == Resultatkode.INNVILGET_VEDTAK -> Resultatkode.INNVILGET_VEDTAK.visningsnavn.intern
            Resultatkode.fraKode(sistePeriode.resultatkode)
                == Resultatkode.INGEN_ENDRING_UNDER_GRENSE -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.visningsnavn.intern
            else -> sluttberegningSistePeriode?.resultatVisningsnavn?.intern
        }

        sistePeriode.resultatkode.validerResultatkkode(resultatSistePeriode, vedtaksId)

        val sluttberegning =
            vedtak.grunnlagListe
                .finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(
                    Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                    sistePeriode.grunnlagReferanseListe,
                ).firstOrNull()
                ?: aldersjusteringFeilet("Fant ingen sluttberegning i vedtak $vedtaksId for siste periode")

        val beløpSistePeriode = sistePeriode.beløp?.setScale(0)
            ?: skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE, resultat = resultatSistePeriode, vedtaksid = vedtaksId)

        if (sistePeriode.periode.fom >= YearMonth.of(aldersjusteresForÅr, 7)) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.LØPENDE_PERIODE_FRA_OG_MED_DATO_ER_LIK_ELLER_ETTER_ALDERSJUSTERING)
        }

        if (sluttberegning.innhold.bidragJustertForDeltBosted && BigDecimal.ZERO.equals(beløpSistePeriode)) {
            aldersjusteresManuelt(
                SkalAldersjusteresManueltBegrunnelse.SISTE_VEDTAK_HAR_RESULTAT_DELT_BOSTED_MED_BELØP_0,
                resultat = resultatSistePeriode,
                vedtaksid = vedtaksId,
            )
        }

        if (sluttberegning.innhold.begrensetRevurderingUtført && sluttberegning.innhold.bidragJustertTilForskuddssats) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.SISTE_VEDTAK_ER_BEGRENSET_REVURDERING_JUSTERT_OPP_TIL_FORSKUDDSATS)
        }

        if (sluttberegning.innhold.bidragJustertForNettoBarnetilleggBM) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.JUSTERT_FOR_BARNETILLEGG_BM)
        }

        if (sluttberegning.innhold.bidragJustertForNettoBarnetilleggBP) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.JUSTERT_FOR_BARNETILLEGG_BP)
        }
        if (sluttberegning.innhold.bidragJustertNedTilEvne) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.JUSTERT_PÅ_GRUNN_AV_EVNE)
        }
        if (sluttberegning.innhold.bidragJustertNedTil25ProsentAvInntekt) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.JUSTERT_PÅ_GRUNN_AV_25_PROSENT)
        }

        if (begrunnelser.isNotEmpty()) {
            secureLogger.warn { "Stønad ${stønad.toReferanse()} skal ikke aldersjusteres $begrunnelser" }
            skalIkkeAldersjusteres(*begrunnelser.toTypedArray(), resultat = resultatSistePeriode, vedtaksid = vedtaksId)
        }
    }

    private fun String.validerResultatkkode(resultatSistePeriode: String?, vedtaksId: Int?) = when (this) {
        Resultatkode.INNVILGET_VEDTAK.bisysKode.first().resultatKode -> aldersjusteresManuelt(
            SkalAldersjusteresManueltBegrunnelse.SISTE_VEDTAK_ER_INNVILGET_VEDTAK,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )
        Resultatkode.SKJØNN_UTLANDET.bisysKode.first().resultatKode -> skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.SISTE_VEDTAK_ER_SKJØNNSFASTSATT_AV_UTLAND,
            resultat = resultatSistePeriode,
            vedtaksid = vedtaksId,
        )
        Resultatkode.PRIVAT_AVTALE.bisysKode.first().resultatKode -> skalIkkeAldersjusteres(
            SkalIkkeAldersjusteresBegrunnelse.SISTE_VEDTAK_ER_PRIVAT_AVTALE,
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

    private fun SisteManuelleVedtak.byggGrunnlagForBeregning(stønad: Stønadsid, aldersjusteresForÅr: Int): BeregnGrunnlagAldersjustering {
        val grunnlagsliste = vedtak.grunnlagListe

        val søknadsbarn = grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi)!!
        val stønadsendring = finnStønadsendring(stønad)
        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!

        val personobjekter =
            listOf(
                søknadsbarn,
                grunnlagsliste.bidragsmottaker!!,
                grunnlagsliste.bidragspliktig!!,
            ) as List<GrunnlagDto>
        val fomPeriode = YearMonth.of(aldersjusteresForÅr, 7)
        val tilPeriode = if (sistePeriode.periode.til != null &&
            sistePeriode.periode.til!!.isAfter(fomPeriode)
        ) {
            sistePeriode.periode.til
        } else {
            YearMonth.of(aldersjusteresForÅr, 8)
        }
        return BeregnGrunnlagAldersjustering(
            periode = ÅrMånedsperiode(fomPeriode, tilPeriode),
            personObjektListe = personobjekter,
            vedtakListe =
            listOf(
                BeregnGrunnlagVedtak(
                    gjelderBarnReferanse = søknadsbarn.referanse,
                    vedtakId = vedtaksId.toLong(),
                    vedtakInnhold = vedtak,
                ),
            ),
            beløpshistorikkListe = listOf(
                vedtakService.hentBeløpshistorikk(stønad, personobjekter, LocalDateTime.now().withYear(aldersjusteresForÅr)),
            ),
        )
    }

    private fun StønadPeriodeDto?.validerSkalAldersjusteres(sak: BidragssakDto) {
        if (this == null) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
        if (valutakode != "NOK") skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.LØPER_MED_UTENLANDSK_VALUTA)
        if (sak.kategori == Sakskategori.U) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.UTENLANDSSAK)
    }
}
