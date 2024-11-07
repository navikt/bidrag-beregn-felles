package no.nav.bidrag.beregn.core.mapping

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBoforhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningVoksneIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

@JvmName("barnIHusstandenTilGrunnlag")
fun List<BarnIHusstandenPeriodeCore>.mapTilGrunnlag(bidragspliktigReferanse: String) = map {
    it.tilGrunnlag(bidragspliktigReferanse)
}
fun BarnIHusstandenPeriodeCore.tilGrunnlag(bidragspliktigReferanse: String) = GrunnlagDto(
    referanse = referanse,
    type = bestemGrunnlagstype(referanse),
    innhold = POJONode(
        DelberegningBarnIHusstand(
            periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
            antallBarn = antall,
        ),
    ),
    grunnlagsreferanseListe = grunnlagsreferanseListe.sorted(),
    gjelderReferanse = bidragspliktigReferanse,
)

@JvmName("voksneIHusstandenTilGrunnlag")
fun List<VoksneIHusstandenPeriodeCore>.mapTilGrunnlag(bidragspliktigReferanse: String) = map {
    GrunnlagDto(
        referanse = it.referanse,
        type = bestemGrunnlagstype(it.referanse),
        innhold = POJONode(
            DelberegningVoksneIHusstand(
                periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                borMedAndreVoksne = it.borMedAndreVoksne,
            ),
        ),
        grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
        gjelderReferanse = bidragspliktigReferanse,
    )
}

@JvmName("boforholdTilGrunnlag")
fun List<BoforholdPeriodeCore>.mapTilGrunnlag(bidragspliktigReferanse: String) = map {
    GrunnlagDto(
        referanse = it.referanse,
        type = bestemGrunnlagstype(it.referanse),
        innhold = POJONode(
            it.tilGrunnlag(),
        ),
        grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
        gjelderReferanse = bidragspliktigReferanse,
    )
}

fun BoforholdPeriodeCore.tilGrunnlag() = DelberegningBoforhold(
    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
    antallBarn = antallBarn,
    borMedAndreVoksne = borMedAndreVoksne,
)

fun bestemGrunnlagstype(referanse: String) = Grunnlagstype.entries.find {
    referanse.contains(it.name)
} ?: throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
