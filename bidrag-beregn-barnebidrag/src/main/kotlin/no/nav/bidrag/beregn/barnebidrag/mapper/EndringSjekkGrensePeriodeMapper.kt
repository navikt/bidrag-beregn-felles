package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeGrunnlagV2
import no.nav.bidrag.beregn.barnebidrag.bo.PrivatAvtaleIndeksregulertPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlagV2
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidragV2
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object EndringSjekkGrensePeriodeMapper : CoreMapper() {
    fun mapEndringSjekkGrensePeriodeGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) =
        EndringSjekkGrensePeriodePeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            sluttberegningPeriodeGrunnlagListe = mapSluttberegning(mottattGrunnlag),
            beløpshistorikkBidragPeriodeGrunnlag = mapBeløpshistorikk(mottattGrunnlag, grunnlagstype),
            privatAvtaleIndeksregulertPeriodeGrunnlagListe = mapPrivatAvtale(mottattGrunnlag),
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
        )

    fun mapEndringSjekkGrensePeriodeGrunnlagV2(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) =
        EndringSjekkGrensePeriodePeriodeGrunnlagV2(
            beregningsperiode = mottattGrunnlag.periode,
            sluttberegningPeriodeGrunnlagListe = mapSluttberegningV2(mottattGrunnlag),
            beløpshistorikkBidragPeriodeGrunnlag = mapBeløpshistorikk(mottattGrunnlag, grunnlagstype),
            privatAvtaleIndeksregulertPeriodeGrunnlagListe = mapPrivatAvtale(mottattGrunnlag),
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
        )

    private fun mapSluttberegning(beregnGrunnlag: BeregnGrunnlag): List<SluttberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
                .map {
                    SluttberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        sluttberegningPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSluttberegningV2(beregnGrunnlag: BeregnGrunnlag): List<SluttberegningPeriodeGrunnlagV2> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidragV2>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
                .map {
                    SluttberegningPeriodeGrunnlagV2(
                        referanse = it.referanse,
                        sluttberegningPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBeløpshistorikk(beregnGrunnlag: BeregnGrunnlag, grunnlagstype: Grunnlagstype): BeløpshistorikkPeriodeGrunnlag? =
        beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(grunnlagType = grunnlagstype)
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()

    private fun mapPrivatAvtale(beregnGrunnlag: BeregnGrunnlag): List<PrivatAvtaleIndeksregulertPeriodeGrunnlag> = beregnGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtale>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE)
        .firstOrNull()?.let { dpa ->
            dpa.innhold.perioder.map {
                PrivatAvtaleIndeksregulertPeriodeGrunnlag(
                    referanse = dpa.referanse,
                    privatAvtaleIndeksregulertPeriode = it,
                )
            }
        } ?: emptyList()
}
