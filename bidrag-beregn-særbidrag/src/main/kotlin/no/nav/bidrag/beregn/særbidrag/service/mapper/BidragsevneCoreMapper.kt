package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.util.ArrayList
import java.util.Collections.emptyList

internal object BidragsevneCoreMapper : CoreMapper() {
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
                innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntekt(sjablonListe.sjablonSjablontallResponse),
                erSærbidrag = true,
            )
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnGrunnlag, referanseTilRolle)
        val voksneIHusstandenPeriodeCoreListe = mapVoksneIHusstanden(beregnGrunnlag, referanseTilRolle)
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
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }

    private fun mapBarnIHusstanden(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter {
                        it.innhold.bostatus == Bostatuskode.MED_FORELDER ||
                            it.innhold.bostatus == Bostatuskode.IKKE_MED_FORELDER ||
                            it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG ||
                            it.innhold.bostatus == Bostatuskode.DELT_BOSTED ||
                            it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN
                    }
                    .map {
                        BarnIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            antall =
                            when (it.innhold.bostatus) {
                                Bostatuskode.IKKE_MED_FORELDER -> 0.0
                                Bostatuskode.REGNES_IKKE_SOM_BARN -> 0.0
                                Bostatuskode.DELT_BOSTED -> 0.5
                                else -> 1.0
                            },
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                grunnlagListe = barnIHusstandenGrunnlagListe,
                søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                gjelderReferanse = referanseTilRolle,
                clazz = BarnIHusstandenPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapVoksneIHusstanden(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse): List<VoksneIHusstandenPeriodeCore> {
        try {
            val voksneIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter {
                        it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                            it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE ||
                            it.innhold.bostatus == Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
                    }
                    .map {
                        VoksneIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            borMedAndre = it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                                it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                voksneIHusstandenGrunnlagListe,
                beregnGrunnlag.søknadsbarnReferanse,
                referanseTilRolle,
                VoksneIHusstandenPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
}
