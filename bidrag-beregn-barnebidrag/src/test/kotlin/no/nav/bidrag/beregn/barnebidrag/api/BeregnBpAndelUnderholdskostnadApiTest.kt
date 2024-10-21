package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
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
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

@ExtendWith(MockitoExtension::class)
internal class BeregnBpAndelUnderholdskostnadApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetEndeligAndelFaktor: BigDecimal
    private lateinit var forventetAndelBeløp: BigDecimal
    private lateinit var forventetBeregnetAndelFaktor: BigDecimal
    private lateinit var forventetBarnEndeligInntekt: BigDecimal
    private var forventetBarnetErSelvforsørget: Boolean = false

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 1 - Barnet er selvforsørget")
    fun testBpAndelUnderholdskostnad_Eksempel01() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel1.json"
        forventetEndeligAndelFaktor = BigDecimal.ZERO
        forventetAndelBeløp = BigDecimal.ZERO
        forventetBeregnetAndelFaktor = BigDecimal.ZERO
        forventetBarnEndeligInntekt = BigDecimal.valueOf(200000)
        forventetBarnetErSelvforsørget = true
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 2 - BPs andel er høyere enn fem sjettedeler")
    fun testBpAndelUnderholdskostnad_Eksempel02() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel2.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.8333333333)
        forventetAndelBeløp = BigDecimal.valueOf(7500)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.8765010080).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900)
        forventetBarnetErSelvforsørget = false
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 3 - BPs andel er lavere enn fem sjettedeler")
    fun testBpAndelUnderholdskostnad_Eksempel03() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel3.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(7021)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900)
        forventetBarnetErSelvforsørget = false
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 4 - Barnets endelige inntekt er lavere enn 0")
    fun testBpAndelUnderholdskostnad_Eksempel04() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel4.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(5625)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO
        forventetBarnetErSelvforsørget = false
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 5 - Barnet har ikke inntekt")
    fun testBpAndelUnderholdskostnad_Eksempel05() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel5.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(5625)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO
        forventetBarnetErSelvforsørget = false
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP andel underholdskostnad - eksempel med flere perioder")
    fun testBpAndelUnderholdskostnad_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnadFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad() {
        val request = lesFilOgByggRequest(filnavn)
        val bpAndelUnderholdskostnadResultat = beregnBarnebidragService.beregnBpAndelUnderholdskostnad(request)
        printJson(bpAndelUnderholdskostnadResultat)

        val alleReferanser = hentAlleReferanser(bpAndelUnderholdskostnadResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bpAndelUnderholdskostnadResultat)

        val bpAndelUnderholdskostnadResultatListe = bpAndelUnderholdskostnadResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                DelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                    andelBeløp = it.innhold.andelBeløp,
                    beregnetAndelFaktor = it.innhold.beregnetAndelFaktor,
                    barnEndeligInntekt = it.innhold.barnEndeligInntekt,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                )
            }

        assertAll(
            { assertThat(bpAndelUnderholdskostnadResultat).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(1) },

            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor).isEqualTo(forventetEndeligAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp).isEqualTo(forventetAndelBeløp) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor).isEqualTo(forventetBeregnetAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt).isEqualTo(forventetBarnEndeligInntekt) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørget) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnadFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val bpAndelUnderholdskostnadResultat = beregnBarnebidragService.beregnBpAndelUnderholdskostnad(request)
        printJson(bpAndelUnderholdskostnadResultat)

        val alleReferanser = hentAlleReferanser(bpAndelUnderholdskostnadResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bpAndelUnderholdskostnadResultat)

        val bpAndelUnderholdskostnadResultatListe = bpAndelUnderholdskostnadResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                DelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                    andelBeløp = it.innhold.andelBeløp,
                    beregnetAndelFaktor = it.innhold.beregnetAndelFaktor,
                    barnEndeligInntekt = it.innhold.barnEndeligInntekt,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                )
            }

        assertAll(
            { assertThat(bpAndelUnderholdskostnadResultat).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(6) },

            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-09", "2023-11")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt).isEqualTo(BigDecimal.valueOf(200000)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isTrue() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-11", "2024-01")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].endeligAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].andelBeløp).isEqualTo(BigDecimal.valueOf(7500)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].beregnetAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8744316194)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].barnEndeligInntekt).isEqualTo(BigDecimal.valueOf(43600)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-05")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].endeligAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].andelBeløp).isEqualTo(BigDecimal.valueOf(7500)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].beregnetAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].barnEndeligInntekt).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].endeligAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].andelBeløp).isEqualTo(BigDecimal.valueOf(6667)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].beregnetAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].barnEndeligInntekt).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].endeligAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].andelBeløp).isEqualTo(BigDecimal.valueOf(6667)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].beregnetAndelFaktor).isEqualTo(BigDecimal.valueOf(0.8333333333)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].barnEndeligInntekt).isEqualTo(BigDecimal.ZERO) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-10")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].endeligAndelFaktor).isEqualTo(BigDecimal.valueOf(0.5866823115)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].andelBeløp).isEqualTo(BigDecimal.valueOf(4693)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].beregnetAndelFaktor).isEqualTo(BigDecimal.valueOf(0.5866823115)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].barnEndeligInntekt).isEqualTo(BigDecimal.valueOf(40900)) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].barnetErSelvforsørget).isFalse() },

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
