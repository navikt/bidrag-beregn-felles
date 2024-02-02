package no.nav.bidrag.beregn.sivilstand.service

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SivilstandPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SoknadBarnCore
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import java.time.LocalDate

internal object CoreMapper {
    private val MAX_DATO = LocalDate.parse("9999-12-31")

    fun mapGrunnlagTilCore(beregnForskuddGrunnlag: BeregnGrunnlag, sjablontallListe: List<Sjablontall>): BeregnForskuddGrunnlagCore {
        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        SjablonTallNavn.entries.forEach {
            sjablontallMap[it.id] = it
        }

        // Mapper grunnlagstyper til input for core
        val soknadbarnCore = mapSoknadsbarn(beregnForskuddGrunnlag)
        val bostatusPeriodeCoreListe = mapBostatus(beregnForskuddGrunnlag)
        val inntektPeriodeCoreListe = mapInntekt(beregnForskuddGrunnlag)
        val sivilstandPeriodeCoreListe = mapSivilstand(beregnForskuddGrunnlag)
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnForskuddGrunnlag)

        // Validerer at alle nødvendige grunnlag er med
        validerGrunnlag(
            soknadbarnGrunnlag = soknadbarnCore != null,
            bostatusGrunnlag = bostatusPeriodeCoreListe.isNotEmpty(),
            inntektGrunnlag = inntektPeriodeCoreListe.isNotEmpty(),
            sivilstandGrunnlag = sivilstandPeriodeCoreListe.isNotEmpty(),
            barnIHusstandenGrunnlag = barnIHusstandenPeriodeCoreListe.isNotEmpty(),
        )

        val sjablonPeriodeCoreListe =
            mapSjablonVerdier(
                beregnDatoFra = beregnForskuddGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnForskuddGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablontallListe,
                sjablontallMap = sjablontallMap,
            )

        return BeregnForskuddGrunnlagCore(
            beregnDatoFra = beregnForskuddGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnForskuddGrunnlag.periode.til!!.atDay(1),
            soknadBarn = soknadbarnCore!!,
            bostatusPeriodeListe = bostatusPeriodeCoreListe,
            inntektPeriodeListe = inntektPeriodeCoreListe,
            sivilstandPeriodeListe = sivilstandPeriodeCoreListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeCoreListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }

    private fun mapSoknadsbarn(beregnForskuddGrunnlag: BeregnGrunnlag): SoknadBarnCore? {
        try {
            val soknadsbarnGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<Person>(
                    grunnlagType = Grunnlagstype.PERSON_SØKNADSBARN,
                    referanse = beregnForskuddGrunnlag.søknadsbarnReferanse,
                )

            return if (soknadsbarnGrunnlag.isEmpty() || soknadsbarnGrunnlag.count() > 1) {
                null
            } else {
                SoknadBarnCore(
                    referanse = soknadsbarnGrunnlag[0].referanse,
                    fodselsdato = soknadsbarnGrunnlag[0].innhold.fødselsdato,
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.PERSON er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapBostatus(beregnForskuddGrunnlag: BeregnGrunnlag): List<BostatusPeriodeCore> {
        try {
            val bostatusGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåFremmedReferanse<BostatusPeriode>(
                    grunnlagType = Grunnlagstype.BOSTATUS_PERIODE,
                    referanse = beregnForskuddGrunnlag.søknadsbarnReferanse,
                )

            return bostatusGrunnlag.map {
                BostatusPeriodeCore(
                    referanse = it.referanse,
                    periode =
                    PeriodeCore(
                        datoFom = it.innhold.periode.toDatoperiode().fom,
                        datoTil = it.innhold.periode.toDatoperiode().til,
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

    private fun mapInntekt(beregnForskuddGrunnlag: BeregnGrunnlag): List<InntektPeriodeCore> {
        try {
            val inntektGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<InntektsrapporteringPeriode>(
                    grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                )

            return inntektGrunnlag
                .filter { it.innhold.valgt }
                .filter { it.innhold.gjelderBarn == null || it.innhold.gjelderBarn == beregnForskuddGrunnlag.søknadsbarnReferanse }
                .map {
                    InntektPeriodeCore(
                        referanse = it.referanse,
                        periode =
                        PeriodeCore(
                            datoFom = it.innhold.periode.toDatoperiode().fom,
                            datoTil = it.innhold.periode.toDatoperiode().til,
                        ),
                        type = it.innhold.inntektsrapportering.name,
                        belop = it.innhold.beløp,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig: " +
                    e.message,
            )
        }
    }

    private fun mapSivilstand(beregnForskuddGrunnlag: BeregnGrunnlag): List<SivilstandPeriodeCore> {
        try {
            val sivilstandGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<SivilstandPeriode>(Grunnlagstype.SIVILSTAND_PERIODE)

            return sivilstandGrunnlag.map {
                SivilstandPeriodeCore(
                    referanse = it.referanse,
                    periode =
                    PeriodeCore(
                        datoFom = it.innhold.periode.toDatoperiode().fom,
                        datoTil = it.innhold.periode.toDatoperiode().til,
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

    private fun mapBarnIHusstanden(beregnForskuddGrunnlag: BeregnGrunnlag): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlag =
                beregnForskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)

            return barnIHusstandenGrunnlag
                .filter { it.innhold.bostatus == Bostatuskode.MED_FORELDER || it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG }
                .map {
                    BarnIHusstandenPeriodeCore(
                        referanse = it.referanse,
                        periode =
                        PeriodeCore(
                            datoFom = it.innhold.periode.toDatoperiode().fom,
                            datoTil = it.innhold.periode.toDatoperiode().til,
                        ),
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun validerGrunnlag(
        soknadbarnGrunnlag: Boolean,
        bostatusGrunnlag: Boolean,
        inntektGrunnlag: Boolean,
        sivilstandGrunnlag: Boolean,
        barnIHusstandenGrunnlag: Boolean,
    ) {
        when {
            !soknadbarnGrunnlag -> {
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

            !barnIHusstandenGrunnlag -> {
                throw IllegalArgumentException("Barn i husstanden mangler i input")
            }
        }
    }

    // Plukker ut aktuelle sjabloner og flytter inn i inputen til core-modulen
    private fun mapSjablonVerdier(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: HashMap<String, SjablonTallNavn>,
    ): List<SjablonPeriodeCore> {
        return sjablonSjablontallListe
            .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
            .filter { (sjablontallMap.getOrDefault(it.typeSjablon, SjablonTallNavn.DUMMY)).forskudd }
            .map {
                SjablonPeriodeCore(
                    periode = PeriodeCore(it.datoFom!!, justerTilDato(it.datoTom)),
                    navn = sjablontallMap.getOrDefault(it.typeSjablon, SjablonTallNavn.DUMMY).navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = it.verdi!!)),
                )
            }
    }

    private fun justerTilDato(dato: LocalDate?): LocalDate? {
        return if (dato == null || dato == MAX_DATO) {
            null
        } else if (dato.dayOfMonth != 1) {
            dato.plusMonths(1).withDayOfMonth(1)
        } else {
            dato
        }
    }
}
