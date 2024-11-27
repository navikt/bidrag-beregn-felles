package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumInntektDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
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

    // TODO Flytte til CoreMapper
    private fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
                .map {
                    SjablonSjablontallPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonSjablontallPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for sjablontall: " + e.message,
            )
        }
    }

    private fun mapSjablonTrinnvisSkattesats(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonTrinnvisSkattesatsPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonTrinnvisSkattesatsPeriode>()
                .map {
                    SjablonTrinnvisSkattesatsPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonTrinnvisSkattesatsPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for trinnvis skattesats: " + e.message,
            )
        }
    }
}
