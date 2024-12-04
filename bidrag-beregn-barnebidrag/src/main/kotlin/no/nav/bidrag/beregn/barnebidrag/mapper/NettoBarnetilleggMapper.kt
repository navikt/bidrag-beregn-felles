package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilleggPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse

internal object NettoBarnetilleggMapper : CoreMapper() {
    fun mapNettoBarnetilleggGrunnlag(mottattGrunnlag: BeregnGrunnlag, referanseTilRolle: String): NettoBarnetilleggPeriodeGrunnlag =
        NettoBarnetilleggPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            barnetilleggPeriodeGrunnlagListe = mapBarnetillegg(beregnGrunnlag = mottattGrunnlag, referanseTilRolle),
            barnetilleggSkattesatsListe = mapBarnetilleggSkattesats(beregnGrunnlag = mottattGrunnlag, referanseTilRolle),
        )

    private fun mapBarnetillegg(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: String): List<BarnetilleggPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                    grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                    referanse = referanseTilRolle,
                )
                .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
                .filter { it.innhold.gjelderBarn == beregnGrunnlag.søknadsbarnReferanse }
                .flatMap {
                    it.innhold.inntektspostListe.mapNotNull { inntektspost ->
                        inntektspost.inntektstype?.let { inntektstype ->
                            BarnetilleggPeriodeGrunnlag(
                                referanse = it.referanse,
                                barnetilleggPeriode = BarnetilleggPeriode(
                                    periode = it.innhold.periode,
                                    type = inntektstype,
                                    beløp = inntektspost.beløp,
                                    manueltRegistrert = false,
                                ),
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.BARNETILLEGG_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBarnetilleggSkattesats(
        beregnGrunnlag: BeregnGrunnlag,
        referanseTilRolle: String,
    ): List<BarnetilleggSkattesatsDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningBarnetilleggSkattesats>(
                    grunnlagType = Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS,
                    referanse = referanseTilRolle,
                )
                .map {
                    BarnetilleggSkattesatsDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        gjelderReferanse = it.gjelderReferanse!!,
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
