package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_12MND
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_12MND_OV
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_3MND
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.KEY_3MND_OV
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.PERIODE_MÅNED
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.PERIODE_ÅR
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnCutOffDag
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnSisteAarSomSkalRapporteres
import no.nav.bidrag.inntekt.util.isNumeric
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertMånedsinntekt
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Suppress("NonAsciiCharacters")
class AinntektService {
    // Summerer, grupperer og transformerer ainntekter pr år.
    // 3 mnd og 12 mnd fra opprinnelig vedtakstidspunkt leveres bare hvis vedtakstidspunktOpprinneligVedtak er satt.
    fun beregnAarsinntekt(
        ainntektListeInn: List<Ainntektspost>,
        ainntektHentetDato: LocalDate,
        vedtakstidspunktOpprinneligeVedtak: List<LocalDate> = emptyList(),
    ): List<SummertÅrsinntekt> {
        return if (ainntektListeInn.isNotEmpty()) {
            val ainntektMap = summerAarsinntekter(
                ainntektsposter = ainntektListeInn,
                ainntektHentetDato = ainntektHentetDato,
                vedtakstidspunktOpprinneligeVedtak = vedtakstidspunktOpprinneligeVedtak,
            )
            val ainntektListeUt = mutableListOf<SummertÅrsinntekt>()

            ainntektMap.forEach {
                if (it.key.isNumeric() && it.key.toInt() > finnSisteAarSomSkalRapporteres(ainntektHentetDato)) {
                    return@forEach // Går videre til neste forekomst
                }
                ainntektListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering =
                        when {
                            it.key == KEY_3MND -> Inntektsrapportering.AINNTEKT_BEREGNET_3MND
                            it.key == KEY_12MND -> Inntektsrapportering.AINNTEKT_BEREGNET_12MND
                            it.key.startsWith(KEY_3MND_OV) -> Inntektsrapportering.AINNTEKT_BEREGNET_3MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                            it.key.startsWith(KEY_12MND_OV) -> Inntektsrapportering.AINNTEKT_BEREGNET_12MND_FRA_OPPRINNELIG_VEDTAKSTIDSPUNKT
                            else -> Inntektsrapportering.AINNTEKT
                        },
                        sumInntekt =
                        when {
                            it.key.startsWith(KEY_3MND) -> it.value.sumInntekt.multiply(BigDecimal.valueOf(4)) // Regner om til årsinntekt
                            else -> it.value.sumInntekt
                        }.setScale(0, RoundingMode.HALF_UP),
                        periode = ÅrMånedsperiode(fom = it.value.periodeFra, til = it.value.periodeTil),
                        inntektPostListe =
                        when {
                            it.key.startsWith(KEY_3MND) -> grupperOgSummerDetaljposter(
                                inntektPostListe = it.value.inntektPostListe,
                                multiplikator = 4,
                            )

                            else -> grupperOgSummerDetaljposter(inntektPostListe = it.value.inntektPostListe)
                        },
                        grunnlagsreferanseListe = it.value.grunnlagreferanseListe.toList(),
                    ),
                )
            }
            ainntektListeUt
                .sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
                .distinctBy { Pair(it.inntektRapportering.toString(), it.periode.fom) }
        } else {
            emptyList()
        }
    }

    // Summerer, grupperer og transformerer ainntekter pr måned
    fun beregnMånedsinntekt(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertMånedsinntekt> {
        val ainntektMap = summerMånedsinntekter(ainntektListeInn, ainntektHentetDato)
        val ainntektListeUt = mutableListOf<SummertMånedsinntekt>()

        ainntektMap.forEach {
            ainntektListeUt.add(
                SummertMånedsinntekt(
                    gjelderÅrMåned = YearMonth.of(it.key.substring(0, 4).toInt(), it.key.substring(4, 6).toInt()),
                    sumInntekt = it.value.sumInntekt.setScale(2, RoundingMode.HALF_UP),
                    inntektPostListe = grupperOgSummerDetaljposter(inntektPostListe = it.value.inntektPostListe, scale = 2),
                    grunnlagsreferanseListe = it.value.grunnlagreferanseListe.toList(),
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
        scale: Int = 0,
    ): List<InntektPost> = inntektPostListe
        .groupBy(InntektPost::kode)
        .map {
            InntektPost(
                kode = it.key,
                beløp = it.value.sumOf(InntektPost::beløp).multiply(BigDecimal.valueOf(multiplikator.toLong()))
                    .setScale(scale, RoundingMode.HALF_UP),
            )
        }

    // Summerer og grupperer ainntekter pr år
    private fun summerAarsinntekter(
        ainntektsposter: List<Ainntektspost>,
        ainntektHentetDato: LocalDate,
        vedtakstidspunktOpprinneligeVedtak: List<LocalDate>,
    ): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektsposter.forEach { ainntektPost ->
            kalkulerbeløpForPeriode(
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                beregningsperiode = PERIODE_ÅR,
                ainntektHentetDato = ainntektHentetDato,
                vedtakstidspunktOpprinneligeVedtak = vedtakstidspunktOpprinneligeVedtak,
                referanse = ainntektPost.referanse,
            ).forEach { periodeMap ->
                akkumulerPost(
                    ainntektMap = ainntektMap,
                    key = periodeMap.key,
                    value = periodeMap.value,
                    ainntektHentetDato = ainntektHentetDato,
                )
            }
        }

        // Lager map for 3 mnd med beløp 0 hvis det ikke finnes poster som matcher
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

        // Lager map for 12 mnd med beløp 0 hvis det ikke finnes poster som matcher
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
    private fun summerMånedsinntekter(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektListeInn.forEach { ainntektPost ->
            kalkulerbeløpForPeriode(
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                beregningsperiode = PERIODE_MÅNED,
                ainntektHentetDato = ainntektHentetDato,
                vedtakstidspunktOpprinneligeVedtak = emptyList(),
                referanse = ainntektPost.referanse,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap = ainntektMap, key = periodeMap.key, value = periodeMap.value, ainntektHentetDato = ainntektHentetDato)
            }
        }
        return ainntektMap.toMap()
    }

    // Summerer inntekter og legger til detaljposter til map
    private fun akkumulerPost(ainntektMap: MutableMap<String, InntektSumPost>, key: String, value: Detaljpost, ainntektHentetDato: LocalDate) {
        val beregnFraDato =
            if (key.startsWith(KEY_3MND_OV) || key.startsWith(KEY_12MND_OV)) {
                LocalDate.parse(key.substringAfterLast('_'))
            } else {
                ainntektHentetDato
            }
        val periode = bestemPeriode(periodeVerdi = key, beregnFraDato = beregnFraDato)
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
        val nyInntektPost = InntektPost(kode = value.kode, beløp = value.beløp)
        inntektPostListe.add(nyInntektPost)
        ainntektMap[key] =
            InntektSumPost(
                sumInntekt = sumInntekt.add(value.beløp),
                periodeFra = periode.periodeFra,
                periodeTil = periode.periodeTil,
                inntektPostListe = inntektPostListe,
                grunnlagreferanseListe = inntektSumPost.grunnlagreferanseListe + value.referanse,
            )
    }

    // Kalkulerer beløp for periode (måned, år eller intervall)
    private fun kalkulerbeløpForPeriode(
        utbetalingsperiode: String,
        beskrivelse: String,
        beløp: BigDecimal,
        beregningsperiode: String,
        ainntektHentetDato: LocalDate,
        vedtakstidspunktOpprinneligeVedtak: List<LocalDate>,
        referanse: String,
    ): Map<String, Detaljpost> {
        val periodeMap = mutableMapOf<String, Detaljpost>()
        val periode = YearMonth.of(utbetalingsperiode.substring(0, 4).toInt(), utbetalingsperiode.substring(5, 7).toInt())

        // Hvis periode er måned, returner map med en forekomst for hver måned beløpet dekker
        // Hvis periode er år, returner map med en forekomst for hvert år beløpet dekker + forekomst for siste 3 mnd + forekomst for siste 12 mnd
        when (beregningsperiode) {
            PERIODE_MÅNED ->
                periodeMap[periode.year.toString() + periode.toString().substring(5, 7)] =
                    Detaljpost(beløp = beløp, kode = beskrivelse, referanse = referanse)

            else -> {
                periodeMap[periode.year.toString()] =
                    Detaljpost(beløp = beløp, kode = beskrivelse, referanse = referanse)

                periodeMap.putAll(
                    kalkulerBeløpForIntervall(
                        periode = periode,
                        beskrivelse = beskrivelse,
                        beløp = beløp,
                        beregningsperiode = KEY_3MND,
                        beregnFraDato = ainntektHentetDato,
                        referanse = referanse,
                    ),
                )

                periodeMap.putAll(
                    kalkulerBeløpForIntervall(
                        periode = periode,
                        beskrivelse = beskrivelse,
                        beløp = beløp,
                        beregningsperiode = KEY_12MND,
                        beregnFraDato = ainntektHentetDato,
                        referanse = referanse,
                    ),
                )

                vedtakstidspunktOpprinneligeVedtak.forEach {
                    periodeMap.putAll(
                        kalkulerBeløpForIntervall(
                            periode = periode,
                            beskrivelse = beskrivelse,
                            beløp = beløp,
                            beregningsperiode = KEY_3MND_OV + "_" + it.toString(),
                            beregnFraDato = it,
                            referanse = referanse,
                        ),
                    )

                    periodeMap.putAll(
                        kalkulerBeløpForIntervall(
                            periode = periode,
                            beskrivelse = beskrivelse,
                            beløp = beløp,
                            beregningsperiode = KEY_12MND_OV + "_" + it.toString(),
                            beregnFraDato = it,
                            referanse = referanse,
                        ),
                    )
                }
            }
        }

        return periodeMap
    }

    // Kalkulerer totalt beløp for intervall (3 mnd eller 12 mnd) som forekomsten evt dekker
    private fun kalkulerBeløpForIntervall(
        periode: YearMonth,
        beskrivelse: String,
        beløp: BigDecimal,
        beregningsperiode: String,
        beregnFraDato: LocalDate,
        referanse: String,
    ): Map<String, Detaljpost> {
        val periodeMap = mutableMapOf<String, Detaljpost>()

        val sistePeriodeIIntervall =
            if (beregnFraDato.dayOfMonth > finnCutOffDag(beregnFraDato)) {
                YearMonth.of(beregnFraDato.year, beregnFraDato.month)
            } else {
                YearMonth.of(beregnFraDato.year, beregnFraDato.month).minusMonths(1)
            }
        val forstePeriodeIIntervall =
            if (beregningsperiode == KEY_3MND || beregningsperiode.startsWith(KEY_3MND_OV)) {
                sistePeriodeIIntervall.minusMonths(3)
            } else {
                sistePeriodeIIntervall.minusMonths(12)
            }

        if ((!periode.isBefore(forstePeriodeIIntervall)) && (periode.isBefore(sistePeriodeIIntervall))) {
            periodeMap[beregningsperiode] =
                Detaljpost(
                    beløp = beløp,
                    kode = beskrivelse,
                    referanse = referanse,
                )
        }

        return periodeMap
    }

    // Finner riktig periode basert på nøkkelverdi i map og om det er type år, måned eller intervall
    private fun bestemPeriode(periodeVerdi: String, beregnFraDato: LocalDate): Periode {
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
            periodeTil =
                if (beregnFraDato.dayOfMonth > finnCutOffDag(beregnFraDato)) {
                    YearMonth.of(beregnFraDato.year, beregnFraDato.month).minusMonths(1)
                } else {
                    YearMonth.of(beregnFraDato.year, beregnFraDato.month).minusMonths(2)
                }
            periodeFra = if (periodeVerdi.startsWith(KEY_3MND)) periodeTil.minusMonths(2) else periodeTil.minusMonths(11)
        }

        return Periode(periodeFra = periodeFra, periodeTil = periodeTil)
    }
}

data class Detaljpost(val beløp: BigDecimal, val kode: String, val referanse: String)
