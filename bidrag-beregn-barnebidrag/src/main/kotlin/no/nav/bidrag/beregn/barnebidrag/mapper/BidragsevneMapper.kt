package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.grunnlag.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.time.LocalDate

internal object BidragsevneMapper : CoreMapper() {
    fun mapBidragsevneGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): BidragsevnePeriodeGrunnlag =
        BidragsevnePeriodeGrunnlag(
            beregnDatoFra = LocalDate.now(),
            beregnDatoTil = LocalDate.now(),
            inntektPerioder = emptyList(),
            barnIHusstandenPerioder = emptyList(),
            sjablonPeriodeListe = emptyList(),
        )
}
