package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal

internal object BpAndelUnderholdskostnadMapper : CoreMapper() {
    fun mapBpAndelUnderholdskostnadGrunnlag(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
        åpenSluttperiode: Boolean,
        innslagKapitalInntekt: BigDecimal,
    ): BpAndelUnderholdskostnadPeriodeGrunnlag = BpAndelUnderholdskostnadPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(beregnGrunnlag = mottattGrunnlag),
        inntektBPPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
            ),
            innslagKapitalinntektSjablonverdi = innslagKapitalInntekt,
            åpenSluttperiode = åpenSluttperiode,
        ),
        inntektBMPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
            ),
            innslagKapitalinntektSjablonverdi = innslagKapitalInntekt,
            åpenSluttperiode = åpenSluttperiode,
        ),
        inntektSBPeriodeGrunnlagListe = mapInntekt(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN,
            ),
            innslagKapitalinntektSjablonverdi = innslagKapitalInntekt,
            åpenSluttperiode = åpenSluttperiode,
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
}
