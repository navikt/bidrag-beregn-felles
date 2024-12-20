package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnNettoTilsynsutgiftApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Netto tilsynsutgift - eksempel 1  ")
    fun testNettoTilsynsutgift_Eksempel01() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_eksempel1.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1.0))) },
            { assertEquals(0, resultat[0].skattefradrag.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(275.55))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].totalTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1.0))) },
            { assertEquals(0, resultat[1].skattefradrag.compareTo(BigDecimal.valueOf(41.76))) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(126.29))) },

            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[2].totalTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[2].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[2].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1.0))) },
            { assertEquals(0, resultat[2].skattefradrag.compareTo(BigDecimal.valueOf(41.76))) },
            { assertEquals(0, resultat[2].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(126.29))) },

            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
        )
    }

    @Test
    @DisplayName("Netto tilsynsutgift - eksempel 2  ")
    fun testNettoTilsynsutgift_Eksempel02_flere_barn_og_perioder() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_eksempel_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(5) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1.0))) },
            { assertEquals(0, resultat[0].skattefradragPerBarn.compareTo(BigDecimal.valueOf(45.56))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(321.11))) },

            { assertThat(resultat[0].tilsynsutgiftBarnListe).hasSize(1) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-04")) },
            { assertEquals(0, resultat[1].totalTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1.0))) },
            { assertEquals(0, resultat[1].skattefradragPerBarn.compareTo(BigDecimal.valueOf(20.88))) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(147.17))) },

            { assertThat(resultat[1].tilsynsutgiftBarnListe).hasSize(1) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-04", "2024-05")) },
            { assertEquals(0, resultat[2].totalTilsynsutgift.compareTo(BigDecimal.valueOf(8906))) },
            { assertEquals(0, resultat[2].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(160.34))) },
            { assertEquals(0, resultat[2].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(0.01800327330))) },
            { assertEquals(0, resultat[2].skattefradragPerBarn.compareTo(BigDecimal.valueOf(414.13))) },
            { assertEquals(0, resultat[2].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(0))) },

            { assertThat(resultat[2].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(160.34))) },

            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(9166.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(8745.66))) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(resultat[3].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[3].totalTilsynsutgift.compareTo(BigDecimal.valueOf(8906))) },
            { assertEquals(0, resultat[3].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(160.34))) },
            { assertEquals(0, resultat[3].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(0.01800327330))) },
            { assertEquals(0, resultat[3].skattefradragPerBarn.compareTo(BigDecimal.valueOf(414.13))) },
            { assertEquals(0, resultat[3].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(0))) },

            { assertThat(resultat[2].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(160.34))) },

            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(9166.67))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(8745.66))) },

            { assertThat(resultat[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[4].totalTilsynsutgift.compareTo(BigDecimal.valueOf(9334.72))) },
            { assertEquals(0, resultat[4].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[4].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(0.01800327330))) },
            { assertEquals(0, resultat[4].skattefradragPerBarn.compareTo(BigDecimal.valueOf(414.13))) },
            { assertEquals(0, resultat[4].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(0))) },

            { assertThat(resultat[2].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(168.06))) },

            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(9166.67))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(9166.67))) },

        )
    }

    @Test
    @DisplayName("Netto tilsynsutgift - eksempel 3 - totalt beløp med faktiske utgifter høyere enn sjablon for maks tilsyn ")
    fun testNettoTilsynsutgift_Eksempel03_totalt_over_maks_tilsyn() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_eksempel_over_sjablon_maks_tilsyn_flere_barn.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(1) },

            // Resultat

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(resultat[0].tilsynsutgiftBarnListe).hasSize(3) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.valueOf(10742))) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(1625.73))) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(0.1513437058))) },
            { assertEquals(0, resultat[0].skattefradragPerBarn.compareTo(BigDecimal.valueOf(379.63))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1246.11))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(1634.72))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(1625.73))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(3666.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(3646.51))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(5500))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(5469.76))) },

        )
    }

    @Test
    @DisplayName("Netto tilsynsutgift - flere faktiske utgifter for barn ")
    fun testNettoTilsynsutgift_flere_utgifter_for_samme_barn() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_flere_faktiske_utgifter_for_ett_barn.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1))) },
            { assertEquals(0, resultat[0].skattefradrag.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(275.55))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(366.67))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].totalTilsynsutgift.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[1].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[1].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1))) },
            { assertEquals(0, resultat[1].skattefradrag.compareTo(BigDecimal.valueOf(383.45))) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1159.61))) },

            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(1543.06))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[2].totalTilsynsutgift.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[2].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[2].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1))) },
            { assertEquals(0, resultat[2].skattefradrag.compareTo(BigDecimal.valueOf(383.45))) },
            { assertEquals(0, resultat[2].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1159.61))) },

            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(1543.06))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(1543.06))) },
        )
    }

    @Test
    @DisplayName("Netto tilsynsutgift - eksempel 2 test utregning kun faktiske utgifter ")
    fun testNettoTilsynsutgift_Eksempel02() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_eksempel2_kun_faktiske_utgifter.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(1) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.valueOf(1))) },
            { assertEquals(0, resultat[0].skattefradrag.compareTo(BigDecimal.valueOf(517.71))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(2736.46))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.valueOf(3254.17))) },
        )
    }

    @Test
    @DisplayName("Netto tilsynsutgift - eksempel 3 test utregning kun tilleggsstønad ")
    fun testNettoTilsynsutgift_Eksempel03() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_eksempel3_utregning_tilleggsstønad.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        assertAll(
            { assertThat(resultat).hasSize(1) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[0].totalTilsynsutgift.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].justertBruttoTilsynsutgift.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].andelTilsynsutgiftFaktor.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].skattefradrag.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumTilsynsutgifter.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumTilsynsutgifter.compareTo(BigDecimal.ZERO)) },
        )
    }

    @Test
    @DisplayName("Beregn månedsbeløp faktisk utgift og tilleggsstønad")
    fun testBeregnMånedsbeløpFaktiskUtgiftTilleggsstønad() {
        val faktiskUtgift = BigDecimal.valueOf(1000)
        val kostpenger = BigDecimal.valueOf(400)
        val responseFaktiskUtgift = api.beregnMånedsbeløpFaktiskeUtgifter(faktiskUtgift, kostpenger)

        val tilleggsstønad = BigDecimal.valueOf(17)
        val responseTilleggsstønad = api.beregnMånedsbeløpTilleggsstønad(tilleggsstønad)

        assertThat(responseFaktiskUtgift).isEqualByComparingTo(BigDecimal.valueOf(550))
        assertThat(responseTilleggsstønad).isEqualByComparingTo(BigDecimal.valueOf(368.33))

        // Test uten angitt kostpenger, default er BigDecimal.ZERO
        val faktiskUtgift2 = BigDecimal.valueOf(500)
        val responseFaktiskUtgift2 = api.beregnMånedsbeløpFaktiskeUtgifter(faktiskUtgift2, BigDecimal.ZERO)
        assertThat(responseFaktiskUtgift2).isEqualByComparingTo(BigDecimal.valueOf(458.33))
    }

    @Test
    @DisplayName("Test at antall barn under 12 år ikke tar med barn født før beregning start, skal lage brudd på faktisk fødemåned for disse")
    fun testBortfiltreringAvBarnSomIkkeErFødtFørBeregningStart() {
        filnavn = "src/test/resources/testfiler/nettotilsynsutgift/nettotilsynsutgift_barnoverogundertolvår.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgift()

        // Bms barn nummer to er født 2024-10-17. Det skal lages bruddperiode fra oktober -24 og antall barn under tolv år skal da gå fra 1 til 2.
        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-01"), YearMonth.parse("2024-07"))) },
            { assertThat(resultat[0].antallBarnBMUnderTolvÅr).isEqualTo(1) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2024-10"))) },
            { assertThat(resultat[1].antallBarnBMUnderTolvÅr).isEqualTo(1) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-10"), null)) },
            { assertThat(resultat[2].antallBarnBMUnderTolvÅr).isEqualTo(2) },

        )
    }

    private fun utførBeregningerOgEvaluerResultatNettoTilsynsutgift(): List<DelberegningNettoTilsynsutgift> {
        val request = lesFilOgByggRequest(filnavn)
        val nettoTilsynsutgiftResultat = api.beregnNettoTilsynsutgift(request)
        printJson(nettoTilsynsutgiftResultat)

        val alleReferanser = hentAlleReferanser(nettoTilsynsutgiftResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(nettoTilsynsutgiftResultat)

        val nettoTilsynsutgiftResultatListe = nettoTilsynsutgiftResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningNettoTilsynsutgift>(Grunnlagstype.DELBEREGNING_NETTO_TILSYNSUTGIFT)
            .map {
                DelberegningNettoTilsynsutgift(
                    periode = it.innhold.periode,
                    totalTilsynsutgift = it.innhold.totalTilsynsutgift,
                    justertBruttoTilsynsutgift = it.innhold.justertBruttoTilsynsutgift,
                    andelTilsynsutgiftFaktor = it.innhold.andelTilsynsutgiftFaktor,
                    skattefradrag = it.innhold.skattefradrag,
                    nettoTilsynsutgift = it.innhold.nettoTilsynsutgift,
                    tilsynsutgiftBarnListe = it.innhold.tilsynsutgiftBarnListe,
                    antallBarnBMUnderTolvÅr = it.innhold.antallBarnBMUnderTolvÅr,
                    skattefradragPerBarn = it.innhold.skattefradragPerBarn,
                    skattefradragMaksfradrag = it.innhold.skattefradragMaksfradrag,
                    skattefradragTotalTilsynsutgift = it.innhold.skattefradragTotalTilsynsutgift,
                    erBegrensetAvMaksTilsyn = it.innhold.erBegrensetAvMaksTilsyn,
                    bruttoTilsynsutgift = it.innhold.bruttoTilsynsutgift,
                )
            }

        assertAll(
            { assertThat(nettoTilsynsutgiftResultat).isNotNull },
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return nettoTilsynsutgiftResultatListe
    }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

    private fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
