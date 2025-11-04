package no.nav.bidrag.beregn.barnebidrag.service.orkestrering

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregningGrunnlagV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequestV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponse
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponseV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningResultatBarnV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtakV2
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnGyldigeGrunnlagForBarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service

@Service
@Import(BeregnBarnebidragApi::class, OmgjøringOrkestrator::class)
class BidragsberegningOrkestratorV2(
    private val barnebidragApi: BeregnBarnebidragApi,
    private val omgjøringOrkestrator: OmgjøringOrkestrator,
    private val hentLøpendeBidragService: HentLøpendeBidragService,
) {

    fun utførBidragsberegningV3(request: BidragsberegningOrkestratorRequestV2): BidragsberegningOrkestratorResponseV2 {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }

                val søknadsbarnIdentMap = hentAlleSøknadsbarn(request.beregningBarn, request.grunnlagsliste)
                val bidragspliktig = request.grunnlagsliste.bidragspliktig!!

                // Henter grunnlag for løpende bidrag (som ikke er en del av søknadsgrunnlagene)
                val evnevurderingBeregningResultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
                    bidragspliktigIdent = Personident(bidragspliktig.personIdent!!),
                    søknadsbarnidentMap = søknadsbarnIdentMap,
                )
                val løpendeBidragListe = evnevurderingBeregningResultat.tilGrunnlagDto(bidragspliktig.referanse)

                // Sjekk om det skal gis direkte avslag for alle barn
                if (request.erDirekteAvslag) {
                    // Kaller beregning for ett og ett søknadsbarn
                    val respons = request.beregningBarn.map { beregningBarn ->
                        try {
                            val beregningResultat =
                                barnebidragApi.opprettAvslag(
                                    beregnGrunnlag = beregningBarn.tilBeregnGrunnlagV1(
                                        request.grunnlagsliste,
                                    ).leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
                                )
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
                    secureLogger.debug { "Direkte avslag, respons fra beregning: $respons" }

                    return BidragsberegningOrkestratorResponseV2(
                        grunnlagListe = respons.flatMap { it.second }.distinct(),
                        resultat = respons.map { it.first },
                    )
                } else {
                    // Kaller beregning for alle barn samlet
                    val beregnGrunnlagListe = request.tilListeBeregnGrunnlagV1(
                        grunnlagListe = request.grunnlagsliste + løpendeBidragListe,
                    )
                    return try {
                        val beregningResultat =
                            barnebidragApi.beregnV2(
                                beregnGrunnlagListe = beregnGrunnlagListe,
                            )

                        secureLogger.debug { "Resultat av bidragsberegning: $beregningResultat" }
                        BidragsberegningOrkestratorResponseV2(
                            grunnlagListe = beregningResultat.flatMap { it.second }.distinct(),
                            resultat = beregningResultat.map { it.first },
                        )
                    } catch (e: Exception) {
                        secureLogger.error(e) { "Feil ved beregning for flere barn i bidragsberegningorkestrator" }
                        BidragsberegningOrkestratorResponseV2(
                            grunnlagListe = request.grunnlagsliste,
                            resultat = request.beregningBarn.map {
                                BidragsberegningResultatBarnV2(
                                    søknadsbarnreferanse = it.søknadsbarnreferanse,
                                    resultatVedtakListe = emptyList(),
                                    beregningsfeil = e,
                                )
                            },
                        )
                    }
                }
            }

            Beregningstype.OMGJØRING -> {
                secureLogger.debug { "Utfører omgjøringsberegning for request: $request" }
                val respons = request.beregningBarn.map {
                    try {
                        val klageberegningResultat = if (request.erDirekteAvslag) {
                            // Avslagsperiode skal alltid være løpende hvis det ikke kommer noe periode etter opphøret (feks ved etterfølgende vedtak i orkestrering)
                            barnebidragApi.opprettAvslag(
                                beregnGrunnlag = it.tilBeregnGrunnlagV1(request.grunnlagsliste)
                                    .leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
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
                                beregnGrunnlag = barn.tilBeregnGrunnlagV1(request.grunnlagsliste)
                                    .leggTilÅpenSluttperiodeHvisDirekteAvslagBeregning(),
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
                        ResultatVedtak(
                            resultat = beregningResultat,
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

    private fun BidragsberegningOrkestratorRequestV2.tilListeBeregnGrunnlagV1(grunnlagListe: List<GrunnlagDto>): List<BeregnGrunnlag> =
        beregningBarn.map { beregningBarn ->
            beregningBarn.tilBeregnGrunnlagV1(grunnlagListe)
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
