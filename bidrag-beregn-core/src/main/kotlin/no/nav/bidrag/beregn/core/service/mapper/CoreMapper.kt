package no.nav.bidrag.beregn.core.service.mapper

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonTrinnvisSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.Delberegning
import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNøkkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.inneholderBarnetilleggTiltakspenger
import no.nav.bidrag.beregn.core.util.InntektUtil.justerForBarnetilleggTiltakspenger
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Collections.emptyList

abstract class CoreMapper {
    private val maxDato = LocalDate.parse("9999-12-31")

    // Henter sjablonverdi for innslag kapitalinntekt
    // NB! Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier.
    fun finnInnslagKapitalinntektFraSjablontallListe(sjablontallListe: List<Sjablontall>): Sjablontall? =
        sjablontallListe.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }

    // Henter sjablonverdi for kapitalinntekt og returnerer Sjablontall-objekt
    // NB! Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier.
    fun finnInnslagKapitalinntektFraGrunnlag(sjablonListe: List<GrunnlagDto>): Sjablontall? = sjablonListe
        .filter { it.referanse.contains(SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.navn) }
        .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
        .firstOrNull()
        ?.let { innslagKapitalinntektSjablon ->
            Sjablontall(
                typeSjablon = innslagKapitalinntektSjablon.innhold.sjablon.id,
                datoFom = innslagKapitalinntektSjablon.innhold.periode.fom.atDay(1),
                datoTom = innslagKapitalinntektSjablon.innhold.periode.til?.atEndOfMonth()?.minusMonths(1),
                verdi = innslagKapitalinntektSjablon.innhold.verdi,
            )
        }

    fun finnReferanseTilRolle(grunnlagListe: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) = grunnlagListe
        .firstOrNull { it.type == grunnlagstype }?.referanse ?: throw NoSuchElementException("Grunnlagstype $grunnlagstype mangler i input")

    fun finnPersonFraReferanse(grunnlagListe: List<GrunnlagDto>, referanse: String): Person {
        val person = grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<Person>(referanse = referanse)
            .filter { it.referanse == referanse }

        return Person(
            ident = person.first().innhold.ident,
            fødselsdato = person.first().innhold.fødselsdato,
            navn = person.first().innhold.navn,
        )
    }

    fun mapInntekt(
        beregnGrunnlag: BeregnGrunnlag,
        referanseTilRolle: String,
        innslagKapitalinntektSjablonverdi: BigDecimal,
        erSærbidrag: Boolean = false,
        åpenSluttperiode: Boolean = false,
        erForskudd: Boolean = false,
    ): List<InntektPeriodeCore> {
        try {
            val inntektGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                        grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        referanse = referanseTilRolle,
                    )
                    .filter { it.innhold.valgt }
                    .filter { it.innhold.gjelderBarn == null || it.innhold.gjelderBarn == beregnGrunnlag.søknadsbarnReferanse }
                    .map {
                        InntektPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil =
                                if (erSærbidrag) {
                                    it.innhold.periode.toDatoperiode().til
                                } else {
                                    mapDatoTil(
                                        grunnlagPeriode = it.innhold.periode,
                                        beregnPeriodeTil = beregnGrunnlag.periode.til,
                                    )
                                },
                            ),
                            beløp = if (erKapitalinntekt(it.innhold.inntektsrapportering)) {
                                justerKapitalinntekt(
                                    beløp = it.innhold.beløp,
                                    innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                                )
                            } else if (inneholderBarnetilleggTiltakspenger(it.innhold)) {
                                justerForBarnetilleggTiltakspenger(it.innhold)
                            } else {
                                it.innhold.beløp
                            },
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            val akkumulertInntektListe: MutableList<InntektPeriodeCore>

            if (inntektGrunnlagListe.isEmpty()) {
                // Oppretter en periode med inntekt = 0 hvis grunnlagslisten er tom
                akkumulertInntektListe = mutableListOf(
                    InntektPeriodeCore(
                        referanse = opprettDelberegningreferanse(
                            type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                            periode = ÅrMånedsperiode(fom = beregnGrunnlag.periode.fom, til = null),
                            gjelderReferanse = referanseTilRolle,
                            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
                        ),
                        periode = PeriodeCore(
                            datoFom = beregnGrunnlag.periode.toDatoperiode().fom,
                            datoTil = beregnGrunnlag.periode.toDatoperiode().til,
                        ),
                        beløp = BigDecimal.valueOf(0.00).setScale(2),
                        grunnlagsreferanseListe = emptyList(),
                    ),
                )
            } else {
                akkumulertInntektListe = akkumulerOgPeriodiser(
                    grunnlagListe = inntektGrunnlagListe,
                    søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = referanseTilRolle,
                    clazz = InntektPeriodeCore::class.java,
                    beregningsperiode = beregnGrunnlag.periode,
                    erForskudd = erForskudd,
                ).toMutableList()
            }

            // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true (ikke for særbidrag)
            if ((!erSærbidrag) && (akkumulertInntektListe.isNotEmpty())) {
                val sisteElement = akkumulertInntektListe.last()
                if (sisteElement.periode.datoTil != null && åpenSluttperiode) {
                    val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(datoTil = null))
                    akkumulertInntektListe[akkumulertInntektListe.size - 1] = oppdatertSisteElement
                }
            }

            return akkumulertInntektListe
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig: " +
                    e.message,
            )
        }
    }

    fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
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

    fun mapSjablonSjablontall(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: Map<String, SjablonTallNavn>,
        criteria: (SjablonTallNavn) -> Boolean,
    ): List<SjablonPeriodeCore> = sjablonSjablontallListe
        .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
        .filter { criteria(sjablontallMap.getOrDefault(it.typeSjablon, SjablonTallNavn.DUMMY)) }
        .map {
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                navn = sjablontallMap.getOrDefault(it.typeSjablon, SjablonTallNavn.DUMMY).navn,
                nøkkelListe = emptyList(),
                innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = it.verdi!!)),
            )
        }

    fun mapSjablonBidragsevne(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonBidragsevneListe: List<Bidragsevne>,
    ): List<SjablonPeriodeCore> = sjablonBidragsevneListe
        .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
        .map {
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                navn = SjablonNavn.BIDRAGSEVNE.navn,
                nøkkelListe = listOf(SjablonNøkkelCore(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = it.bostatus!!)),
                innholdListe = listOf(
                    SjablonInnholdCore(navn = SjablonInnholdNavn.BOUTGIFT_BELØP.navn, verdi = it.belopBoutgift!!),
                    SjablonInnholdCore(navn = SjablonInnholdNavn.UNDERHOLD_BELØP.navn, verdi = it.belopUnderhold!!),
                ),
            )
        }

    fun mapSjablonTrinnvisSkattesats(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonTrinnvisSkattesatsPeriodeGrunnlag> {
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

    // Mapper sjabloner av typen trinnvis skattesats
    // Filtrerer bort de sjablonene som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    fun mapSjablonTrinnvisSkattesats(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonTrinnvisSkattesatsListe: List<TrinnvisSkattesats>,
    ): List<SjablonPeriodeCore> = sjablonTrinnvisSkattesatsListe
        .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
        .map {
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                nøkkelListe = emptyList(),
                innholdListe = listOf(
                    SjablonInnholdCore(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = it.inntektgrense!!),
                    SjablonInnholdCore(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = it.sats!!),
                ),
            )
        }

    fun mapSjablonSamværsfradrag(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSamværsfradragListe: List<Samværsfradrag>,
    ): List<SjablonPeriodeCore> = sjablonSamværsfradragListe
        .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
        .map {
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                navn = SjablonNavn.SAMVÆRSFRADRAG.navn,
                nøkkelListe = listOf(
                    SjablonNøkkelCore(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = it.samvaersklasse!!),
                    SjablonNøkkelCore(navn = SjablonNøkkelNavn.ALDER_TOM.navn, verdi = it.alderTom!!.toString()),
                ),
                innholdListe = listOf(
                    SjablonInnholdCore(navn = SjablonInnholdNavn.FRADRAG_BELØP.navn, verdi = it.belopFradrag!!),
                ),
            )
        }

    // Lager en gruppert liste hvor grunnlaget er akkumulert pr bruddperiode, med en liste over tilhørende grunnlagsreferanser
    fun <T : Delberegning> akkumulerOgPeriodiser(
        grunnlagListe: List<T>,
        søknadsbarnreferanse: String,
        gjelderReferanse: String,
        clazz: Class<T>,
        beregningsperiode: ÅrMånedsperiode = ÅrMånedsperiode(fom = LocalDate.MIN, til = LocalDate.MAX),
        erForskudd: Boolean = false,
    ): List<T> {
        // TODO Logikken bør være lik for forskudd, særbidrag og bidrag. Splittet opp ifbm. at beregningsperiode blir justert hvis barnet fyller 18 år
        // TODO i perioden. Dette medførte feil i forskuddstestene. Holder derfor logikken adskilt for nå.
        val periodeListe: List<Periode>

        if (erForskudd) {
            // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
            periodeListe = grunnlagListe
                .flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
                .distinct()
                .sortedBy { it }
                .sortedWith(compareBy { it == null })
                .zipWithNext()
                .map { Periode(it.first!!, it.second) }
        } else {
            // Legger til beregningsperiode i bruddato-listen
            val bruddatoListe = grunnlagListe
                .flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
                .toMutableList()
            if (beregningsperiode.fom != YearMonth.from(LocalDate.MIN)) {
                bruddatoListe.add(beregningsperiode.fom.atDay(1))
                bruddatoListe.add(beregningsperiode.til?.atDay(1))
            }

            // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst og slår sammen brudddatoer til en liste med
            // perioder (fom-/til-dato)
            periodeListe = bruddatoListe
                .distinct()
                .filter { it == null || beregningsperiode.inneholder(YearMonth.from(it)) } // Filtrerer ut datoer utenfor beregningsperiode
                .filter { it != null || (beregningsperiode.til == null) } // Filtrerer ut null hvis beregningsperiode.til ikke er null
                .sortedBy { it }
                .sortedWith(compareBy { it == null })
                .zipWithNext()
                .map { Periode(it.first!!, it.second) }
        }

        // Returnerer en gruppert og akkumulert liste, med en liste over tilhørende grunnlagsreferanser, pr bruddperiode
        return when (clazz) {
            InntektPeriodeCore::class.java -> {
                akkumulerOgPeriodiserInntekter(
                    grunnlagListe as List<InntektPeriodeCore>,
                    periodeListe,
                    søknadsbarnreferanse,
                    gjelderReferanse,
                ) as List<T>
            }

            BarnIHusstandenPeriodeCore::class.java -> {
                akkumulerOgPeriodiserBarnIHusstanden(
                    grunnlagListe as List<BarnIHusstandenPeriodeCore>,
                    periodeListe,
                    søknadsbarnreferanse,
                    gjelderReferanse,
                ) as List<T>
            }

            VoksneIHusstandenPeriodeCore::class.java -> {
                akkumulerOgPeriodiserVoksneIHusstanden(
                    grunnlagListe as List<VoksneIHusstandenPeriodeCore>,
                    periodeListe,
                    søknadsbarnreferanse,
                    gjelderReferanse,
                ) as List<T>
            }

            FaktiskUtgiftPeriodeCore::class.java -> {
                akkumulerOgPeriodiserFaktiskUtgift(
                    grunnlagListe as List<FaktiskUtgiftPeriodeCore>,
                    periodeListe,
                    gjelderReferanse,
                ) as List<T>
            }

            TilleggsstønadPeriodeCore::class.java -> {
                akkumulerOgPeriodiserTilleggsstønad(
                    grunnlagListe as List<TilleggsstønadPeriodeCore>,
                    periodeListe,
                    gjelderReferanse,
                ) as List<T>
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
        søknadsbarnreferanse: Grunnlagsreferanse,
        gjelderReferanse: Grunnlagsreferanse,
    ): List<InntektPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = inntektGrunnlagListe, periode = periode)

            InntektPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = null),
                    søknadsbarnReferanse = søknadsbarnreferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                beløp = filtrertGrunnlagsliste.sumOf { it.beløp }.setScale(2),
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
            )
        }

    // Grupperer og teller antall barn i husstanden pr bruddperiode
    private fun akkumulerOgPeriodiserBarnIHusstanden(
        barnIHusstandenGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
        periodeListe: List<Periode>,
        søknadsbarnReferanse: Grunnlagsreferanse,
        gjelderReferanse: Grunnlagsreferanse,
    ): List<BarnIHusstandenPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = barnIHusstandenGrunnlagListe, periode = periode)

            BarnIHusstandenPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = null),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                antall = filtrertGrunnlagsliste.sumOf { it.antall },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
            )
        }

    // Grupperer og teller antall voksne i husstanden pr bruddperiode
    private fun akkumulerOgPeriodiserVoksneIHusstanden(
        voksneIHusstandenGrunnlagListe: List<VoksneIHusstandenPeriodeCore>,
        periodeListe: List<Periode>,
        søknadsbarnReferanse: Grunnlagsreferanse,
        gjelderReferanse: Grunnlagsreferanse,
    ): List<VoksneIHusstandenPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = voksneIHusstandenGrunnlagListe, periode = periode)

            VoksneIHusstandenPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = null),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                borMedAndreVoksne = filtrertGrunnlagsliste.any { it.borMedAndreVoksne },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
            )
        }

    // Grupperer og teller faktiske utgifter pr bruddperiode
    private fun akkumulerOgPeriodiserFaktiskUtgift(
        faktiskUtgiftPeriodeCoreListe: List<FaktiskUtgiftPeriodeCore>,
        periodeListe: List<Periode>,
        gjelderReferanse: Grunnlagsreferanse,
    ): List<FaktiskUtgiftPeriodeCore> {
        val resultatListe = mutableListOf<FaktiskUtgiftPeriodeCore>()

        // Akkumulerer beregnetBeløp per gjelderBarn i perioden
        periodeListe
            .map { periode ->
                val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = faktiskUtgiftPeriodeCoreListe, periode = periode)

                val totalBeregnetBeløpBarn = filtrertGrunnlagsliste
                    .groupBy { it.gjelderBarn }
                    .mapValues { entry -> entry.value.sumOf { it.beregnetBeløp } }

                totalBeregnetBeløpBarn.forEach { (gjelderBarn, beregnetBeløp) ->
                    resultatListe.add(
                        FaktiskUtgiftPeriodeCore(
                            referanse = opprettDelberegningreferanse(
                                type = Grunnlagstype.DELBEREGNING_FAKTISK_UTGIFT,
                                periode = ÅrMånedsperiode(fom = periode.datoFom, til = null),
                                søknadsbarnReferanse = gjelderBarn,
                                gjelderReferanse = gjelderReferanse,
                            ),
                            periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                            gjelderBarn = gjelderBarn,
                            beregnetBeløp = beregnetBeløp,
                            grunnlagsreferanseListe = filtrertGrunnlagsliste.filter { it.gjelderBarn == gjelderBarn }.map { it.referanse }
                                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
                        ),
                    )
                }
            }

        return resultatListe
    }

    // Grupperer og teller faktiske utgifter pr bruddperiode
    private fun akkumulerOgPeriodiserTilleggsstønad(
        tilleggsstønadPeriodeCoreListe: List<TilleggsstønadPeriodeCore>,
        periodeListe: List<Periode>,
        gjelderReferanse: Grunnlagsreferanse,
    ): List<TilleggsstønadPeriodeCore> {
        val resultatListe = mutableListOf<TilleggsstønadPeriodeCore>()

        periodeListe
            .map { periode ->
                val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = tilleggsstønadPeriodeCoreListe, periode = periode)

                filtrertGrunnlagsliste.forEach {
                    resultatListe.add(
                        TilleggsstønadPeriodeCore(
                            referanse = opprettDelberegningreferanse(
                                type = Grunnlagstype.DELBEREGNING_TILLEGGSSTØNAD,
                                periode = ÅrMånedsperiode(fom = periode.datoFom, til = null),
                                søknadsbarnReferanse = it.gjelderBarn,
                                gjelderReferanse = gjelderReferanse,
                            ),
                            periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                            gjelderBarn = it.gjelderBarn,
                            beregnetBeløp = it.beregnetBeløp,
                            grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse }
                                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
                        ),
                    )
                }
            }

        return resultatListe
    }

    // Filtrerer ut grunnlag som tilhører en gitt periode
    private fun <T : Delberegning> filtrerGrunnlagsliste(grunnlagsliste: List<T>, periode: Periode): List<T> = grunnlagsliste.filter { grunnlag ->
        (grunnlag.periode.datoTil == null || periode.datoFom.isBefore(grunnlag.periode.datoTil)) &&
            (periode.datoTil == null || periode.datoTil.isAfter(grunnlag.periode.datoFom))
    }

    private fun justerTilDato(dato: LocalDate?): LocalDate? = if (dato == null || dato == maxDato) {
        null
    } else if (dato.dayOfMonth != 1) {
        dato.plusMonths(1).withDayOfMonth(1)
    } else {
        dato
    }

    // Setter til-dato for grunnlaget til null hvis det er lik eller etter beregnDatoTil
    fun mapDatoTil(grunnlagPeriode: ÅrMånedsperiode, beregnPeriodeTil: YearMonth?): LocalDate? =
        if (grunnlagPeriode.til == null || (beregnPeriodeTil != null && (!grunnlagPeriode.til!!.isBefore(beregnPeriodeTil)))) {
            null
        } else {
            grunnlagPeriode.toDatoperiode().til
        }
}
