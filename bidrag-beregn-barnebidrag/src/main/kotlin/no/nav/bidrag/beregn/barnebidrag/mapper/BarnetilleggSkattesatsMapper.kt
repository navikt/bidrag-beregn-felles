package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumInntektDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse

internal object BarnetilleggSkattesatsMapper : CoreMapper() {
    fun mapBarnetilleggSkattesatsGrunnlag(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
        referanseTilRolle: String,
    ): BarnetilleggSkattesatsPeriodeGrunnlag = BarnetilleggSkattesatsPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        sumInntektBeregningGrunnlag = mapSumInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = referanseTilRolle,
        ),
        sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
        sjablonTrinnvisSkattesatsPeriodeGrunnlagListe = mapSjablonTrinnvisSkattesats(sjablonGrunnlag),
    )

    private fun mapSumInntekt(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: String): List<SumInntektDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningSumInntekt>(
                    grunnlagType = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                    referanse = referanseTilRolle,
                )
                .map {
                    SumInntektDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        sumInntektPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_SUM_INNTEKT er ikke gyldig: " + e.message,
            )
        }
    }
}
