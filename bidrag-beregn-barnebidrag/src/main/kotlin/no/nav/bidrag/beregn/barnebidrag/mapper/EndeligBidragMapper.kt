package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklassePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilleggPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsklassePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import java.math.BigDecimal

internal object EndeligBidragMapper : CoreMapper() {
    fun mapEndeligBidragGrunnlag(mottattGrunnlag: BeregnGrunnlag): EndeligBidragPeriodeGrunnlag {
        val bidragsevneDelberegningPeriodeGrunnlagListe = mapBidragsevne(mottattGrunnlag)

        return EndeligBidragPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            bidragsevneDelberegningPeriodeGrunnlagListe = bidragsevneDelberegningPeriodeGrunnlagListe,
            underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(mottattGrunnlag),
            bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
            samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag),
            samværsklassePeriodeGrunnlagListe = mapSamværsklasse(mottattGrunnlag),
            barnetilleggBPPeriodeGrunnlagListe = mapBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
                // TODO Blir det riktig å hente fra siste periode?
                skattFaktor = bidragsevneDelberegningPeriodeGrunnlagListe.last().bidragsevnePeriode.skatt.sumSkattFaktor,
            ),
            barnetilleggBMPeriodeGrunnlagListe = mapBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
                // TODO Må være skatt som tilhører BM
                skattFaktor = bidragsevneDelberegningPeriodeGrunnlagListe.last().bidragsevnePeriode.skatt.sumSkattFaktor,
            ),
        )
    }

    private fun mapBidragsevne(beregnGrunnlag: BeregnGrunnlag): List<BidragsevneDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragsevne>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE)
                .map {
                    BidragsevneDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragsevnePeriode = it.innhold,
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

    private fun mapBpAndelUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag): List<BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
                .map {
                    BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bpAndelUnderholdskostnadPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapSamværsfradrag(beregnGrunnlag: BeregnGrunnlag): List<SamværsfradragDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
                .map {
                    SamværsfradragDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        samværsfradragPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSamværsklasse(beregnGrunnlag: BeregnGrunnlag): List<SamværsklassePeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<SamværsklassePeriode>(Grunnlagstype.SAMVÆRSPERIODE)
                .map {
                    SamværsklassePeriodeGrunnlag(
                        referanse = it.referanse,
                        samværsklassePeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.SAMVÆRSPERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBarnetillegg(
        beregnGrunnlag: BeregnGrunnlag,
        referanseTilRolle: String,
        skattFaktor: BigDecimal,
    ): List<BarnetilleggPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                    grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                    referanse = referanseTilRolle,
                )
                .filter { it.gjelderBarnReferanse == beregnGrunnlag.søknadsbarnReferanse }
                .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
                .filter { it.innhold.valgt }
                .map {
                    BarnetilleggPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnetilleggPeriode = BarnetilleggPeriode(
                            periode = it.innhold.periode,
                            beløp = it.innhold.beløp,
                            skattFaktor = skattFaktor,
                            manueltRegistrert = it.innhold.manueltRegistrert,
                        ),
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
}
