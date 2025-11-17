package no.nav.bidrag.beregn.barnebidrag.service.orkestrering

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.annotation.Timed
import no.nav.bidrag.beregn.barnebidrag.service.external.VedtakService
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragssak
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidragV2
import no.nav.bidrag.transport.behandling.felles.grunnlag.erSluttberegningGammelStruktur
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnBidragJustertForBarnetilleggBP
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSamværsklasse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
@Import(Vedtaksfiltrering::class)
class HentLøpendeBidragService(private val vedtakService: VedtakService) {
    @Timed
    fun hentLøpendeBidragForBehandling(
        bidragspliktigIdent: Personident,
        søknadsbarnidentMap: Map<Personident, String>,
    ): EvnevurderingBeregningResultat {
        try {
            // Henter løpende stønader, men filtrerer bort kravhavere som er søknadsbarn
            val løpendeStønader = vedtakService.hentSisteLøpendeStønader(bidragspliktigIdent).filterNot { it.kravhaver in søknadsbarnidentMap }
            secureLogger.info { "Hentet løpende stønader $løpendeStønader for BP ${bidragspliktigIdent.verdi}" }
            val sisteLøpendeVedtak = løpendeStønader.hentLøpendeVedtak(bidragspliktigIdent)
            secureLogger.info { "Hentet siste løpende vedtak $sisteLøpendeVedtak for BP ${bidragspliktigIdent.verdi}" }
            val beregnetBeløpListe = sisteLøpendeVedtak.hentBeregning()
            secureLogger.info { "Hentet beregnet beløp $beregnetBeløpListe" }
            return EvnevurderingBeregningResultat(beregnetBeløpListe, løpendeStønader)
        } catch (e: Exception) {
            log.error(e) { "Det skjedde en feil ved opprettelse av grunnlag for løpende bidrag for BP evnevurdering: ${e.message}" }
            throw e
        }
    }

    private fun List<VedtakForStønad>.hentBeregning(): BidragBeregningResponsDto {
        val hentBeregningFraBidragVedtakListe = mutableListOf<VedtakForStønad>()
        val hentBeregningFraBBMListe = mutableListOf<VedtakForStønad>()

        // Bestemmer hvilke vedtak som skal hentes fra bidrag-vedtak og hvilke som skal hentes fra BBM og lager en liste for hver
        map {
            if (it.behandlingsreferanser.any { it.kilde == BehandlingsrefKilde.BEHANDLING_ID }) {
                hentBeregningFraBidragVedtakListe.add(it)
            } else {
                hentBeregningFraBBMListe.add(it)
            }
        }

        // Henter beregningsgrunnlag fra BBM
        var bidragBeregningResponsDtoFraBBM = BidragBeregningResponsDto(emptyList())
        if (hentBeregningFraBBMListe.isNotEmpty()) {
            secureLogger.info { "Følgende beregninger skal hentes fra BBM: $hentBeregningFraBBMListe" }
            bidragBeregningResponsDtoFraBBM =
                vedtakService.hentBeregningFraBBM(hentBeregningFraBBMListe)
            secureLogger.info { "Respons fra BBM: $bidragBeregningResponsDtoFraBBM" }
        }

        // Henter beregningsgrunnlag fra bidrag-vedtak
        var bidragBeregningResponsDtoFraBidragVedtak = BidragBeregningResponsDto(emptyList())
        if (hentBeregningFraBidragVedtakListe.isNotEmpty()) {
            secureLogger.info { "Følgende beregninger skal hentes fra bidrag-vedtak: $hentBeregningFraBidragVedtakListe" }
            val beregningListe = mutableListOf<BidragBeregningResponsDto.BidragBeregning>()

            map {
                secureLogger.info { "Behandler VedtakForStønad: $it" }
                val beregning = finnBeregningIBidragVedtak(it)
                if (beregning != null) {
                    secureLogger.info { "Legger til følgende beregning for vedtak ${it.vedtaksid} i bidrag-vedtak: $beregning" }
                    beregningListe.add(beregning)
                }
            }
            bidragBeregningResponsDtoFraBidragVedtak = BidragBeregningResponsDto(beregningListe)
        }

        // Returnerer sammenslått beregningsgrunnlag fra BBM og bidrag-vedtak
        return BidragBeregningResponsDto(
            bidragBeregningResponsDtoFraBBM.beregningListe + bidragBeregningResponsDtoFraBidragVedtak.beregningListe,
        )
    }

    private fun finnBeregningIBidragVedtak(vedtakForStønad: VedtakForStønad): BidragBeregningResponsDto.BidragBeregning? {
        // Henter vedtak fra bidrag-vedtak (med fullstendige opplysninger)
        val vedtakDto = vedtakService.hentVedtak(vedtakForStønad.vedtaksid)
        if (vedtakDto == null) {
            secureLogger.warn { "Fant ikke vedtak for vedtaksid ${vedtakForStønad.vedtaksid} i bidrag-vedtak." }
            return null
        }

        // Henter stønadsendringen fra vedtaket som matcher med det som ligger i VedtakForStønad
        val stønadsendringDto =
            vedtakDto.stønadsendringListe.firstOrNull { stønadsendringDto ->
                stønadsendringDto.type == vedtakForStønad.stønadsendring.type &&
                    stønadsendringDto.sak == vedtakForStønad.stønadsendring.sak &&
                    stønadsendringDto.skyldner == vedtakForStønad.stønadsendring.skyldner &&
                    stønadsendringDto.kravhaver == vedtakForStønad.stønadsendring.kravhaver
            }
        if (stønadsendringDto == null) {
            secureLogger.warn { "Fant ikke stønadsendring for vedtak ${vedtakForStønad.vedtaksid} i bidrag-vedtak." }
            return null
        }
        secureLogger.info { "Fant stønadsendring for vedtak ${vedtakForStønad.vedtaksid} i bidrag-vedtak: $stønadsendringDto" }

        // Finner siste periode i stønadsendringen
        val sistePeriode =
            stønadsendringDto.periodeListe.maxByOrNull { periode -> periode.periode.fom } ?: run {
                secureLogger.warn {
                    "Fant ikke siste periode for vedtak ${vedtakForStønad.vedtaksid} og stønadsendring $stønadsendringDto i " +
                        "bidrag-vedtak."
                }
                return null
            }

        // Finner sluttberegning-grunnlaget
        val sluttberegningGrunnlag =
            vedtakDto.grunnlagListe.finnSluttberegningIReferanser(sistePeriode.grunnlagReferanseListe) ?: run {
                secureLogger.warn {
                    "Fant ikke sluttberegning i siste periode i grunnlag for vedtak ${vedtakForStønad.vedtaksid} og " +
                        "stønadsendring $stønadsendringDto i bidrag-vedtak."
                }
                return null
            }
        // Finner samværsklasse
        val samværsklasse = vedtakDto.grunnlagListe.finnSamværsklasse(sluttberegningGrunnlag)
        secureLogger.info { "Samværsklasse: $samværsklasse" }

        return BidragBeregningResponsDto.BidragBeregning(
            periode = finnPeriode(sluttberegningGrunnlag),
            saksnummer = vedtakForStønad.stønadsendring.sak.verdi,
            personidentBarn = vedtakForStønad.stønadsendring.kravhaver,
            datoSøknad = LocalDate.now(), // Brukes ikke
            beregnetBeløp = vedtakDto.grunnlagListe.finnBidragTilFordeling(sluttberegningGrunnlag).avrundetTilNærmesteTier,
            faktiskBeløp = vedtakDto.grunnlagListe.finnBidragJustertForBarnetilleggBP(sluttberegningGrunnlag).avrundetTilNærmesteTier,
            beløpSamvær = BigDecimal.ZERO, // Brukes ikke
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = samværsklasse,
        )
    }

    private fun finnPeriode(sluttberegningGrunnlag: GrunnlagDto): ÅrMånedsperiode {
        if (sluttberegningGrunnlag.erSluttberegningGammelStruktur()) {
            val sluttberegningObjekt = sluttberegningGrunnlag.innholdTilObjekt<SluttberegningBarnebidrag>()
            return sluttberegningObjekt.periode
        }
        val sluttberegningObjekt = sluttberegningGrunnlag.innholdTilObjekt<SluttberegningBarnebidragV2>()
        return sluttberegningObjekt.periode
    }

    private fun List<LøpendeBidragssak>.hentLøpendeVedtak(bidragspliktigIdent: Personident): List<VedtakForStønad> = mapNotNull {
        vedtakService.finnSisteManuelleVedtakForEvnevurdering(
            Stønadsid(
                type = it.type,
                kravhaver = it.kravhaver,
                skyldner = bidragspliktigIdent,
                sak = it.sak,
            ),
        )
    }
}

data class EvnevurderingBeregningResultat(val beregnetBeløpListe: BidragBeregningResponsDto, val løpendeBidragsaker: List<LøpendeBidragssak>)

// TODO Gå gjennom denne
fun EvnevurderingBeregningResultat.tilGrunnlagDto(bpReferanse: String): List<GrunnlagDto> {
    val grunnlag =
        GrunnlagDto(
            referanse = "XXX", // TODO
            gjelderReferanse = bpReferanse,
            gjelderBarnReferanse = "XXX", // TODO
            type = Grunnlagstype.LØPENDE_BIDRAG,
            innhold = POJONode(
                løpendeBidragsaker.map { løpendeStønad ->
                    val beregning = beregnetBeløpListe.beregningListe.find { it.personidentBarn == løpendeStønad.kravhaver }
                    LøpendeBidragPeriode(
                        periode = beregning?.periode!!,
                        saksnummer = Saksnummer(løpendeStønad.sak.verdi),
                        stønadstype = løpendeStønad.type,
                        løpendeBeløp = løpendeStønad.løpendeBeløp,
                        valutakode = løpendeStønad.valutakode,
                        samværsklasse = beregning.samværsklasse ?: Samværsklasse.SAMVÆRSKLASSE_0,
                        beregnetBeløp = beregning.beregnetBeløp,
                        faktiskBeløp = beregning.faktiskBeløp,
                    )
                },
            ),
        )
    return mutableListOf(grunnlag)
}
