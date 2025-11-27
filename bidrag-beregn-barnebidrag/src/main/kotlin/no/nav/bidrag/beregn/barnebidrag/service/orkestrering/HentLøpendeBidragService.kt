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
import no.nav.bidrag.transport.behandling.belopshistorikk.request.LøpendeBidragPeriodeRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidrag
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragPeriodeResponse
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSamværsklasse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentBeregnetBeløp
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentResultatBeløp
import no.nav.bidrag.transport.behandling.felles.grunnlag.tilPersonreferanse
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.felles.toCompactString
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
@Import(Vedtaksfiltrering::class)
class HentLøpendeBidragService(private val vedtakService: VedtakService) {
    @Timed
    fun hentLøpendeBidragForBehandling(bpIdent: Personident, beregningsperiode: ÅrMånedsperiode): LøpendeBidragOgBeregninger {
        try {
            // Henter alle bidrag tilknyttet BP som er eller har vært løpende i beregningsperioden. Filtrerer først
            // bort perioder som er utenfor beregningsperioden. Stønader som har ingen perioder innenfor beregningsperioden fjernes.
            val løpendeBidragIPerioden = vedtakService.hentAlleStønaderForBidragspliktig(
                LøpendeBidragPeriodeRequest(bpIdent, beregningsperiode),
            ).filtrerForPeriode(beregningsperiode)

            secureLogger.info {
                "Hentet løpende bidrag i perioden:  ${løpendeBidragIPerioden.joinToString { it.toString() }} " +
                    "for BP: ${bpIdent.verdi}"
            }

//          Henter manuelle vedtak som har perioder som overlapper beregningsperioden
            val manuelleVedtak = løpendeBidragIPerioden.hentManuelleVedtak(bpIdent).sortedByDescending { it.vedtakstidspunkt }
                .filtrerVedtakMotBeregningsperiode(beregningsperiode)

            secureLogger.info { "Hentede manuelle vedtak: ${manuelleVedtak.joinToString { it.toString() }} for BP: ${bpIdent.verdi}" }
            val beregningsdataIManuelleVedtak = manuelleVedtak.hentBeregning()
            secureLogger.info {
                "Hentede beregningsdata i manuelle vedtak: " +
                    "${beregningsdataIManuelleVedtak.beregningListe.joinToString { it.toString() }} } "
            }

            return LøpendeBidragOgBeregninger(
                beregnetBeløpListe = beregningsdataIManuelleVedtak,
                løpendeBidragListe = løpendeBidragIPerioden,
            )
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
            secureLogger.info { "Følgende beregninger skal hentes fra BBM: ${hentBeregningFraBBMListe.joinToString { it.toString() }}" }
            bidragBeregningResponsDtoFraBBM =
                vedtakService.hentAlleBeregningerFraBBM(hentBeregningFraBBMListe)
            secureLogger.info { "Respons fra BBM: $bidragBeregningResponsDtoFraBBM" }
        }

        // Henter beregningsgrunnlag fra bidrag-vedtak
        var bidragBeregningResponsDtoFraBidragVedtak = BidragBeregningResponsDto(emptyList())
        if (hentBeregningFraBidragVedtakListe.isNotEmpty()) {
            secureLogger.info {
                "Følgende beregninger skal hentes fra bidrag-vedtak: " +
                    hentBeregningFraBidragVedtakListe.joinToString { it.toString() }
            }
            val beregningListe = mutableListOf<BidragBeregningResponsDto.BidragBeregning>()

            map {
                secureLogger.info { "Behandler VedtakForStønad: $it" }
                val beregning = finnAlleBeregningerIBidragVedtak(it)
                if (beregning.isNotEmpty()) {
                    secureLogger.info { "Legger til følgende beregning for vedtak ${it.vedtaksid} i bidrag-vedtak: $beregning" }
                    beregningListe.addAll(beregning)
                }
            }
            bidragBeregningResponsDtoFraBidragVedtak = BidragBeregningResponsDto(beregningListe)
        }

        // Returnerer sammenslått beregningsgrunnlag fra BBM og bidrag-vedtak
        return BidragBeregningResponsDto(
            bidragBeregningResponsDtoFraBBM.beregningListe + bidragBeregningResponsDtoFraBidragVedtak.beregningListe,
        )
    }

    private fun finnAlleBeregningerIBidragVedtak(vedtakForStønad: VedtakForStønad): List<BidragBeregningResponsDto.BidragBeregning> {
        // Henter vedtak fra bidrag-vedtak (med fullstendige opplysninger)
        val vedtakDto = vedtakService.hentVedtak(vedtakForStønad.vedtaksid)
        if (vedtakDto == null) {
            secureLogger.warn { "Fant ikke vedtak for vedtaksid ${vedtakForStønad.vedtaksid} i bidrag-vedtak." }
            return emptyList()
        }

        val bidragBeregningListe = mutableListOf<BidragBeregningResponsDto.BidragBeregning>()

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
            return emptyList()
        }
        secureLogger.info { "Fant stønadsendring for vedtak ${vedtakForStønad.vedtaksid} i bidrag-vedtak: $stønadsendringDto" }

        stønadsendringDto.periodeListe.forEach { periode ->
            // Finner sluttberegning-grunnlaget
            val sluttberegningReferanse =
                periode.grunnlagReferanseListe.firstOrNull { grunnlagsReferanse ->
                    grunnlagsReferanse.lowercase().contains("sluttberegning")
                } ?: ""
            val sluttberegningGrunnlag =
                vedtakDto.grunnlagListe.finnSluttberegningIReferanser(listOf(sluttberegningReferanse))!!
            val beregnetBeløp = sluttberegningGrunnlag.hentBeregnetBeløp()
            val faktiskBeløp = sluttberegningGrunnlag.hentResultatBeløp()
            val samværsklasse = vedtakDto.grunnlagListe.finnSamværsklasse(sluttberegningGrunnlag)

            bidragBeregningListe.add(
                BidragBeregningResponsDto.BidragBeregning(
                    periode = periode.periode,
                    saksnummer = vedtakForStønad.stønadsendring.sak.verdi,
                    personidentBarn = vedtakForStønad.stønadsendring.kravhaver,
                    datoSøknad = LocalDate.now(), // Brukes ikke
                    beregnetBeløp = beregnetBeløp,
                    faktiskBeløp = faktiskBeløp,
                    beløpSamvær = BigDecimal.ZERO, // Brukes ikke
                    stønadstype = Stønadstype.BIDRAG,
                    samværsklasse = samværsklasse,
                ),
            )
        }
        return bidragBeregningListe
    }

    private fun List<LøpendeBidrag>.hentManuelleVedtak(bidragspliktigIdent: Personident): List<VedtakForStønad> = flatMap {
        vedtakService.finnAlleManuelleVedtakForEvnevurdering(
            Stønadsid(
                type = it.type,
                kravhaver = it.kravhaver,
                skyldner = bidragspliktigIdent,
                sak = it.sak,
            ),
        )
    }

    private fun LøpendeBidragPeriodeResponse.filtrerForPeriode(beregningsperiode: ÅrMånedsperiode): List<LøpendeBidrag> =
        bidragListe.mapNotNull { bidrag ->
            val periodeListe = bidrag.periodeListe.filter { it.periode.overlapper(beregningsperiode) }
            if (periodeListe.isNotEmpty()) {
                LøpendeBidrag(
                    sak = bidrag.sak,
                    type = bidrag.type,
                    kravhaver = bidrag.kravhaver,
                    periodeListe = periodeListe,
                )
            } else {
                null
            }
        }
}

data class LøpendeBidragOgBeregninger(val beregnetBeløpListe: BidragBeregningResponsDto, val løpendeBidragListe: List<LøpendeBidrag>)

// Skal returnere alle vedtak som overlapper med beregningsperioden. Listen med vedtak er sortert på vedtakstidspunkt descending slik at
// nyeste kommer først.
fun List<VedtakForStønad>.filtrerVedtakMotBeregningsperiode(beregningsperiode: ÅrMånedsperiode): List<VedtakForStønad> {
    val relevanteVedtakListe = mutableListOf<VedtakForStønad>()
    this.forEach { vedtak ->
        if (vedtak.stønadsendring.periodeListe.firstOrNull()?.periode?.fom?.isBefore(beregningsperiode.fom.plusMonths(1)) == true) {
            // vedtaket dekker starten av beregningsperioden og vi kan avslutte søket
            relevanteVedtakListe.add(vedtak)
            return relevanteVedtakListe
        }
        relevanteVedtakListe.add(vedtak)
    }
    return relevanteVedtakListe
}

fun LøpendeBidragOgBeregninger.tilBeregnGrunnlag(
    bpReferanse: String,
    søknadsbarnIdentMap: Map<Personident, String>,
    løpendeBarnFødselsdatoMap: Map<Personident, LocalDate?>,
): List<BeregnGrunnlag> {
    val beregnGrunnlagListe = mutableListOf<BeregnGrunnlag>()
    var forrigeKravhaver: Personident? = null

    this.løpendeBidragListe.sortedBy { it.kravhaver }.forEach { løpendeBidrag ->
        val grunnlagListe = mutableListOf<GrunnlagDto>()
        val fom = løpendeBidrag.periodeListe.minOf { it.periode.fom }
        val til = løpendeBidrag.periodeListe.mapNotNull { it.periode.til }.maxOrNull()

        val barnReferanse = søknadsbarnIdentMap[løpendeBidrag.kravhaver]
            ?: // barnet er ikke søknadsbarn, hent fødselsdato fra løpendeBarnFødselsdatoMap
            run {
                val fødselsdato = løpendeBarnFødselsdatoMap.filter { it.key == løpendeBidrag.kravhaver }
                    .values
                    .firstOrNull().toCompactString()
                val generertReferanse = Grunnlagstype.PERSON_BARN_BIDRAGSPLIKTIG.tilPersonreferanse(
                    (fødselsdato + "_innhentet"),
                    (løpendeBidrag.kravhaver.verdi + 1).hashCode(),
                )
                generertReferanse
            }

        løpendeBidrag.periodeListe.forEach { løpendeBidragPeriode ->
            val beregning = this.beregnetBeløpListe.beregningListe
                .sortedBy { it.periode?.fom }
                .find {
                    it.personidentBarn == løpendeBidrag.kravhaver &&
                        it.periode?.fom?.equals(løpendeBidragPeriode.periode.fom) == true
                } ?: this.beregnetBeløpListe.beregningListe.last()
            // hvis manuelt vedtak ikke har matchende periode så må seneste periode brukes
            BidragBeregningResponsDto(emptyList())

            val søknadsbarnReferanse = barnReferanse
            grunnlagListe.add(
                GrunnlagDto(
                    referanse = "innhentet_løpende_bidrag_${bpReferanse.let { "_$it" }}_$søknadsbarnReferanse" +
                        "_${løpendeBidragPeriode.periode.fom.toCompactString()}${løpendeBidragPeriode.periode.til?.let {
                            "_${it.toCompactString()}"
                        } ?: ""}",
                    gjelderReferanse = bpReferanse,
                    gjelderBarnReferanse = barnReferanse,
                    type = Grunnlagstype.LØPENDE_BIDRAG_PERIODE,
                    innhold = POJONode(
                        LøpendeBidragPeriode(
                            periode = beregning.periode!!,
                            saksnummer = Saksnummer(løpendeBidrag.sak.verdi),
                            stønadstype = løpendeBidrag.type,
                            løpendeBeløp = løpendeBidragPeriode.løpendeBeløp,
                            valutakode = løpendeBidragPeriode.valutakode,
                            samværsklasse = beregning.samværsklasse ?: Samværsklasse.SAMVÆRSKLASSE_0,
                            beregnetBeløp = beregning.beregnetBeløp,
                            faktiskBeløp = beregning.faktiskBeløp,
                        ),
                    ),
                ),
            )
        }
        beregnGrunnlagListe.add(
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(
                    fom = fom,
                    til = til,
                ),
                opphørsdato = null,
                stønadstype = løpendeBidrag.type,
                søknadsbarnReferanse = barnReferanse,
                grunnlagListe = grunnlagListe,
            ),
        )
    }
    return beregnGrunnlagListe
}
