package no.nav.bidrag.beregn.barnebidrag.service

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
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.sak.BidragssakDto
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.YearMonth

private val log = KotlinLogging.logger {}

data class AldersjusteringResultat(val vedtaksid: Int, val beregning: BeregnetBarnebidragResultat)
enum class SkalIkkeAldersjusteresBegrunnelse {
    INGEN_LØPENDE_PERIODE,
    IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING,
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
    MANGLER_GRUNNLAG,
    FANT_INGEN_MANUELL_VEDTAK,
}

class SkalIkkeAldersjusteresException(vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse) :
    RuntimeException("Skal ikke aldersjusteres med begrunnelse ${begrunnelse.joinToString(",")}") {
    val begrunnelser: List<SkalIkkeAldersjusteresBegrunnelse> = begrunnelse.toList()
}

class AldersjusteresManueltException(begrunnelse: SkalAldersjusteresManueltBegrunnelse) :
    RuntimeException("Skal aldersjusteres manuelt med begrunnelse $begrunnelse") {
    val begrunnelse: SkalAldersjusteresManueltBegrunnelse = begrunnelse
}

fun aldersjusteresManuelt(begrunnelse: SkalAldersjusteresManueltBegrunnelse): Nothing = throw AldersjusteresManueltException(begrunnelse)

fun skalIkkeAldersjusteres(vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse): Nothing = throw SkalIkkeAldersjusteresException(*begrunnelse)

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
            val fødselsdatoBarn = personConsumer.hentFødselsdatoForPerson(stønad.kravhaver)

            if (!AldersjusteringUtils.skalAldersjusteres(fødselsdatoBarn, aldersjusteresForÅr)) {
                log.warn {
                    "Barn som er født $fødselsdatoBarn skal ikke alderjusteres for år $aldersjusteresForÅr. " +
                        "Aldersgruppene for aldersjustering er $aldersjusteringAldersgrupper"
                }
                skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING)
            }

            vedtakService
                .hentLøpendeStønad(stønad)
                .validerSkalAldersjusteres(sak)

            val sisteManuelleVedtak =
                vedtakService.finnSisteManuelleVedtak(stønad) ?: aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.FANT_INGEN_MANUELL_VEDTAK)

            sisteManuelleVedtak.validerSkalAldersjusteres(stønad)

            val beregningInput = sisteManuelleVedtak.byggGrunnlagForBeregning(
                stønad,
                aldersjusteresForÅr,
            )
            return barnebidragApi
                .beregnAldersjustering(beregningInput).let {
                    secureLogger.info {
                        "Resultat av beregning av aldersjustering for stønad $stønad og år $aldersjusteresForÅr: ${it.beregnetBarnebidragPeriodeListe}"
                    }
                    AldersjusteringResultat(sisteManuelleVedtak.vedtaksId, it)
                }
        } catch (e: AldersjusteringLavereEnnEllerLikLøpendeBidragException) {
            SkalIkkeAldersjusteresException(
                SkalIkkeAldersjusteresBegrunnelse.ALDERSJUSTERT_BELØP_LAVERE_ELLER_LIK_LØPENDE_BIDRAG,
            ).loggOgKastFeil(stønad, aldersjusteresForÅr)
        } catch (e: Exception) {
            e.loggOgKastFeil(stønad, aldersjusteresForÅr)
        }
    }

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
    private fun SisteManuelleVedtak.validerSkalAldersjusteres(stønad: Stønadsid) {
        if (this.vedtak.grunnlagListe.isEmpty()) aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.MANGLER_GRUNNLAG)
        val begrunnelser: MutableSet<SkalIkkeAldersjusteresBegrunnelse> = mutableSetOf()
        val stønadsendring =
            vedtak.stønadsendringListe.find {
                it.type == stønad.type &&
                    it.kravhaver == stønad.kravhaver
            }!!

        if (stønadsendring.periodeListe.isEmpty()) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)

        val sistePeriode = stønadsendring.periodeListe.hentSisteLøpendePeriode()!!
        val sluttberegning =
            vedtak.grunnlagListe
                .finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(
                    Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                    sistePeriode.grunnlagReferanseListe,
                ).firstOrNull() ?: skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)

        if (sistePeriode.beløp == null) {
            skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
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
            skalIkkeAldersjusteres(*begrunnelser.toTypedArray())
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
            beløpshistorikkListe = listOf(vedtakService.hentBeløpshistorikk(stønad, personobjekter)),
        )
    }

    private fun StønadPeriodeDto?.validerSkalAldersjusteres(sak: BidragssakDto) {
        if (this == null) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
        if (valutakode != "NOK") skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.LØPER_MED_UTENLANDSK_VALUTA)
        if (sak.eierfogd.verdi == enhet_utland) aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.UTENLANDSSAK_MED_NORSK_VALUTA)
    }
}
