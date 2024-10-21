package no.nav.bidrag.beregn.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype.Companion.inngårIInntektRapporteringer
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class BeregnService {
    fun håndterAvvik(avvikListe: List<AvvikCore>, kontekst: String) {
        if (avvikListe.isNotEmpty()) {
            val avviktekst = avvikListe.joinToString("; ") { it.avvikTekst }
            secureLogger.warn { "Ugyldig input ved beregning av $kontekst. Følgende avvik ble funnet: $avviktekst" }
            throw UgyldigInputException("Ugyldig input ved beregning av $kontekst. Følgende avvik ble funnet: $avviktekst")
        }
    }

    // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
    fun mapDelberegningSumInntekt(
        sumInntektListe: List<InntektPeriodeCore>,
        beregnGrunnlag: BeregnGrunnlag,
        innslagKapitalinntektSjablon: Sjablontall?,
        referanseTilRolle: String? = null,
    ) = sumInntektListe
//        .filter { it.grunnlagsreferanseListe.isNotEmpty() }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = bestemGrunnlagstype(it.referanse),
                innhold = POJONode(
                    DelberegningSumInntekt(
                        periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                        totalinntekt = it.beløp,
                        kontantstøtte = summerInntekter(
                            beregnGrunnlag = beregnGrunnlag,
                            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                            inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer(),
                        ),
                        skattepliktigInntekt = summerInntekter(
                            beregnGrunnlag = beregnGrunnlag,
                            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                            inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer() +
                                Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer() +
                                Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer() +
                                Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                            ekskluderInntekter = true,
                            innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablon?.verdi ?: BigDecimal.ZERO,
                        ),
                        barnetillegg = summerInntekter(
                            beregnGrunnlag = beregnGrunnlag,
                            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                            inntektsrapporteringListe = Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer(),
                        ),
                        utvidetBarnetrygd = summerInntekter(
                            beregnGrunnlag = beregnGrunnlag,
                            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                            inntektsrapporteringListe = Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer(),
                        ),
                        småbarnstillegg = summerInntekter(
                            beregnGrunnlag = beregnGrunnlag,
                            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                            inntektsrapporteringListe = Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                        ),
                    ),
                ),
                grunnlagsreferanseListe = lagGrunnlagsreferanselisteInntekt(
                    grunnlagsreferanseliste = it.grunnlagsreferanseListe,
                    innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                ).sorted(),
                gjelderReferanse = referanseTilRolle,
            )
        }

    // Bestemmer grunnlagstype basert på referanse
    fun bestemGrunnlagstype(referanse: String) = when {
        referanse.contains(Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_SUM_INNTEKT
        referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
        referanse.contains(Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSEVNE
        referanse.contains(
            Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG.name,
        ) -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL

        else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
    }

    // Summerer inntekter som matcher med en liste over referanser og som inkluderer eller ekskluderer en liste over inntektsrapporteringstyper
    // (basert på om inputparameter ekskluderInntekter er satt til true eller false). Hvis den filtrerte inntektslisten er tom, returneres null.
    private fun summerInntekter(
        beregnGrunnlag: BeregnGrunnlag,
        grunnlagsreferanseListe: List<String>,
        inntektsrapporteringListe: List<Inntektsrapportering>,
        ekskluderInntekter: Boolean = false,
        innslagKapitalinntektSjablonverdi: BigDecimal = BigDecimal.ZERO,
    ): BigDecimal? {
        var summertInntekt: BigDecimal? = BigDecimal.ZERO
        beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<InntektsrapporteringPeriode>(grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE)
            .filter { it.referanse in grunnlagsreferanseListe }
            .let { filtrertListe ->
                if (ekskluderInntekter) {
                    filtrertListe.filterNot { it.innhold.inntektsrapportering in inntektsrapporteringListe }
                } else {
                    filtrertListe.filter { it.innhold.inntektsrapportering in inntektsrapporteringListe }
                }
            }
            .let { filtrertListe ->
                if (filtrertListe.isNotEmpty()) {
                    filtrertListe.forEach {
                        summertInntekt = summertInntekt?.plus(
                            if (erKapitalinntekt(it.innhold.inntektsrapportering)) {
                                justerKapitalinntekt(
                                    beløp = it.innhold.beløp,
                                    innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                                )
                            } else {
                                it.innhold.beløp
                            },
                        )
                    }
                } else {
                    summertInntekt = null
                }
            }
        return summertInntekt
    }

    // Legger til referanse for sjablon 0006 (innslag kapitalinntekt) hvis det er kapitalinntekt i grunnlagsreferanseliste
    private fun lagGrunnlagsreferanselisteInntekt(
        grunnlagsreferanseliste: List<String>,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): List<Grunnlagsreferanse> = if (grunnlagsreferanseliste.any { it.contains("kapitalinntekt", ignoreCase = true) }) {
        if (innslagKapitalinntektSjablon != null) {
            grunnlagsreferanseliste + opprettSjablonreferanse(
                navn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!).navn,
                periode = ÅrMånedsperiode(fom = innslagKapitalinntektSjablon.datoFom!!, til = innslagKapitalinntektSjablon.datoTom),
            )
        } else {
            grunnlagsreferanseliste
        }
    } else {
        grunnlagsreferanseliste
    }

    // Mapper ut grunnlag for sjablon 0005 hvis forskuddssats er brukt i beregningen
    fun mapSjablontallForskuddssats(forskuddssatsSjablon: Sjablontall) = GrunnlagDto(
        referanse = opprettSjablonreferanse(
            navn = SjablonTallNavn.fromId(forskuddssatsSjablon.typeSjablon!!).navn,
            periode = ÅrMånedsperiode(fom = forskuddssatsSjablon.datoFom!!, til = forskuddssatsSjablon.datoTom),
        ),
        type = Grunnlagstype.SJABLON,
        innhold = POJONode(
            SjablonSjablontallPeriode(
                periode = ÅrMånedsperiode(forskuddssatsSjablon.datoFom!!, forskuddssatsSjablon.datoTom),
                sjablon = SjablonTallNavn.fromId(forskuddssatsSjablon.typeSjablon!!),
                verdi = forskuddssatsSjablon.verdi!!,
            ),
        ),
    )

    // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
    fun mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon: Sjablontall) = GrunnlagDto(
        referanse = opprettSjablonreferanse(
            navn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!).navn,
            periode = ÅrMånedsperiode(fom = innslagKapitalinntektSjablon.datoFom!!, til = innslagKapitalinntektSjablon.datoTom),
        ),
        type = Grunnlagstype.SJABLON,
        innhold = POJONode(
            SjablonSjablontallPeriode(
                periode = ÅrMånedsperiode(innslagKapitalinntektSjablon.datoFom!!, innslagKapitalinntektSjablon.datoTom),
                sjablon = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!),
                verdi = innslagKapitalinntektSjablon.verdi!!,
            ),
        ),
    )

    // Lager grunnlagsobjekter for sjabloner av type Sjablontall som er av riktig delberegningstype og som er innenfor perioden
    protected fun mapSjablonSjablontallGrunnlag(
        periode: ÅrMånedsperiode,
        sjablonListe: List<Sjablontall>,
        delberegning: (SjablonTallNavn) -> Boolean,
    ): List<GrunnlagDto> {
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            if (delberegning(sjablonTallNavn)) {
                sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
            }
        }

        return sjablonListe
            .filter { sjablontallMap.containsKey(it.typeSjablon) }
            // TODO Sjekk om periode.overlapper er dekkende
            .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
            .map {
                GrunnlagDto(
                    referanse = lagSjablonReferanse("Sjablontall_${sjablontallMap[it.typeSjablon]!!.navn}", it.datoFom!!),
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonSjablontallPeriode(
                            periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                            sjablon = SjablonTallNavn.from(sjablontallMap[it.typeSjablon]!!.navn),
                            verdi = it.verdi!!,
                        ),
                    ),
                )
            }
    }

    // Lager grunnlagsobjekter for sjabloner av type Bidragsevne som er innenfor perioden
    protected fun mapSjablonBidragsevneGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Bidragsevne>): List<GrunnlagDto> = sjablonListe
        // TODO Sjekk om periode.overlapper er dekkende
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map {
            GrunnlagDto(
                referanse = lagSjablonReferanse(SjablonNavn.BIDRAGSEVNE.navn, it.datoFom!!, "_${it.bostatus}"),
                type = Grunnlagstype.SJABLON,
                innhold = POJONode(
                    SjablonBidragsevnePeriode(
                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                        bostatus = it.bostatus!!,
                        boutgiftBeløp = it.belopBoutgift!!,
                        underholdBeløp = it.belopUnderhold!!,
                    ),
                ),
            )
        }

    // Lager grunnlagsobjekter for sjabloner av type TrinnvisSkattesats som er innenfor perioden
    protected fun mapSjablonTrinnvisSkattesatsGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<TrinnvisSkattesats>): List<GrunnlagDto> {
        val periodeMap = mutableMapOf<ÅrMånedsperiode, MutableList<SjablonTrinnvisSkattesats>>()

        sjablonListe
            // TODO Sjekk om periode.overlapper er dekkende
            .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
            .sortedBy { it.datoFom }
            .map {
                val sjablonPeriode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!))
                periodeMap.getOrPut(sjablonPeriode) { mutableListOf() }
                    .add(SjablonTrinnvisSkattesats(inntektsgrense = it.inntektgrense!!.intValueExact(), sats = it.sats!!))
            }

        return periodeMap
            .entries
            .map {
                GrunnlagDto(
                    referanse = lagSjablonReferanse(SjablonNavn.TRINNVIS_SKATTESATS.navn, it.key.fom.atDay(1)),
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonTrinnvisSkattesatsPeriode(
                            periode = it.key,
                            trinnliste = it.value,
                        ),
                    ),
                )
            }
    }

    // Lager grunnlagsobjekter for sjabloner av type Samværsfradrag som er innenfor perioden
    protected fun mapSjablonSamværsfradragGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Samværsfradrag>): List<GrunnlagDto> = sjablonListe
        // TODO Sjekk om periode.overlapper er dekkende
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map {
            GrunnlagDto(
                referanse = lagSjablonReferanse(SjablonNavn.SAMVÆRSFRADRAG.navn, it.datoFom!!, "_${it.samvaersklasse}_${it.alderTom}"),
                type = Grunnlagstype.SJABLON,
                innhold = POJONode(
                    SjablonSamværsfradragPeriode(
                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                        samværsklasse = it.samvaersklasse!!,
                        alderTom = it.alderTom!!,
                        antallDagerTom = it.antDagerTom!!,
                        antallNetterTom = it.antNetterTom!!,
                        beløpFradrag = it.belopFradrag!!,
                    ),
                ),
            )
        }

    private fun justerSjablonTomDato(datoTom: LocalDate): LocalDate? = if (datoTom == LocalDate.parse("9999-12-31")) null else datoTom.plusMonths(1)

    private fun lagSjablonReferanse(sjablonNavn: String, fomDato: LocalDate, postfix: String = ""): String =
        "sjablon_${sjablonNavn}_${fomDato.format(DateTimeFormatter.ofPattern("yyyyMM"))}$postfix"

    // Lager liste over bruddperioder
    fun lagBruddPeriodeListe(periodeListe: Sequence<ÅrMånedsperiode>, beregningsperiode: ÅrMånedsperiode): List<ÅrMånedsperiode> = periodeListe
        .flatMap { sequenceOf(it.fom, it.til) } // Flater ut perioder til en sekvens av ÅrMåned-elementer
        .distinct() // Fjerner evt. duplikater
        .filter { it == null || beregningsperiode.inneholder(it) } // Filtrerer ut datoer utenfor beregningsperiode
        .filter { it != null || (beregningsperiode.til == null) } // Filtrerer ut null hvis beregningsperiode.til ikke er null
        .sortedBy { it } // Sorterer datoer
        .sortedWith(compareBy { it == null }) // Legger evt. null-verdi bakerst
        .zipWithNext() // Lager periodepar
        .map { ÅrMånedsperiode(it.first!!, it.second) } // Mapper til ÅrMånedperiode
        .toList()

    fun tilJson(json: Any): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.writerWithDefaultPrettyPrinter()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper.writeValueAsString(json)
    }
}
