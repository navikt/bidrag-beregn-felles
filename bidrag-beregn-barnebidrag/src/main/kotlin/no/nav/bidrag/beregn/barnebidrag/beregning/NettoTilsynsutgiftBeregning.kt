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
internal data class SkattefradragBeregningResultat(
    val antallBarnBeregnet: Int,
    val skattefradrag: BigDecimal,
    val skattefradragPerBarn: BigDecimal,
    val skattefradragTotalTilsynsutgift: BigDecimal = BigDecimal.ZERO,
    val skattefradragMaksfradrag: BigDecimal = BigDecimal.ZERO,
)
internal object NettoTilsynsutgiftBeregning {
    // Beregner netto tilsynsutgift for alle barn  med faktiske utgifter. Hvis samlede faktiske utgifter er større enn
    // sjablon maks tilsynsbeløp, skal beløpene justeres forholdsmessig.

    fun beregn(grunnlag: NettoTilsynsutgiftBeregningGrunnlag): NettoTilsynsutgiftBeregningResultat {
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

        val skattefradragResultat = if (totalTilsynsutgift > BigDecimal.ZERO) {
            beregnFradragsbeløpPerBarn(
                antallBarnBeregnet = grunnlag.antallBarnBMBeregnet,
                totalTilsynsutgift = minOf(totalTilsynsutgift, sjablonMaksTilsynsutgift),
                sjablonSkattesatsAlminneligInntektProsent = sjablonSkattAlminneligInntektProsent.verdi.toBigDecimal(),
                sjablonMaksFradragsbeløp = sjablonMaksFradragsbeløp,
            )
        } else {
            SkattefradragBeregningResultat(
                skattefradrag = BigDecimal.ZERO,
                skattefradragPerBarn = BigDecimal.ZERO,
                antallBarnBeregnet = grunnlag.antallBarnBMBeregnet,
            )
        }

        val tilsynsutgiftBarnListe = mutableListOf<TilsynsutgiftBarn>()

        var justertBruttoTilsynsutgiftBeløp = BigDecimal.ZERO
        var andelTilsynsutgiftFaktor: BigDecimal
        var justertBruttoTilsynsutgifterBeløpSøknadsbarn = BigDecimal.ZERO
        var bruttoTilsynsutgifterBeløpSøknadsbarn = BigDecimal.ZERO
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

                justertBruttoTilsynsutgiftBeløp = if (totalTilsynsutgift > sjablonMaksTilsynsutgift) {
                    andelTilsynsutgiftFaktor * sjablonMaksTilsynsutgift
                } else {
                    utgifter
                }

                tilsynsutgiftBarnListe.add(
                    TilsynsutgiftBarn(
                        gjelderBarn = gjelderBarn,
                        sumTilsynsutgifter = utgifter.avrundetMedToDesimaler,
                        endeligSumTilsynsutgifter = justertBruttoTilsynsutgiftBeløp.avrundetMedToDesimaler,
                    ),
                )

                if (gjelderBarn == grunnlag.søknadsbarnReferanse) {
                    bruttoTilsynsutgifterBeløpSøknadsbarn = utgifter.avrundetMedToDesimaler
                    justertBruttoTilsynsutgifterBeløpSøknadsbarn = justertBruttoTilsynsutgiftBeløp
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
            erBegrensetAvMaksTilsyn = totalTilsynsutgift == sjablonMaksTilsynsutgift,
            totalTilsynsutgift = totalTilsynsutgift.avrundetMedToDesimaler,
            sjablonMaksTilsynsutgift = sjablonMaksTilsynsutgift,
            bruttoTilsynsutgift = bruttoTilsynsutgifterBeløpSøknadsbarn.avrundetMedToDesimaler,
            justertBruttoTilsynsutgift = justertBruttoTilsynsutgifterBeløpSøknadsbarn.avrundetMedToDesimaler,
            andelTilsynsutgiftFaktor = andelTilsynsutgifterFaktorSøknadsbarn,
            skattefradrag = skattefradragResultat.skattefradrag.avrundetMedToDesimaler,
            skattefradragPerBarn = skattefradragResultat.skattefradragPerBarn.avrundetMedToDesimaler,
            skattefradragTotalTilsynsutgift = skattefradragResultat.skattefradragTotalTilsynsutgift.avrundetMedToDesimaler,
            skattefradragMaksfradrag = skattefradragResultat.skattefradragMaksfradrag.avrundetMedToDesimaler,
            nettoTilsynsutgift = (justertBruttoTilsynsutgifterBeløpSøknadsbarn - skattefradragResultat.skattefradragPerBarn).avrundetMedToDesimaler
                .coerceAtLeast(BigDecimal.ZERO),
            tilsynsutgiftBarnListe = tilsynsutgiftBarnListe,
            antallBarnBMBeregnet = skattefradragResultat.antallBarnBeregnet,
            antallBarnBMUnderTolvÅr = grunnlag.barnBMListeUnderTolvÅr.size,
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
        antallBarnBeregnet: Int,
        totalTilsynsutgift: BigDecimal,
        sjablonSkattesatsAlminneligInntektProsent: BigDecimal,
        sjablonMaksFradragsbeløp: BigDecimal,
    ): SkattefradragBeregningResultat {
        val skattesatsFaktor = sjablonSkattesatsAlminneligInntektProsent.divide(BigDecimal(100)).avrundetMedTiDesimaler
        val maksFradragsbeløp = sjablonMaksFradragsbeløp * skattesatsFaktor
        val skattefradragTotalTilsynsutgift = totalTilsynsutgift.multiply(skattesatsFaktor, MathContext(10, RoundingMode.HALF_UP))
        val skattefradrag = minOf(skattefradragTotalTilsynsutgift, maksFradragsbeløp)

        val skattefradragPerBarn = skattefradrag.divide(antallBarnBeregnet.toBigDecimal(), MathContext(10, RoundingMode.HALF_UP))
        return SkattefradragBeregningResultat(
            skattefradrag = skattefradrag,
            skattefradragPerBarn = skattefradragPerBarn,
            skattefradragMaksfradrag = maksFradragsbeløp,
            skattefradragTotalTilsynsutgift = skattefradragTotalTilsynsutgift,
            antallBarnBeregnet = antallBarnBeregnet,
        )
    }
}
