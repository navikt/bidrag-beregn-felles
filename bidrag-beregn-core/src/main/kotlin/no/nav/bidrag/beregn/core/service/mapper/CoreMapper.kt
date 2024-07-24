package no.nav.bidrag.beregn.core.service.mapper

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.Delberegning
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNøkkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
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
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

abstract class CoreMapper {
    private val maxDato = LocalDate.parse("9999-12-31")

    // Henter sjablonverdi for innslag kapitalinntekt
    // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
    fun finnInnslagKapitalinntekt(sjablontallListe: List<Sjablontall>): BigDecimal =
        sjablontallListe.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi ?: BigDecimal.ZERO

    fun finnReferanseTilRolle(grunnlagListe: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) = grunnlagListe
        .firstOrNull { it.type == grunnlagstype }?.referanse ?: throw NoSuchElementException("Grunnlagstype $grunnlagstype mangler i input")

    fun mapInntekt(
        beregnGrunnlag: BeregnGrunnlag,
        referanseTilRolle: String,
        innslagKapitalinntektSjablonverdi: BigDecimal,
        erSærbidrag: Boolean = false,
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
                            } else {
                                it.innhold.beløp
                            },
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            return if (erSærbidrag && inntektGrunnlagListe.isEmpty()) {
                // Oppretter en periode med inntekt = 0 hvis grunnlagslisten er tom
                listOf(
                    InntektPeriodeCore(
                        referanse = opprettDelberegningreferanse(
                            type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                            periode = ÅrMånedsperiode(fom = beregnGrunnlag.periode.fom, til = beregnGrunnlag.periode.til),
                            gjelderReferanse = referanseTilRolle,
                            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
                        ),
                        periode = PeriodeCore(
                            datoFom = beregnGrunnlag.periode.toDatoperiode().fom,
                            datoTil = beregnGrunnlag.periode.toDatoperiode().til,
                        ),
                        beløp = BigDecimal.ZERO,
                        grunnlagsreferanseListe = emptyList(),
                    ),
                )
            } else {
                akkumulerOgPeriodiser(
                    grunnlagListe = inntektGrunnlagListe,
                    søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = referanseTilRolle,
                    clazz = InntektPeriodeCore::class.java,
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig: " +
                    e.message,
            )
        }
    }

    fun mapSjablonSjablontall(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: Map<String, SjablonTallNavn>,
        criteria: (SjablonTallNavn) -> Boolean,
    ): List<SjablonPeriodeCore> {
        return sjablonSjablontallListe
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
    }

    fun mapSjablonBidragsevne(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonBidragsevneListe: List<Bidragsevne>,
    ): List<SjablonPeriodeCore> {
        return sjablonBidragsevneListe
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
    }

    // Mapper sjabloner av typen trinnvis skattesats
    // Filtrerer bort de sjablonene som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    fun mapSjablonTrinnvisSkattesats(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonTrinnvisSkattesatsListe: List<TrinnvisSkattesats>,
    ): List<SjablonPeriodeCore> {
        return sjablonTrinnvisSkattesatsListe
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
    }

    // Lager en gruppert liste hvor grunnlaget er akkumulert pr bruddperiode, med en liste over tilhørende grunnlagsreferanser
    fun <T : Delberegning> akkumulerOgPeriodiser(
        grunnlagListe: List<T>,
        søknadsbarnreferanse: String,
        gjelderReferanse: String,
        clazz: Class<T>,
    ): List<T> {
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
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    søknadsbarnReferanse = søknadsbarnreferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                beløp = filtrertGrunnlagsliste.sumOf { it.beløp },
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
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
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
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                borMedAndre = filtrertGrunnlagsliste.any { it.borMedAndre },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
            )
        }

    // Filtrerer ut grunnlag som tilhører en gitt periode
    private fun <T : Delberegning> filtrerGrunnlagsliste(grunnlagsliste: List<T>, periode: Periode): List<T> = grunnlagsliste.filter { grunnlag ->
        (grunnlag.periode.datoTil == null || periode.datoFom.isBefore(grunnlag.periode.datoTil)) &&
            (periode.datoTil == null || periode.datoTil.isAfter(grunnlag.periode.datoFom))
    }

    private fun justerTilDato(dato: LocalDate?): LocalDate? {
        return if (dato == null || dato == maxDato) {
            null
        } else if (dato.dayOfMonth != 1) {
            dato.plusMonths(1).withDayOfMonth(1)
        } else {
            dato
        }
    }

    // Setter til-dato for grunnlaget til null hvis det er lik eller etter beregnDatoTil
    fun mapDatoTil(grunnlagPeriode: ÅrMånedsperiode, beregnPeriodeTil: YearMonth?): LocalDate? {
        return if (grunnlagPeriode.til == null || (beregnPeriodeTil != null && (!grunnlagPeriode.til!!.isBefore(beregnPeriodeTil)))) {
            null
        } else {
            grunnlagPeriode.toDatoperiode().til
        }
    }
}
