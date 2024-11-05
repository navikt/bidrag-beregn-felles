package no.nav.bidrag.beregn.core.mapping

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse

fun Samværsfradrag.tilGrunnlagsobjekt(
    periode: ÅrMånedsperiode,
    referanse: String? = null,
    gjelderReferanse: String? = null,
    postfix: String? = null,
): GrunnlagDto {
    return GrunnlagDto(
        gjelderReferanse = gjelderReferanse,
        referanse = referanse ?: opprettSjablonreferanse(
            Grunnlagstype.SJABLON_SAMVARSFRADRAG.name,
            periode,
            "${samvaersklasse}_${alderTom}${postfix?.let { "_$it" } ?: ""}",
        ),
        type = Grunnlagstype.SJABLON_SAMVARSFRADRAG,
        innhold = POJONode(
            SjablonSamværsfradragPeriode(
                periode = periode,
                samværsklasse = samvaersklasse ?: "",
                alderTom = alderTom ?: 0,
                antallDagerTom = antDagerTom ?: 0,
                antallNetterTom = antNetterTom ?: 0,
                beløpFradrag = belopFradrag!!,
            ),
        ),
    )
}
