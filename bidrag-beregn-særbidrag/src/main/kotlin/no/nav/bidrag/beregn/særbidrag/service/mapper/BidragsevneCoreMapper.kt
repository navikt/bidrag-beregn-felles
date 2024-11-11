package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.BeregnApi
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import java.util.ArrayList

internal object BidragsevneCoreMapper : CoreMapper() {
    private val beregnApi: BeregnApi = BeregnApi()
    fun mapBidragsevneGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBidragsevneGrunnlagCore {
        // Mapper grunnlagstyper til input for core
        val referanseTilRolle = finnReferanseTilRolle(
            grunnlagListe = beregnGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )
        val inntektBPPeriodeCoreListe =
            mapInntekt(
                beregnGrunnlag = beregnGrunnlag,
                referanseTilRolle = referanseTilRolle,
                innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraSjablontall(sjablonListe.sjablonSjablontallResponse),
                erSærbidrag = true,
            )

        val (boforholdPeriodeGrunnlagListe, barnIHusstandenPeriodeCoreListe, voksneIHusstandenPeriodeCoreListe) =
            beregnApi.beregnBoforholdCore(beregnGrunnlag, referanseTilRolle)

        val sjablonPeriodeCoreListe = ArrayList<SjablonPeriodeCore>()

        // Henter aktuelle sjabloner
        sjablonPeriodeCoreListe.addAll(
            mapSjablonSjablontall(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablonListe.sjablonSjablontallResponse,
                sjablontallMap = sjablontallMap,
                criteria = { it.bidragsevne },
            ),
        )
        sjablonPeriodeCoreListe.addAll(
            mapSjablonBidragsevne(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonBidragsevneListe = sjablonListe.sjablonBidragsevneResponse,
            ),
        )
        sjablonPeriodeCoreListe.addAll(
            mapSjablonTrinnvisSkattesats(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonTrinnvisSkattesatsListe = sjablonListe.sjablonTrinnvisSkattesatsResponse,
            ),
        )

        return BeregnBidragsevneGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            inntektPeriodeListe = inntektBPPeriodeCoreListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeCoreListe,
            voksneIHusstandenPeriodeListe = voksneIHusstandenPeriodeCoreListe,
            boforholdPeriodeListe = boforholdPeriodeGrunnlagListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }
}
