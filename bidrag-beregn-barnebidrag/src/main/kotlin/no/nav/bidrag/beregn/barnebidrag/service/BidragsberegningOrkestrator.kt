package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.node.POJONode
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponse
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.OmgjøringOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagPeriodeInnhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Import(BeregnBarnebidragApi::class, OmgjøringOrkestrator::class)
class BidragsberegningOrkestrator(
    private val barnebidragApi: BeregnBarnebidragApi,
    private val omgjøringOrkestrator: OmgjøringOrkestrator,
    private val vedtakService: VedtakService,
) {

    fun utførBidragsberegning(request: BidragsberegningOrkestratorRequest): BidragsberegningOrkestratorResponse {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }

                // Henter ut alle unike søknadsbarn fra grunnlagslista
                val søknadsbarnIdentMap: Map<Personident, String> =
                    request.beregnGrunnlagListe
                        .flatMap { grunnlag ->
                            grunnlag.grunnlagListe
                                .filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
                                .mapNotNull {
                                    it.innhold.ident?.let { ident ->
                                        ident to it.referanse
                                    }
                                }
                        }
                        .toMap()

                // TODO Kaste exception hvis ikke funnet
                val bidragspliktig = request.beregnGrunnlagListe[0].grunnlagListe.bidragspliktig!!

                // Hvis det finnes løpende bidrag som mangler grunnlag må det hentes inn grunnlag for disse
                val løpendeBidragListe = hentLøpendeBidrag(
                    bidragspliktigIdent = Personident(bidragspliktig.personIdent!!),
                    bidragspliktigReferanse = bidragspliktig.referanse,
                    søknadsbarnIdentMap = søknadsbarnIdentMap,
                )

                var beregningResultatListe: List<BeregnetBarnebidragResultat>

                // Hvis det bare er ett søknadsbarn og ingen andre løpende saker kjøres ordinær beregning
                if (søknadsbarnIdentMap.size == 1 && løpendeBidragListe.isEmpty()) {
                    beregningResultatListe = listOf(barnebidragApi.beregn(request.beregnGrunnlagListe[0]))
                    // Hvis ikke kjøres beregning med sjekk av om det blir forholdsmessig fordeling
                } else {
                    // TODO Skal returnere en liste
                    beregningResultatListe = utførBeregningMedFF(request, løpendeBidragListe)
                }

                // TODO Respons må endres til å returnere en liste i resultatet
                val respons = BidragsberegningOrkestratorResponse(
                    listOf(
                        ResultatVedtak(
                            resultat = beregningResultatListe[0],
                            delvedtak = false,
                            omgjøringsvedtak = false,
                            vedtakstype = Vedtakstype.ENDRING,
                        ),
                    ),
                )
                secureLogger.debug { "Resultat av bidragsberegning: $respons" }
                return respons
            }

            Beregningstype.OMGJØRING -> {
                secureLogger.debug { "Utfører omgjøringsberegning for request: $request" }
                val klageberegningResultat = barnebidragApi.beregn(
                    beregnGrunnlag = request.beregnGrunnlagListe[0],
                )
                val respons = BidragsberegningOrkestratorResponse(
                    listOf(
                        ResultatVedtak(
                            resultat = klageberegningResultat,
                            delvedtak = true,
                            omgjøringsvedtak = true,
                            vedtakstype = Vedtakstype.KLAGE,
                        ),
                    ),
                )
                secureLogger.debug { "Resultat av omgjøringsberegning: $respons" }
                return respons
            }

            Beregningstype.OMGJØRING_ENDELIG -> {
                secureLogger.debug { "Utfører endelig omgjøringsberegning for request: $request" }
                val klageberegningResultat = barnebidragApi.beregn(
                    beregnGrunnlag = request.beregnGrunnlagListe[0],
                )
                val endeligKlageberegningResultat = omgjøringOrkestrator.utførOmgjøringEndelig(
                    omgjøringResultat = klageberegningResultat,
                    omgjøringGrunnlag = request.beregnGrunnlagListe[0],
                    omgjøringOrkestratorGrunnlag =
                    request.omgjøringOrkestratorGrunnlag ?: throw IllegalArgumentException("klageOrkestratorGrunnlag må være angitt"),
                )
                val respons = BidragsberegningOrkestratorResponse(endeligKlageberegningResultat)
                secureLogger.debug { "Resultat av endelig klageberegning: $respons" }
                return respons
            }
        }
    }

    // Kjører beregning med forholdsmessig fordeling
    // 3 deler:
    // - 1) For hvert søknadsbarn: Kjør beregning til og med beregning av BP's andel av U (+ hensynta BM's barnetillegg og 25% av inntekt)
    // - 2) Summer BP's andel av U for alle søknadsbarn og løpende bidrag og sammenlign med BP's bidragsevne
    // - 3) For hvert søknadsbarn: Kjør siste del av beregningen
    private fun utførBeregningMedFF(
        request: BidragsberegningOrkestratorRequest,
        løpendeBidragListe: List<GrunnlagDto>,
    ): List<BeregnetBarnebidragResultat> {
        val forholdsmessigFordelingGrunnlagPeriodeListe = mutableListOf<ForholdsmessigFordelingGrunnlagPeriode>()

        // Kaller første del av beregningen for hvert søknadsbarn
//        request.beregnGrunnlagListe.forEach {
//            forholdsmessigFordelingGrunnlagPeriodeListe.add(barnebidragApi.beregnGrunnlagTilForholdsmessigFordeling(it))
//        }

        // Sjekker for og gjør en initiell beregning av forholdsmessig fordeling
//        val forholdsmessigFordelingPeriodeListe =
//            barnebidragApi.beregnForholdsmessigFordeling(
//                beregnetBarnebidragResultatListe = beregnetBarnebidragResultatListeFørsteDel,
//                løpendeBidragListe = løpendeBidragListe,
//            )

        // Kaller siste del av beregningen for hvert søknadsbarn
        // TODO Legge delberegning forholdsmessig fordeling inn i utvidet grunnlag (skal inngå i kall til siste del av beregningen
        val beregnetBarnebidragResultatListe = mutableListOf<BeregnetBarnebidragResultat>()
        request.beregnGrunnlagListe.forEach {
            beregnetBarnebidragResultatListe.add(
                barnebidragApi.beregnMedForholdsmessigFordeling(it),
            )
        }

        return beregnetBarnebidragResultatListe
    }

    // TODO Hvilken info trenger vi egentlig å hente?
    private fun hentLøpendeBidrag(
        bidragspliktigIdent: Personident,
        bidragspliktigReferanse: String,
        søknadsbarnIdentMap: Map<Personident, String>,
    ): List<GrunnlagDto> {
        val grunnlagListe = mutableListOf<GrunnlagDto>()

        // Henter BP's løpende stønader, men filtrerer bort de som inneholder noen av søknadsbarna
        val løpendeStønader = vedtakService.hentSisteLøpendeStønader(bidragspliktigIdent).filterNot { it.kravhaver in søknadsbarnIdentMap }

        // Finner siste manuelle vedtak for hver løpende stønad
        val sisteManuelleVedtakListe =
            løpendeStønader.mapNotNull {
                vedtakService.finnSisteManuelleVedtakForEvnevurdering(
                    Stønadsid(
                        type = it.type,
                        kravhaver = it.kravhaver,
                        skyldner = bidragspliktigIdent,
                        sak = it.sak,
                    ),
                )
            }

        // Bygger opp liste over løpende grunnlag
        sisteManuelleVedtakListe.forEach { sisteManuelleVedtak ->
            // TODO Kaste exception hvis det returneres null?
            val beregningsdetaljerFraVedtak = hentBeregningsdetaljer(sisteManuelleVedtak)
            grunnlagListe.add(
                GrunnlagDto(
                    referanse = "løpende_bidrag_$bidragspliktigReferanse",
                    gjelderReferanse = bidragspliktigReferanse,
                    type = Grunnlagstype.LØPENDE_BIDRAG,
                    innhold = POJONode(
                        LøpendeBidragGrunnlag(
                            løpendeBidragListe =
                            beregningsdetaljerFraVedtak.beregningListe.map {
                                // TODO Sjekk verdiene her
                                LøpendeBidrag(
                                    faktiskBeløp = it.faktiskBeløp,
                                    samværsklasse = it.samværsklasse!!,
                                    beregnetBeløp = it.beregnetBeløp,
                                    løpendeBeløp = it.faktiskBeløp,
                                    type = it.stønadstype,
                                    gjelderBarn = søknadsbarnIdentMap.getOrDefault(it.personidentBarn, "UKJENT"),
                                    saksnummer = Saksnummer(it.saksnummer),
                                    valutakode = "NOK",
                                )
                            },
                        ),
                    ),
                ),
            )
        }

        return grunnlagListe
    }

    // TODO Kode delvis kopiert fra bidrag-behandling
    private fun hentBeregningsdetaljer(vedtak: VedtakForStønad): BidragBeregningResponsDto {
        val beregningListe: List<BidragBeregningResponsDto.BidragBeregning>

        // Bestemmer om vedtaket skal hentes fra bidrag-vedtak eller fra BBM
        if (vedtak.behandlingsreferanser.any { it.kilde == BehandlingsrefKilde.BEHANDLING_ID }) {
            // Henter beregningsgrunnlag fra bidrag-vedtak
            val beregning = finnBeregningIBidragVedtak(vedtak)
            beregningListe = if (beregning != null) listOf(beregning) else emptyList()
            secureLogger.info { "Respons fra bidrag-vedtak: $beregning" }
        } else {
            // Henter beregningsgrunnlag fra BBM
            // TODO Utvide respons med U, BP's andel av U ++ (endring i bidrag-bbm)
            val bidragBeregningResponsDtoFraBBM = vedtakService.hentBeregningFraBBM(listOf(vedtak))
            beregningListe = bidragBeregningResponsDtoFraBBM.beregningListe
            secureLogger.info { "Respons fra BBM: $bidragBeregningResponsDtoFraBBM" }
        }

        return BidragBeregningResponsDto(beregningListe)
    }

    // TODO Kode kopiert fra bidrag-behandling
    // TODO Utvide respons med U, BP's andel av U ++
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
        val sluttberegningReferanse =
            sistePeriode.grunnlagReferanseListe.firstOrNull { grunnlagsReferanse ->
                grunnlagsReferanse.lowercase().contains("sluttberegning")
            } ?: ""
        val sluttberegningGrunnlag =
            vedtakDto.grunnlagListe.finnSluttberegningIReferanser(listOf(sluttberegningReferanse)) ?: run {
                secureLogger.warn {
                    "Fant ikke sluttberegning i siste periode i grunnlag for vedtak ${vedtakForStønad.vedtaksid} og " +
                        "stønadsendring $stønadsendringDto i bidrag-vedtak."
                }
                return null
            }
        secureLogger.info { "Fant sluttberegning-grunnlag: $sluttberegningGrunnlag" }
        val sluttberegningObjekt = sluttberegningGrunnlag.innholdTilObjekt<SluttberegningBarnebidrag>()

        // Henter ut alle grunnlag som refereres av sluttberegning
        val grunnlagListeSluttberegningSistePeriode =
            vedtakDto.grunnlagListe.filter { grunnlag -> grunnlag.referanse in sluttberegningGrunnlag.grunnlagsreferanseListe }

        // Finner samværsklasse
        val samværsklasse =
            (
                grunnlagListeSluttberegningSistePeriode
                    .filtrerOgKonverterBasertPåEgenReferanse<SamværsperiodeGrunnlag>(Grunnlagstype.SAMVÆRSPERIODE)
                    .firstOrNull()
                    ?: run {
                        secureLogger.warn { "Fant ikke tilhørende samværsklasse i sluttberegning med referanse $sluttberegningReferanse." }
                        return null
                    }
                ).innhold.samværsklasse
        secureLogger.info { "Samværsklasse: $samværsklasse" }

        // Finner underholdskostnad
        val underholdskostnad =
            (
                grunnlagListeSluttberegningSistePeriode
                    .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
                    .firstOrNull()
                    ?: run {
                        secureLogger.warn { "Fant ikke tilhørende underholdskostnad i sluttberegning med referanse $sluttberegningReferanse." }
                        return null
                    }
                ).innhold.underholdskostnad
        secureLogger.info { "Samværsklasse: $samværsklasse" }

        // Finner BPs andel av underholdskotnad
        val bpAndelUnderholdskostnad =
            (
                grunnlagListeSluttberegningSistePeriode
                    .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
                    .firstOrNull()
                    ?: run {
                        secureLogger.warn {
                            "Fant ikke tilhørende BP's andel av underholdskostnad i sluttberegning med referanse $sluttberegningReferanse."
                        }
                        return null
                    }
                ).innhold.andelBeløp
        secureLogger.info { "Samværsklasse: $samværsklasse" }

        return BidragBeregningResponsDto.BidragBeregning(
            saksnummer = vedtakForStønad.stønadsendring.sak.verdi,
            personidentBarn = vedtakForStønad.stønadsendring.kravhaver,
            gjelderFom = LocalDate.now(), // Brukes ikke
            datoSøknad = LocalDate.now(), // Brukes ikke
            beregnetBeløp = sluttberegningObjekt.bruttoBidragEtterBarnetilleggBM.avrundetTilNærmesteTier,
            faktiskBeløp = sluttberegningObjekt.bruttoBidragEtterBarnetilleggBP.avrundetTilNærmesteTier,
            beløpSamvær = BigDecimal.ZERO, // Brukes ikke
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = samværsklasse,
            underholdskostnad = underholdskostnad,
            bpAndelUnderholdskostnad = bpAndelUnderholdskostnad,
        )
    }

    // TODO Bør flyttes? Ligger allerede i CoreMapper
    fun finnReferanseTilRolle(grunnlagListe: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) = grunnlagListe
        .firstOrNull { it.type == grunnlagstype }?.referanse ?: throw NoSuchElementException("Grunnlagstype $grunnlagstype mangler i input")
}

// TODO Flytte til bidrag-felles
@Schema(description = "Request til BidragsberegningOrkestrator")
data class BidragsberegningOrkestratorRequest(
    @Schema(description = "Grunnlag for beregning av barnebidrag")
    val beregnGrunnlagListe: List<BeregnGrunnlag> = emptyList(),
    @Schema(description = "Grunnlag for orkestrering av klage")
    @JsonAlias("klageOrkestratorGrunnlag")
    val omgjøringOrkestratorGrunnlag: OmgjøringOrkestratorGrunnlag? = null,
    val erDirekteAvslag: Boolean = false,
    @Schema(description = "Type beregning")
    val beregningstype: Beregningstype = Beregningstype.BIDRAG,
)

@Schema(description = "Respons fra beregning av grunnlag til forholdsmessig fordeling")
data class ForholdsmessigFordelingGrunnlagPeriode(
    override val periode: ÅrMånedsperiode,
    val bidragsevne: BigDecimal,
    val tjuefemProsentAvInntekt: BigDecimal,
    val underholdskostnad: BigDecimal,
    val bpAndelAvUnderholdskostnad: BigDecimal,
    val bmBarnetillegg: BigDecimal,
    override val manueltRegistrert: Boolean,
) : GrunnlagPeriodeInnhold
