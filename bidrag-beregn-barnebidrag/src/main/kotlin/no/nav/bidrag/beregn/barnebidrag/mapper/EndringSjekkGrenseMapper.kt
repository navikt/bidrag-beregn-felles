package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object EndringSjekkGrenseMapper : CoreMapper() {
    fun mapEndringSjekkGrenseGrunnlag(mottattGrunnlag: BeregnGrunnlag) =
        EndringSjekkGrensePeriodeGrunnlag(mapEndringSjekkGrensePeriode(mottattGrunnlag))

    private fun mapEndringSjekkGrensePeriode(beregnGrunnlag: BeregnGrunnlag): List<EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
                .map {
                    EndringSjekkGrensePeriodeDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        endringSjekkGrensePeriodePeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
}
