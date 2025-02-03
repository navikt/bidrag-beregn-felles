package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BegrensetRevurderingPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklassePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsklassePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SøknadGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse

internal object EndeligBidragMapper : CoreMapper() {

    fun mapEndeligBidragGrunnlag(mottattGrunnlag: BeregnGrunnlag): EndeligBidragPeriodeGrunnlag {

        return EndeligBidragPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            bidragsevneDelberegningPeriodeGrunnlagListe = mapBidragsevne(mottattGrunnlag),
            underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(mottattGrunnlag),
            bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
            samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag),
            samværsklassePeriodeGrunnlagListe = mapSamværsklasse(mottattGrunnlag),
            nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe = mapNettoBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
            ),
            nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe = mapNettoBarnetillegg(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
            ),
            beløpshistorikkForskuddPeriodeGrunnlag = mapBeløpshistorikk(
                beregnGrunnlag = mottattGrunnlag,
                grunnlagstype = Grunnlagstype.BELØPSHISTORIKK_FORSKUDD
            ),
            beløpshistorikkBidragPeriodeGrunnlag = mapBeløpshistorikk(
                beregnGrunnlag = mottattGrunnlag,
                grunnlagstype = Grunnlagstype.BELØPSHISTORIKK_BIDRAG
            ),
            begrensetRevurderingPeriodeGrunnlag = mapSøknadGrunnlag(mottattGrunnlag),
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

    private fun mapNettoBarnetillegg(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: String): List<NettoBarnetilleggDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningNettoBarnetillegg>(
                    grunnlagType = Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG,
                    referanse = referanseTilRolle,
                )
                .map {
                    NettoBarnetilleggDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        nettoBarnetilleggPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBeløpshistorikk(beregnGrunnlag: BeregnGrunnlag, grunnlagstype: Grunnlagstype): BeløpshistorikkPeriodeGrunnlag? {
        return beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(grunnlagType = grunnlagstype)
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()
    }

    private fun mapSøknadGrunnlag(beregnGrunnlag: BeregnGrunnlag): BegrensetRevurderingPeriodeGrunnlag? {
        return beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<SøknadGrunnlag>(Grunnlagstype.SØKNAD)
            .map {
                BegrensetRevurderingPeriodeGrunnlag(
                    referanse = it.referanse,
                    begrensetRevurdering = it.innhold.begrensetRevurdering,
                )
            }
            .firstOrNull()
    }
}
