package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevneDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragspliktigesAndelDeltBostedPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BostatusPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.LøpendeBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklassePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningBarnebidragV2PeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningAndelAvBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragJustertForBPBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordelingLøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndelDeltBosted
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEvne25ProsentAvInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsklassePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse

internal object EndeligBidragMapperV2 : CoreMapper() {
    fun mapBidragspliktigesAndelDeltBostedGrunnlag(mottattGrunnlag: BeregnGrunnlag) = BidragspliktigesAndelDeltBostedPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(mottattGrunnlag),
        bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
        samværsklassePeriodeGrunnlagListe = mapSamværsklasse(mottattGrunnlag),
    )

    fun mapBidragTilFordelingGrunnlag(mottattGrunnlag: BeregnGrunnlag) = BidragTilFordelingPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        underholdskostnadDelberegningPeriodeGrunnlagListe = mapUnderholdskostnad(mottattGrunnlag),
        bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
        nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe = mapNettoBarnetillegg(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
            ),
        ),
        samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag.grunnlagListe),
        bidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlagListe = mapBidragspliktigesAndelDeltBosted(mottattGrunnlag.grunnlagListe),
    )

    fun mapSumBidragTilFordelingGrunnlag(beregningsperiode: ÅrMånedsperiode, mottattGrunnlagListe: List<BeregnGrunnlag>) =
        SumBidragTilFordelingPeriodeGrunnlag(
            beregningsperiode = beregningsperiode,
            bidragTilFordelingDelberegningPeriodeGrunnlagListe = mapBidragTilFordelingFraBeregnGrunnlagListe(mottattGrunnlagListe),
            bidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlagListe = mapBidragTilFordelingLøpendeBidrag(
                mottattGrunnlagListe.flatMap
                    { it.grunnlagListe },
            ),
        )

    fun mapEvne25ProsentAvInntektGrunnlag(mottattGrunnlag: BeregnGrunnlag) = Evne25ProsentAvInntektPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        bidragsevneDelberegningPeriodeGrunnlagListe = mapBidragsevne(mottattGrunnlag),
    )

    fun mapAndelAvBidragsevneGrunnlag(mottattGrunnlag: BeregnGrunnlag) = AndelAvBidragsevnePeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        evne25ProsentAvInntektDelberegningPeriodeGrunnlagListe = mapEvne25ProsentAvInntekt(mottattGrunnlag),
        sumBidragTilFordelingDelberegningPeriodeGrunnlagListe = mapSumBidragTilFordeling(mottattGrunnlag),
        bidragTilFordelingDelberegningPeriodeGrunnlagListe = mapBidragTilFordeling(mottattGrunnlag.grunnlagListe),
    )

    fun mapBidragTilFordelingLøpendeBidragGrunnlag(mottattGrunnlag: BeregnGrunnlag) = BidragTilFordelingLøpendeBidragPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        løpendeBidragPeriodeGrunnlagListe = mapLøpendeBidrag(mottattGrunnlag),
        samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag.grunnlagListe),
    )

    fun mapBidragJustertForBPBarnetilleggGrunnlag(mottattGrunnlag: BeregnGrunnlag) = BidragJustertForBPBarnetilleggPeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        andelAvBidragsevneDelberegningPeriodeGrunnlagListe = mapAndelAvBidragsevne(mottattGrunnlag),
        nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe = mapNettoBarnetillegg(
            beregnGrunnlag = mottattGrunnlag,
            referanseTilRolle = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
            ),
        ),
        bidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlagListe = mapBidragspliktigesAndelDeltBosted(mottattGrunnlag.grunnlagListe),
    )

    fun mapSluttberegningBarnebidragGrunnlag(mottattGrunnlag: BeregnGrunnlag) = SluttberegningBarnebidragV2PeriodeGrunnlag(
        beregningsperiode = mottattGrunnlag.periode,
        bidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlagListe = mapBidragJustertForBPBarnetillegg(mottattGrunnlag),
        samværsfradragDelberegningPeriodeGrunnlagListe = mapSamværsfradrag(mottattGrunnlag.grunnlagListe),
        bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe = mapBpAndelUnderholdskostnad(mottattGrunnlag),
        bostatusPeriodeGrunnlagListe = mapBostatus(mottattGrunnlag, søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse),
        bidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlagListe = mapBidragspliktigesAndelDeltBosted(mottattGrunnlag.grunnlagListe),
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

    private fun mapSamværsfradrag(beregnGrunnlag: List<GrunnlagDto>): List<SamværsfradragDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
                .map {
                    SamværsfradragDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnReferanse = it.gjelderBarnReferanse,
                        samværsfradragPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG er ikke gyldig: " + e.message,
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

    private fun mapBidragspliktigesAndelDeltBosted(
        beregnGrunnlag: List<GrunnlagDto>,
    ): List<BidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndelDeltBosted>(
                    Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_DELT_BOSTED,
                )
                .map {
                    BidragspliktigesAndelDeltBostedDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragspliktigesAndelDeltBostedPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_DELT_BOSTED er ikke " +
                    "gyldig: " + e.message,
            )
        }
    }

    private fun mapBidragTilFordelingFraBeregnGrunnlagListe(
        beregnGrunnlagListe: List<BeregnGrunnlag>,
    ): List<BidragTilFordelingDelberegningPeriodeGrunnlag> = mapBidragTilFordeling(beregnGrunnlagListe.flatMap { it.grunnlagListe })

    private fun mapBidragTilFordeling(grunnlagListe: List<GrunnlagDto>): List<BidragTilFordelingDelberegningPeriodeGrunnlag> {
        try {
            return grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragTilFordeling>(Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING)
                .map {
                    BidragTilFordelingDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragTilFordelingPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapBidragTilFordelingLøpendeBidrag(
        grunnlagListe: List<GrunnlagDto>,
    ): List<BidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlag> {
        try {
            return grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragTilFordelingLøpendeBidrag>(
                    Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG,
                )
                .map {
                    BidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragTilFordelingLøpendeBidragPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG er ikke " +
                    "gyldig: " + e.message,
            )
        }
    }

    private fun mapLøpendeBidrag(beregnGrunnlag: BeregnGrunnlag): List<LøpendeBidragPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<LøpendeBidragPeriode>(Grunnlagstype.LØPENDE_BIDRAG_PERIODE)
                .map {
                    LøpendeBidragPeriodeGrunnlag(
                        referanse = it.referanse,
                        løpendeBidragPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.LØPENDE_BIDRAG_PERIODE er ikke gyldig: " + e.message,
            )
        }
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

    private fun mapEvne25ProsentAvInntekt(beregnGrunnlag: BeregnGrunnlag): List<Evne25ProsentAvInntektDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEvne25ProsentAvInntekt>(Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT)
                .map {
                    Evne25ProsentAvInntektDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        evne25ProsentAvInntektPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapSumBidragTilFordeling(beregnGrunnlag: BeregnGrunnlag): List<SumBidragTilFordelingDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSumBidragTilFordeling>(Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING)
                .map {
                    SumBidragTilFordelingDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        sumBidragTilFordelingPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapAndelAvBidragsevne(beregnGrunnlag: BeregnGrunnlag): List<AndelAvBidragsevneDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningAndelAvBidragsevne>(Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE)
                .map {
                    AndelAvBidragsevneDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        andelAvBidragsevnePeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapBidragJustertForBPBarnetillegg(beregnGrunnlag: BeregnGrunnlag): List<BidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragJustertForBPBarnetillegg>(
                    Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG,
                )
                .map {
                    BidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlag(
                        referanse = it.referanse,
                        bidragJustertForBPBarnetilleggPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG er ikke " +
                    "gyldig: " + e.message,
            )
        }
    }

    private fun mapBostatus(beregnGrunnlag: BeregnGrunnlag, søknadsbarnReferanse: String): List<BostatusPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                .filter { it.gjelderBarnReferanse == søknadsbarnReferanse }
                .map {
                    BostatusPeriodeGrunnlag(
                        referanse = it.referanse,
                        bostatusPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
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
}
