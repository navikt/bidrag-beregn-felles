package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriode2
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriodeGrunnlag2
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object NettoBarnetilleggMapper : CoreMapper() {
    fun mapNettoBarnetilleggGrunnlag(mottattGrunnlag: BeregnGrunnlag): NettoBarnetilleggPeriodeGrunnlag = NettoBarnetilleggPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        barnetilleggPeriodeGrunnlagListe = mapBarnetillegg2(beregnGrunnlag = mottattGrunnlag),
        barnetilleggSkattesatsListe = mapBarnetilleggSkattesats(beregnGrunnlag = mottattGrunnlag),
    )

    private fun mapBarnetillegg2(beregnGrunnlag: BeregnGrunnlag): List<BarnetilleggPeriodeGrunnlag2> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BarnetilleggPeriode2>(Grunnlagstype.BARNETILLEGG_PERIODE)
                .map {
                    BarnetilleggPeriodeGrunnlag2(
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
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS er ikke gyldig: " +
                    e.message,
            )
        }
    }
}
