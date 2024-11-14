package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
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

@ExtendWith(MockitoExtension::class)
internal class BeregnUnderholdskostnadApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 1  ")
    fun test_underholdskostnad_kun_sjabloner_flere_perioder() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_kun_sjabloner_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        // Boutgifter 01.24 -> 07.24: 3198
        // Boutgifter 07.24 -> 09.24: 3596
        // Forbruksutgifter 01.24 -> 07.24: 6335
        // Forbruksutgifter 07.24 -> 09.24: 6385
        // Barnetrygd 01.24 -> 07.24: 1310
        // Barnetrygd 07.24 -> 09.24: 1510

        assertAll(
            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-07")) },
            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            { assertThat(resultat).hasSize(2) },

            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[0].barnetilsynMedStønad).isNull() },
            { assertThat(resultat[0].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(8223))) },

            { assertEquals(0, resultat[1].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[1].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[0].barnetilsynMedStønad).isNull() },
            { assertThat(resultat[0].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[1].underholdskostnad.compareTo(BigDecimal.valueOf(8471))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 2  ")
    fun test_underholdskostnad_med_barnetilsyn_flere_perioder() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_med_barnetilsyn_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        // Boutgifter 01.24 -> 07.24: 3198
        // Boutgifter 07.24 -> 09.24: 3596
        // Forbruksutgifter 01.24 -> 07.24: 6335
        // Forbruksutgifter 07.24 -> 09.24: 6385
        // Barnetrygd 01.24 -> 07.24: 1310
        // Barnetrygd 07.24 -> 09.24: 1510
        // Barnetilsyn 02.24 -> 07.24: HELTID/UNDER: 630
        // Barnetilsyn 07.24 -> 08.24: HELTID/UNDER: 621

        assertAll(
            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-08")) },
            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(resultat).hasSize(4) },

            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[0].barnetilsynMedStønad).isNull() },
            { assertThat(resultat[0].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(8223))) },

            { assertEquals(0, resultat[1].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[1].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[1].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(630))) },
            { assertThat(resultat[1].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[1].underholdskostnad.compareTo(BigDecimal.valueOf(8853))) },

            { assertEquals(0, resultat[2].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[2].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[2].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(621))) },
            { assertThat(resultat[2].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[2].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[2].underholdskostnad.compareTo(BigDecimal.valueOf(9092))) },

            { assertEquals(0, resultat[3].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[3].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[3].barnetilsynMedStønad).isNull() },
            { assertThat(resultat[3].nettoTilsynsutgift).isNull() },
            { assertEquals(0, resultat[3].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[3].underholdskostnad.compareTo(BigDecimal.valueOf(8471))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 3  ")
    fun test_underholdskostnad_med_barnetilsyn_og_netto_tilsynsutgift_flere_perioder() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_med_barnetilsyn_og_netto_tilsynsutgift_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        // Boutgifter 01.24 -> 07.24: 3198
        // Boutgifter 07.24 -> 09.24: 3596
        // Forbruksutgifter 01.24 -> 07.24: 6335
        // Forbruksutgifter 07.24 -> 09.24: 6385
        // Barnetrygd 01.24 -> 07.24: 1310
        // Barnetrygd 07.24 -> 09.24: 1510
        // Barnetilsyn 02.24 -> 07.24: HELTID/UNDER: 630
        // Barnetilsyn 07.24 -> 08.24: HELTID/OVER: 686
        // Netto tilsynsutgift 01.24 -> 02.24: 300.60
        // Netto tilsynsutgift 02.24 -> 09.24: 1211.18

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(4) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[0].barnetilsynMedStønad).isNull() },
            { assertEquals(0, resultat[0].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(300.60))) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(8523.60))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[1].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[1].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(630))) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(1211.18))) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[1].underholdskostnad.compareTo(BigDecimal.valueOf(10064.18))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-08")) },
            { assertEquals(0, resultat[2].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[2].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[2].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(686))) },
            { assertEquals(0, resultat[2].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(1211.18))) },
            { assertEquals(0, resultat[2].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[2].underholdskostnad.compareTo(BigDecimal.valueOf(10368.18))) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertEquals(0, resultat[3].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[3].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[3].barnetilsynMedStønad).isNull() },
            { assertEquals(0, resultat[3].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(1211.18))) },
            { assertEquals(0, resultat[3].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[3].underholdskostnad.compareTo(BigDecimal.valueOf(9682.18))) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatUnderholdskostnad(): List<DelberegningUnderholdskostnad> {
        val request = lesFilOgByggRequest(filnavn)
        val underholdskostnadResultat = beregnBarnebidragService.beregnUnderholdskostnad(request)
        printJson(underholdskostnadResultat)

        val alleReferanser = hentAlleReferanser(underholdskostnadResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(underholdskostnadResultat)

        val underholdskostnadResultatResultatListe = underholdskostnadResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
            .map {
                DelberegningUnderholdskostnad(
                    periode = it.innhold.periode,
                    forbruksutgift = it.innhold.forbruksutgift,
                    boutgift = it.innhold.boutgift,
                    barnetilsynMedStønad = it.innhold.barnetilsynMedStønad,
                    nettoTilsynsutgift = it.innhold.nettoTilsynsutgift,
                    barnetrygd = it.innhold.barnetrygd,
                    underholdskostnad = it.innhold.underholdskostnad,
                )
            }

        assertAll(
            { assertThat(underholdskostnadResultat).isNotNull },
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return underholdskostnadResultatResultatListe
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
