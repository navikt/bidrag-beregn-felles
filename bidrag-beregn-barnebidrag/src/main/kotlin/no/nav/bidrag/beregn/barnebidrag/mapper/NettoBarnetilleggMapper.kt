package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilleggPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object NettoBarnetilleggMapper : CoreMapper() {
    fun mapNettoBarnetilleggGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): NettoBarnetilleggPeriodeGrunnlag =
        NettoBarnetilleggPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            barnetilleggPeriodeGrunnlagListe = mapBarnetillegg2(beregnGrunnlag = mottattGrunnlag),
            barnetilleggSkattesatsListe = mapBarnetilleggSkattesats(beregnGrunnlag = mottattGrunnlag),
        )

    private fun mapBarnetillegg2(beregnGrunnlag: BeregnGrunnlag): List<BarnetilleggPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BarnetilleggPeriode>(Grunnlagstype.BARNETILLEGG_PERIODE)
                .map {
                    BarnetilleggPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnetilleggPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.BARNETILLEGG_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBarnetilleggSkattesats(beregnGrunnlag: BeregnGrunnlag): List<BarnetilleggSkattesatsDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBarnetilleggSkattesats>(Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS)
                .map {
                    BarnetilleggSkattesatsDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnetilleggSkattesatsPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS er ikke gyldig: " + e.message,
            )
        }
    }
}
}
