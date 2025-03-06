package no.nav.bidrag.beregn.core.inntekt.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.core.util.justerPeriodeTilOpphørsdato
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype.Companion.inngårIInntektRapporteringer
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnValgteInntekterGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnValgteInntekterResultat
import no.nav.bidrag.transport.behandling.beregning.felles.InntektPerBarn
import no.nav.bidrag.transport.behandling.beregning.felles.InntektsgrunnlagPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.YearMonth

internal class BeregnInntektService {
    fun beregn(grunnlag: BeregnValgteInntekterGrunnlag): BeregnValgteInntekterResultat {
        secureLogger.debug { "Beregning av inntekt - følgende request mottatt: ${tilJson(grunnlag)}" }

        // Henter sjablonverdi for kapitalinntekt
        // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
        val innslagKapitalinntektSjablonverdi =
            SjablonProvider.hentSjablontall().firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi
                ?: BigDecimal.ZERO

        val beregnetInntektListe = mutableListOf<InntektPerBarn>()
        beregnetInntektListe.addAll(summerOgPeriodiserFellesInntekter(grunnlag, innslagKapitalinntektSjablonverdi))
        beregnetInntektListe.addAll(summerOgPeriodiserInntekterPerBarn(grunnlag, innslagKapitalinntektSjablonverdi))

        val respons = BeregnValgteInntekterResultat(beregnetInntektListe)

        secureLogger.debug { "Beregning av inntekt - returnerer følgende respons: ${tilJson(respons)}" }

        return respons
    }

    // Kategoriserer, periodiserer og summerer felles inntekter (dvs. inntekter som ikke gjelder et spesifikt barn)
    private fun summerOgPeriodiserFellesInntekter(
        grunnlag: BeregnValgteInntekterGrunnlag,
        innslagKapitalinntektSjablonverdi: BigDecimal,
    ): List<InntektPerBarn> {
        val gjelderIdent = grunnlag.gjelderIdent

        val generelleInntekterListe = grunnlag.grunnlagListe
            .filter { it.inntektEiesAvIdent == gjelderIdent }
            .filter { it.inntektGjelderBarnIdent == null }
            .map {
                InntektsgrunnlagPeriode(
                    periode = it.periode,
                    inntektsrapportering = it.inntektsrapportering,
                    beløp = if (erKapitalinntekt(it.inntektsrapportering)) {
                        justerKapitalinntekt(
                            beløp = it.beløp,
                            innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                        )
                    } else {
                        it.beløp
                    },
                    inntektEiesAvIdent = it.inntektEiesAvIdent,
                )
            }

        val generelleInntekterAkkumulertOgPeriodisertListe =
            akkumulerOgPeriodiser(generelleInntekterListe, grunnlag.opphørsdato)

        return listOf(
            InntektPerBarn(
                inntektGjelderBarnIdent = null,
                summertInntektListe = generelleInntekterAkkumulertOgPeriodisertListe,
            ),
        )
    }

    // Kategoriserer, periodiserer og summerer inntekter per barn
    private fun summerOgPeriodiserInntekterPerBarn(
        grunnlag: BeregnValgteInntekterGrunnlag,
        innslagKapitalinntektSjablonverdi: BigDecimal,
    ): List<InntektPerBarn> {
        val bidragsmottakerIdent = grunnlag.gjelderIdent
        val inntektPerBarnListe = mutableListOf<InntektPerBarn>()

        grunnlag.barnIdentListe.forEach { barnIdent ->

            // Skal ikke summere inntekter for barnet hvis det ikke er noen inntekter som gjelder spesifikt for barnet
            // Kommenterer ut denne sjekken da det er forventet at det beregnes inntekter for hvert barn selv om de ikke har noe spesifikke inntekter
//            if (grunnlag.grunnlagListe.none { it.inntektGjelderBarnIdent == barnIdent }) {
//                return@forEach
//            }

            val barnInntekterListe = grunnlag.grunnlagListe
                .filter { it.inntektEiesAvIdent == bidragsmottakerIdent }
                .filter { it.inntektGjelderBarnIdent.let { barn -> barn == barnIdent || barn == null } }
                .map {
                    InntektsgrunnlagPeriode(
                        periode = it.periode,
                        inntektsrapportering = it.inntektsrapportering,
                        beløp = if (erKapitalinntekt(it.inntektsrapportering)) {
                            justerKapitalinntekt(
                                beløp = it.beløp,
                                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                            )
                        } else {
                            it.beløp
                        },
                        inntektEiesAvIdent = it.inntektEiesAvIdent,
                    )
                }

            val barnInntekterAkkumulertOgPeriodisertListe = akkumulerOgPeriodiser(barnInntekterListe, grunnlag.opphørsdato)

            inntektPerBarnListe.add(
                InntektPerBarn(
                    inntektGjelderBarnIdent = barnIdent,
                    summertInntektListe = barnInntekterAkkumulertOgPeriodisertListe,
                ),
            )
        }

        return inntektPerBarnListe
    }

    // Lager en gruppert liste over inntekter summert pr bruddperiode
    private fun akkumulerOgPeriodiser(grunnlagListe: List<InntektsgrunnlagPeriode>, opphørsdato: YearMonth? = null): List<DelberegningSumInntekt> {
        val filteredGrunnlagListe = if (opphørsdato != null) justerPerioderForOpphørsdato(grunnlagListe, opphørsdato) else grunnlagListe
        // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
        val bruddatoListe = filteredGrunnlagListe
            .flatMap { listOf(it.periode.fom, it.periode.til) }
            .distinct()
            .sortedBy { it }
            .sortedWith(compareBy { it == null })

        // Slår sammen brudddatoer til en liste med perioder (fom-/til-dato)
        val periodeListe = bruddatoListe
            .zipWithNext()
            .map { Periode(it.first!!.atDay(1), it.second?.atDay(1)) }.toMutableList()

        if (opphørsdato != null && periodeListe.isNotEmpty()) {
            periodeListe[periodeListe.lastIndex] = Periode(periodeListe.last().datoFom, justerPeriodeTilOpphørsdato(opphørsdato)?.atDay(1))
        }

        // Returnerer en gruppert og summert liste over inntekter pr bruddperiode
        return akkumulerOgPeriodiserInntekter(filteredGrunnlagListe, periodeListe)
    }
    fun justerPerioderForOpphørsdato(grunnlagsliste: List<InntektsgrunnlagPeriode>, opphørsdato: YearMonth?): List<InntektsgrunnlagPeriode> =
        grunnlagsliste.filter {
            it.periode.fom.isBefore(opphørsdato)
        }
            .map { grunnlag ->
                if (grunnlag.periode.til == null || grunnlag.periode.til!! >= opphørsdato) {
                    grunnlag.copy(periode = grunnlag.periode.copy(til = justerPeriodeTilOpphørsdato(opphørsdato)))
                } else {
                    grunnlag
                }
            }

    // Grupperer og summerer inntekter pr bruddperiode
    private fun akkumulerOgPeriodiserInntekter(
        inntektGrunnlagListe: List<InntektsgrunnlagPeriode>,
        periodeListe: List<Periode>,
    ): List<DelberegningSumInntekt> = periodeListe
        .map { periode ->
            val filtrertGrunnlagsliste = filtrerGrunnlagsliste(grunnlagsliste = inntektGrunnlagListe, periode = periode)

            DelberegningSumInntekt(
                periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                totalinntekt = filtrertGrunnlagsliste
                    .sumOf { it.beløp },
                kontantstøtte = filtrertGrunnlagsliste
                    .filter { it.inntektsrapportering in Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer() }
                    .sumOf { it.beløp }
                    .takeIf { it != BigDecimal.ZERO },
                skattepliktigInntekt = filtrertGrunnlagsliste
                    .filterNot {
                        it.inntektsrapportering in Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer() +
                            Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer() +
                            Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer() +
                            Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer()
                    }
                    .sumOf { it.beløp }
                    .takeIf { it != BigDecimal.ZERO },
                barnetillegg = filtrertGrunnlagsliste
                    .filter { it.inntektsrapportering in Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer() }
                    .sumOf { it.beløp }
                    .takeIf { it != BigDecimal.ZERO },
                utvidetBarnetrygd = filtrertGrunnlagsliste
                    .filter { it.inntektsrapportering in Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer() }
                    .sumOf { it.beløp }
                    .takeIf { it != BigDecimal.ZERO },
                småbarnstillegg = filtrertGrunnlagsliste
                    .filter { it.inntektsrapportering in Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer() }
                    .sumOf { it.beløp }
                    .takeIf { it != BigDecimal.ZERO },
            )
        }

    // Filtrerer ut grunnlag som tilhører en gitt periode
    private fun filtrerGrunnlagsliste(grunnlagsliste: List<InntektsgrunnlagPeriode>, periode: Periode): List<InntektsgrunnlagPeriode> =
        grunnlagsliste.filter { grunnlag ->
            (grunnlag.periode.til == null || periode.datoFom.isBefore(grunnlag.periode.til!!.atDay(1))) &&
                (periode.datoTil == null || periode.datoTil.isAfter(grunnlag.periode.fom.atDay(1)))
        }

    private fun tilJson(json: Any): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.writerWithDefaultPrettyPrinter()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper.writeValueAsString(json)
    }
}
