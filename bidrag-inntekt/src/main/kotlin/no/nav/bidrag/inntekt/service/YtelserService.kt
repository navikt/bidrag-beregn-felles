package no.nav.bidrag.inntekt.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.commons.service.finnVisningsnavn
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnCutOffDag
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnSisteAarSomSkalRapporteres
import no.nav.bidrag.inntekt.util.beregneBeløpPerMåned
import no.nav.bidrag.inntekt.util.isNumeric
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Suppress("NonAsciiCharacters")
class YtelserService {
    internal val objectmapper = ObjectMapper(YAMLFactory()).findAndRegisterModules().registerKotlinModule()

    // Summerer, grupperer og transformerer ainntekter pr år per Navytelse
    fun beregnYtelser(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertÅrsinntekt> {
        val alleYtelser = mutableListOf<SummertÅrsinntekt>()
        val mapping = hentMappingYtelser()

        for (ytelse in mapping.keys) {
            alleYtelser.addAll(
                beregnYtelse(
                    ainntektListeInn,
                    Inntektsrapportering.valueOf(ytelse),
                    mapping[ytelse]!!.beskrivelser,
                    ainntektHentetDato,
                ),
            )
        }
        return alleYtelser
    }

    // Summerer, grupperer og transformerer ainntekter pr år per Navytelse
    fun beregnYtelse(
        ainntektListeInn: List<Ainntektspost>,
        ytelse: Inntektsrapportering,
        beskrivelserListe: List<String>,
        ainntektHentetDato: LocalDate,
    ): List<SummertÅrsinntekt> {
        // Filterer bort poster som ikke er AAP
        val ainntektListe = filtrerInntekterPåYtelse(ainntektListeInn, beskrivelserListe)

        return if (ainntektListe.isNotEmpty()) {
            val ytelseMap = summerAarsinntekter(ainntektListe, ainntektHentetDato)
            val ytelseListeUt = mutableListOf<SummertÅrsinntekt>()

            ytelseMap.forEach {
                if (it.key.toInt() > finnSisteAarSomSkalRapporteres(ainntektHentetDato)) {
                    return@forEach // Går videre til neste forekomst
                }
                ytelseListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = ytelse,
                        visningsnavn = ytelse.visningsnavn.intern,
                        sumInntekt = it.value.sumInntekt,
                        periode = ÅrMånedsperiode(fom = it.value.periodeFra, til = it.value.periodeTil),
                        inntektPostListe = grupperOgSummerDetaljposter(it.value.inntektPostListe),
                        grunnlagsreferanseListe = it.value.grunnlagreferanseListe.toList(),
                    ),
                )
            }
            ytelseListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
        } else {
            emptyList()
        }
    }

    // Grupperer og summerer poster som har samme kode/beskrivelse
    private fun grupperOgSummerDetaljposter(inntektPostListe: List<InntektPost>): List<InntektPost> {
        return inntektPostListe
            .groupBy(InntektPost::kode)
            .map {
                InntektPost(
                    kode = it.key,
                    visningsnavn = finnVisningsnavn(it.key),
                    beløp = it.value.sumOf(InntektPost::beløp),
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
                referanse = ainntektPost.referanse,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap, periodeMap.key, periodeMap.value, ainntektHentetDato)
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
                grunnlagreferanseListe = inntektSumPost.grunnlagreferanseListe + value.referanse,
            )
    }

    // Kalkulerer beløp for periode (måned, år eller intervall)
    private fun kalkulerbeløpForPeriode(
        opptjeningsperiodeFra: LocalDate?,
        opptjeningsperiodeTil: LocalDate?,
        utbetalingsperiode: String,
        beskrivelse: String,
        beløp: BigDecimal,
        referanse: String,
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

        // Returner map med en forekomst for hvert år beløpet dekker
        return kalkulerBeløpForAar(periodeFra = periodeFra, periodeTil = periodeTil, beskrivelse = beskrivelse, beløp = beløp, referanse = referanse)
    }

    // Kalkulerer totalt beløp for hvert år forekomsten dekker
    private fun kalkulerBeløpForAar(
        periodeFra: YearMonth,
        periodeTil: YearMonth,
        beskrivelse: String,
        beløp: BigDecimal,
        referanse: String,
    ): Map<String, Detaljpost> {
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
                    Detaljpost(
                        beløp = antallMndIÅr.toBigDecimal().times(månedsbeløp),
                        kode = beskrivelse,
                        referanse = referanse,
                    )
            }
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
            periodeTil =
                if (ainntektHentetDato.dayOfMonth > finnCutOffDag(ainntektHentetDato)) {
                    YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(1)
                } else {
                    YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(2)
                }
            periodeFra = periodeTil.minusMonths(11)
        }

        return Periode(periodeFra = periodeFra, periodeTil = periodeTil)
    }

    private fun filtrerInntekterPåYtelse(ainntektListeInn: List<Ainntektspost>, beskrivelserListe: List<String>) = ainntektListeInn.filter {
        it.beskrivelse in beskrivelserListe
    }

    // les innhold fra fil mapping_ytelser.yaml og returner dette som en map
    private fun hentMappingYtelser(): Map<String, Beskrivelser> {
        val fil =
            YtelserService::class.java.getResource("/files/mapping_ytelser.yaml")
                ?: throw RuntimeException("Fant ingen fil på sti mapping_ytelser.yaml")
        return objectmapper.readValue(fil)
    }
}

data class Beskrivelser(
    val beskrivelser: List<String>,
)
