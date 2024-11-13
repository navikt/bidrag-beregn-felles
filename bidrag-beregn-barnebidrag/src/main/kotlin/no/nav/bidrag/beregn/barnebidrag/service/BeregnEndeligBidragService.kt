package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndeligBidragBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.DeltBostedBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndeligBidragMapper.mapEndeligBidragGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse

internal object BeregnEndeligBidragService : BeregnService() {

    fun delberegningEndeligBidrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne endelig bidrag
        val endeligBidragPeriodeGrunnlag = mapEndeligBidragGrunnlag(mottattGrunnlag)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeEndeligBidrag(
            grunnlagListe = endeligBidragPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val endeligBidragBeregningResultatListe = mutableListOf<EndeligBidragPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner endelig bidrag
        bruddPeriodeListe.forEach { bruddPeriode ->
            val endeligBidragBeregningGrunnlag = lagEndeligBidragBeregningGrunnlag(
                endeligBidragPeriodeGrunnlag = endeligBidragPeriodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )
            endeligBidragBeregningResultatListe.add(
                EndeligBidragPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregning.beregn(endeligBidragBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapEndeligBidragResultatGrunnlag(
            endeligBidragBeregningResultatListe = endeligBidragBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
        )

        // Mapper ut grunnlag for delberegning endelig bidrag (sluttberegning)
        resultatGrunnlagListe.addAll(
            mapDelberegningEndeligBidrag(
                endeligBidragPeriodeResultatListe = endeligBidragBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeEndeligBidrag(
        grunnlagListe: EndeligBidragPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.bidragsevneDelberegningPeriodeGrunnlagListe.asSequence().map { it.bidragsevnePeriode.periode })
            .plus(grunnlagListe.underholdskostnadDelberegningPeriodeGrunnlagListe.asSequence().map { it.underholdskostnadPeriode.periode })
            .plus(
                grunnlagListe.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe.asSequence()
                    .map { it.bpAndelUnderholdskostnadPeriode.periode },
            )
            .plus(grunnlagListe.samværsfradragDelberegningPeriodeGrunnlagListe.asSequence().map { it.samværsfradragPeriode.periode })
            .plus(grunnlagListe.samværsklassePeriodeGrunnlagListe.asSequence().map { it.samværsklassePeriode.periode })
            .plus(grunnlagListe.barnetilleggBPPeriodeGrunnlagListe.asSequence().map { it.barnetilleggPeriode.periode })
            .plus(grunnlagListe.barnetilleggBMPeriodeGrunnlagListe.asSequence().map { it.barnetilleggPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for endelig bidrag beregning som ligger innenfor bruddPeriode
    private fun lagEndeligBidragBeregningGrunnlag(
        endeligBidragPeriodeGrunnlag: EndeligBidragPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): EndeligBidragBeregningGrunnlag = EndeligBidragBeregningGrunnlag(
        bidragsevneBeregningGrunnlag = endeligBidragPeriodeGrunnlag.bidragsevneDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bidragsevnePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BidragsevneDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.bidragsevnePeriode.beløp,
                    sumInntekt25Prosent = it.bidragsevnePeriode.sumInntekt25Prosent,
                )
            }
            ?: throw IllegalArgumentException("Bidragsevne grunnlag mangler for periode $bruddPeriode"),
        underholdskostnadBeregningGrunnlag = endeligBidragPeriodeGrunnlag.underholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.underholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                UnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.underholdskostnadPeriode.underholdskostnad,
                )
            }
            ?: throw IllegalArgumentException("Underholdskostnad grunnlag mangler for periode $bruddPeriode"),
        bpAndelUnderholdskostnadBeregningGrunnlag = endeligBidragPeriodeGrunnlag.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bpAndelUnderholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    andelBeløp = it.bpAndelUnderholdskostnadPeriode.andelBeløp,
                    andelFaktor = it.bpAndelUnderholdskostnadPeriode.endeligAndelFaktor,
                    barnetErSelvforsørget = it.bpAndelUnderholdskostnadPeriode.barnetErSelvforsørget,
                )
            }
            ?: throw IllegalArgumentException("BP andel underholdskostnad grunnlag mangler for periode $bruddPeriode"),
        samværsfradragBeregningGrunnlag = endeligBidragPeriodeGrunnlag.samværsfradragDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.samværsfradragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { SamværsfradragDelberegningBeregningGrunnlag(referanse = it.referanse, beløp = it.samværsfradragPeriode.beløp) }
            ?: throw IllegalArgumentException("Samværsfradrag grunnlag mangler for periode $bruddPeriode"),
        deltBostedBeregningGrunnlag = endeligBidragPeriodeGrunnlag.samværsklassePeriodeGrunnlagListe
            .firstOrNull { it.samværsklassePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                DeltBostedBeregningGrunnlag(
                    referanse = it.referanse,
                    deltBosted = it.samværsklassePeriode.samværsklasse == Samværsklasse.DELT_BOSTED,
                )
            }
            ?: throw IllegalArgumentException("Delt bosted grunnlag mangler for periode $bruddPeriode"),
        barnetilleggBPBeregningGrunnlag = endeligBidragPeriodeGrunnlag.barnetilleggBPPeriodeGrunnlagListe
            .firstOrNull { it.barnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.barnetilleggPeriode.beløp,
                    skattFaktor = it.barnetilleggPeriode.skattFaktor,
                )
            },
        barnetilleggBMBeregningGrunnlag = endeligBidragPeriodeGrunnlag.barnetilleggBMPeriodeGrunnlagListe
            .firstOrNull { it.barnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.barnetilleggPeriode.beløp,
                    skattFaktor = it.barnetilleggPeriode.skattFaktor,
                )
            },
    )

    private fun mapEndeligBidragResultatGrunnlag(
        endeligBidragBeregningResultatListe: List<EndeligBidragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            endeligBidragBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        return resultatGrunnlagListe
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) = grunnlagListe
        .filter { grunnlagReferanseListe.contains(it.referanse) }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = it.type,
                innhold = it.innhold,
                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                gjelderReferanse = it.gjelderReferanse,
            )
        }

    // Mapper ut DelberegningEndeligBidrag
    private fun mapDelberegningEndeligBidrag(
        endeligBidragPeriodeResultatListe: List<EndeligBidragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = endeligBidragPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettSluttberegningreferanse(
                    barnreferanse = mottattGrunnlag.søknadsbarnReferanse,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, it.periode.til),
                ),
                type = Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                innhold = POJONode(
                    SluttberegningBarnebidrag(
                        periode = it.periode,
                        beregnetBeløp = it.resultat.beregnetBeløp,
                        resultatKode = it.resultat.resultatKode,
                        resultatBeløp = it.resultat.resultatBeløp,
                        kostnadsberegnetBidrag = it.resultat.kostnadsberegnetBidrag,
                        nettoBarnetilleggBP = it.resultat.nettoBarnetilleggBP,
                        nettoBarnetilleggBM = it.resultat.nettoBarnetilleggBM,
                        justertNedTilEvne = it.resultat.justertNedTilEvne,
                        justertNedTil25ProsentAvInntekt = it.resultat.justertNedTil25ProsentAvInntekt,
                        justertForNettoBarnetilleggBP = it.resultat.justertForNettoBarnetilleggBP,
                        justertForNettoBarnetilleggBM = it.resultat.justertForNettoBarnetilleggBM,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
            )
        }
}
