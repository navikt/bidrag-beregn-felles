package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.core.BeregnApi
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object BidragsevneMapper : CoreMapper() {
    private val beregnApi: BeregnApi = BeregnApi()
    fun mapBidragsevneGrunnlag(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
        åpenSluttperiode: Boolean
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
                innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag),
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

    // TODO Flytte til CoreMapper
    private fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
                .map {
                    SjablonSjablontallPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonSjablontallPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for sjablontall: " + e.message,
            )
        }
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

    private fun mapSjablonTrinnvisSkattesats(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonTrinnvisSkattesatsPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonTrinnvisSkattesatsPeriode>()
                .map {
                    SjablonTrinnvisSkattesatsPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonTrinnvisSkattesatsPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for trinnvis skattesats: " + e.message,
            )
        }
    }
}
