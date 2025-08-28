package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponse
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service

@Service
@Import(BeregnBarnebidragApi::class, OmgjøringOrkestrator::class)
class BidragsberegningOrkestrator(private val barnebidragApi: BeregnBarnebidragApi, private val omgjøringOrkestrator: OmgjøringOrkestrator) {

    fun utførBidragsberegning(request: BidragsberegningOrkestratorRequest): BidragsberegningOrkestratorResponse {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.debug { "Utfører bidragsberegning for request: $request" }
                val beregningResultat = barnebidragApi.beregn(
                    beregnGrunnlag = request.beregnGrunnlag,
                )
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
                val klageberegningResultat = barnebidragApi.beregn(
                    beregnGrunnlag = request.beregnGrunnlag,
                )
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
                val klageberegningResultat = barnebidragApi.beregn(
                    beregnGrunnlag = request.beregnGrunnlag,
                )
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
}
