package no.nav.bidrag.beregn.forskudd.service

import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SivilstandPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SøknadsbarnCore
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import java.math.BigDecimal

internal object ForskuddCoreMapper : CoreMapper() {

    fun mapGrunnlagTilCore(
        beregnForskuddGrunnlag: BeregnGrunnlag,
        sjablontallListe: List<Sjablontall>,
        åpenSluttperiode: Boolean,
    ): BeregnForskuddGrunnlagCore {
        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        SjablonTallNavn.entries.forEach {
            sjablontallMap[it.id] = it
        }

        val referanseBidragsmottaker = beregnForskuddGrunnlag.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_BIDRAGSMOTTAKER mangler i input")

        // Mapper grunnlagstyper til input for core
        val søknadsbarnCore = mapSøknadsbarn(beregnForskuddGrunnlag)
        val bostatusPeriodeCoreListe = mapBostatus(beregnForskuddGrunnlag)
        val inntektPeriodeCoreListe = mapInntekt(
            beregnGrunnlag = beregnForskuddGrunnlag,
            referanseTilRolle = referanseBidragsmottaker,
            innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntektFraSjablontallListe(sjablontallListe)?.verdi ?: BigDecimal.ZERO,
            åpenSluttperiode = åpenSluttperiode,
        )
        val sivilstandPeriodeCoreListe = mapSivilstand(beregnForskuddGrunnlag)
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnForskuddGrunnlag, referanseBidragsmottaker)

        // Validerer at alle nødvendige grunnlag er med
        validerGrunnlag(
            søknadsbarnGrunnlag = søknadsbarnCore != null,
            bostatusGrunnlag = bostatusPeriodeCoreListe.isNotEmpty(),
            inntektGrunnlag = inntektPeriodeCoreListe.isNotEmpty(),
            sivilstandGrunnlag = sivilstandPeriodeCoreListe.isNotEmpty(),
        )

        val sjablonPeriodeCoreListe =
            mapSjablonSjablontall(
                beregnDatoFra = beregnForskuddGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnForskuddGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablontallListe,
                sjablontallMap = sjablontallMap,
                criteria = { it.forskudd },
            )

        return BeregnForskuddGrunnlagCore(
            beregnDatoFra = beregnForskuddGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnForskuddGrunnlag.periode.til!!.atDay(1),
            søknadsbarn = søknadsbarnCore!!,
            bostatusPeriodeListe = bostatusPeriodeCoreListe,
            inntektPeriodeListe = inntektPeriodeCoreListe,
            sivilstandPeriodeListe = sivilstandPeriodeCoreListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeCoreListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
            åpenSluttperiode = åpenSluttperiode,
        )
    }

    private fun mapSøknadsbarn(beregnForskuddGrunnlag: BeregnGrunnlag): SøknadsbarnCore? {
        try {
            val søknadsbarnGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<Person>(
                    referanse = beregnForskuddGrunnlag.søknadsbarnReferanse,
                )

            return if (søknadsbarnGrunnlag.isEmpty() || søknadsbarnGrunnlag.count() > 1) {
                null
            } else {
                SøknadsbarnCore(
                    referanse = søknadsbarnGrunnlag[0].referanse,
                    fødselsdato = søknadsbarnGrunnlag[0].innhold.fødselsdato,
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.PERSON_SØKNADSBARN er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBostatus(beregnForskuddGrunnlag: BeregnGrunnlag): List<BostatusPeriodeCore> {
        try {
            val bostatusGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåFremmedReferanse<BostatusPeriode>(
                    grunnlagType = Grunnlagstype.BOSTATUS_PERIODE,
                    gjelderBarnReferanse = beregnForskuddGrunnlag.søknadsbarnReferanse,
                )

            return bostatusGrunnlag.map {
                BostatusPeriodeCore(
                    referanse = it.referanse,
                    periode =
                    PeriodeCore(
                        datoFom = it.innhold.periode.toDatoperiode().fom,
                        datoTil = mapDatoTil(it.innhold.periode, beregnForskuddGrunnlag.periode.til),
                    ),
                    kode = it.innhold.bostatus.name,
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSivilstand(beregnForskuddGrunnlag: BeregnGrunnlag): List<SivilstandPeriodeCore> {
        try {
            val sivilstandGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<SivilstandPeriode>(grunnlagType = Grunnlagstype.SIVILSTAND_PERIODE)

            return sivilstandGrunnlag.map {
                SivilstandPeriodeCore(
                    referanse = it.referanse,
                    periode =
                    PeriodeCore(
                        datoFom = it.innhold.periode.toDatoperiode().fom,
                        datoTil = mapDatoTil(it.innhold.periode, beregnForskuddGrunnlag.periode.til),
                    ),
                    kode = it.innhold.sivilstand.name,
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.SIVILSTAND_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBarnIHusstanden(
        beregnForskuddGrunnlag: BeregnGrunnlag,
        referanseBidragsmottaker: Grunnlagsreferanse,
    ): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlagListe =
                beregnForskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter { it.innhold.bostatus == Bostatuskode.MED_FORELDER || it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG }
                    .map {
                        BarnIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = mapDatoTil(it.innhold.periode, beregnForskuddGrunnlag.periode.til),
                            ),
                            antall = 1.0,
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }
            return akkumulerOgPeriodiser(
                grunnlagListe = barnIHusstandenGrunnlagListe,
                søknadsbarnreferanse = beregnForskuddGrunnlag.søknadsbarnReferanse,
                gjelderReferanse = referanseBidragsmottaker,
                clazz = BarnIHusstandenPeriodeCore::class.java,
                beregningsperiode = beregnForskuddGrunnlag.periode,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun validerGrunnlag(søknadsbarnGrunnlag: Boolean, bostatusGrunnlag: Boolean, inntektGrunnlag: Boolean, sivilstandGrunnlag: Boolean) {
        when {
            !søknadsbarnGrunnlag -> {
                throw IllegalArgumentException("Søknadsbarn mangler i input")
            }

            !bostatusGrunnlag -> {
                throw IllegalArgumentException("Bostatus mangler i input")
            }

            !inntektGrunnlag -> {
                throw IllegalArgumentException("Inntekt mangler i input")
            }

            !sivilstandGrunnlag -> {
                throw IllegalArgumentException("Sivilstand mangler i input")
            }
        }
    }
}
