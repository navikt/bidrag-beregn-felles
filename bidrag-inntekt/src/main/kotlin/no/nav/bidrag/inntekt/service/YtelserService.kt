package no.nav.bidrag.inntekt.service

import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.filtrerInntekterPåYtelse
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnSisteAarSomSkalRapporteres
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.hentMappingYtelser
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Suppress("NonAsciiCharacters")
class YtelserService {

    // Summerer, grupperer og transformerer ainntekter pr år per Navytelse
    fun beregnYtelser(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertÅrsinntekt> {
        val alleYtelser = mutableListOf<SummertÅrsinntekt>()
        val mapping = hentMappingYtelser()

        for (ytelse in mapping.keys) {
            // Overgangsstønad behandles i egen service
            if (ytelse == Inntektsrapportering.OVERGANGSSTØNAD.toString()) {
                continue
            }
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
    private fun beregnYtelse(
        ainntektListeInn: List<Ainntektspost>,
        ytelse: Inntektsrapportering,
        beskrivelserListe: List<String>,
        ainntektHentetDato: LocalDate,
    ): List<SummertÅrsinntekt> {
        // Filterer bort poster som ikke er AAP
        val ainntektListe = filtrerInntekterPåYtelse(ainntektListeInn = ainntektListeInn, beskrivelserListe = beskrivelserListe)

        return if (ainntektListe.isNotEmpty()) {
            val ytelseMap = summerAarsinntekter(ainntektListe)
            val ytelseListeUt = mutableListOf<SummertÅrsinntekt>()

            ytelseMap.forEach {
                if (it.key.toInt() > finnSisteAarSomSkalRapporteres(ainntektHentetDato)) {
                    return@forEach // Går videre til neste forekomst
                }
                ytelseListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = ytelse,
                        sumInntekt = it.value.sumInntekt.setScale(0, RoundingMode.HALF_UP),
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
                    beløp = it.value.sumOf(InntektPost::beløp).setScale(0, RoundingMode.HALF_UP),
                )
            }
    }

    // Summerer og grupperer ainntekter pr år
    private fun summerAarsinntekter(ainntektsposter: List<Ainntektspost>): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektsposter.forEach { ainntektPost ->
            kalkulerbeløpForÅr(
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                referanse = ainntektPost.referanse,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap = ainntektMap, key = periodeMap.key, value = periodeMap.value)
            }
        }

        return ainntektMap.toMap()
    }

    // Summerer inntekter og legger til detaljposter til map
    private fun akkumulerPost(ainntektMap: MutableMap<String, InntektSumPost>, key: String, value: Detaljpost) {
        val periode = Periode(periodeFra = YearMonth.of(key.toInt(), 1), periodeTil = YearMonth.of(key.toInt(), 12))
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
    private fun kalkulerbeløpForÅr(utbetalingsperiode: String, beskrivelse: String, beløp: BigDecimal, referanse: String): Map<String, Detaljpost> {
        val år = utbetalingsperiode.substring(0, 4)
        return mapOf(år to Detaljpost(beløp = beløp, kode = beskrivelse, referanse = referanse))
    }
}

data class Beskrivelser(
    val beskrivelser: List<String>,
)
