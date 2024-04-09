package no.nav.bidrag.inntekt.service

import no.nav.bidrag.commons.service.finnVisningsnavn
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.visningsnavnIntern
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.BRUDD_MÅNED_OVERGANSSTØNAD
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.filtrerInntekterPåYtelse
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.finnCutOffDag
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.hentMappingYtelser
import no.nav.bidrag.transport.behandling.inntekt.request.Ainntektspost
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.SummertÅrsinntekt
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@Suppress("NonAsciiCharacters")
class YtelserServiceOvergangsstønad {

    // Summerer, grupperer og transformerer ainntekter pr år (overgangsstønad)).
    // Spesiallogikk for overgangsstønad ifht. andre ytelser (som dekkes av YtelserService):
    // - Etterbetalingsperiode overstyrer utbetalingsperiode hvis den er satt
    // - Året strekker seg ikke over et kalenderår, men går fom. mai tom. april neste år
    // - Det skal rapporteres helt tom. "inneværende år", selv om "året" ikke er sluttført (det beregnes da for resten av året basert på snittet av
    //   inntektene som er rapportert)
    fun beregnYtelser(ainntektListeInn: List<Ainntektspost>, ainntektHentetDato: LocalDate): List<SummertÅrsinntekt> {
        val beskrivelserListe =
            hentMappingYtelser().filter { it.key == Inntektsrapportering.OVERGANGSSTØNAD.toString() }.flatMap { it.value.beskrivelser }
        val ainntektListeFiltrert = filtrerInntekterPåYtelse(ainntektListeInn = ainntektListeInn, beskrivelserListe = beskrivelserListe)
        val periodeListe = hentPerioder(ainntektListeFiltrert)
        val sisteRapportertePeriode = finnSisteRapportertePeriode(ainntektHentetDato)

        return if (ainntektListeFiltrert.isNotEmpty()) {
            val ytelseMap = summerAarsinntekter(ainntektsposter = ainntektListeFiltrert, sisteRapportertePeriode = sisteRapportertePeriode)
            val ytelseListeUt = mutableListOf<SummertÅrsinntekt>()

            ytelseMap.forEach {
                ytelseListeUt.add(
                    SummertÅrsinntekt(
                        inntektRapportering = Inntektsrapportering.OVERGANGSSTØNAD,
                        visningsnavn = Inntektsrapportering.OVERGANGSSTØNAD.visningsnavnIntern(it.value.periodeFra.year),
                        sumInntekt = beregnInntekt(
                            sisteRapportertePeriode = sisteRapportertePeriode,
                            periodeListe = periodeListe,
                            sumInntekt = it.value.sumInntekt,
                            periodeFra = it.value.periodeFra,
                            periodeTil = it.value.periodeTil!!,
                        ),
                        periode = ÅrMånedsperiode(fom = it.value.periodeFra, til = it.value.periodeTil),
                        inntektPostListe = grupperOgSummerDetaljposter(
                            it.value.inntektPostListe,
                            sisteRapportertePeriode,
                            periodeListe,
                            it.value.periodeFra,
                            it.value.periodeTil!!
                        ),
                        grunnlagsreferanseListe = it.value.grunnlagreferanseListe.toList(),
                    ),
                )
            }
            ytelseListeUt.sortedWith(compareBy({ it.inntektRapportering.toString() }, { it.periode.fom }))
        } else {
            emptyList()
        }
    }

    // Finner siste periode som er rapportert i ainntekt ihht. frister i a-ordningen
    private fun finnSisteRapportertePeriode(ainntektHentetDato: LocalDate): YearMonth {
        val cutOffDato = ainntektHentetDato.withDayOfMonth(finnCutOffDag(ainntektHentetDato))
        return if (ainntektHentetDato.isAfter(cutOffDato)) {
            YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(1)
        } else {
            YearMonth.of(ainntektHentetDato.year, ainntektHentetDato.month).minusMonths(2)
        }
    }

    // Henter ut alle unike perioder det er levert data for for en spesifikk ytelse/beskrivelse. For overgangsstønad brukes
    // etterbetalingsperiode hvis den er satt, ellers brukes utbetalingsperiode. Det forutsettes her at etterbetalingsperiode strekker seg
    // over kun en måned og perioden varer fra den første til den siste i måneden. I eksemplene som er gjennomgått er etterbetalingsperiode
    // satt på denne måten for ytelser, men i teorien kan etterbetalingsperiode gå over flere måneder og starte og slutte på en annen dato
    // enn den første i måneden. Hvis det finnes eksempler på dette må denne logikken revurderes.
    private fun hentPerioder(ainntektListe: List<Ainntektspost>): Map<String, Set<YearMonth>> {
        val periodeListe = mutableMapOf<String, MutableSet<YearMonth>>()

        ainntektListe.forEach { ainntektspost ->
            val periode = if (ainntektspost.etterbetalingsperiodeFra != null) {
                YearMonth.of(ainntektspost.etterbetalingsperiodeFra!!.year, ainntektspost.etterbetalingsperiodeFra!!.monthValue)
            } else {
                YearMonth.of(
                    ainntektspost.utbetalingsperiode!!.substring(0, 4).toInt(),
                    ainntektspost.utbetalingsperiode!!.substring(5, 7).toInt()
                )
            }

            periodeListe.getOrPut(ainntektspost.beskrivelse!!) { mutableSetOf() }.add(periode)
        }

        return periodeListe
    }

    // Beregner inntekt for et "år". Finner antall unike perioder som har data på tvers av postene, deler summert inntekt med antall
    // måneder og ganger med 12 for å regne om til årsinntekt.
    private fun beregnInntekt(
        sisteRapportertePeriode: YearMonth,
        periodeListe: Map<String, Set<YearMonth>>,
        sumInntekt: BigDecimal,
        periodeFra: YearMonth,
        periodeTil: YearMonth,
    ): BigDecimal {
        val unikPeriodeListe = periodeListe.values.flatten().toSet()
        val periodeTilJustert = if (sisteRapportertePeriode.isBefore(periodeTil)) sisteRapportertePeriode else periodeTil
        val antallMånederMedDataIPerioden = unikPeriodeListe.count { it in periodeFra..periodeTilJustert }.toLong()

        return sumInntekt
            .divide(BigDecimal.valueOf(antallMånederMedDataIPerioden))
            .multiply(BigDecimal.valueOf(12))
            .setScale(0, RoundingMode.HALF_UP)
    }

    // Grupperer og summerer poster som har samme kode/beskrivelse. Bruker samme prinsipp for å regne om til årsinntekt som i beregnInntekt.
    private fun grupperOgSummerDetaljposter(
        inntektPostListe: List<InntektPost>,
        sisteRapportertePeriode: YearMonth,
        periodeListe: Map<String, Set<YearMonth>>,
        periodeFra: YearMonth,
        periodeTil: YearMonth
    ): List<InntektPost> {
        val summertInntektPostListe = inntektPostListe
            .groupBy(InntektPost::kode)
            .map {
                InntektPost(
                    kode = it.key,
                    visningsnavn = finnVisningsnavn(it.key),
                    beløp = it.value.sumOf(InntektPost::beløp),
                )
            }

        val unikPeriodeListe = periodeListe.values.flatten().toSet()
        val periodeTilJustert = if (sisteRapportertePeriode.isBefore(periodeTil)) sisteRapportertePeriode else periodeTil
        val antallMånederMedDataIPerioden = unikPeriodeListe.count { it in periodeFra..periodeTilJustert }.toLong()

        val summertInntektPostListeTilÅrsinntekt = mutableListOf<InntektPost>()
        summertInntektPostListe.forEach { inntektPost ->
            summertInntektPostListeTilÅrsinntekt.add(
                InntektPost(
                    kode = inntektPost.kode,
                    visningsnavn = inntektPost.visningsnavn,
                    beløp = inntektPost.beløp
                        .divide(BigDecimal.valueOf(antallMånederMedDataIPerioden))
                        .multiply(BigDecimal.valueOf(12))
                        .setScale(0, RoundingMode.HALF_UP),
                ),
            )
        }

        return summertInntektPostListeTilÅrsinntekt
    }


    // Summerer og grupperer ainntekter pr år
    private fun summerAarsinntekter(ainntektsposter: List<Ainntektspost>, sisteRapportertePeriode: YearMonth): Map<String, InntektSumPost> {
        val ainntektMap = mutableMapOf<String, InntektSumPost>()
        ainntektsposter.forEach { ainntektPost ->
            kalkulerbeløpForPeriode(
                etterbetalingsperiodeFra = ainntektPost.etterbetalingsperiodeFra,
                utbetalingsperiode = ainntektPost.utbetalingsperiode!!,
                beskrivelse = ainntektPost.beskrivelse!!,
                beløp = ainntektPost.beløp,
                referanse = ainntektPost.referanse,
                sisteRapportertePeriode = sisteRapportertePeriode,
            ).forEach { periodeMap ->
                akkumulerPost(ainntektMap = ainntektMap, key = periodeMap.key, value = periodeMap.value)
            }
        }

        return ainntektMap.toMap()
    }

    // Summerer inntekter og legger til detaljposter til map. Bruddmåned styrer hvilken periode som blir satt.
    private fun akkumulerPost(ainntektMap: MutableMap<String, InntektSumPost>, key: String, value: Detaljpost) {
        val periode = Periode(YearMonth.of(key.toInt(), BRUDD_MÅNED_OVERGANSSTØNAD), YearMonth.of(key.toInt() + 1, BRUDD_MÅNED_OVERGANSSTØNAD - 1))
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

    // Kalkulerer beløp for periode (i dette tilfellet helt år, men ikke kalenderår)
    private fun kalkulerbeløpForPeriode(
        etterbetalingsperiodeFra: LocalDate?,
        utbetalingsperiode: String,
        beskrivelse: String,
        beløp: BigDecimal,
        referanse: String,
        sisteRapportertePeriode: YearMonth,
    ): Map<String, Detaljpost> {
        val periode =
            if (etterbetalingsperiodeFra != null) {
                YearMonth.of(etterbetalingsperiodeFra.year, etterbetalingsperiodeFra.month)
            } else {
                YearMonth.of(utbetalingsperiode.substring(0, 4).toInt(), utbetalingsperiode.substring(5, 7).toInt())
            }

        // Siste rapporterte periode skal ikke inkluderes hvis den er etter siste fullt rapporterte periode (ihht. frister i a-ordningen)
        if (sisteRapportertePeriode.isBefore(periode)) {
            return emptyMap()
        }

        return kalkulerBeløpForÅr(
            periode = periode,
            beskrivelse = beskrivelse,
            beløp = beløp,
            referanse = referanse,
        )
    }

    // Kalkulerer totalt beløp for hvert år forekomsten dekker
    private fun kalkulerBeløpForÅr(periode: YearMonth, beskrivelse: String, beløp: BigDecimal, referanse: String): Map<String, Detaljpost> {
        val år = if (periode.monthValue < BRUDD_MÅNED_OVERGANSSTØNAD) periode.year - 1 else periode.year
        return mapOf(år.toString() to Detaljpost(beløp = beløp, kode = beskrivelse, referanse = referanse))
    }
}
