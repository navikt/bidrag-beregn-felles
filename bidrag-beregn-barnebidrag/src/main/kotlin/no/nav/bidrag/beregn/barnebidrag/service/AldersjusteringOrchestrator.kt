package no.nav.bidrag.beregn.barnebidrag.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningSakConsumer
import no.nav.bidrag.beregn.barnebidrag.utils.AldersjusteringUtils
import no.nav.bidrag.beregn.barnebidrag.utils.hentSisteLøpendePeriode
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

enum class SkalIkkeAldersjusteresBegrunnelse {
    INGEN_LØPENDE_PERIODE,
    IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING,
    LØPER_MED_UTENLANDSK_VALUTA,
    JUSTERT_FOR_BARNETILLEGG_BM,
    JUSTERT_FOR_BARNETILLEGG_BP,
    JUSTERT_PÅ_GRUNN_AV_EVNE,
    JUSTERT_PÅ_GRUNN_AV_25_PROSENT,
}

enum class SkalAldersjusteresManueltBegrunnelse {
    UTENLANDSSAK_MED_NORSK_VALUTA,
    MANGLER_GRUNNLAG,
}

class SkalIkkeAldersjusteresException(vararg begrunnelse: SkalIkkeAldersjusteresBegrunnelse) :
    RuntimeException("Skal ikke aldersjusteres med begrunnelse ${begrunnelse.joinToString(",")}")

class AldersjusteresManueltException(begrunnelse: SkalAldersjusteresManueltBegrunnelse) :
    RuntimeException("Skal aldersjusteres manuelt med begrunnelse $begrunnelse")

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
    fun utførAldersjustering(stønad: Stønadsid, aldersjusteresForÅr: Int = YearMonth.now().year): BeregnetBarnebidragResultat {
        // TODO: MDC
        // TODO: Catch exception
        log.info { "Aldersjustering kjøres for stønadstype ${stønad.type} og sak ${stønad.sak} for årstall $aldersjusteresForÅr" }
        secureLogger.info { "Aldersjustering kjøres for stønad $stønad og årstall $aldersjusteresForÅr" }
        val sak = sakConsumer.hentSak(stønad.sak.verdi)
        val fødselsdatoBarn = personConsumer.hentFødselsdatoForPerson(stønad.kravhaver)

        if (!AldersjusteringUtils.skalAldersjusteres(fødselsdatoBarn, aldersjusteresForÅr)) {
            log.warn { "Skal barn som er født $fødselsdatoBarn skal ikke alderjusteres for år $aldersjusteresForÅr" }
            skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING)
        }

        vedtakService
            .hentLøpendeStønad(stønad)
            .validerSkalAldersjusteres(aldersjusteresForÅr, sak)

        val sisteManuelleVedtak = vedtakService.finnSisteManuelleVedtak(stønad)!!

        sisteManuelleVedtak.validerSkalAldersjusteres(stønad)
        val beregningInput = sisteManuelleVedtak.byggGrunnlagForBeregning(
            stønad,
            aldersjusteresForÅr,
        )
        return barnebidragApi
            .beregnAldersjustering(
                beregningInput,
            ).let {
                secureLogger.info {
                    "Resultat av beregning av aldersjustering for stønad $stønad og år $aldersjusteresForÅr: ${it.beregnetBarnebidragPeriodeListe}"
                }
                it.copy(
                    grunnlagListe = (it.grunnlagListe + beregningInput.personObjektListe).toSet().toList(),
                )
            }
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

    private fun StønadPeriodeDto?.validerSkalAldersjusteres(aldersjusteresForÅr: Int = YearMonth.now().year, sak: BidragssakDto) {
        if (this == null) skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE)
        if (valutakode != "NOK") skalIkkeAldersjusteres(SkalIkkeAldersjusteresBegrunnelse.LØPER_MED_UTENLANDSK_VALUTA)
        if (sak.eierfogd.verdi == enhet_utland) aldersjusteresManuelt(SkalAldersjusteresManueltBegrunnelse.UTENLANDSSAK_MED_NORSK_VALUTA)
    }
}
