package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object EndringSjekkGrensePeriodeMapper : CoreMapper() {
    fun mapEndringSjekkGrensePeriodeGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>) =
        EndringSjekkGrensePeriodePeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            sluttberegningPeriodeGrunnlagListe = mapSluttberegning(mottattGrunnlag),
            beløpshistorikkBidragPeriodeGrunnlag = mapBeløpshistorikk(mottattGrunnlag),
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

    private fun mapBeløpshistorikk(beregnGrunnlag: BeregnGrunnlag): BeløpshistorikkPeriodeGrunnlag? {
        return beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(grunnlagType = Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()
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
}
