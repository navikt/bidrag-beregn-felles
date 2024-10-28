package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.bo.DelberegningNettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

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
        utførBeregningerOgEvaluerResultatNettoTilsynsutgift()
    }

//    @Test
//    @DisplayName("Samværsfradrag - eksempel med flere perioder")
//    fun testSamværsfradrag_Eksempel_Flere_Perioder() {
//        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel_flere_perioder.json"
//        utførBeregningerOgEvaluerResultatNettoTilsynsutgift()
//    }

    private fun utførBeregningerOgEvaluerResultatNettoTilsynsutgift() {
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
                    nettoTilsynsutgiftBeløp = it.innhold.nettoTilsynsutgiftBeløp,
                    samletFaktiskUtgiftBeløp = it.innhold.samletFaktiskUtgiftBeløp,
                    samletTilleggstønadBeløp = it.innhold.samletTilleggstønadBeløp,
                    skattefradragsbeløpPerBarn = it.innhold.skattefradragsbeløpPerBarn,
                    bruttoTilsynsutgiftBarnListe = it.innhold.bruttoTilsynsutgiftBarnListe,
                )
            }

//        val antallSamværsklasse = nettoTilsynsutgiftResultat
//            .filter { it.type == Grunnlagstype.SAMVÆRSKLASSE }
//            .size
//
//        val antallSjablon = nettoTilsynsutgiftResultat
//            .filter { it.type == Grunnlagstype.SJABLON }
//            .size

        assertAll(
            { assertThat(nettoTilsynsutgiftResultat).isNotNull },
            { assertThat(nettoTilsynsutgiftResultatListe).isNotNull },
//            { assertThat(nettoTilsynsutgiftResultatListe).hasSize(6) },

            // Resultat
            { assertThat(nettoTilsynsutgiftResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-02")) },
//            { assertThat(nettoTilsynsutgiftResultatListe[0].nettoTilsynsutgiftBeløp.avrundetMedNullDesimaler).isEqualTo(BigDecimal.valueOf(400)) },

            { assertThat(nettoTilsynsutgiftResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-07")) },
//            { assertThat(nettoTilsynsutgiftResultatListe[1].nettoTilsynsutgiftBeløp.avrundetMedNullDesimaler).isEqualTo(BigDecimal.valueOf(300)) },

            { assertThat(nettoTilsynsutgiftResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
//            { assertThat(nettoTilsynsutgiftResultatListe[2].nettoTilsynsutgiftBeløp.avrundetMedNullDesimaler).isEqualTo(BigDecimal.valueOf(300)) },

            // Grunnlag
//            { assertThat(antallSamværsklasse).isEqualTo(2) },
//            { assertThat(antallSjablon).isEqualTo(6) },

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
