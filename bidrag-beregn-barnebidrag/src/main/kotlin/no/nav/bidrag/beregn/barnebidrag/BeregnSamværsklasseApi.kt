package no.nav.bidrag.beregn.barnebidrag

import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.beregning.samvær.SamværskalkulatorDetaljer
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class SamværsklasseAntallDager(
    val samværsklasse: Samværsklasse,
    val antallNetterFra: BigDecimal,
    val antallNetterTil: BigDecimal,
)

data class BeregnSamværsklasseResultat(
    val samværsklasse: Samværsklasse,
    val sumGjennomsnittligSamværPerMåned: BigDecimal,
)

internal val totalNetterOverToÅr = BigDecimal(730)
internal val totalNetterOverToUker = BigDecimal(14)
internal val totalMånederOverToÅr = BigDecimal(24)
internal val BigDecimal.gjennomsnittOverToÅr get() = divide(totalMånederOverToÅr, 2, RoundingMode.HALF_UP)
internal val BigDecimal.gjennomsnittOverToUker get() = divide(totalNetterOverToUker, 2, RoundingMode.HALF_UP)

@Service
class BeregnSamværsklasseApi {
    companion object {
        fun beregnSumGjennomsnittligSamværPerMåned(detaljer: SamværskalkulatorDetaljer): BigDecimal = detaljer.gjennomsnittligMånedligSamvær()
    }

    fun List<Samværsfradrag>.tilSamværsklasseAntallDagerListe(): List<SamværsklasseAntallDager> = filter {
        it.datoTom == null || it.datoTom!! > LocalDate.now()
    }.distinctBy { it.samvaersklasse }.sortedBy { it.samvaersklasse }.fold(emptyList()) { acc, samværsfradrag ->
        val antallNetterFra = if (acc.isEmpty()) BigDecimal.ZERO else acc.last().antallNetterTil + BigDecimal.ONE
        val antallNetterTil = samværsfradrag.antNetterTom!!.toBigDecimal()
        acc + SamværsklasseAntallDager(Samværsklasse.fromBisysKode(samværsfradrag.samvaersklasse!!)!!, antallNetterFra, antallNetterTil)
    }

    fun beregnSamværsklasse(kalkulator: SamværskalkulatorDetaljer): BeregnSamværsklasseResultat {
        val samværsklasser = SjablonProvider.hentSjablonSamværsfradrag().tilSamværsklasseAntallDagerListe()

        val gjennomsnittligSamvær = kalkulator.gjennomsnittligMånedligSamvær()
        val gjennomsnittligSamværAvrundet = gjennomsnittligSamvær.avrundetMedNullDesimaler
        val samværsklasse =
            samværsklasser
                .find {
                    gjennomsnittligSamværAvrundet >= it.antallNetterFra && gjennomsnittligSamværAvrundet <= it.antallNetterTil
                }?.samværsklasse
                ?: run {
                    val sisteSamværsklasse = samværsklasser.last()
                    if (gjennomsnittligSamværAvrundet > sisteSamværsklasse.antallNetterTil) {
                        sisteSamværsklasse.samværsklasse
                    } else {
                        Samværsklasse.INGEN_SAMVÆR
                    }
                }

        return BeregnSamværsklasseResultat(samværsklasse, gjennomsnittligSamvær)
    }
}

private fun List<SamværskalkulatorDetaljer.SamværskalkulatorFerie>.bmTotalNetter() = sumOf {
    it.bidragsmottakerTotalAntallNetterOverToÅr.toBigDecimal().avrundetMedToDesimaler
}

private fun List<SamværskalkulatorDetaljer.SamværskalkulatorFerie>.bpTotalNetter() = sumOf {
    it.bidragspliktigTotalAntallNetterOverToÅr.toBigDecimal().avrundetMedToDesimaler
}

private fun SamværskalkulatorDetaljer.totalGjennomsnittligSamvær() =
    regelmessigSamværNetter.toBigDecimal().multiply(samværOverFjortendagersDagersperiode())

private fun SamværskalkulatorDetaljer.gjennomsnittligMånedligSamvær() = totalSamvær().gjennomsnittOverToÅr

private fun SamværskalkulatorDetaljer.totalSamvær() = ferier.bpTotalNetter() + totalGjennomsnittligSamvær()

private fun SamværskalkulatorDetaljer.samværOverFjortendagersDagersperiode() = regelmessigSamværHosBm().gjennomsnittOverToUker.avrundetMedToDesimaler

private fun SamværskalkulatorDetaljer.regelmessigSamværHosBm(): BigDecimal {
    return totalNetterOverToÅr - ferier.bpTotalNetter() - ferier.bmTotalNetter()
}
