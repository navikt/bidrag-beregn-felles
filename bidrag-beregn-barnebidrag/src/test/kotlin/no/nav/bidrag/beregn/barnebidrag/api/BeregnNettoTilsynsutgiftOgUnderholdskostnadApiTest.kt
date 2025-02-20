package no.nav.bidrag.beregn.barnebidrag.api

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
internal class BeregnNettoTilsynsutgiftOgUnderholdskostnadApiTest : FellesApiTest() {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 1")
    fun test_underholdskostnad_kun_sjabloner_flere_perioder() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_kun_sjabloner_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

        // Boutgifter 01.24 -> 07.24: 3198
        // Boutgifter 07.24 -> 09.24: 3596
        // Forbruksutgifter 01.24 -> 07.24: 6335
        // Forbruksutgifter 07.24 -> 09.24: 6385
        // Barnetrygd 01.24 -> 07.24: 1310
        // Barnetrygd 07.24 -> 09.24: 1510

        assertAll(
            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-07")) },
            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
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
    @DisplayName("Underholdskostnad - eksempel 2")
    fun test_netto_tilsynsugift_og_underholdskostnad_med_barnetilsyn_flere_perioder() {
        filnavn = "src/test/resources/testfiler/nettobarnetilsynogunderholdskostnad/nettotilsynsutgift_og_underholdskostnad_flere_perioder.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

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
            { assertThat(resultat).hasSize(6) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(7776))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[0].barnetilsynMedStønad).isNull() },
            { assertEquals(0, resultat[0].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(275.55))) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(9939.55))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-04")) },
            { assertEquals(0, resultat[1].forbruksutgift.compareTo(BigDecimal.valueOf(7776))) },
            { assertEquals(0, resultat[1].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[1].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(630))) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(126.29))) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[1].underholdskostnad.compareTo(BigDecimal.valueOf(10420.29))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-04", "2024-05")) },
            { assertEquals(0, resultat[2].forbruksutgift.compareTo(BigDecimal.valueOf(7776))) },
            { assertEquals(0, resultat[2].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[2].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(630))) },
            { assertEquals(0, resultat[2].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(84.53))) },
            { assertEquals(0, resultat[2].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[2].underholdskostnad.compareTo(BigDecimal.valueOf(10378.53))) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertEquals(0, resultat[3].forbruksutgift.compareTo(BigDecimal.valueOf(7776))) },
            { assertEquals(0, resultat[3].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertThat(resultat[3].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(630))) },
            { assertEquals(0, resultat[3].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(84.53))) },
            { assertEquals(0, resultat[3].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[3].underholdskostnad.compareTo(BigDecimal.valueOf(10378.53))) },

            { assertThat(resultat[4].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-08")) },
            { assertEquals(0, resultat[4].forbruksutgift.compareTo(BigDecimal.valueOf(7587))) },
            { assertEquals(0, resultat[4].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[4].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(686))) },
            { assertEquals(0, resultat[4].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(84.53))) },
            { assertEquals(0, resultat[4].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[4].underholdskostnad.compareTo(BigDecimal.valueOf(10443.53))) },

            { assertThat(resultat[5].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertEquals(0, resultat[5].forbruksutgift.compareTo(BigDecimal.valueOf(7587))) },
            { assertEquals(0, resultat[5].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[5].barnetilsynMedStønad).isNull() },
            { assertEquals(0, resultat[5].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(84.53))) },
            { assertEquals(0, resultat[5].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[5].underholdskostnad.compareTo(BigDecimal.valueOf(9757.53))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 3")
    fun test_netto_tilsynsugift_og_underholdskostnad_med_barnetilsyn_enkel_test() {
        filnavn = "src/test/resources/testfiler/nettobarnetilsynogunderholdskostnad/nettotilsynsutgift_og_underholdskostnad_enkel_test.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

        // Boutgifter 08.24 -> 09.24: 3596
        // Forbruksutgifter 08.24 -> 09.24: 6385
        // Barnetrygd 08.24 -> 09.24: 1510
        // Barnetilsyn 08.24 -> 08.24: HELTID/UNDER: 621
        // Netto tilsynsutgift 08.24 -> 09.24: 58.83

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(1) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(7587))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertEquals(0, resultat[0].barnetilsynMedStønad?.compareTo(BigDecimal.valueOf(621))) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(58.83))) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(10352.83))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 4")
    fun test_netto_tilsynsugift_og_underholdskostnad_med_full_request() {
        filnavn = "src/test/resources/testfiler/nettobarnetilsynogunderholdskostnad/nettotilsynsutgift_og_underholdskostnad_full_request.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

        assertAll(
            // Resultat

            { resultat[0].nettoTilsynsutgift?.shouldBeGreaterThanOrEqualTo(BigDecimal.ZERO) },
            { resultat[0].underholdskostnad shouldBeGreaterThanOrEqualTo BigDecimal.ZERO },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 5")
    fun test_netto_tilsynsugift_og_underholdskostnad_tilsynsutgift_kun_i_deler_av_perioden() {
        filnavn =
            "src/test/resources/testfiler/nettobarnetilsynogunderholdskostnad/nettotilsynsutgift_og_underholdskostnad_test_periodisering_tilsynsutgift.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

        // Netto tilsynsutgift 08.24 -> 09.24: 58.83

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(4) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-03")) },
            { assertNull(resultat[0].nettoTilsynsutgift) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-03", "2024-05")) },
            { assertNotNull(resultat[1].nettoTilsynsutgift) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertNull(resultat[2].nettoTilsynsutgift) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertNull(resultat[3].nettoTilsynsutgift) },

        )
    }

    @Test
    @DisplayName("Underholdskostnad - eksempel 5")
    fun test_netto_tilsynsugift_og_underholdskostnad_alle_barn_refereres_i_resultatet() {
        filnavn =
            "src/test/resources/testfiler/nettobarnetilsynogunderholdskostnad/nettotilsynsutgift_og_underholdskostnad_flere_barn.json"
        val resultat = utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad()

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(3) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertNotNull(resultat[0].nettoTilsynsutgift) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertNotNull(resultat[1].nettoTilsynsutgift) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertNotNull(resultat[2].nettoTilsynsutgift) },

        )
    }

    private fun utførBeregningerOgEvaluerResultatNettoTilsynsutgiftOgUnderholdskostnad(): List<DelberegningUnderholdskostnad> {
        val request = lesFilOgByggRequest(filnavn)
        val underholdskostnadResultat = api.beregnNettoTilsynsutgiftOgUnderholdskostnad(request)
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
}
