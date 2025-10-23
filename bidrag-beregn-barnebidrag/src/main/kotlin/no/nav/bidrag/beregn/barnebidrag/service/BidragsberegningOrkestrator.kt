package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
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
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregningGrunnlagV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequestV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponse
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponseV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningResultatBarnV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtakV2
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnGyldigeGrunnlagForBarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
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

    fun utførBidragsberegningV3(request: BidragsberegningOrkestratorRequestV2): BidragsberegningOrkestratorResponseV2 {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }

                val søknadsbarnIdentMap = hentAlleSøknadsbarn(request.beregningBarn, request.grunnlagsliste)
                val bidragspliktig = request.grunnlagsliste.bidragspliktig!!

                // Henter grunnlag for løpende bidrag (som ikke er en del av søknadsgrunnlagene)
                // TODO
//                val løpendeBidragListe = hentLøpendeBidrag(
//                    bidragspliktigIdent = Personident(bidragspliktig.personIdent!!),
//                    bidragspliktigReferanse = bidragspliktig.referanse,
//                    søknadsbarnIdentMap = søknadsbarnIdentMap,
//                )

                // Kaller beregning for ett og ett søknadsbarn
                val respons = request.beregningBarn.map { beregningBarn ->
                    val bidragsmottakerReferanse =
                        request.grunnlagsliste.filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
                            .firstOrNull { it.referanse == beregningBarn.søknadsbarnreferanse }
                            ?.innhold?.bidragsmottaker
                            ?: throw IllegalArgumentException(
                                "Finner ikke bidragsmottaker for søknadsbarn med referanse ${beregningBarn.søknadsbarnreferanse}",
                            )

                    try {
                        val beregningResultat = if (request.erDirekteAvslag) {
                            barnebidragApi.opprettAvslag(
                                beregnGrunnlag = beregningBarn.tilBeregnGrunnlagV1(
                                    request.grunnlagsliste,
                                ).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = beregningBarn.tilBeregnGrunnlagV1(
                                    request.grunnlagsliste.finnGyldigeGrunnlagForBarn(
                                        bmRef = bidragsmottakerReferanse,
                                        bpRef = bidragspliktig.referanse,
                                        barnRef = beregningBarn.søknadsbarnreferanse,
                                    ),
                                ),
                            )
                        }
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = beregningBarn.søknadsbarnreferanse,
                            resultatVedtakListe = listOf(
                                ResultatVedtakV2(
                                    periodeListe = beregningResultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = false,
                                    omgjøringsvedtak = false,
                                    vedtakstype = Vedtakstype.ENDRING,
                                ),
                            ),
                        ) to beregningResultat.grunnlagListe
                    } catch (e: Exception) {
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = beregningBarn.søknadsbarnreferanse,
                            resultatVedtakListe = emptyList(),
                            beregningsfeil = e,
                        ) to request.grunnlagsliste
                    }
                }

                secureLogger.debug { "Resultat av bidragsberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    grunnlagListe = respons.flatMap { it.second }.distinct(),
                    resultat = respons.map { it.first },
                )
            }

            Beregningstype.OMGJØRING -> {
                secureLogger.debug { "Utfører omgjøringsberegning for request: $request" }
                val respons = request.beregningBarn.map {
                    try {
                        val klageberegningResultat = if (request.erDirekteAvslag) {
                            // Avslagsperiode skal alltid være løpende hvis det ikke kommer noe periode etter opphøret (feks ved etterfølgende vedtak i orkestrering)
                            barnebidragApi.opprettAvslag(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            )
                        }
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = it.søknadsbarnreferanse,
                            resultatVedtakListe = listOf(
                                ResultatVedtakV2(
                                    periodeListe = klageberegningResultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = false,
                                    omgjøringsvedtak = true,
                                    vedtakstype = Vedtakstype.KLAGE,
                                ),
                            ),
                        ) to klageberegningResultat.grunnlagListe
                    } catch (e: Exception) {
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = it.søknadsbarnreferanse,
                            resultatVedtakListe = emptyList(),
                            beregningsfeil = e,
                        ) to request.grunnlagsliste
                    }
                }
                secureLogger.debug { "Resultat av omgjøringsberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    grunnlagListe = respons.flatMap { it.second },
                    resultat = respons.map { it.first },
                )
            }

            Beregningstype.OMGJØRING_ENDELIG -> {
                secureLogger.debug { "Utfører endelig omgjøringsberegning for request: $request" }
                val respons = request.beregningBarn.map { barn ->
                    try {
                        val klageberegningResultat = if (request.erDirekteAvslag) {
                            barnebidragApi.opprettAvslag(
                                beregnGrunnlag = barn.tilBeregnGrunnlagV1(request.grunnlagsliste).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = barn.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            )
                        }
                        val endeligKlageberegningResultat = omgjøringOrkestrator.utførOmgjøringEndelig(
                            omgjøringResultat = klageberegningResultat,
                            omgjøringGrunnlag = barn.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            omgjøringOrkestratorGrunnlag =
                            barn.omgjøringOrkestratorGrunnlag ?: throw IllegalArgumentException("klageOrkestratorGrunnlag må være angitt"),
                        )
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = barn.søknadsbarnreferanse,
                            resultatVedtakListe = endeligKlageberegningResultat.map {
                                ResultatVedtakV2(
                                    periodeListe = it.resultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = it.delvedtak,
                                    grunnlagslisteDelvedtak = if (it.delvedtak) it.resultat.grunnlagListe else emptyList(),
                                    omgjøringsvedtak = it.omgjøringsvedtak,
                                    beregnet = it.beregnet,
                                    vedtakstype = it.vedtakstype,
                                )
                            },
                        ) to
                            endeligKlageberegningResultat.flatMap {
                                if (!it.delvedtak && !it.omgjøringsvedtak) {
                                    it.resultat.grunnlagListe
                                } else {
                                    emptyList()
                                }
                            }
                    } catch (ex: Exception) {
                        BidragsberegningResultatBarnV2(
                            søknadsbarnreferanse = barn.søknadsbarnreferanse,
                            resultatVedtakListe = emptyList(),
                            beregningsfeil = ex,
                        ) to request.grunnlagsliste
                    }
                }
                secureLogger.debug { "Resultat av endelig klageberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    grunnlagListe = respons.flatMap { it.second },
                    resultat = respons.map { it.first },
                )
            }
        }
    }

    // TODO: Ikke gjør endringer her. Lag en utførBidragsberegningV3 som tar hensyn til FF. utførBidragsberegningV3 skal ha samme api (input og output) som utførBidragsberegningV2
    fun utførBidragsberegningV2(request: BidragsberegningOrkestratorRequestV2): BidragsberegningOrkestratorResponseV2 {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }
                val respons = request.beregningBarn.map {
                    try {
                        val beregningResultat = if (request.erDirekteAvslag) {
                            barnebidragApi.opprettAvslag(
                                it.tilBeregnGrunnlagV1(request.grunnlagsliste).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            )
                        }
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            listOf(
                                ResultatVedtakV2(
                                    periodeListe = beregningResultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = false,
                                    omgjøringsvedtak = false,
                                    vedtakstype = Vedtakstype.ENDRING,
                                ),
                            ),
                        ) to beregningResultat.grunnlagListe
                    } catch (e: Exception) {
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            emptyList(),
                            beregningsfeil = e,
                        ) to request.grunnlagsliste
                    }
                }

                secureLogger.debug { "Resultat av bidragsberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    respons.flatMap { it.second },
                    respons.map { it.first },
                )
            }

            Beregningstype.OMGJØRING -> {
                secureLogger.debug { "Utfører omgjøringsberegning for request: $request" }
                val respons = request.beregningBarn.map {
                    try {
                        val klageberegningResultat = if (request.erDirekteAvslag) {
                            // Avslagsperiode skal alltid være løpende hvis det ikke kommer noe periode etter opphøret (feks ved etterfølgende vedtak i orkestrering)
                            barnebidragApi.opprettAvslag(
                                it.tilBeregnGrunnlagV1(request.grunnlagsliste).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            )
                        }
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            listOf(
                                ResultatVedtakV2(
                                    periodeListe = klageberegningResultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = false,
                                    omgjøringsvedtak = true,
                                    vedtakstype = Vedtakstype.KLAGE,
                                ),
                            ),
                        ) to klageberegningResultat.grunnlagListe
                    } catch (e: Exception) {
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            emptyList(),
                            beregningsfeil = e,
                        ) to request.grunnlagsliste
                    }
                }
                secureLogger.debug { "Resultat av omgjøringsberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    respons.flatMap { it.second },
                    respons.map { it.first },
                )
            }

            Beregningstype.OMGJØRING_ENDELIG -> {
                secureLogger.debug { "Utfører endelig omgjøringsberegning for request: $request" }
                val respons = request.beregningBarn.map {
                    try {
                        val klageberegningResultat = if (request.erDirekteAvslag) {
                            barnebidragApi.opprettAvslag(
                                it.tilBeregnGrunnlagV1(request.grunnlagsliste).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                            )
                        } else {
                            barnebidragApi.beregn(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            )
                        }
                        val endeligKlageberegningResultat = omgjøringOrkestrator.utførOmgjøringEndelig(
                            omgjøringResultat = klageberegningResultat,
                            omgjøringGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste),
                            omgjøringOrkestratorGrunnlag =
                            it.omgjøringOrkestratorGrunnlag ?: throw IllegalArgumentException("klageOrkestratorGrunnlag må være angitt"),
                        )
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            endeligKlageberegningResultat.map {
                                ResultatVedtakV2(
                                    periodeListe = it.resultat.beregnetBarnebidragPeriodeListe,
                                    delvedtak = it.delvedtak,
                                    grunnlagslisteDelvedtak = if (it.delvedtak) it.resultat.grunnlagListe else emptyList(),
                                    omgjøringsvedtak = it.omgjøringsvedtak,
                                    beregnet = it.beregnet,
                                    vedtakstype = it.vedtakstype,
                                )
                            },
                        ) to
                            endeligKlageberegningResultat.flatMap {
                                if (!it.delvedtak && !it.omgjøringsvedtak) {
                                    it.resultat.grunnlagListe
                                } else {
                                    emptyList()
                                }
                            }
                    } catch (ex: Exception) {
                        BidragsberegningResultatBarnV2(
                            it.søknadsbarnreferanse,
                            emptyList(),
                            beregningsfeil = ex,
                        ) to request.grunnlagsliste
                    }
                }
                secureLogger.debug { "Resultat av endelig klageberegning: $respons" }
                return BidragsberegningOrkestratorResponseV2(
                    respons.flatMap { it.second },
                    respons.map { it.first },
                )
            }
        }
    }

    @Deprecated("Bruk utførBidragsberegningV2 eller utførBidragsberegningV3 istedenfor")
    fun utførBidragsberegning(request: BidragsberegningOrkestratorRequest): BidragsberegningOrkestratorResponse {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }
                val beregningResultat = if (request.erDirekteAvslag) {
                    barnebidragApi.opprettAvslag(request.leggTilÅpenSluttperiodeHvisDirekteAvslag())
                } else {
                    barnebidragApi.beregn(
                        beregnGrunnlag = request.beregnGrunnlag,
                    )
                }
                val respons = BidragsberegningOrkestratorResponse(
                    listOf(
                        ResultatVedtak(resultat = beregningResultat, delvedtak = false, omgjøringsvedtak = false, vedtakstype = Vedtakstype.ENDRING),
                    ),
                )
                secureLogger.debug { "Resultat av bidragsberegning: $respons" }
                return respons
            }

            Beregningstype.OMGJØRING -> {
                secureLogger.debug { "Utfører omgjøringsberegning for request: $request" }
                val klageberegningResultat = if (request.erDirekteAvslag) {
                    // Avslagsperiode skal alltid være løpende hvis det ikke kommer noe periode etter opphøret (feks ved etterfølgende vedtak i orkestrering)
                    barnebidragApi.opprettAvslag(
                        request.leggTilÅpenSluttperiodeHvisDirekteAvslag(),
                    )
                } else {
                    barnebidragApi.beregn(
                        beregnGrunnlag = request.beregnGrunnlag,
                    )
                }
                val respons = BidragsberegningOrkestratorResponse(
                    listOf(
                        ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, omgjøringsvedtak = true, vedtakstype = Vedtakstype.KLAGE),
                    ),
                )
                secureLogger.debug { "Resultat av omgjøringsberegning: $respons" }
                return respons
            }

            Beregningstype.OMGJØRING_ENDELIG -> {
                secureLogger.debug { "Utfører endelig omgjøringsberegning for request: $request" }
                val klageberegningResultat = if (request.erDirekteAvslag) {
                    barnebidragApi.opprettAvslag(request.beregnGrunnlag)
                } else {
                    barnebidragApi.beregn(
                        beregnGrunnlag = request.beregnGrunnlag,
                    )
                }
                val endeligKlageberegningResultat = omgjøringOrkestrator.utførOmgjøringEndelig(
                    omgjøringResultat = klageberegningResultat,
                    omgjøringGrunnlag = request.beregnGrunnlag,
                    omgjøringOrkestratorGrunnlag =
                    request.omgjøringOrkestratorGrunnlag ?: throw IllegalArgumentException("klageOrkestratorGrunnlag må være angitt"),
                )
                val respons = BidragsberegningOrkestratorResponse(endeligKlageberegningResultat)
                secureLogger.debug { "Resultat av endelig klageberegning: $respons" }
                return respons
            }
        }
    }

    private fun BeregnGrunnlag.leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning() = copy(
        periode = periode.copy(
            til = null,
        ),
    )

    private fun BidragsberegningOrkestratorRequest.leggTilÅpenSluttperiodeHvisDirekteAvslag() = beregnGrunnlag.copy(
        periode = beregnGrunnlag.periode.copy(
            til = null,
        ),
    )

    private fun BeregningGrunnlagV2.tilBeregnGrunnlagV1(grunnlagListe: List<GrunnlagDto>) = BeregnGrunnlag(
        periode = periode,
        opphørsdato = opphørsdato,
        stønadstype = stønadstype,
        søknadsbarnReferanse = søknadsbarnreferanse,
        grunnlagListe = grunnlagListe,
    )

    // TODO Hvilken info trenger vi egentlig å hente?
    // Henter løpende bidrag for en bidragspliktig
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

    // Henter alle søknadsbarn og deres referanser og personidenter fra grunnlagslista
    private fun hentAlleSøknadsbarn(beregningBarnListe: List<BeregningGrunnlagV2>, grunnlagsliste: List<GrunnlagDto>): Map<Personident, String> {
        val søknadsbarnIdentMap = mutableMapOf<Personident, String>()
        beregningBarnListe.forEach { beregningsbarn ->
            val barnGrunnlag =
                grunnlagsliste.filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
                    .firstOrNull { it.referanse == beregningsbarn.søknadsbarnreferanse }
                    ?: throw IllegalArgumentException(
                        "Fant ikke PERSON_SØKNADSBARN-grunnlag for barn med referanse ${beregningsbarn.søknadsbarnreferanse}",
                    )
            søknadsbarnIdentMap[barnGrunnlag.innhold.ident!!] = beregningsbarn.søknadsbarnreferanse
        }
        return søknadsbarnIdentMap
    }
}
