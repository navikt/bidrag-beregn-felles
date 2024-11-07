package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.util.Collections

internal object BidragsevneMapper : CoreMapper() {
    fun mapBidragsevneGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): BidragsevnePeriodeGrunnlag {
        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        val barnIHusstandenPeriodeGrunnlagListe = mapBarnIHusstanden(beregnGrunnlag = mottattGrunnlag, referanseTilRolle = referanseTilBP)
        val voksneIHusstandenPeriodeGrunnlagListe = mapVoksneIHusstanden(beregnGrunnlag = mottattGrunnlag, referanseTilRolle = referanseTilBP)
        val boforholdPeriodeGrunnlagListe = slåSammenBarnOgVoksneIHusstanden(
            barnIHusstandenPeriodeGrunnlagListe = barnIHusstandenPeriodeGrunnlagListe,
            voksneIHusstandenPeriodeGrunnlagListe = voksneIHusstandenPeriodeGrunnlagListe,
            søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            gjelderReferanse = referanseTilBP,
        )

        return BidragsevnePeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            inntektBPPeriodeGrunnlagListe = mapInntekt(
                beregnGrunnlag = mottattGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
                innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag),
            ),
            barnIHusstandenPeriodeGrunnlagListe = barnIHusstandenPeriodeGrunnlagListe,
            voksneIHusstandenPeriodeGrunnlagListe = voksneIHusstandenPeriodeGrunnlagListe,
            boforholdPeriodeGrunnlagListe = boforholdPeriodeGrunnlagListe,
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
            sjablonBidragsevnePeriodeGrunnlagListe = mapSjablonBidragsevne(sjablonGrunnlag),
            sjablonTrinnvisSkattesatsPeriodeGrunnlagListe = mapSjablonTrinnvisSkattesats(sjablonGrunnlag),
        )
    }

    // TODO: Flytte til CoreMapper? (ligger pt også i BidragsevneCoreMapper under særbidrag)
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
                            grunnlagsreferanseListe = Collections.emptyList(),
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
                "Ugyldig input ved beregning av bidragsevne. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    // TODO: Flytte til CoreMapper? (ligger pt også i BidragsevneCoreMapper under særbidrag)
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
                            grunnlagsreferanseListe = Collections.emptyList(),
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
                "Ugyldig input ved beregning av bidragsevne. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    // TODO: Flytte til CoreMapper? (ligger pt også i BidragsevneCoreMapper under særbidrag)
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
