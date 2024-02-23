package no.nav.bidrag.beregn.forskudd.service

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.forskudd.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.DelberegningForskudd
import no.nav.bidrag.beregn.forskudd.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SivilstandPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SøknadsbarnCore
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal
import java.time.LocalDate

internal object CoreMapper {
    private val MAX_DATO = LocalDate.parse("9999-12-31")

    fun mapGrunnlagTilCore(beregnForskuddGrunnlag: BeregnGrunnlag, sjablontallListe: List<Sjablontall>): BeregnForskuddGrunnlagCore {
        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        SjablonTallNavn.entries.forEach {
            sjablontallMap[it.id] = it
        }

        // Henter sjablonverdi for kapitalinntekt
        // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
        val innslagKapitalinntektSjablonverdi =
            sjablontallListe.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi ?: BigDecimal.ZERO

        val referanseBidragsmottaker = beregnForskuddGrunnlag.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_BIDRAGSMOTTAKER mangler i input")

        // Mapper grunnlagstyper til input for core
        val søknadsbarnCore = mapSøknadsbarn(beregnForskuddGrunnlag)
        val bostatusPeriodeCoreListe = mapBostatus(beregnForskuddGrunnlag)
        val inntektPeriodeCoreListe = mapInntekt(beregnForskuddGrunnlag, referanseBidragsmottaker, innslagKapitalinntektSjablonverdi)
        val sivilstandPeriodeCoreListe = mapSivilstand(beregnForskuddGrunnlag)
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnForskuddGrunnlag)

        // Validerer at alle nødvendige grunnlag er med
        validerGrunnlag(
            søknadsbarnGrunnlag = søknadsbarnCore != null,
            bostatusGrunnlag = bostatusPeriodeCoreListe.isNotEmpty(),
            inntektGrunnlag = inntektPeriodeCoreListe.isNotEmpty(),
            sivilstandGrunnlag = sivilstandPeriodeCoreListe.isNotEmpty(),
            barnIHusstandenGrunnlag = barnIHusstandenPeriodeCoreListe.isNotEmpty(),
        )

        val sjablonPeriodeCoreListe =
            mapSjablonverdier(
                beregnDatoFra = beregnForskuddGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnForskuddGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablontallListe,
                sjablontallMap = sjablontallMap,
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

    private fun mapInntekt(
        beregnForskuddGrunnlag: BeregnGrunnlag,
        referanseBidragsmottaker: String,
        innslagKapitalinntektSjablonverdi: BigDecimal,
    ): List<InntektPeriodeCore> {
        try {
            val inntektGrunnlagListe =
                beregnForskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                        grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        referanse = referanseBidragsmottaker,
                    )
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
                            beløp = if (erKapitalinntekt(it.innhold.inntektsrapportering)) {
                                justerKapitalinntekt(
                                    beløp = it.innhold.beløp,
                                    innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                                )
                            } else {
                                it.innhold.beløp
                            },
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }
            return akkumulerOgPeriodiser(inntektGrunnlagListe, InntektPeriodeCore::class.java)
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
                    .filtrerOgKonverterBasertPåEgenReferanse<SivilstandPeriode>(grunnlagType = Grunnlagstype.SIVILSTAND_PERIODE)

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
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            antall = 1,
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }
            return akkumulerOgPeriodiser(barnIHusstandenGrunnlagListe, BarnIHusstandenPeriodeCore::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun validerGrunnlag(
        søknadsbarnGrunnlag: Boolean,
        bostatusGrunnlag: Boolean,
        inntektGrunnlag: Boolean,
        sivilstandGrunnlag: Boolean,
        barnIHusstandenGrunnlag: Boolean,
    ) {
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

            !barnIHusstandenGrunnlag -> {
                throw IllegalArgumentException("Barn i husstanden mangler i input")
            }
        }
    }

    // Plukker ut aktuelle sjabloner og flytter inn i inputen til core-modulen
    private fun mapSjablonverdier(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: HashMap<String, SjablonTallNavn>,
    ): List<SjablonPeriodeCore> {
        return sjablonSjablontallListe
            .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
            .filter { (sjablontallMap.getOrDefault(key = it.typeSjablon, defaultValue = SjablonTallNavn.DUMMY)).forskudd }
            .map {
                SjablonPeriodeCore(
                    periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                    navn = sjablontallMap.getOrDefault(key = it.typeSjablon, defaultValue = SjablonTallNavn.DUMMY).navn,
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

    // Lager en gruppert liste hvor grunnlaget er akkumulert pr bruddperiode, med en liste over tilhørende grunnlagsreferanser
    private fun <T : DelberegningForskudd> akkumulerOgPeriodiser(grunnlagListe: List<T>, clazz: Class<T>): List<T> {
        // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
        val bruddatoListe = grunnlagListe
            .flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
            .distinct()
            .sortedBy { it }
            .sortedWith(compareBy { it == null })

        // Slå sammen brudddatoer til en liste med perioder (fom-/til-dato)
        val periodeListe = bruddatoListe
            .zipWithNext()
            .map { Periode(it.first!!, it.second) }

        // Returnerer en gruppert og akkumulert liste, med en liste over tilhørende grunnlagsreferanser, pr bruddperiode
        return when (clazz) {
            InntektPeriodeCore::class.java -> {
                akkumulerOgPeriodiserInntekter(grunnlagListe as List<InntektPeriodeCore>, periodeListe) as List<T>
            }

            BarnIHusstandenPeriodeCore::class.java -> {
                akkumulerOgPeriodiserBarnIHusstanden(grunnlagListe as List<BarnIHusstandenPeriodeCore>, periodeListe) as List<T>
            }

            else -> {
                emptyList()
            }
        }
    }

    // Grupperer og summerer inntekter pr bruddperiode
    private fun akkumulerOgPeriodiserInntekter(
        inntektGrunnlagListe: List<InntektPeriodeCore>,
        periodeListe: List<Periode>,
    ): List<InntektPeriodeCore> {
        return periodeListe
            .map { periode ->
                val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = inntektGrunnlagListe, periode = periode)

                InntektPeriodeCore(
                    referanse = opprettDelberegningreferanse(
                        type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                        periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    ),
                    periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                    beløp = filtrertGrunnlagsliste.sumOf { it.beløp },
                    grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse },
                )
            }
    }

    // Grupperer og teller antall barn i husstanden pr bruddperiode
    private fun akkumulerOgPeriodiserBarnIHusstanden(
        barnIHusstandenGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
        periodeListe: List<Periode>,
    ): List<BarnIHusstandenPeriodeCore> {
        return periodeListe
            .map { periode ->
                val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = barnIHusstandenGrunnlagListe, periode = periode)

                BarnIHusstandenPeriodeCore(
                    referanse = opprettDelberegningreferanse(
                        type = Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND,
                        periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    ),
                    periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                    antall = filtrertGrunnlagsliste.sumOf { it.antall },
                    grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse },
                )
            }
    }

    // Filtrerer ut grunnlag som tilhører en gitt periode
    private fun <T : DelberegningForskudd> filtrerGrunnlagsliste(grunnlagsliste: List<T>, periode: Periode): List<T> {
        return grunnlagsliste.filter { grunnlag ->
            (grunnlag.periode.datoTil == null || periode.datoFom.isBefore(grunnlag.periode.datoTil)) &&
                (periode.datoTil == null || periode.datoTil!!.isAfter(grunnlag.periode.datoFom))
        }
    }
}
