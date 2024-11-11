package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object BpAndelUnderholdskostnadMapper : CoreMapper() {
    fun mapBpAndelUnderholdskostnadGrunnlag(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): BpAndelUnderholdskostnadPeriodeGrunnlag = BpAndelUnderholdskostnadPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(beregnGrunnlag = mottattGrunnlag),
        inntektBPPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
            ),
            innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag),
        ),
        inntektBMPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
            ),
            innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag),
        ),
        inntektSBPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN,
            ),
            innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag),
        ),
        sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
    )

    private fun mapUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag): List<UnderholdskostnadDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
                .map {
                    UnderholdskostnadDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        underholdskostnadPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD er ikke gyldig: " + e.message,
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
}
