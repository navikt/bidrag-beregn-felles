package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.core.BeregnApi
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal

internal object BidragsevneMapper : CoreMapper() {
    private val beregnApi: BeregnApi = BeregnApi()
    fun mapBidragsevneGrunnlag(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
        åpenSluttperiode: Boolean,
        innslagKapitalInntekt: BigDecimal
    ): BidragsevnePeriodeGrunnlag {
        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        val (boforholdPeriodeGrunnlagListe, barnIHusstandenPeriodeGrunnlagListe, voksneIHusstandenPeriodeGrunnlagListe) =
            beregnApi.beregnBoforholdCore(mottattGrunnlag, referanseTilBP)

        return BidragsevnePeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            inntektBPPeriodeGrunnlagListe = mapInntekt(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalInntekt,
                åpenSluttperiode = åpenSluttperiode,
            ),
            barnIHusstandenPeriodeGrunnlagListe = barnIHusstandenPeriodeGrunnlagListe,
            voksneIHusstandenPeriodeGrunnlagListe = voksneIHusstandenPeriodeGrunnlagListe,
            boforholdPeriodeGrunnlagListe = boforholdPeriodeGrunnlagListe,
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
            sjablonBidragsevnePeriodeGrunnlagListe = mapSjablonBidragsevne(sjablonGrunnlag),
            sjablonTrinnvisSkattesatsPeriodeGrunnlagListe = mapSjablonTrinnvisSkattesats(sjablonGrunnlag),
        )
    }

    private fun mapSjablonBidragsevne(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonBidragsevnePeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_BIDRAGSEVNE }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonBidragsevnePeriode>()
                .map {
                    SjablonBidragsevnePeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonBidragsevnePeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for bidragsevne: " + e.message,
            )
        }
    }
}
