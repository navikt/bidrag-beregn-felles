package no.nav.bidrag.beregn.core.mapping

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse

fun Samværsfradrag.tilGrunnlagsobjekt(periode: ÅrMånedsperiode, postfix: String? = null): GrunnlagDto {
    return GrunnlagDto(
        referanse = opprettSjablonreferanse(
            Grunnlagstype.SJABLON_SAMVARSFRADRAG.name,
            periode,
            "${samvaersklasse}_${alderTom}${postfix?.let { "_$it" } ?: ""}",
        ),
        type = Grunnlagstype.SJABLON_SAMVARSFRADRAG,
        innhold = POJONode(
            SjablonSamværsfradragPeriode(
                periode = periode,
                samværsklasse = samvaersklasse!!,
                alderTom = alderTom!!,
                antallDagerTom = antDagerTom!!,
                antallNetterTom = antNetterTom!!,
                beløpFradrag = belopFradrag!!,
            ),
        ),
    )
}
