package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.DeltBostedPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.DeltBostedPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.UnderholdskostnadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse

internal object EndeligBidragMapper : CoreMapper() {
    fun mapEndeligBidragGrunnlag(mottattGrunnlag: BeregnGrunnlag): EndeligBidragPeriodeGrunnlag =
        EndeligBidragPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            bidragsevneDelberegningPeriodeGrunnlagListe = mapBidragsevne(mottattGrunnlag),
            underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(mottattGrunnlag),
            bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
            samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag),
            deltBostedPeriodeGrunnlagListe = mapDeltBosted(mottattGrunnlag),
            barnetilleggBPPeriodeGrunnlagListe = mapBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                )
            ),
            barnetilleggBMPeriodeGrunnlagListe = mapBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                )
            ),
        )

    private fun mapBidragsevne(beregnGrunnlag: BeregnGrunnlag): List<BidragsevneDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BidragsevnePeriode>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE)
                .map {
                    BidragsevneDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragsevnePeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAGSEVNE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag): List<UnderholdskostnadDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
//TODO Bør endres til DELBEREGNING_UNDERHOLDSKOSTNAD
                .filtrerOgKonverterBasertPåEgenReferanse<UnderholdskostnadPeriode>(Grunnlagstype.UNDERHOLDSKOSTNAD)
                .map {
                    UnderholdskostnadDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        underholdskostnadPeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.UNDERHOLDSKOSTNAD er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBpAndelUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag): List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BpAndelUnderholdskostnadPeriode>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
                .map {
                    BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bpAndelUnderholdskostnadPeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSamværsfradrag(beregnGrunnlag: BeregnGrunnlag): List<SamværsfradragDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<SamværsfradragPeriode>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
                .map {
                    SamværsfradragDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        samværsfradragPeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapDeltBosted(beregnGrunnlag: BeregnGrunnlag): List<DeltBostedPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DeltBostedPeriode>(Grunnlagstype.DELT_BOSTED)
                .map {
                    DeltBostedPeriodeGrunnlag(
                        referanse = it.referanse,
                        deltBostedPeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELT_BOSTED er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBarnetillegg(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: String): List<BarnetilleggPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<BarnetilleggPeriode>(
                    grunnlagType = Grunnlagstype.INNHENTET_INNTEKT_BARNETILLEGG,
                    referanse = referanseTilRolle,
                )
                .map {
                    BarnetilleggPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnetilleggPeriode = it.innhold
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.INNHENTET_INNTEKT_BARNETILLEGG er ikke gyldig: " + e.message,
            )
        }
    }
}
