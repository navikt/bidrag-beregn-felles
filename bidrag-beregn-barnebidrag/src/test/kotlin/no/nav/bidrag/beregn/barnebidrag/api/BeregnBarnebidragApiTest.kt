package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
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
internal class BeregnBarnebidragApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Test av samværsfradrag - eksempel 1")
    fun testSamværsfradrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/samværsfradrag_eksempel1.json"
        utførBeregningerOgEvaluerResultatSamværsfradrag()
    }

    @Test
    @DisplayName("Test av bidragsevne - eksempel 1")
    fun testBidragsevne_Eksempel01() {
        filnavn = "src/test/resources/testfiler/bidragsevne_eksempel1.json"
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Test av bidragsevne - eksempel 2")
    fun testBidragsevne_Eksempel02() {
        filnavn = "src/test/resources/testfiler/bidragsevne_eksempel2.json"
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    private fun utførBeregningerOgEvaluerResultatSamværsfradrag() {
        val request = lesFilOgByggRequest(filnavn)
        val samværsfradragResultat = beregnBarnebidragService.beregnSamværsfradrag(request)
        printJson(samværsfradragResultat)

        val objectMapper = ObjectMapper()
        val alleReferanser = hentAlleReferanser(samværsfradragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(samværsfradragResultat)

        val delberegningSamværsfradragListe = samværsfradragResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG }
        val samværsfradragResultatListe = mutableListOf<DelberegningSamværsfradrag>()
        for (delberegning in delberegningSamværsfradragListe) {
            samværsfradragResultatListe.add(objectMapper.treeToValue(delberegning.innhold, DelberegningSamværsfradrag::class.java))
        }

        assertAll(
            { assertThat(samværsfradragResultat).isNotNull },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(6) },

            // Delberegning Samværsfradrag
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2021-05", "2021-07")) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(353)) },
            { assertThat(samværsfradragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2021-07", "2022-07")) },
            { assertThat(samværsfradragResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(354)) },
            { assertThat(samværsfradragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2022-07", "2023-01")) },
            { assertThat(samværsfradragResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(365)) },
            { assertThat(samværsfradragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-07")) },
            { assertThat(samværsfradragResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(1209)) },
            { assertThat(samværsfradragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(samværsfradragResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(1760)) },
            { assertThat(samværsfradragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-10")) },
            { assertThat(samværsfradragResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(1813)) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatBidragsevne() {
        val request = lesFilOgByggRequest(filnavn)
        val bidragsevneResultat = beregnBarnebidragService.beregnBidragsevne(request)
        printJson(bidragsevneResultat)

        val objectMapper = ObjectMapper()
        val alleReferanser = hentAlleReferanser(bidragsevneResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bidragsevneResultat)

        val delberegningBidragsevneListe = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
        val bidragsevneResultatListe = mutableListOf<DelberegningBidragsevne>()
        for (delberegning in delberegningBidragsevneListe) {
            bidragsevneResultatListe.add(objectMapper.treeToValue(delberegning.innhold, DelberegningBidragsevne::class.java))
        }

        assertAll(
            { assertThat(bidragsevneResultat).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
//            { assertThat(bidragsevneResultatListe).hasSize(6) },

            // Delberegning Bidragsevne
//            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2021-05", "2021-07")) },
//            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(353)) },
//            { assertThat(bidragsevneResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2021-07", "2022-07")) },
//            { assertThat(bidragsevneResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(354)) },
//            { assertThat(bidragsevneResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2022-07", "2023-01")) },
//            { assertThat(bidragsevneResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(365)) },
//            { assertThat(bidragsevneResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-07")) },
//            { assertThat(bidragsevneResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(1209)) },
//            { assertThat(bidragsevneResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
//            { assertThat(bidragsevneResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(1760)) },
//            { assertThat(bidragsevneResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-10")) },
//            { assertThat(bidragsevneResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(1813)) },

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
