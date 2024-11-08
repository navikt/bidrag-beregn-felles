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
fun List<BarnIHusstandenPeriodeCore>.mapTilGrunnlag(bidragspliktigReferanse: String? = null) = map {
    it.tilGrunnlag(bidragspliktigReferanse)
}
fun BarnIHusstandenPeriodeCore.tilGrunnlag(bidragspliktigReferanse: String? = null) = GrunnlagDto(
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

fun bestemGrunnlagstype(referanse: String) = when {
    referanse.contains(Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_SUM_INNTEKT
    referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
    referanse.contains(Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND
    referanse.contains(Grunnlagstype.DELBEREGNING_BOFORHOLD.name) -> Grunnlagstype.DELBEREGNING_BOFORHOLD
    referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSEVNE
    referanse.contains(
        Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG.name,
    ) -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG

    referanse.contains(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD.name) -> Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD
    referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL
    referanse.contains(Grunnlagstype.DELBEREGNING_FAKTISK_UTGIFT.name) -> Grunnlagstype.DELBEREGNING_FAKTISK_UTGIFT
    referanse.contains(Grunnlagstype.DELBEREGNING_TILLEGGSSTØNAD.name) -> Grunnlagstype.DELBEREGNING_TILLEGGSSTØNAD

    else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
}
