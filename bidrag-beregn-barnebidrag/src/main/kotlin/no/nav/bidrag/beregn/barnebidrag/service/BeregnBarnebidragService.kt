package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.service.BeregnBidragsevneService.delberegningBidragsevne
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBpAndelUnderholdskostnadService.delberegningBpAndelUnderholdskostnad
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndeligBidragService.delberegningEndeligBidrag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoTilsynsutgiftService.delberegningNettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.service.BeregnSamværsfradragService.delberegningSamværsfradrag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

class BeregnBarnebidragService : BeregnService() {

    // Beregning av bidragsevne
    fun beregnBidragsevne(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av bidragsevne - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av bidragsevne: " + e.message)
        }

        // Kaller delberegninger
        val delberegningBidragsevneResultat = delberegningBidragsevne(mottattGrunnlag)

        return delberegningBidragsevneResultat
    }

    // Beregning av netto tilsynsutgift
    fun beregnNettoTilsynsutgift(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av netto tilsynsutgift - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }
        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av netto tilsynsutgift: " + e.message)
        }

        // Kaller delberegninger
        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(mottattGrunnlag)
        return delberegningNettoTilsynsutgiftResultat
    }

    // Beregning av BP's andel av underholdskostnad
    fun beregnBpAndelUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av BP's andel av underholdskostnad - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av BP's andel av underholdskostnad: " + e.message)
        }

        // Kaller delberegninger
        val delberegningBpAndelUnderholdskostnadResultat = delberegningBpAndelUnderholdskostnad(mottattGrunnlag)

        return delberegningBpAndelUnderholdskostnadResultat
    }

    // Beregning av samværsfradrag
    fun beregnSamværsfradrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av samværsfradrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av samværsfradrag: " + e.message)
        }

        // Kaller delberegninger
        val delberegningSamværsfradragResultat = delberegningSamværsfradrag(mottattGrunnlag)

        return delberegningSamværsfradragResultat
    }

    // Beregning av endelig bidrag (sluttberegning)
    fun beregnEndeligBidrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av endelig bidrag (sluttberegning) - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av endelig bidrag (sluttberegning): " + e.message)
        }

        // Kaller delberegninger
        val delberegningEndeligBidragResultat = delberegningEndeligBidrag(mottattGrunnlag)

        return delberegningEndeligBidragResultat
    }
}
