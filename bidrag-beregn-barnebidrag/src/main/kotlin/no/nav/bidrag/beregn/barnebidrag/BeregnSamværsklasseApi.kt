@file:Suppress("unused")

package no.nav.bidrag.beregn.barnebidrag
import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.mapping.tilGrunnlagsobjekt
import no.nav.bidrag.beregn.core.util.SjablonUtil.justerSjablonTomDato
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.SjablonService
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.samværskalkulator.SamværskalkulatorNetterFrekvens
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.beregning.samvær.SamværskalkulatorDetaljer
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsklasse
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsklasserNetter
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate

internal val totalNetterOverToÅr = BigDecimal(730)
internal val totalNetterOverToUker = BigDecimal(14)
internal val totalMånederOverToÅr = BigDecimal(24)

internal val BigDecimal.gjennomsnittOverToUker get() = divide(totalNetterOverToUker, 10, RoundingMode.HALF_EVEN)
internal val BigDecimal.gjennomsnittOverToÅr get() = divide(totalMånederOverToÅr, 10, RoundingMode.HALF_EVEN)

// Tilpasset slik at det skal være lik bidragskalkulator mtp avrunding.
// Det er for å unngå forskjellige resultater mellom offentlig samværskalkulator og ny samværskalkulator.
// Erstatt med metodene over for å få riktig avrunding.
internal val BigDecimal.gjennomsnittOverToÅrOffentligSamværskalkulator get() = BigDecimal(toDouble() / totalMånederOverToÅr.toDouble())
internal val BigDecimal.gjennomsnittOverToUkerOffentligSamværskalkulator get() = BigDecimal(toDouble() / totalNetterOverToUker.toDouble())
internal val BigDecimal.tilpassetOffentligSamværskalkulator get() = avrundetMedToDesimaler

internal data class DelberegningSamværsklasserNetterIntern(
    val delberegning: DelberegningSamværsklasserNetter.SamværsklasseNetter,
    val sjablon: Samværsfradrag,
)

internal data class DelberegningSamværsklasseIntern(
    val delberegning: DelberegningSamværsklasserNetterIntern?,
    val samværsklasse: Samværsklasse,
    val gjennomsnittligSamvær: BigDecimal,
    val gjennomsnittligSamværAvrundet: BigDecimal,
    val samværsklasser: List<DelberegningSamværsklasserNetterIntern>,
)

@Service
class BeregnSamværsklasseApi(private val sjablonService: SjablonService) {
    companion object {
        fun beregnSumGjennomsnittligSamværPerMåned(detaljer: SamværskalkulatorDetaljer): BigDecimal =
            detaljer.gjennomsnittligMånedligSamvær().avrundetMedToDesimaler
    }

    fun beregnSamværsklasse(kalkulator: SamværskalkulatorDetaljer): List<GrunnlagDto> {
        val resultat = beregnSamværsklasseDelberegning(kalkulator)
        val grunnlagSjablon = resultat.samværsklasser.map {
            it.sjablon.tilGrunnlagsobjekt(periode = ÅrMånedsperiode(it.sjablon.datoFom!!, it.sjablon.datoTom?.let { justerSjablonTomDato(it) }))
        }
        val grunnlagSamværsklasseNetter = resultat.delberegning?.run {
            GrunnlagDto(
                type = Grunnlagstype.DELBEREGNING_SAMVÆRSKLASSER_NETTER,
                referanse = "delberegning_samværsklasser_netter",
                innhold = POJONode(
                    DelberegningSamværsklasserNetter(resultat.samværsklasser.map { it.delberegning }),
                ),
                grunnlagsreferanseListe = grunnlagSjablon.map { it.referanse },
            )
        }
        val grunnlagKalkulator = GrunnlagDto(
            type = Grunnlagstype.SAMVÆRSKALKULATOR,
            innhold = POJONode(kalkulator),
            referanse = "samværskalkulator_hash_${kalkulator.hashCode()}",
        )
        val grunnlagSamværsklasse = GrunnlagDto(
            type = Grunnlagstype.DELBEREGNING_SAMVÆRSKLASSE,
            referanse = "delberegning_samværsklasse_${resultat.samværsklasse}" +
                "_gjennomsnittlig_samvær_${resultat.gjennomsnittligSamvær.avrundetMedToDesimaler}",
            innhold = POJONode(
                DelberegningSamværsklasse(resultat.samværsklasse, resultat.gjennomsnittligSamvær.avrundetMedToDesimaler),
            ),
            grunnlagsreferanseListe = listOfNotNull(grunnlagSamværsklasseNetter?.referanse, grunnlagKalkulator.referanse),
        )
        return listOfNotNull(grunnlagSamværsklasseNetter, grunnlagSamværsklasse, grunnlagKalkulator) + grunnlagSjablon
    }

    internal fun List<Samværsfradrag>.delberegningSamværsklasserNetter(): List<DelberegningSamværsklasserNetterIntern> {
        val sjabloner = filter {
            it.datoTom == null || it.datoTom!! > LocalDate.now()
        }.distinctBy { it.samvaersklasse }.sortedBy { it.samvaersklasse }

        return sjabloner.foldIndexed(emptyList()) { index, acc, samværsfradrag ->
            val antallNetterFra = if (acc.isEmpty()) {
                BigDecimal.ZERO
            } else {
                acc.last().delberegning.antallNetterTil.setScale(
                    0,
                    RoundingMode.FLOOR,
                ) + BigDecimal.ONE
            }
            val antallNetterTil = if (index == sjabloner.lastIndex) {
                samværsfradrag.antNetterTom!!.toBigDecimal().avrundetMedToDesimaler
            } else {
                (samværsfradrag.antNetterTom!!.toBigDecimal() + BigDecimal(0.99)).avrundetMedToDesimaler
            }
            acc + DelberegningSamværsklasserNetterIntern(
                DelberegningSamværsklasserNetter.SamværsklasseNetter(
                    Samværsklasse.fromBisysKode(samværsfradrag.samvaersklasse!!)!!,
                    antallNetterFra,
                    antallNetterTil,
                ),
                sjablon = samværsfradrag,
            )
        }
    }

    private fun beregnSamværsklasseDelberegning(kalkulator: SamværskalkulatorDetaljer): DelberegningSamværsklasseIntern {
        val samværsklasser = sjablonService.hentSjablonSamværsfradrag().delberegningSamværsklasserNetter()

        val gjennomsnittligSamvær = kalkulator.gjennomsnittligMånedligSamvær()
        val gjennomsnittligSamværAvrundet = gjennomsnittligSamvær.avrundetMedToDesimaler
        val samværsklasseDelberegning =
            samværsklasser
                .find {
                    gjennomsnittligSamværAvrundet >= it.delberegning.antallNetterFra &&
                        gjennomsnittligSamværAvrundet <= it.delberegning.antallNetterTil
                }?.let {
                    DelberegningSamværsklasseIntern(
                        it,
                        it.delberegning.samværsklasse,
                        gjennomsnittligSamvær,
                        gjennomsnittligSamværAvrundet,
                        samværsklasser,
                    )
                }
                ?: run {
                    val sisteSamværsklasse = samværsklasser.last()
                    if (gjennomsnittligSamværAvrundet > sisteSamværsklasse.delberegning.antallNetterTil) {
                        DelberegningSamværsklasseIntern(
                            sisteSamværsklasse,
                            sisteSamværsklasse.delberegning.samværsklasse,
                            gjennomsnittligSamvær,
                            gjennomsnittligSamværAvrundet,
                            samværsklasser,
                        )
                    } else {
                        DelberegningSamværsklasseIntern(
                            null,
                            Samværsklasse.SAMVÆRSKLASSE_0,
                            gjennomsnittligSamvær,
                            gjennomsnittligSamværAvrundet,
                            samværsklasser,
                        )
                    }
                }

        return samværsklasseDelberegning
    }
}

private fun List<SamværskalkulatorDetaljer.SamværskalkulatorFerie>.bmTotalNetter() = sumOf {
    it.bidragsmottakerTotalAntallNetterOverToÅr
}

private fun List<SamværskalkulatorDetaljer.SamværskalkulatorFerie>.bpTotalNetter() = sumOf {
    it.bidragspliktigTotalAntallNetterOverToÅr
}

private val SamværskalkulatorDetaljer.SamværskalkulatorFerie.frekvensSomAntallNetter get() =
    if (frekvens == SamværskalkulatorNetterFrekvens.HVERT_ÅR) {
        BigDecimal.TWO
    } else {
        BigDecimal.ONE
    }

private val SamværskalkulatorDetaljer.SamværskalkulatorFerie.bidragsmottakerTotalAntallNetterOverToÅr get() =
    bidragsmottakerNetter.multiply(frekvensSomAntallNetter, MathContext(10, RoundingMode.HALF_EVEN)).tilpassetOffentligSamværskalkulator

private val SamværskalkulatorDetaljer.SamværskalkulatorFerie.bidragspliktigTotalAntallNetterOverToÅr get() =
    bidragspliktigNetter.multiply(frekvensSomAntallNetter, MathContext(10, RoundingMode.HALF_EVEN)).tilpassetOffentligSamværskalkulator

private fun SamværskalkulatorDetaljer.totalGjennomsnittligSamvær() = regelmessigSamværNetter.multiply(
    samværOverFjortendagersDagersperiode(),
    MathContext(10, RoundingMode.HALF_EVEN),
).tilpassetOffentligSamværskalkulator

private fun SamværskalkulatorDetaljer.gjennomsnittligMånedligSamvær() = totalSamvær().gjennomsnittOverToÅrOffentligSamværskalkulator

private fun SamværskalkulatorDetaljer.totalSamvær() =
    ferier.bpTotalNetter().tilpassetOffentligSamværskalkulator + totalGjennomsnittligSamvær().tilpassetOffentligSamværskalkulator

private fun SamværskalkulatorDetaljer.samværOverFjortendagersDagersperiode() =
    regelmessigSamværHosBm().gjennomsnittOverToUkerOffentligSamværskalkulator.tilpassetOffentligSamværskalkulator

private fun SamværskalkulatorDetaljer.regelmessigSamværHosBm(): BigDecimal = totalNetterOverToÅr - ferier.bpTotalNetter() - ferier.bmTotalNetter()
