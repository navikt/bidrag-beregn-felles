package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.util.Collections

internal object BidragsevneMapper : CoreMapper() {
    fun mapBidragsevneGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): BidragsevnePeriodeGrunnlag {

        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
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
            barnIHusstandenPeriodeGrunnlagListe = mapBarnIHusstanden(mottattGrunnlag, referanseTilBP),
            voksneIHusstandenPeriodeGrunnlagListe = mapVoksneIHusstanden(mottattGrunnlag, referanseTilBP),
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
            sjablonBidragsevnePeriodeGrunnlagListe = mapSjablonBidragsevne(sjablonGrunnlag),
            sjablonTrinnvisSkattesatsPeriodeGrunnlagListe = mapSjablonTrinnvisSkattesats(sjablonGrunnlag),
        )
    }

    //TODO: Flytte til CoreMapper? (ligger pt også i BidragsevneCoreMapper under særbidrag)
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
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    //TODO: Flytte til CoreMapper? (ligger pt også i BidragsevneCoreMapper under særbidrag)
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
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    //TODO Flytte til CoreMapper
    private fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.contains("SJABLONTALL") }
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
                .filter { it.referanse.contains(SjablonNavn.BIDRAGSEVNE.navn) }
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
                .filter { it.referanse.contains(SjablonNavn.TRINNVIS_SKATTESATS.navn) }
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
