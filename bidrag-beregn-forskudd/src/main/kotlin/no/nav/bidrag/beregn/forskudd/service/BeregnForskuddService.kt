package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype.Companion.inngårIInntektRapporteringer
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningForskudd
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

internal class BeregnForskuddService(private val forskuddCore: ForskuddCore = ForskuddCore()) {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetForskuddResultat {
        secureLogger.info { "Forskuddsberegning - følgende request mottatt: $grunnlag" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
        }

        // Henter sjabloner
        val sjablontallListe: List<Sjablontall> = SjablonProvider.hentSjablontall()

        if (sjablontallListe.isEmpty()) {
            logger.error { "Klarte ikke å hente sjabloner" }
            return BeregnetForskuddResultat()
        }

        // Lager input-grunnlag til core-modulen
        val grunnlagTilCore =
            CoreMapper.mapGrunnlagTilCore(
                beregnForskuddGrunnlag = grunnlag,
                sjablontallListe = sjablontallListe,
            )

        secureLogger.debug { "Forskuddsberegning - grunnlag for beregning: $grunnlagTilCore" }

        // Kaller core-modulen for beregning av forskudd
        val resultatFraCore =
            try {
                forskuddCore.beregnForskudd(grunnlagTilCore)
            } catch (e: Exception) {
                throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
            }

        if (resultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = resultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            logger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst" }
            secureLogger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst" }
            secureLogger.info {
                "Forskuddsberegning - grunnlag for beregning: " + System.lineSeparator() +
                    "beregnDatoFra= " + grunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + grunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "soknadBarn= " + grunnlagTilCore.søknadsbarn + System.lineSeparator() +
                    "barnIHusstandenPeriodeListe= " + grunnlagTilCore.barnIHusstandenPeriodeListe + System.lineSeparator() +
                    "inntektPeriodeListe= " + grunnlagTilCore.inntektPeriodeListe + System.lineSeparator() +
                    "sivilstandPeriodeListe= " + grunnlagTilCore.sivilstandPeriodeListe + System.lineSeparator() +
                    "bostatusPeriodeListe= " + grunnlagTilCore.bostatusPeriodeListe + System.lineSeparator()
            }
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.info { "Forskuddberegning - resultat av beregning: ${resultatFraCore.beregnetForskuddPeriodeListe}" }

        // Henter sjablonverdi for kapitalinntekt
        // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
        val innslagKapitalinntektSjablon =
            sjablontallListe.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }

        val grunnlagsliste =
            lagGrunnlagsliste(
                forskuddGrunnlag = grunnlag,
                resultatFraCore = resultatFraCore,
                grunnlagTilCore = grunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
            )

        val resultatPeriodeListe = lagSluttperiodeOgResultatperioder(resultatFraCore.beregnetForskuddPeriodeListe, grunnlagsliste)

        val respons =
            BeregnetForskuddResultat(
                beregnetForskuddPeriodeListe = resultatPeriodeListe,
                grunnlagListe = grunnlagsliste.distinctBy { it.referanse },
            )

        secureLogger.info { "Forskuddsberegning - returnerer følgende respons: $respons" }

        return respons
    }

    // Oppretter resultatperioder som refererer til sluttberegning, som igjen refererer til delberegninger og grunnlag
    private fun lagSluttperiodeOgResultatperioder(
        resultatPeriodeCoreListe: List<ResultatPeriodeCore>,
        grunnlagReferanseListe: List<GrunnlagDto>,
    ): List<ResultatPeriode> {
        return resultatPeriodeCoreListe.map { resultatPeriode ->

            val søknadsbarnReferanse = grunnlagReferanseListe.filter { it.type == Grunnlagstype.PERSON_SØKNADSBARN }.map { it.referanse }.first()
            val sluttberegningReferanse = opprettSluttberegningreferanse(
                barnreferanse = søknadsbarnReferanse,
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
            )

            // Oppretter sluttberegning, som legges til i grunnlagslista
            grunnlagReferanseListe.addFirst(
                GrunnlagDto(
                    referanse = sluttberegningReferanse,
                    type = Grunnlagstype.SLUTTBEREGNING_FORSKUDD,
                    innhold = POJONode(
                        SluttberegningForskudd(
                            periode = ÅrMånedsperiode(resultatPeriode.periode.datoFom, resultatPeriode.periode.datoTil),
                            beløp = resultatPeriode.resultat.beløp,
                            resultatKode = resultatPeriode.resultat.kode,
                            aldersgruppe = resultatPeriode.resultat.aldersgruppe,
                        ),
                    ),
                    grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe,
                    gjelderReferanse = søknadsbarnReferanse,
                ),
            )

            // Oppretter resultatperioder, som refererer til sluttberegning
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
                resultat =
                ResultatBeregning(
                    belop = resultatPeriode.resultat.beløp,
                    kode = resultatPeriode.resultat.kode,
                    regel = resultatPeriode.resultat.regel,
                ),
                grunnlagsreferanseListe = listOf(sluttberegningReferanse),
            )
        }
    }

    // Lager en liste over resultatgrunnlag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagsliste(
        forskuddGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnetForskuddResultatCore,
        grunnlagTilCore: BeregnForskuddGrunnlagCore,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.beregnetForskuddPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            forskuddGrunnlag.grunnlagListe
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektListe = grunnlagTilCore.inntektPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumAntallBarnListe = grunnlagTilCore.barnIHusstandenPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        resultatGrunnlagListe.addAll(
            sumInntektListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = bestemGrunnlagstype(it.referanse),
                        innhold = POJONode(
                            DelberegningSumInntekt(
                                periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                                totalinntekt = it.beløp,
                                kontantstøtte = summerInntekter(
                                    forskuddGrunnlag = forskuddGrunnlag,
                                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                    inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer(),
                                ),
                                skattepliktigInntekt = summerInntekter(
                                    forskuddGrunnlag = forskuddGrunnlag,
                                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                    inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer() +
                                        Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer() +
                                        Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer() +
                                        Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                                    ekskluderInntekter = true,
                                    innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablon?.verdi ?: BigDecimal.ZERO,
                                ),
                                barnetillegg = summerInntekter(
                                    forskuddGrunnlag = forskuddGrunnlag,
                                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                    inntektsrapporteringListe = Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer(),
                                ),
                                utvidetBarnetrygd = summerInntekter(
                                    forskuddGrunnlag = forskuddGrunnlag,
                                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                    inntektsrapporteringListe = Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer(),
                                ),
                                småbarnstillegg = summerInntekter(
                                    forskuddGrunnlag = forskuddGrunnlag,
                                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                    inntektsrapporteringListe = Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                                ),
                            ),
                        ),
                        grunnlagsreferanseListe = lagGrunnlagsreferanselisteInntekt(it.grunnlagsreferanseListe, innslagKapitalinntektSjablon),
                    )
                },
        )

        // Mapper ut DelberegningBarnIHusstand.
        resultatGrunnlagListe.addAll(
            sumAntallBarnListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = bestemGrunnlagstype(it.referanse),
                        innhold = POJONode(
                            DelberegningBarnIHusstand(
                                periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                                antallBarn = it.antall,
                            ),
                        ),
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                    )
                },
        )

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe = sumInntektListe
            .flatMap { it.grunnlagsreferanseListe }
            .union(
                sumAntallBarnListe
                    .flatMap { it.grunnlagsreferanseListe },
            )
            .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            forskuddGrunnlag.grunnlagListe
                .filter { it.referanse in delberegningReferanseListe }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            resultatFraCore.sjablonListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = Grunnlagstype.SJABLON,
                        innhold = POJONode(
                            SjablonGrunnlag(
                                periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                                sjablon = SjablonTallNavn.from(it.navn),
                                verdi = it.verdi,
                            ),
                        ),
                    )
                },
        )

        // Lager grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
        if (delberegningReferanseListe.any { it.contains("Kapitalinntekt") } && innslagKapitalinntektSjablon != null) {
            resultatGrunnlagListe.add(
                GrunnlagDto(
                    referanse = opprettSjablonreferanse(
                        navn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!).navn,
                        periode = ÅrMånedsperiode(fom = innslagKapitalinntektSjablon.datoFom!!, til = innslagKapitalinntektSjablon.datoTom),
                    ),
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonGrunnlag(
                            periode = ÅrMånedsperiode(innslagKapitalinntektSjablon.datoFom!!, innslagKapitalinntektSjablon.datoTom),
                            sjablon = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!),
                            verdi = innslagKapitalinntektSjablon.verdi!!,
                        ),
                    ),
                ),
            )
        }

        return resultatGrunnlagListe
    }

    // Legger til referanse for sjablon 0006 (innslag kapitalinntekt) hvis det er kapitalinntekt i grunnlagsreferanseliste
    private fun lagGrunnlagsreferanselisteInntekt(
        grunnlagsreferanseliste: List<String>,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): List<Grunnlagsreferanse> {
        return if (grunnlagsreferanseliste.any { it.contains("Kapitalinntekt") }) {
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
    }

    // Summerer inntekter som matcher med en liste over referanser og som inkluderer eller ekskluderer en liste over inntektsrapporteringstyper
    // (basert på om inputparameter ekskluderInntekter er satt til true eller false). Hvis den filtrerte inntektslisten er tom, returneres null.
    private fun summerInntekter(
        forskuddGrunnlag: BeregnGrunnlag,
        grunnlagsreferanseListe: List<String>,
        inntektsrapporteringListe: List<Inntektsrapportering>,
        ekskluderInntekter: Boolean = false,
        innslagKapitalinntektSjablonverdi: BigDecimal = BigDecimal.ZERO,
    ): BigDecimal? {
        var summertInntekt: BigDecimal? = BigDecimal.ZERO
        forskuddGrunnlag.grunnlagListe
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

    private fun bestemGrunnlagstype(referanse: String) = when {
        referanse.contains(Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_SUM_INNTEKT
        referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
        else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
    }
}
