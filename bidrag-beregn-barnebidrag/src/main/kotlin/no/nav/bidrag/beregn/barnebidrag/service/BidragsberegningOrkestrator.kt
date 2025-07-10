package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorResponse
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service

@Service
@Import(BeregnBarnebidragApi::class, KlageOrkestrator::class)
class BidragsberegningOrkestrator(private val barnebidragApi: BeregnBarnebidragApi, private val klageOrkestrator: KlageOrkestrator) {

    fun utførBidragsberegning(request: BidragsberegningOrkestratorRequest): BidragsberegningOrkestratorResponse {
        when (request.beregningstype) {
            Beregningstype.BIDRAG -> {
                secureLogger.info { "Utfører bidragsberegning for request: $request" }
                val beregningResultat = barnebidragApi.beregn(
                    request.beregnGrunnlag ?: throw IllegalArgumentException("beregnGrunnlag må være angitt"),
                )
                return BidragsberegningOrkestratorResponse(
                    listOf(ResultatVedtak(resultat = beregningResultat, delvedtak = false, klagevedtak = false)),
                )
            }
            Beregningstype.KLAGE -> {
                secureLogger.info { "Utfører klageberegning for request: $request" }
                val klageberegningResultat = barnebidragApi.beregn(
                    request.beregnGrunnlag ?: throw IllegalArgumentException("beregnGrunnlag må være angitt"),
                )
                return BidragsberegningOrkestratorResponse(
                    listOf(ResultatVedtak(resultat = klageberegningResultat, delvedtak = true, klagevedtak = true)),
                )
            }
            Beregningstype.KLAGE_ENDELIG -> {
                secureLogger.info { "Utfører endelig klageberegning for request: $request" }
                val endeligKlageberegningResultat = klageOrkestrator.utførKlageEndelig(
                    request.klageOrkestratorGrunnlag ?: throw IllegalArgumentException("klageOrkestratorGrunnlag må være angitt"),
                )
                return BidragsberegningOrkestratorResponse(endeligKlageberegningResultat)
            }
        }
    }
}
