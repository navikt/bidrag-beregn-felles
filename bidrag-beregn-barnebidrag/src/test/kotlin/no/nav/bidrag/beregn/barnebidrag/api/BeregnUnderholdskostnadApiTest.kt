package no.nav.bidrag.beregn.barnebidrag.api

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

@ExtendWith(MockitoExtension::class)
internal class BeregnUnderholdskostnadApiTest: FellesApiTest() {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
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
            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
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

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertEquals(0, resultat[3].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[3].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertThat(resultat[3].barnetilsynMedStønad).isNull() },
            { assertEquals(0, resultat[3].nettoTilsynsutgift?.compareTo(BigDecimal.valueOf(1211.18))) },
            { assertEquals(0, resultat[3].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[3].underholdskostnad.compareTo(BigDecimal.valueOf(9682.18))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - fødselsmåned  ")
    fun test_underholdskostnad_kun_sjabloner_ikke_barnetrygd_fødselsmåned() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_kun_sjabloner_fødselsmåned.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(3) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.ZERO)) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1766))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[2].barnetrygd.compareTo(BigDecimal.valueOf(1766))) },
        )
    }

    @Test
    @DisplayName("Underholdskostnad - ingen forhøyet barnetrygd før juli 2021  ")
    fun test_underholdskostnad_kun_sjabloner_ikke_forhøyet_barnetrygd_før_juli_2021() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_kun_sjabloner_ingen_forhøyet_barnetrygd_før_juli_2021.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(2) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2021-01", "2021-07")) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1054))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2021-07"), null)) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1354))) },

        )
    }

    @Test
    @DisplayName("Underholdskostnad - ingen forhøyet barnetrygd etter juli året barnet fyller seks år ")
    fun test_underholdskostnad_kun_sjabloner_ikke_forhøyet_barnetrygd_etter_fyllte_seks_år() {
        filnavn = "src/test/resources/testfiler/underholdskostnad/underholdskostnad_kun_sjabloner_seksårsdag.json"
        val resultat = utførBeregningerOgEvaluerResultatUnderholdskostnad()

        assertAll(
            // Resultat
            { assertThat(resultat).hasSize(2) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-07")) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1766))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },

        )
    }

    private fun utførBeregningerOgEvaluerResultatUnderholdskostnad(): List<DelberegningUnderholdskostnad> {
        val request = lesFilOgByggRequest(filnavn)
        val underholdskostnadResultat = api.beregnUnderholdskostnad(request)
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
