package no.nav.bidrag.inntekt.service

import no.nav.bidrag.commons.service.finnVisningsnavn
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.domene.util.visningsnavnIntern
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.CUT_OFF_DATO
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_12MND
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_3MND
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.PERIODE_AAR
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.PERIODE_MAANED
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnAntallMndOverlapp
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnSisteAarSomSkalRapporteres
import no.nav.bidrag.inntekt.util.beregneBeløpPerMåned
import no.nav.bidrag.inntekt.util.isNumeric
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertMånedsinntekt
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Suppress("NonAsciiCharacters")
class AinntektService {
    // Summerer, grupperer og transformerer ainntekter pr år
    fun beregnAarsinntekt(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertÅrsinntekt> {
        return if (ainntektListeInn.isNotEmpty()) {
            val ainntektMap = summerAarsinntekter(ainntektListeInn, ainntektHentetDato)
            val ainntektListeUt = mutableListOf<SummertÅrsinntekt>()

            ainntektMap.forEach {
                if (it.key.isNumeric() && it.key.toInt() > finnSisteAarSomSkalRapporteres(ainntektHentetDato)) {
                    return@forEach // Går videre til neste forekomst
                }
                ainntektListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering =
                        when (it.key) {
                            KEY_3MND -> Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                            KEY_12MND -> Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                            else -> Inntektsrapportering.AINNTEKT
                        },
                        visningsnavn =
                        when (it.key) {
                            KEY_3MND -> Inntektsrapportering.AINNTEKT_BEREGNET_3MND.visningsnavn.intern
                            KEY_12MND -> Inntektsrapportering.AINNTEKT_BEREGNET_12MND.visningsnavn.intern
                            else -> Inntektsrapportering.AINNTEKT.visningsnavnIntern(it.value.periodeFra.year)
                        },
                        sumInntekt =
                        when (it.key) {
                            KEY_3MND -> it.value.sumInntekt.toInt().times(4).toBigDecimal() // Regner om til årsinntekt
                            else -> it.value.sumInntekt
                        },
                        periode = ÅrMånedsperiode(fom = it.value.periodeFra, til = it.value.periodeTil),
                        inntektPostListe =
                        when (it.key) {
                            KEY_3MND ->
                                grupperOgSummerDetaljposter(
                                    inntektPostListe = it.value.inntektPostListe,
                                    multiplikator = 4,
                                )
                            else ->
                                grupperOgSummerDetaljposter(
                                    inntektPostListe = it.value.inntektPostListe,
                                )
                        },
                    ),
                )
            }
            ainntektListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
        } else {
            emptyList()
        }
    }

    // Summerer, grupperer og transformerer ainntekter pr måned
    fun beregnMaanedsinntekt(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertMånedsinntekt> {
        val ainntektMap = summerMaanedsinntekter(ainntektListeInn, ainntektHentetDato)
        val ainntektListeUt = mutableListOf<SummertMånedsinntekt>()

        ainntektMap.forEach {
            ainntektListeUt.add(
                SummertMånedsinntekt(
                    gjelderÅrMåned = YearMonth.of(it.key.substring(0, 4).toInt(), it.key.substring(4, 6).toInt()),
                    sumInntekt = it.value.sumInntekt,
                    inntektPostListe = grupperOgSummerDetaljposter(inntektPostListe = it.value.inntektPostListe),
                ),
            )
        }

        return ainntektListeUt.sortedWith(compareBy { it.gjelderÅrMåned })
    }

    // Grupperer og summerer poster som har samme kode/beskrivelse
    private fun grupperOgSummerDetaljposter(
        inntektPostListe: List<InntektPost>,
        // Avviker fra default hvis beløp skal regnes om til årsverdi
        multiplikator: Int = 1,
    ): List<InntektPost> {
        return inntektPostListe
            .groupBy(InntektPost::kode)
            .map {
                InntektPost(
                    kode = it.key,
                    visningsnavn = finnVisningsnavn(it.key),
                    beløp = it.value.sumOf(InntektPost::beløp).toInt().times(multiplikator).toBigDecimal(),
                )
            }
    }

    // Summerer og grupperer ainntekter pr år
    private fun summerAarsinntekter(ainntektsposter: List<Ainntektspost>, ainntektHentetDato: LocalDate): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektsposter.forEach { ainntektPost ->
            kalkulerbeløpForPeriode(
                opptjeningsperiodeFra = ainntektPost.opptjeningsperiodeFra,
                opptjeningsperiodeTil = ainntektPost.opptjeningsperiodeTil,
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                beregningsperiode = PERIODE_AAR,
                ainntektHentetDato,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap, periodeMap.key, periodeMap.value, ainntektHentetDato)
            }
        }

        if (!ainntektMap.containsKey(KEY_3MND)) {
            val periode = bestemPeriode(KEY_3MND, ainntektHentetDato)
            ainntektMap[KEY_3MND] =
                InntektSumPost(
                    sumInntekt = BigDecimal.ZERO,
                    periodeFra = periode.periodeFra,
                    periodeTil = periode.periodeTil,
                    inntektPostListe = mutableListOf(),
                )
        }
        if (!ainntektMap.containsKey(KEY_12MND)) {
            val periode = bestemPeriode(KEY_12MND, ainntektHentetDato)
            ainntektMap[KEY_12MND] =
                InntektSumPost(
                    sumInntekt = BigDecimal.ZERO,
                    periodeFra = periode.periodeFra,
                    periodeTil = periode.periodeTil,
                    inntektPostListe = mutableListOf(),
                )
        }

        return ainntektMap.toMap()
    }

    // Summerer og grupperer ainntekter pr måned
    private fun summerMaanedsinntekter(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektListeInn.forEach { ainntektPost ->
            kalkulerbeløpForPeriode(
                opptjeningsperiodeFra = ainntektPost.opptjeningsperiodeFra,
                opptjeningsperiodeTil = ainntektPost.opptjeningsperiodeTil,
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                beregningsperiode = PERIODE_MAANED,
                ainntektHentetDato,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap = ainntektMap, key = periodeMap.key, value = periodeMap.value, ainntektHentetDato)
            }
        }
        return ainntektMap.toMap()
    }

    // Summerer inntekter og legger til detaljposter til map
    private fun akkumulerPost(ainntektMap: MutableMap<String, InntektSumPost>, key: String, value: Detaljpost, ainntektHentetDato: LocalDate) {
        val periode = bestemPeriode(key, ainntektHentetDato)
        val inntektSumPost =
            ainntektMap.getOrDefault(
                key,
                InntektSumPost(
                    sumInntekt = BigDecimal.ZERO,
                    periodeFra = periode.periodeFra,
                    periodeTil = periode.periodeTil,
                    inntektPostListe = mutableListOf(),
                ),
            )
        val sumInntekt = inntektSumPost.sumInntekt
        val inntektPostListe = inntektSumPost.inntektPostListe
        val nyInntektPost = InntektPost(kode = value.kode, visningsnavn = "", beløp = value.beløp)
        inntektPostListe.add(nyInntektPost)
        ainntektMap[key] =
            InntektSumPost(
                sumInntekt = sumInntekt.add(value.beløp),
                periodeFra = periode.periodeFra,
                periodeTil = periode.periodeTil,
                inntektPostListe = inntektPostListe,
            )
    }

    // Kalkulerer beløp for periode (måned, år eller intervall)
    private fun kalkulerbeløpForPeriode(
        opptjeningsperiodeFra: LocalDate?,
        opptjeningsperiodeTil: LocalDate?,
        utbetalingsperiode: String,
        beskrivelse: String,
        beløp: BigDecimal,
        beregningsperiode: String,
        ainntektHentetDato: LocalDate,
    ): Map<String, Detaljpost> {
        val periodeFra =
            if (opptjeningsperiodeFra != null) {
                YearMonth.of(opptjeningsperiodeFra.year, opptjeningsperiodeFra.month)
            } else {
                YearMonth.of(utbetalingsperiode.substring(0, 4).toInt(), utbetalingsperiode.substring(5, 7).toInt())
            }

        val periodeTil =
            if (opptjeningsperiodeTil != null) {
                YearMonth.of(opptjeningsperiodeTil.year, opptjeningsperiodeTil.month)
            } else {
                if (opptjeningsperiodeFra != null) {
                    YearMonth.of(opptjeningsperiodeFra.year, opptjeningsperiodeFra.month).plusMonths(1)
                } else {
                    YearMonth.of(utbetalingsperiode.substring(0, 4).toInt(), utbetalingsperiode.substring(5, 7).toInt())
                        .plusMonths(1)
                }
            }

        // Hvis periode er måned, returner map med en forekomst for hver måned beløpet dekker
        // Hvis periode er år, returner map med en forekomst for hvert år beløpet dekker + forekomst for siste 3 mnd + forekomst for siste 12 mnd
        return when (beregningsperiode) {
            PERIODE_MAANED ->
                kalkulerBeløpForMnd(
                    periodeFra = periodeFra,
                    periodeTil = periodeTil,
                    beskrivelse = beskrivelse,
                    beløp = beløp,
                )
            else ->
                kalkulerBeløpForAar(periodeFra = periodeFra, periodeTil = periodeTil, beskrivelse = beskrivelse, beløp = beløp) +
                    kalkulerBeløpForIntervall(
                        periodeFra = periodeFra,
                        periodeTil = periodeTil,
                        beskrivelse = beskrivelse,
                        beløp = beløp,
                        beregningsperiode = KEY_3MND,
                        ainntektHentetDato,
                    ) +
                    kalkulerBeløpForIntervall(
                        periodeFra = periodeFra,
                        periodeTil = periodeTil,
                        beskrivelse = beskrivelse,
                        beløp = beløp,
                        beregningsperiode = KEY_12MND,
                        ainntektHentetDato,
                    )
        }
    }

    private fun kalkulerBeløpForMnd(periodeFra: YearMonth, periodeTil: YearMonth, beskrivelse: String, beløp: BigDecimal): Map<String, Detaljpost> {
        val periodeMap = mutableMapOf<String, Detaljpost>()
        val antallMnd = ChronoUnit.MONTHS.between(periodeFra, periodeTil).toInt()
        val månedsbeløp = beregneBeløpPerMåned(beløp = beløp, antallMnd = antallMnd)
        var periode = periodeFra

        while (periode.isBefore(periodeTil)) {
            periodeMap[periode.year.toString() + periode.toString().substring(5, 7)] =
                Detaljpost(beløp = månedsbeløp, kode = beskrivelse)
            periode = periode.plusMonths(1)
        }

        return periodeMap
    }

    // Kalkulerer totalt beløp for hvert år forekomsten dekker
    private fun kalkulerBeløpForAar(periodeFra: YearMonth, periodeTil: YearMonth, beskrivelse: String, beløp: BigDecimal): Map<String, Detaljpost> {
        val periodeMap = mutableMapOf<String, Detaljpost>()
        val antallMndTotalt = ChronoUnit.MONTHS.between(periodeFra, periodeTil).toInt()
        val månedsbeløp = beregneBeløpPerMåned(beløp = beløp, antallMnd = antallMndTotalt)
        val førsteÅr = periodeFra.year
        val sisteÅr = periodeTil.minusMonths(1).year

        for (år in førsteÅr..sisteÅr) {
            val antallMndIÅr =
                when {
                    periodeFra.year == år && periodeTil.year == år -> periodeTil.monthValue.minus(periodeFra.monthValue)
                    periodeFra.year == år -> 13.minus(periodeFra.monthValue)
                    periodeTil.year == år -> periodeTil.monthValue.minus(1)
                    else -> 12
                }
            if (antallMndIÅr > 0) {
                periodeMap[år.toString()] =
                    Detaljpost(beløp = antallMndIÅr.toBigDecimal().times(månedsbeløp), kode = beskrivelse)
            }
        }

        return periodeMap
    }

    // Kalkulerer totalt beløp for intervall (3 mnd eller 12 mnd) som forekomsten evt dekker
    private fun kalkulerBeløpForIntervall(
        periodeFra: YearMonth,
        periodeTil: YearMonth,
        beskrivelse: String,
        beløp: BigDecimal,
        beregningsperiode: String,
        ainntektHentetDato: LocalDate,
    ): Map<String, Detaljpost> {
        val periodeMap = mutableMapOf<String, Detaljpost>()
        val antallMndTotalt = ChronoUnit.MONTHS.between(periodeFra, periodeTil).toInt()
        val maanedsbeløp = beregneBeløpPerMåned(beløp = beløp, antallMnd = antallMndTotalt)

        // TODO Bør CUT_OFF_DATO være dynamisk? (se https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/frister-og-betaling-i-a-meldingen/)
        val sistePeriodeIIntervall =
            if (ainntektHentetDato.dayOfMonth > CUT_OFF_DATO) {
                YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month)
            } else {
                YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(1)
            }
        val forstePeriodeIIntervall =
            if (beregningsperiode == KEY_3MND) {
                sistePeriodeIIntervall.minusMonths(3)
            } else {
                sistePeriodeIIntervall.minusMonths(
                    12,
                )
            }

        val antallMndOverlapp =
            finnAntallMndOverlapp(
                periodeFra = periodeFra,
                periodeTil = periodeTil,
                forstePeriodeIIntervall = forstePeriodeIIntervall,
                sistePeriodeIIntervall = sistePeriodeIIntervall,
            )

        if (antallMndOverlapp > 0) {
            periodeMap[beregningsperiode] =
                Detaljpost(beløp = antallMndOverlapp.toBigDecimal().times(maanedsbeløp), kode = beskrivelse)
        }

        return periodeMap
    }

    // Finner riktig periode basert på nøkkelverdi i map og om det er type år, måned eller intervall
    private fun bestemPeriode(periodeVerdi: String, ainntektHentetDato: LocalDate): Periode {
        val periodeFra: YearMonth
        val periodeTil: YearMonth

        // År
        if (periodeVerdi.isNumeric() && periodeVerdi.length == 4) {
            periodeFra = YearMonth.of(periodeVerdi.toInt(), 1)
            periodeTil = YearMonth.of(periodeVerdi.toInt(), 12)
            // Måned
        } else if (periodeVerdi.isNumeric() && periodeVerdi.length == 6) {
            periodeFra = YearMonth.of(periodeVerdi.substring(0, 4).toInt(), periodeVerdi.substring(4, 6).toInt())
            periodeTil = periodeFra
            // Intervall
        } else {
//            val dagensDato = LocalDate.now()
            // TODO Bør CUT_OFF_DATO være dynamisk? (se https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/frister-og-betaling-i-a-meldingen/)
            periodeTil =
                if (ainntektHentetDato.dayOfMonth > CUT_OFF_DATO) {
                    YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(1)
                } else {
                    YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(2)
                }
            periodeFra = if (periodeVerdi == KEY_3MND) periodeTil.minusMonths(2) else periodeTil.minusMonths(11)
        }

        return Periode(periodeFra = periodeFra, periodeTil = periodeTil)
    }
}

data class Detaljpost(
    val beløp: BigDecimal,
    val kode: String,
)
