package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
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
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(275.55))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(58.88))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[2].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(58.88))) },
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },
            { assertThat(resultat[0].tilsynsutgiftBarnListe).hasSize(1) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(275.55))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-04")) },
            { assertThat(resultat[1].tilsynsutgiftBarnListe).hasSize(1) },
            { assertEquals(0, resultat[1].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },

            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(58.88))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-04", "2024-05")) },
            { assertThat(resultat[2].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[2].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(870.83))) },

            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(41.80))) },

            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].tilleggsstønad.compareTo(BigDecimal.valueOf(0))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(395.97))) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(resultat[3].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[3].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(870.83))) },

            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(41.80))) },

            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].tilleggsstønad.compareTo(BigDecimal.valueOf(368.33))) },
            { assertEquals(0, resultat[3].tilsynsutgiftBarnListe[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(27.63))) },

            { assertThat(resultat[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(resultat[4].tilsynsutgiftBarnListe).hasSize(2) },
            { assertEquals(0, resultat[4].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(870.83))) },

            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(41.80))) },

            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(504.17))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(108.20))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].tilleggsstønad.compareTo(BigDecimal.valueOf(368.33))) },
            { assertEquals(0, resultat[4].tilsynsutgiftBarnListe[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(27.63))) },
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(11000))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1833.33))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1790.33))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(379.63))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1194.04))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(3666.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(3580.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(379.63))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].tilleggsstønad.compareTo(BigDecimal.valueOf(368.33))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[1].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(2832.71))) },

            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(5500))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(5371))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(379.63))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].tilleggsstønad.compareTo(BigDecimal.valueOf(0))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[2].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(4991.37))) },
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(366.67))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(91.12))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(275.55))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(432.80))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[1].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1092.20))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[2].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(1741.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].skattefradragsbeløpPerBarn.compareTo(BigDecimal.valueOf(432.80))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(216.67))) },
            { assertEquals(0, resultat[2].tilsynsutgiftBarnListe[0].nettoTilsynsutgift.compareTo(BigDecimal.valueOf(1092.20))) },
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.valueOf(3254.17))) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.ZERO)) },
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
            { assertEquals(0, resultat[0].totaltFaktiskUtgiftBeløp.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].sumFaktiskeUtgifter.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].endeligSumFaktiskeUtgifter.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].tilsynsutgiftBarnListe[0].tilleggsstønad.compareTo(BigDecimal.valueOf(585))) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatNettoTilsynsutgift(): List<DelberegningNettoTilsynsutgift> {
        val request = lesFilOgByggRequest(filnavn)
        val nettoTilsynsutgiftResultat = beregnBarnebidragService.beregnNettoTilsynsutgift(request)
        printJson(nettoTilsynsutgiftResultat)

        val alleReferanser = hentAlleReferanser(nettoTilsynsutgiftResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(nettoTilsynsutgiftResultat)

        val nettoTilsynsutgiftResultatListe = nettoTilsynsutgiftResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningNettoTilsynsutgift>(Grunnlagstype.DELBEREGNING_NETTO_TILSYNSUTGIFT)
            .map {
                DelberegningNettoTilsynsutgift(
                    periode = it.innhold.periode,
                    totaltFaktiskUtgiftBeløp = it.innhold.totaltFaktiskUtgiftBeløp,
                    tilsynsutgiftBarnListe = it.innhold.tilsynsutgiftBarnListe,
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
