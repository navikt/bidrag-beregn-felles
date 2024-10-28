package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
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
                innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraSjablontall(sjablonListe.sjablonSjablontallResponse),
                erSærbidrag = true,
            )
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnGrunnlag = beregnGrunnlag, referanseTilRolle = referanseTilRolle)
        val voksneIHusstandenPeriodeCoreListe = mapVoksneIHusstanden(beregnGrunnlag = beregnGrunnlag, referanseTilRolle = referanseTilRolle)
        val boforholdPeriodeGrunnlagListe = slåSammenBarnOgVoksneIHusstanden(
            barnIHusstandenPeriodeGrunnlagListe = barnIHusstandenPeriodeCoreListe,
            voksneIHusstandenPeriodeGrunnlagListe = voksneIHusstandenPeriodeCoreListe,
            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
            gjelderReferanse = referanseTilRolle,
        )
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
                            borMedAndreVoksne = it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
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

    // Slår sammen barn i husstanden og voksne i husstanden til et fellesobjekt
    private fun slåSammenBarnOgVoksneIHusstanden(
        barnIHusstandenPeriodeGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
        voksneIHusstandenPeriodeGrunnlagListe: List<VoksneIHusstandenPeriodeCore>,
        søknadsbarnReferanse: String,
        gjelderReferanse: String,
    ): List<BoforholdPeriodeCore> {
        // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
        val bruddDatoListe = (
            barnIHusstandenPeriodeGrunnlagListe.flatMap { listOf(it.periode.datoFom, it.periode.datoTil) } +
                voksneIHusstandenPeriodeGrunnlagListe.flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
            )
            .distinct()
            .sortedBy { it }
            .sortedWith(compareBy { it == null })

        // Slå sammen brudddatoer til en liste med perioder (fom-/til-dato)
        val periodeListe = bruddDatoListe
            .zipWithNext()
            .map { PeriodeCore(it.first!!, it.second) }

        return periodeListe.map { bruddPeriode ->

            // Finner matchende barnIHusstanden for aktuell periode
            val barnIHusstanden = barnIHusstandenPeriodeGrunnlagListe
                .firstOrNull {
                    ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(
                        ÅrMånedsperiode(
                            bruddPeriode.datoFom,
                            bruddPeriode.datoTil,
                        ),
                    )
                }
            // Finner matchende voksneIHusstanden for aktuell periode
            val voksneIHusstanden = voksneIHusstandenPeriodeGrunnlagListe
                .firstOrNull {
                    ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(
                        ÅrMånedsperiode(
                            bruddPeriode.datoFom,
                            bruddPeriode.datoTil,
                        ),
                    )
                }

            // Oppretter BoforholdPeriodeCore
            BoforholdPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BOFORHOLD,
                    periode = ÅrMånedsperiode(fom = bruddPeriode.datoFom, til = null),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = bruddPeriode,
                antallBarn = barnIHusstanden?.antall ?: 0.0,
                borMedAndreVoksne = voksneIHusstanden?.borMedAndreVoksne ?: false,
                grunnlagsreferanseListe = listOfNotNull(barnIHusstanden?.referanse, voksneIHusstanden?.referanse).distinct(),
            )
        }
    }
}
