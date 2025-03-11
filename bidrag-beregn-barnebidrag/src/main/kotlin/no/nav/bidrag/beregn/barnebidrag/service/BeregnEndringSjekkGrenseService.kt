package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndringSjekkGrenseBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrenseBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndringSjekkGrenseMapper.mapEndringSjekkGrenseGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
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
                    løpendeBidragBeløp = it.endringSjekkGrensePeriodePeriode.løpendeBidragBeløp,
                    beregnetBidragBeløp = it.endringSjekkGrensePeriodePeriode.beregnetBidragBeløp?.avrundetMedToDesimaler,
                ),
            )
        }

        val beregningResultat = EndringSjekkGrenseBeregning.beregn(beregningGrunnlagListe)

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = beregningResultat.grunnlagsreferanseListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
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
        ),
    )
}
