package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndringSjekkGrenseBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrenseBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndringSjekkGrenseMapper.mapEndringSjekkGrenseGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnEndringSjekkGrenseService : BeregnService() {

    fun delberegningEndringSjekkGrense(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {

        // Mapper ut grunnlag som skal brukes i beregningen
        val periodeGrunnlag = mapEndringSjekkGrenseGrunnlag(mottattGrunnlag)

        val beregningGrunnlagListe = mutableListOf<EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag>()

        // Sjekker om endring er under/over grense (ikke periodisert)
        periodeGrunnlag.endringSjekkGrensePeriodePeriodeGrunnlagListe.forEach {
            beregningGrunnlagListe.add(
                EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    endringErOverGrense = it.endringSjekkGrensePeriodePeriode.endringErOverGrense,
                ),
            )
        }
        val beregningResultat = EndringSjekkGrenseBeregning.beregn(beregningGrunnlagListe)

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag)
        val resultatGrunnlagListe = mapResultatGrunnlag(
            beregningResultat = beregningResultat,
            mottattGrunnlag = mottattGrunnlag,
        )

        // Mapper ut grunnlag for delberegning endring sjekk grense
        resultatGrunnlagListe.addAll(
            mapDelberegningEndringSjekkGrense(
                beregningResultat = beregningResultat,
                mottattGrunnlag = mottattGrunnlag,
                åpenSluttperiode = åpenSluttperiode,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    private fun mapResultatGrunnlag(
        beregningResultat: EndringSjekkGrenseBeregningResultat,
        mottattGrunnlag: BeregnGrunnlag,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe = beregningResultat.grunnlagsreferanseListe

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
                gjelderBarnReferanse = it.gjelderBarnReferanse,
            )
        }

    // Mapper ut DelberegningEndringSjekkGrense
    private fun mapDelberegningEndringSjekkGrense(
        beregningResultat: EndringSjekkGrenseBeregningResultat,
        mottattGrunnlag: BeregnGrunnlag,
        åpenSluttperiode: Boolean,
    ): List<GrunnlagDto> = listOf(
        GrunnlagDto(
            referanse = opprettDelberegningreferanse(
                type = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE,
                periode = ÅrMånedsperiode(fom = mottattGrunnlag.periode.fom, til = null),
                søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            ),
            type = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE,
            innhold = POJONode(
                DelberegningEndringSjekkGrense(
                    periode = ÅrMånedsperiode(fom = mottattGrunnlag.periode.fom, til = if (åpenSluttperiode) null else mottattGrunnlag.periode.til),
                    endringErOverGrense = beregningResultat.endringErOverGrense,
                ),
            ),
            grunnlagsreferanseListe = beregningResultat.grunnlagsreferanseListe,
            gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
        )
    )
}
