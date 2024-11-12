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
            { assertThat(resultat).hasSize(5) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-07")) },
            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            { assertThat(resultat).hasSize(2) },

            { assertEquals(0, resultat[0].forbruksutgift.compareTo(BigDecimal.valueOf(6335))) },
            { assertEquals(0, resultat[0].boutgift.compareTo(BigDecimal.valueOf(3198))) },
            { assertEquals(0, resultat[0].barnetilsynMedStønad.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].nettoTilsynsutgift.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[0].barnetrygd.compareTo(BigDecimal.valueOf(1310))) },
            { assertEquals(0, resultat[0].underholdskostnad.compareTo(BigDecimal.valueOf(8223))) },

            { assertEquals(0, resultat[1].forbruksutgift.compareTo(BigDecimal.valueOf(6385))) },
            { assertEquals(0, resultat[1].boutgift.compareTo(BigDecimal.valueOf(3596))) },
            { assertEquals(0, resultat[1].barnetilsynMedStønad.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[1].nettoTilsynsutgift.compareTo(BigDecimal.ZERO)) },
            { assertEquals(0, resultat[1].barnetrygd.compareTo(BigDecimal.valueOf(1510))) },
            { assertEquals(0, resultat[1].underholdskostnad.compareTo(BigDecimal.valueOf(8471))) },

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
