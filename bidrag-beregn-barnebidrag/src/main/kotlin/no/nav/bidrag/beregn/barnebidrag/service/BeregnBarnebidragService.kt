package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.service.BeregnBidragsevneService.delberegningBidragsevne
import no.nav.bidrag.beregn.barnebidrag.service.BeregnSamværsfradragService.delberegningSamværsfradrag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

class BeregnBarnebidragService : BeregnService() {

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
}
