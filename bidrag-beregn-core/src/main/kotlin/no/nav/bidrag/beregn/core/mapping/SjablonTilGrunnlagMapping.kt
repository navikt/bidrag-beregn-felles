package no.nav.bidrag.beregn.core.mapping

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.commons.service.sjablon.Barnetilsyn
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Forbruksutgifter
import no.nav.bidrag.commons.service.sjablon.MaksFradrag
import no.nav.bidrag.commons.service.sjablon.MaksTilsyn
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBarnetilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonForbruksutgifterPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse
import java.math.BigDecimal

fun Samværsfradrag.tilGrunnlagsobjekt(periode: ÅrMånedsperiode, postfix: String? = null): GrunnlagDto = GrunnlagDto(
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

fun Bidragsevne.tilGrunnlagsobjekt(periode: ÅrMånedsperiode): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_BIDRAGSEVNE.name,
        periode,
        "$bostatus",
    ),
    type = Grunnlagstype.SJABLON_BIDRAGSEVNE,
    innhold = POJONode(
        SjablonBidragsevnePeriode(
            periode = periode,
            bostatus = bostatus!!,
            boutgiftBeløp = belopBoutgift!!,
            underholdBeløp = belopUnderhold!!,
        ),
    ),
)

fun MaksTilsyn.tilGrunnlagsobjekt(periode: ÅrMånedsperiode): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_MAKS_TILSYN.name,
        periode,
    ),
    type = Grunnlagstype.SJABLON_MAKS_TILSYN,
    innhold = POJONode(
        SjablonMaksTilsynPeriode(
            periode = periode,
            antallBarnTom = antallBarnTom!!,
            maksBeløpTilsyn = maksBeløpTilsyn!!,
        ),
    ),
)

fun MaksFradrag.tilGrunnlagsobjekt(periode: ÅrMånedsperiode): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_MAKS_FRADRAG.name,
        periode,
    ),
    type = Grunnlagstype.SJABLON_MAKS_FRADRAG,
    innhold = POJONode(
        SjablonMaksFradragPeriode(
            periode = periode,
            antallBarnTom = antallBarnTom!!,
            maksBeløpFradrag = maksBeløpFradrag!!,
        ),
    ),
)

fun Barnetilsyn.tilGrunnlagsobjekt(periode: ÅrMånedsperiode): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_BARNETILSYN.name,
        periode,
    ),
    type = Grunnlagstype.SJABLON_BARNETILSYN,
    innhold = POJONode(
        SjablonBarnetilsynPeriode(
            periode = periode,
            typeStønad = typeStønad!!,
            typeTilsyn = typeTilsyn!!,
            beløpBarnetilsyn = beløpBarneTilsyn!!,
        ),
    ),
)

fun Forbruksutgifter.tilGrunnlagsobjekt(periode: ÅrMånedsperiode): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_FORBRUKSUTGIFTER.name,
        periode,
    ),
    type = Grunnlagstype.SJABLON_FORBRUKSUTGIFTER,
    innhold = POJONode(
        SjablonForbruksutgifterPeriode(
            periode = periode,
            alderTom = alderTom!!,
            beløpForbruk = beløpForbruk!!,
        ),
    ),
)

fun trinnvisSkattesatsTilGrunnlagsobjekt(periode: ÅrMånedsperiode, trinnliste: List<SjablonTrinnvisSkattesats>): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS.name,
        periode,
    ),
    type = Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS,
    innhold = POJONode(
        SjablonTrinnvisSkattesatsPeriode(
            periode = periode,
            trinnliste = trinnliste,
        ),
    ),
)

fun sjablontallTilGrunnlagsobjekt(periode: ÅrMånedsperiode, sjablontallNavn: SjablonTallNavn, verdi: BigDecimal): GrunnlagDto = GrunnlagDto(
    referanse = opprettSjablonreferanse(
        sjablontallNavn.navn,
        periode,
    ),
    type = Grunnlagstype.SJABLON_SJABLONTALL,
    innhold = POJONode(
        SjablonSjablontallPeriode(
            periode = periode,
            sjablon = sjablontallNavn,
            verdi = verdi,
        ),
    ),
)
