package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
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
internal class BeregnSamværsfradragApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel 1 - smaværsklasse mangler")
    fun testSamværsfradrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel1.json"
        val request = lesFilOgByggRequest(filnavn)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            beregnBarnebidragService.beregnSamværsfradrag(request)
        }
        assertThat(exception.message).contains("Ingen samværsklasse funnet")
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel med flere perioder")
    fun testSamværsfradrag_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatSamværsfradrag()
    }

    private fun utførBeregningerOgEvaluerResultatSamværsfradrag() {
        val request = lesFilOgByggRequest(filnavn)
        val samværsfradragResultat = beregnBarnebidragService.beregnSamværsfradrag(request)
        printJson(samværsfradragResultat)

        val alleReferanser = hentAlleReferanser(samværsfradragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(samværsfradragResultat)

        val samværsfradragResultatListe = samværsfradragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
            .map {
                DelberegningSamværsfradrag(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }

        val antallSamværsklasse = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SAMVÆRSKLASSE }
            .size

        val antallSjablon = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SJABLON }
            .size

        assertAll(
            { assertThat(samværsfradragResultat).isNotNull },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(6) },

            // Resultat
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2021-05", "2021-07")) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(353.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2021-07", "2022-07")) },
            { assertThat(samværsfradragResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(354.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2022-07", "2023-01")) },
            { assertThat(samværsfradragResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(365.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-07")) },
            { assertThat(samværsfradragResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(1209.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(samværsfradragResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(1760.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-10")) },
            { assertThat(samværsfradragResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(1813.00).setScale(2)) },

            // Grunnlag
            { assertThat(antallSamværsklasse).isEqualTo(2) },
            { assertThat(antallSjablon).isEqualTo(6) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
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
