package no.nav.bidrag.beregn.særtilskudd.service.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.DelberegningSærtilskudd
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.VoksneIHusstandenPeriodeCore
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
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal
import java.time.LocalDate

abstract class CoreMapper {
    private val maxDato = LocalDate.parse("9999-12-31")

    val filtrerDelberegning: (String) -> (SjablonTallNavn) -> Boolean = { delberegning ->
        { sjablonTallNavn ->
            when (delberegning) {
                "bidragsevne" -> sjablonTallNavn.bidragsevne
                else -> false
            }
        }
    }

    // TODO Kan slås sammen med mapInntekt for forskudd?
    fun mapInntekt(
        beregnSærtilskuddGrunnlag: BeregnGrunnlag,
        referanseBidragspliktig: String,
        innslagKapitalinntektSjablonverdi: BigDecimal,
    ): List<InntektPeriodeCore> {
        try {
            val inntektGrunnlagListe =
                beregnSærtilskuddGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
                        grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        referanse = referanseBidragspliktig,
                    )
                    .filter { it.innhold.valgt }
                    .filter { it.innhold.gjelderBarn == null || it.innhold.gjelderBarn == beregnSærtilskuddGrunnlag.søknadsbarnReferanse }
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
            return akkumulerOgPeriodiser(inntektGrunnlagListe, referanseBidragspliktig, InntektPeriodeCore::class.java)
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
    fun <T : DelberegningSærtilskudd> akkumulerOgPeriodiser(grunnlagListe: List<T>, referanse: String, clazz: Class<T>): List<T> {
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
    private fun <T : DelberegningSærtilskudd> filtrerGrunnlagsliste(grunnlagsliste: List<T>, periode: Periode): List<T> =
        grunnlagsliste.filter { grunnlag ->
            (grunnlag.periode.datoTil == null || periode.datoFom.isBefore(grunnlag.periode.datoTil)) &&
                (periode.datoTil == null || periode.datoTil!!.isAfter(grunnlag.periode.datoFom))
        }

    // Mapper sjabloner av typen sjablontall
    // Filtrerer bort de sjablonene som ikke brukes i den aktuelle delberegningen og de som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    fun mapSjablonSjablontall(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        sjablonSjablontallListe: List<Sjablontall>,
        sjablontallMap: Map<String, SjablonTallNavn>,
        filter: (SjablonTallNavn) -> Boolean,
    ): List<SjablonPeriodeCore> {
        return sjablonSjablontallListe
            .filter { !(it.datoFom!!.isAfter(beregnDatoTil) || it.datoTom!!.isBefore(beregnDatoFra)) }
            .filter { (sjablontallMap.getOrDefault(key = it.typeSjablon, defaultValue = SjablonTallNavn.DUMMY)).bidragsevne }
            .map {
                SjablonPeriodeCore(
                    periode = PeriodeCore(datoFom = it.datoFom!!, datoTil = justerTilDato(it.datoTom)),
                    navn = sjablontallMap.getOrDefault(key = it.typeSjablon, defaultValue = SjablonTallNavn.DUMMY).navn,
                    nokkelListe = emptyList(),
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

    // Mapper sjabloner av typen samværsfradrag
    // Filtrerer bort de sjablonene som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
//    protected fun mapSjablonSamvaersfradrag(
//        sjablonSamvaersfradragListe: List<Samværsfradrag>,
//        beregnGrunnlag: BeregnGrunnlag,
//    ): List<SjablonPeriodeCore> {
//        val beregnDatoFra = beregnGrunnlag.beregnDatoFra
//        val beregnDatoTil = beregnGrunnlag.beregnDatoTil
//        return sjablonSamvaersfradragListe
//            .stream()
//            .filter { (_, _, datoFom, datoTom): Samværsfradrag -> datoFom!!.isBefore(beregnDatoTil) && !datoTom!!.isBefore(beregnDatoFra) }
//            .map { (samvaersklasse, alderTom, datoFom, datoTom, antDagerTom, antNetterTom, belopFradrag): Samværsfradrag ->
//                SjablonPeriodeCore(
//                    PeriodeCore(datoFom!!, datoTom),
//                    SjablonNavn.SAMVÆRSFRADRAG.navn,
//                    Arrays.asList(
//                        SjablonNokkelCore(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, samvaersklasse!!),
//                        SjablonNokkelCore(SjablonNøkkelNavn.ALDER_TOM.navn, alderTom.toString()),
//                    ),
//                    Arrays.asList(
//                        SjablonInnholdCore(
//                            SjablonInnholdNavn.ANTALL_DAGER_TOM.navn,
//                            BigDecimal.valueOf(
//                                antDagerTom!!.toLong(),
//                            ),
//                        ),
//                        SjablonInnholdCore(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(antNetterTom!!.toLong())),
//                        SjablonInnholdCore(SjablonInnholdNavn.FRADRAG_BELØP.navn, belopFradrag!!),
//                    ),
//                )
//            }
//            .toList()
//    }
//
//    protected fun mapSjablontall(): Map<String, SjablonTallNavn> {
//        val sjablontallMap = HashMap<String, SjablonTallNavn>()
//        for (sjablonTallNavn in SjablonTallNavn.entries) {
//            sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
//        }
//        return sjablontallMap
//    }

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
//        const val BP_ANDEL_SAERTILSKUDD = "BPsAndelSaertilskudd"
//        const val BIDRAGSEVNE = "Bidragsevne"
//        const val SAERTILSKUDD = "Saertilskudd"
//
//        // Sjekker om en type SjablonTall er i bruk for en delberegning
//        private fun filtrerSjablonTall(sjablonTallNavn: SjablonTallNavn, delberegning: String): Boolean = when (delberegning) {
//            SAERTILSKUDD -> sjablonTallNavn.saertilskudd
//            BIDRAGSEVNE -> sjablonTallNavn.bidragsevne
//            BP_ANDEL_SAERTILSKUDD -> sjablonTallNavn.bpAndelSaertilskudd
//            else -> false
//        }

        @JvmStatic
        fun tilJsonNode(`object`: Any?): JsonNode {
            val mapper = ObjectMapper()
            return mapper.valueToTree(`object`)
        }
    }
}
