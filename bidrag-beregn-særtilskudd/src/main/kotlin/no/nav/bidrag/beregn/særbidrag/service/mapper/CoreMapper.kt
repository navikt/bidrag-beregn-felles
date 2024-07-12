package no.nav.bidrag.beregn.særbidrag.service.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.DelberegningSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.VoksneIHusstandenPeriodeCore
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

abstract class CoreMapper {
    private val maxDato = LocalDate.parse("9999-12-31")

    // Henter sjablonverdi for kapitalinntekt
    // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
    fun finnInnslagKapitalinntekt(sjablontallListe: List<Sjablontall>) =
        sjablontallListe.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi ?: BigDecimal.ZERO

    fun finnReferanseTilRolle(grunnlagListe: List<GrunnlagDto>, grunnlagstype: Grunnlagstype) = grunnlagListe
        .firstOrNull { it.type == grunnlagstype }?.referanse ?: throw NoSuchElementException("Grunnlagstype $grunnlagstype mangler i input")

    // TODO Kan slås sammen med mapInntekt for forskudd?
    fun mapInntekt(
        beregnSærbidragrunnlag: BeregnGrunnlag,
        referanseBidragspliktig: String,
        innslagKapitalinntektSjablonverdi: BigDecimal,
    ): List<InntektPeriodeCore> {
        try {
            val inntektGrunnlagListe =
                beregnSærbidragrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                        grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        referanse = referanseBidragspliktig,
                    )
                    .filter { it.innhold.valgt }
                    .filter { it.innhold.gjelderBarn == null || it.innhold.gjelderBarn == beregnSærbidragrunnlag.søknadsbarnReferanse }
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
            return akkumulerOgPeriodiser(
                grunnlagListe = inntektGrunnlagListe,
                referanse = referanseBidragspliktig,
                clazz = InntektPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig: " +
                    e.message,
            )
        }
    }

    // TODO Bør det lages delberegninger uansett om det ikke er inntekter og/eller hjemmeboende barn i en periode (i så fall mappe ut 0 eller null)?
    // TODO Søknadsbarnet vil f.eks. alltid ha en bostatus selv om det ikke bor hjemme

    // Lager en gruppert liste hvor grunnlaget er akkumulert pr bruddperiode, med en liste over tilhørende grunnlagsreferanser
    fun <T : DelberegningSærbidrag> akkumulerOgPeriodiser(grunnlagListe: List<T>, referanse: String, clazz: Class<T>): List<T> {
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
                akkumulerOgPeriodiserInntekter(grunnlagListe as List<InntektPeriodeCore>, periodeListe, referanse) as List<T>
            }

            BarnIHusstandenPeriodeCore::class.java -> {
                akkumulerOgPeriodiserBarnIHusstanden(grunnlagListe as List<BarnIHusstandenPeriodeCore>, periodeListe, referanse) as List<T>
            }

            VoksneIHusstandenPeriodeCore::class.java -> {
                akkumulerOgPeriodiserVoksneIHusstanden(grunnlagListe as List<VoksneIHusstandenPeriodeCore>, periodeListe, referanse) as List<T>
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
        referanse: Grunnlagsreferanse,
    ): List<InntektPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = inntektGrunnlagListe, periode = periode)

            InntektPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    søknadsbarnReferanse = referanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                beløp = filtrertGrunnlagsliste.sumOf { it.beløp },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse },
            )
        }

    // Grupperer og teller antall barn i husstanden pr bruddperiode
    private fun akkumulerOgPeriodiserBarnIHusstanden(
        barnIHusstandenGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
        periodeListe: List<Periode>,
        søknadsbarnReferanse: Grunnlagsreferanse,
    ): List<BarnIHusstandenPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = barnIHusstandenGrunnlagListe, periode = periode)

            BarnIHusstandenPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                antall = filtrertGrunnlagsliste.sumOf { it.antall },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse },
            )
        }

    // Grupperer og teller antall voksne i husstanden pr bruddperiode
    private fun akkumulerOgPeriodiserVoksneIHusstanden(
        voksneIHusstandenGrunnlagListe: List<VoksneIHusstandenPeriodeCore>,
        periodeListe: List<Periode>,
        søknadsbarnReferanse: Grunnlagsreferanse,
    ): List<VoksneIHusstandenPeriodeCore> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = voksneIHusstandenGrunnlagListe, periode = periode)

            VoksneIHusstandenPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND,
                    periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                ),
                periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                borMedAndre = filtrertGrunnlagsliste.any { it.borMedAndre },
                grunnlagsreferanseListe = filtrertGrunnlagsliste.map { it.referanse },
            )
        }

    // Filtrerer ut grunnlag som tilhører en gitt periode
    private fun <T : DelberegningSærbidrag> filtrerGrunnlagsliste(grunnlagsliste: List<T>, periode: Periode): List<T> =
        grunnlagsliste.filter { grunnlag ->
            (grunnlag.periode.datoTil == null || periode.datoFom.isBefore(grunnlag.periode.datoTil)) &&
                (periode.datoTil == null || periode.datoTil!!.isAfter(grunnlag.periode.datoFom))
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
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = it.verdi!!)),
                )
            }
    }

    fun mapSjablonSjablontallBidragsevne(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: Map<String, SjablonTallNavn>,
    ): List<SjablonPeriodeCore> {
        return mapSjablonSjablontall(
            beregnDatoFra = beregnDatoFra,
            beregnDatoTil = beregnDatoTil,
            sjablonSjablontallListe = sjablonSjablontallListe,
            sjablontallMap = sjablontallMap,
        ) { it.bidragsevne }
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
                    nokkelListe = listOf(SjablonNokkelCore(navn = SjablonNøkkelNavn.BOSTATUS.navn, verdi = it.bostatus!!)),
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
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnholdCore(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = it.inntektgrense!!),
                        SjablonInnholdCore(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = it.sats!!),
                    ),
                )
            }
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

    companion object {

        @JvmStatic
        fun tilJsonNode(`object`: Any?): JsonNode {
            val mapper = ObjectMapper()
            return mapper.valueToTree(`object`)
        }
    }
}
