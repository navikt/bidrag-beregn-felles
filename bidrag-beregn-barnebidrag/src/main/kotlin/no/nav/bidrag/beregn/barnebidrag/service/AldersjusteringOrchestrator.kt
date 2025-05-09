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
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.felles.enhet_utland
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.VirkningstidspunktGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.sak.BidragssakDto
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

private val log = KotlinLogging.logger {}

data class AldersjusteringResultat(val vedtaksid: Int, val løpendeBeløp: BigDecimal?, val beregning: BeregnetBarnebidragResultat)
enum class SkalIkkeAldersjusteresBegrunnelse {
    INGEN_LØPENDE_PERIODE,
    LØPENDE_PERIODE_FRA_OG_MED_DATO_ER_LIK_ELLER_ETTER_ALDERSJUSTERING,
    IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING,
    BARN_MANGLER_FØDSESLDATO,
    LØPER_MED_UTENLANDSK_VALUTA,
    JUSTERT_FOR_BARNETILLEGG_BM,
    JUSTERT_FOR_BARNETILLEGG_BP,
    JUSTERT_PÅ_GRUNN_AV_EVNE,
    ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
    JUSTERT_PÅ_GRUNN_AV_25_PROSENT,
    SISTE_VEDTAK_ER_BEGRENSET_REVURDERING,
}

enum class SkalAldersjusteresManueltBegrunnelse {
    UTENLANDSSAK_MED_NORSK_VALUTA,
    FANT_INGEN_MANUELL_VEDTAK,
    DELT_BOSTED_MED_BELØP_0,
    SISTE_VEDTAK_ER_INNVILGET_VEDTAK,
}

class SkalIkkeAldersjusteresException(val vedtaksid: Int? = null, vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse) :
    RuntimeException("Skal ikke aldersjusteres med begrunnelse ${begrunnelse.joinToString(",")}") {
    val begrunnelser: List<SkalIkkeAldersjusteresBegrunnelse> = begrunnelse.toList()
}

class AldersjusteresManueltException(val vedtaksid: Int? = null, begrunnelse: SkalAldersjusteresManueltBegrunnelse) :
    RuntimeException("Skal aldersjusteres manuelt med begrunnelse $begrunnelse") {
    val begrunnelse: SkalAldersjusteresManueltBegrunnelse = begrunnelse
}

fun aldersjusteringFeilet(begrunnelse: String): Nothing = throw RuntimeException(begrunnelse)

fun aldersjusteresManuelt(vedtaksid: Int? = null, begrunnelse: SkalAldersjusteresManueltBegrunnelse): Nothing =
    throw AldersjusteresManueltException(vedtaksid, begrunnelse)

fun skalIkkeAldersjusteres(vedtaksid: Int?, vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse): Nothing =
    throw SkalIkkeAldersjusteresException(vedtaksid, *begrunnelse)

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
                null,
                SkalIkkeAldersjusteresBegrunnelse.BARN_MANGLER_FØDSESLDATO,
            )

            if (!AldersjusteringUtils.skalAldersjusteres(fødselsdatoBarn, aldersjusteresForÅr)) {
                log.warn {
                    "Barn som er født $fødselsdatoBarn skal ikke alderjusteres for år $aldersjusteresForÅr. " +
                        "Aldersgruppene for aldersjustering er $aldersjusteringAldersgrupper"
                }
                skalIkkeAldersjusteres(null, SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING)
            }

            vedtakService
                .hentLøpendeStønad(stønad, LocalDateTime.now().withYear(aldersjusteresForÅr))
                .validerSkalAldersjusteres(sak)

            val sisteManuelleVedtak =
                vedtakService.finnSisteManuelleVedtak(stønad)
                    ?: aldersjusteresManuelt(null, SkalAldersjusteresManueltBegrunnelse.FANT_INGEN_MANUELL_VEDTAK)

            sisteManuelleVedtak.validerSkalAldersjusteres(stønad)

            return sisteManuelleVedtak.utførOgBeregn(stønad, aldersjusteresForÅr)
        } catch (e: Exception) {
            if (e is SkalIkkeAldersjusteresException || e is AldersjusteresManueltException) {
                throw e
            }
            e.loggOgKastFeil(stønad, aldersjusteresForÅr)
        }
    }

    internal fun SisteManuelleVedtak.utførOgBeregn(stønad: Stønadsid, aldersjusteresForÅr: Int = YearMonth.now().year): AldersjusteringResultat =
        try {
            val beregningInput = byggGrunnlagForBeregning(
                stønad,
                aldersjusteresForÅr,
            )
            val søknadsbarn = vedtak.grunnlagListe.hentPersonMedIdent(stønad.kravhaver.verdi)!!
            val stønadsendring = finnStønadsendring(stønad)
            val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!
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
                    AldersjusteringResultat(vedtaksId, sistePeriode.beløp, resultatMedGrunnlag)
                }
        } catch (e: AldersjusteringLavereEnnEllerLikLøpendeBidragException) {
            SkalIkkeAldersjusteresException(
                vedtaksId,
                SkalIkkeAldersjusteresBegrunnelse.ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
            ).loggOgKastFeil(stønad, aldersjusteresForÅr)
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

        if (stønadsendring.periodeListe.isEmpty()) skalIkkeAldersjusteres(vedtaksId, SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)

        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!
        if (sistePeriode.resultatkode == "IV") aldersjusteresManuelt(vedtaksId, SkalAldersjusteresManueltBegrunnelse.SISTE_VEDTAK_ER_INNVILGET_VEDTAK)
        val sluttberegning =
            vedtak.grunnlagListe
                .finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(
                    Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                    sistePeriode.grunnlagReferanseListe,
                ).firstOrNull() ?: skalIkkeAldersjusteres(vedtaksId, SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)

        val beløpSistePeriode = sistePeriode.beløp?.setScale(0)
            ?: skalIkkeAldersjusteres(vedtaksId, SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)

        if (sistePeriode.periode.fom >= YearMonth.of(aldersjusteresForÅr, 7)) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.LØPENDE_PERIODE_FRA_OG_MED_DATO_ER_LIK_ELLER_ETTER_ALDERSJUSTERING)
        }

        if (sluttberegning.innhold.bidragJustertForDeltBosted && BigDecimal.ZERO.equals(beløpSistePeriode)) {
            aldersjusteresManuelt(vedtaksId, SkalAldersjusteresManueltBegrunnelse.DELT_BOSTED_MED_BELØP_0)
        }

        if (sluttberegning.innhold.begrensetRevurderingUtført) {
            begrunnelser.add(SkalIkkeAldersjusteresBegrunnelse.SISTE_VEDTAK_ER_BEGRENSET_REVURDERING)
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
            skalIkkeAldersjusteres(vedtaksId, *begrunnelser.toTypedArray())
        }
    }

    private fun SisteManuelleVedtak.byggGrunnlagForBeregning(stønad: Stønadsid, aldersjusteresForÅr: Int): BeregnGrunnlagAldersjustering {
        val grunnlagsliste = vedtak.grunnlagListe

        val søknadsbarn = grunnlagsliste.hentPersonMedIdent(stønad.kravhaver.verdi)!!

        val personobjekter =
            listOf(
                søknadsbarn,
                grunnlagsliste.bidragsmottaker!!,
                grunnlagsliste.bidragspliktig!!,
            ) as List<GrunnlagDto>
        return BeregnGrunnlagAldersjustering(
            periode = ÅrMånedsperiode(YearMonth.of(aldersjusteresForÅr, 7), YearMonth.of(aldersjusteresForÅr, 8)),
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
        if (this == null) skalIkkeAldersjusteres(null, SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
        if (valutakode != "NOK") skalIkkeAldersjusteres(null, SkalIkkeAldersjusteresBegrunnelse.LØPER_MED_UTENLANDSK_VALUTA)
        if (sak.eierfogd.verdi == enhet_utland) aldersjusteresManuelt(null, SkalAldersjusteresManueltBegrunnelse.UTENLANDSSAK_MED_NORSK_VALUTA)
    }
}
