package no.nav.bidrag.beregn.barnebidrag.beregning

import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningResultat
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.TilsynsutgiftBarn
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal object NettoTilsynsutgiftBeregning {
    // Beregner netto tilsynsutgift for alle barn  med faktiske utgifter. Hvis samlede faktiske utgifter er større enn
    // sjablon maks tilsynsbeløp, skal beløpene justeres forholdsmessig.

    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {
        // Henter antall barn i perioden. Dette skal være antall BMs barn under 12 år. i første versjon så må det legges inn perioder med faktiske
        // utgifter for alle barn. I neste versjon så skal alle BMs barn ligge i grunnlaget (grunnlag.barnBMListe), og antall barn telles derfra.
        val antallBarnIPerioden = maxOf(grunnlag.faktiskUtgiftListe.distinctBy { it.gjelderBarn }.size, grunnlag.barnBMListe.size)

        val sjablonSkattAlminneligInntektProsent =
            grunnlag.sjablonSjablontallBeregningGrunnlagListe
                .firstOrNull { it.type == SjablonTallNavn.SKATT_ALMINNELIG_INNTEKT_PROSENT.navn }
                ?: throw IllegalArgumentException("Ingen gyldig sjablon funnet for skatt alminnelig inntekt.")

        val sjablonMaksTilsynsutgift = grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.maxBeløpTilsyn

        val sjablonMaksFradragsbeløp = grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.maxBeløpFradrag

        // lag totaltFaktiskUtgiftBeløp. Dette er (summen av alle faktiske utgifter minus kostpenger) ganger 11 og delt på 12
        val totaltFaktiskUtgiftBeløp = grunnlag.faktiskUtgiftListe.sumOf { it.beregnetMånedsbeløp }

        val månedsbeløpTilleggsstønad = grunnlag.tilleggsstønad?.beregnetMånedsbeløp?.multiply(BigDecimal.valueOf(11))
            ?.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP) ?: BigDecimal.ZERO

        // Beregner bruttobeløp. Dette er faktiske utgifter minus kostpenger minus tilleggsstønad (omregnet fra dagsats til månedsbeløp)
        var totalTilsynsutgift =
            totaltFaktiskUtgiftBeløp.minus(månedsbeløpTilleggsstønad).avrundetMedTiDesimaler.coerceAtLeast(BigDecimal.ZERO)

        val skattefradrag = if (totalTilsynsutgift > BigDecimal.ZERO) {
            beregnFradragsbeløpPerBarn(
                antallBarnIPerioden = antallBarnIPerioden,
                totalTilsynsutgift = minOf(totalTilsynsutgift, sjablonMaksTilsynsutgift),
                sjablonSkattesatsAlminneligInntektProsent = sjablonSkattAlminneligInntektProsent.verdi.toBigDecimal(),
                sjablonMaksFradragsbeløp = sjablonMaksFradragsbeløp,
            )
        } else {
            BigDecimal.ZERO
        }

        val tilsynsutgiftBarnListe = mutableListOf<TilsynsutgiftBarn>()

        var andelTilsynsutgiftBeløp = BigDecimal.ZERO
        var andelTilsynsutgiftFaktor: BigDecimal
        var andelTilsynsutgifterBeløpSøknadsbarn = BigDecimal.ZERO
        var andelTilsynsutgifterFaktorSøknadsbarn = BigDecimal.ZERO

        // Summerer per barn. For søknadsbarn trekkes eventuell tilleggsstønad fra. GroupBy kan fjernes siden utgiftene allerede er summert per barn.
        val sumUtgifterPerBarn = grunnlag.faktiskUtgiftListe.groupBy { it.gjelderBarn }
            .mapValues { (gjelderBarn, utgifter) ->
                if (gjelderBarn == grunnlag.søknadsbarnReferanse) {
                    (utgifter.sumOf { it.beregnetMånedsbeløp } - månedsbeløpTilleggsstønad).coerceAtLeast(BigDecimal.ZERO)
                } else {
                    (utgifter.sumOf { it.beregnetMånedsbeløp }).coerceAtLeast(BigDecimal.ZERO)
                }
            }

        // Finner prosentandel av totalbeløp og beregner så andel av maks tilsynsbeløp
        sumUtgifterPerBarn.forEach { (gjelderBarn, utgifter) ->
            if (totalTilsynsutgift > BigDecimal.ZERO) {
                andelTilsynsutgiftFaktor =
                    utgifter.divide(totalTilsynsutgift, MathContext(10, RoundingMode.HALF_UP)).avrundetMedTiDesimaler

                andelTilsynsutgiftBeløp = if (totalTilsynsutgift > sjablonMaksTilsynsutgift) {
                    andelTilsynsutgiftFaktor * sjablonMaksTilsynsutgift
                } else {
                    utgifter
                }

                tilsynsutgiftBarnListe.add(
                    TilsynsutgiftBarn(
                        gjelderBarn = gjelderBarn,
                        sumTilsynsutgifter = utgifter.avrundetMedToDesimaler,
                        endeligSumTilsynsutgifter = andelTilsynsutgiftBeløp.avrundetMedToDesimaler,
                    ),
                )

                if (gjelderBarn == grunnlag.søknadsbarnReferanse) {
                    andelTilsynsutgifterBeløpSøknadsbarn = andelTilsynsutgiftBeløp
                    andelTilsynsutgifterFaktorSøknadsbarn = andelTilsynsutgiftFaktor
                }
            } else {
                // Hvis summen av alle faktiske utgifter minus tilleggsstønad er 0 så kan ikke andel utregnes
                tilsynsutgiftBarnListe.add(
                    TilsynsutgiftBarn(
                        gjelderBarn = gjelderBarn,
                        sumTilsynsutgifter = utgifter.avrundetMedToDesimaler,
                        endeligSumTilsynsutgifter = utgifter.avrundetMedToDesimaler,
                    ),
                )
            }
        }

        // Sjekker og justerer mot sjablonverdi maks tilsynsbeløp
        totalTilsynsutgift = minOf(totalTilsynsutgift, sjablonMaksTilsynsutgift)

        val resultat = NettoTilsynsutgiftBeregningResultat(
            totalTilsynsutgift = totalTilsynsutgift.avrundetMedToDesimaler,
            sjablonMaksTilsynsutgift = sjablonMaksTilsynsutgift,
            andelTilsynsutgiftBeløp = andelTilsynsutgifterBeløpSøknadsbarn.avrundetMedToDesimaler,
            andelTilsynsutgiftFaktor = andelTilsynsutgifterFaktorSøknadsbarn,
            skattefradrag = skattefradrag.avrundetMedToDesimaler,
            nettoTilsynsutgift = (andelTilsynsutgifterBeløpSøknadsbarn - skattefradrag).avrundetMedToDesimaler
                .coerceAtLeast(BigDecimal.ZERO),
            tilsynsutgiftBarnListe = tilsynsutgiftBarnListe,
            grunnlagsreferanseListe = listOfNotNull(
                grunnlag.barnBMListe.map { it.referanse },
                grunnlag.faktiskUtgiftListe.map { it.referanse },
                grunnlag.tilleggsstønad?.referanse?.let { listOf(it) } ?: emptyList(),
                grunnlag.sjablonSjablontallBeregningGrunnlagListe.map { it.referanse },
                listOf(grunnlag.sjablonMaksTilsynsbeløpBeregningGrunnlag.referanse),
                listOf(grunnlag.sjablonMaksFradragsbeløpBeregningGrunnlag.referanse),
            ).flatten().distinct(),
        )

        return resultat
    }

    private fun beregnFradragsbeløpPerBarn(
        antallBarnIPerioden: Int,
        totalTilsynsutgift: BigDecimal,
        sjablonSkattesatsAlminneligInntektProsent: BigDecimal,
        sjablonMaksFradragsbeløp: BigDecimal,
    ): BigDecimal {
        val skattesatsFaktor = sjablonSkattesatsAlminneligInntektProsent.divide(BigDecimal(100)).avrundetMedTiDesimaler
        val maksFradragsbeløp = sjablonMaksFradragsbeløp * skattesatsFaktor
        val fradragsbeløp = minOf((totalTilsynsutgift * skattesatsFaktor), maksFradragsbeløp)

        return fradragsbeløp.divide(antallBarnIPerioden.toBigDecimal(), MathContext(10, RoundingMode.HALF_UP))
    }
}
