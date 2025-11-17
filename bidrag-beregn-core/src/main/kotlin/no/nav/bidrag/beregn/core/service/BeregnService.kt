package no.nav.bidrag.beregn.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.bo.SjablonForbruksutgifterPeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSamværsfradragPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.beregn.core.mapping.sjablontallTilGrunnlagsobjekt
import no.nav.bidrag.beregn.core.mapping.tilGrunnlagsobjekt
import no.nav.bidrag.beregn.core.mapping.trinnvisSkattesatsTilGrunnlagsobjekt
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.inneholderBarnetilleggTiltakspenger
import no.nav.bidrag.beregn.core.util.InntektUtil.justerForBarnetilleggTiltakspenger
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.core.util.SjablonUtil.justerSjablonTomDato
import no.nav.bidrag.commons.service.sjablon.Barnetilsyn
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Forbruksutgifter
import no.nav.bidrag.commons.service.sjablon.MaksFradrag
import no.nav.bidrag.commons.service.sjablon.MaksTilsyn
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype.Companion.inngårIInntektRapporteringer
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

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
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
                innhold = POJONode(
                    DelberegningSumInntekt(
                        periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                        totalinntekt = it.beløp.setScale(2),
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
                                ).setScale(2)
                            } else if (inneholderBarnetilleggTiltakspenger(it.innhold)) {
                                justerForBarnetilleggTiltakspenger(it.innhold).setScale(2)
                            } else {
                                it.innhold.beløp.setScale(2)
                            },
                        )
                    }
                } else {
                    summertInntekt = BigDecimal.ZERO.setScale(2)
                }
            }
        return if (summertInntekt == BigDecimal.ZERO.setScale(2)) null else summertInntekt
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
    fun mapSjablontallForskuddssats(forskuddssatsSjablon: Sjablontall): GrunnlagDto = sjablontallTilGrunnlagsobjekt(
        periode = ÅrMånedsperiode(forskuddssatsSjablon.datoFom!!, forskuddssatsSjablon.datoTom),
        sjablontallNavn = SjablonTallNavn.fromId(forskuddssatsSjablon.typeSjablon!!),
        verdi = forskuddssatsSjablon.verdi!!,
    )

    // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
    fun mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon: Sjablontall): GrunnlagDto = sjablontallTilGrunnlagsobjekt(
        periode = ÅrMånedsperiode(innslagKapitalinntektSjablon.datoFom!!, innslagKapitalinntektSjablon.datoTom),
        sjablontallNavn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!),
        verdi = innslagKapitalinntektSjablon.verdi!!,
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
            .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
            .map {
                sjablontallTilGrunnlagsobjekt(
                    periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                    sjablontallNavn = sjablontallMap[it.typeSjablon]!!,
                    verdi = it.verdi!!,
                )
            }
    }

    // Lager grunnlagsobjekter for sjabloner av type Bidragsevne som er innenfor perioden
    protected fun mapSjablonBidragsevneGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Bidragsevne>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!))) }

    // Lager grunnlagsobjekter for sjabloner av type TrinnvisSkattesats som er innenfor perioden
    protected fun mapSjablonTrinnvisSkattesatsGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<TrinnvisSkattesats>): List<GrunnlagDto> {
        val periodeMap = mutableMapOf<ÅrMånedsperiode, MutableList<SjablonTrinnvisSkattesats>>()

        sjablonListe
            .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
            .sortedBy { it.datoFom }
            .map {
                val sjablonPeriode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!))
                periodeMap.getOrPut(sjablonPeriode) { mutableListOf() }
                    .add(SjablonTrinnvisSkattesats(inntektsgrense = it.inntektgrense!!.intValueExact(), sats = it.sats!!))
            }

        return periodeMap
            .entries
            .map { trinnvisSkattesatsTilGrunnlagsobjekt(periode = it.key, trinnliste = it.value) }
    }

    // Lager grunnlagsobjekter for sjabloner av type Samværsfradrag som er innenfor perioden
    protected fun mapSjablonSamværsfradragGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Samværsfradrag>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom))) }

    protected fun mapSjablonMaksTilsynsbeløpGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksTilsyn>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom))) }

    protected fun mapSjablonMaksFradragGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksFradrag>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom))) }

    protected fun mapSjablonBarnetilsynGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Barnetilsyn>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom))) }

    protected fun mapSjablonForbruksutgifterGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Forbruksutgifter>): List<GrunnlagDto> = sjablonListe
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map { it.tilGrunnlagsobjekt(ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom))) }

    // Lager liste over bruddperioder
    fun lagBruddPeriodeListe(periodeListe: Sequence<ÅrMånedsperiode>, beregningsperiode: ÅrMånedsperiode): List<ÅrMånedsperiode> {
        val bruddPeriodeListe = periodeListe
            .flatMap { sequenceOf(it.fom, it.til) } // Flater ut perioder til en sekvens av ÅrMåned-elementer
            .distinct() // Fjerner evt. duplikater
            .filter { it == null || beregningsperiode.inneholder(it) } // Filtrerer ut datoer utenfor beregningsperiode
            .filter { it != null || (beregningsperiode.til == null) } // Filtrerer ut null hvis beregningsperiode.til ikke er null
            .sortedBy { it } // Sorterer datoer
            .sortedWith(compareBy { it == null }) // Legger evt. null-verdi bakerst
            .zipWithNext() // Lager periodepar
            .map { ÅrMånedsperiode(it.first!!, it.second) } // Mapper til ÅrMånedperiode
            .toMutableList()

        return bruddPeriodeListe
    }

    fun mapDelberegningResultatGrunnlag(
        grunnlagReferanseListe: List<String>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Matcher sjablongrunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = sjablonGrunnlag,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        return resultatGrunnlagListe
    }

    fun mapDelberegningResultatGrunnlag(
        grunnlagReferanseListe: List<String>,
        mottattGrunnlag: List<GrunnlagDto>,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Matcher sjablongrunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = sjablonGrunnlag,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        return resultatGrunnlagListe
    }

    // Mapper ut grunnlag for Person-objekter
    fun mapPersonobjektGrunnlag(resultatGrunnlagListe: List<GrunnlagDto>, personobjektGrunnlagListe: List<GrunnlagDto>): List<GrunnlagDto> {
        val gjelderReferanseListe = resultatGrunnlagListe
            .flatMap { listOfNotNull(it.gjelderReferanse, it.gjelderBarnReferanse) }
            .distinct()

        return mapGrunnlag(
            grunnlagListe = personobjektGrunnlagListe,
            grunnlagReferanseListe = gjelderReferanseListe,
        )
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) = grunnlagListe
        .filter { grunnlagReferanseListe.contains(it.referanse) }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = it.type,
                innhold = it.innhold,
                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                gjelderReferanse = it.gjelderReferanse,
                gjelderBarnReferanse = it.gjelderBarnReferanse,
            )
        }

    // Bestemmer tilsynskode for oppslag i sjablon barnetilsyn
    protected fun bestemTilsynskode(tilsynstype: Tilsynstype?, skolealder: Skolealder?): String? = if (tilsynstype == null || skolealder == null) {
        null
    } else {
        when (tilsynstype to skolealder) {
            Tilsynstype.HELTID to Skolealder.UNDER -> "HU"
            Tilsynstype.HELTID to Skolealder.OVER -> "HO"
            Tilsynstype.DELTID to Skolealder.UNDER -> "DU"
            Tilsynstype.DELTID to Skolealder.OVER -> "DO"
            else -> null
        }
    }

    // Finner barnets beregnede alder. Alder regnes som om barnet er født 1. juli i fødselsåret.
    protected fun finnBarnetsAlder(fødselsdato: LocalDate, årMåned: YearMonth): Int = Period.between(
        fødselsdato.withMonth(7).withDayOfMonth(1),
        årMåned.atDay(1),
    ).years

    // Lager liste over gyldige alderTom-verdier for sjablon forbruksutgifter
    protected fun hentAlderTomListeForbruksutgifter(sjablonForbruksutgifterPerioder: List<SjablonForbruksutgifterPeriodeGrunnlag>): List<Int> =
        sjablonForbruksutgifterPerioder
            .map { it.sjablonForbruksutgifterPeriode.alderTom }
            .distinct()
            .sorted()

    // Lager liste over gyldige alderTom-verdier for sjablon samværsfradrag
    protected fun hentAlderTomListeSamværsfradrag(sjablonSamværsfradragPerioder: List<SjablonSamværsfradragPeriodeGrunnlag>): List<Int> =
        sjablonSamværsfradragPerioder
            .map { it.sjablonSamværsfradragPeriode.alderTom }
            .distinct()
            .sorted()

    // Henter ut verdi fra delberegning for endring sjekk av grense (her skal det være kun en forekomst)
    fun erOverMinimumsgrenseForEndring(endringSjekkGrenseGrunnlagliste: List<GrunnlagDto>): Boolean = endringSjekkGrenseGrunnlagliste
        .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(grunnlagType = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
        .map { it.innhold.endringErOverGrense }
        .firstOrNull() ?: true

    fun tilJson(json: Any): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.writerWithDefaultPrettyPrinter()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper.writeValueAsString(json)
    }

    // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden. Justerer i så fall til-periode. Hvis stønadstype er BIDRAG18AAR skal det ikke
    // sjekkes om barnet blir 18 år i perioden,
    protected fun justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag: BeregnGrunnlag): BeregnGrunnlagJustert {
        if (mottattGrunnlag.stønadstype == Stønadstype.BIDRAG18AAR) {
            return BeregnGrunnlagJustert(
                beregnGrunnlag = mottattGrunnlag,
                åpenSluttperiode =
                mottattGrunnlag.opphørsdato == null || mottattGrunnlag.opphørsdato!!.isAfter(YearMonth.now()),
            )
        }

        val periodeSøknadsbarnetFyller18År = mottattGrunnlag.grunnlagListe
            .hentPersonMedReferanse(mottattGrunnlag.søknadsbarnReferanse)
            .let { YearMonth.from(it!!.personObjekt.fødselsdato.plusYears(18)) }

        return if (periodeSøknadsbarnetFyller18År.isBefore(mottattGrunnlag.periode.til)) {
            BeregnGrunnlagJustert(
                beregnGrunnlag = mottattGrunnlag.copy(
                    periode = mottattGrunnlag.periode.copy(til = periodeSøknadsbarnetFyller18År.plusMonths(1)),
                ),
                åpenSluttperiode = false,
            )
        } else {
            BeregnGrunnlagJustert(
                beregnGrunnlag = mottattGrunnlag,
                åpenSluttperiode =
                mottattGrunnlag.opphørsdato == null || mottattGrunnlag.opphørsdato!!.isAfter(YearMonth.now()),
            )
        }
    }

    protected fun BeregnGrunnlagJustert.utvidMedNyeGrunnlag(nyeGrunnlag: List<GrunnlagDto>) =
        copy(beregnGrunnlag = beregnGrunnlag.copy(grunnlagListe = (beregnGrunnlag.grunnlagListe + nyeGrunnlag).distinctBy { it.referanse }))

    data class BeregnGrunnlagJustert(val beregnGrunnlag: BeregnGrunnlag, val åpenSluttperiode: Boolean)
}
