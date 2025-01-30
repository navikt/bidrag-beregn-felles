package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
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
internal class BeregnNettoBarnetilleggApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Netto barnetillegg - eksempel 1  ")
    fun testNettoBarnetillegg_Eksempel01() {
        filnavn = "src/test/resources/testfiler/nettobarnetillegg/netto_barnetillegg_eksempel1.json"
        val rolle = Grunnlagstype.PERSON_BIDRAGSPLIKTIG
        val resultat = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolle)

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertEquals(0, resultat[0].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(1700.00))) },
            { assertEquals(0, resultat[0].summertNettoBarnetillegg.compareTo(BigDecimal.valueOf(1105.00))) },
            { assertEquals(0, resultat[0].barnetilleggTypeListe[0].barnetilleggType.compareTo(Inntektstype.BARNETILLEGG_PENSJON)) },
            { assertEquals(0, resultat[0].barnetilleggTypeListe[0].bruttoBarnetillegg.compareTo(BigDecimal.valueOf(1700.00))) },
            { assertEquals(0, resultat[0].barnetilleggTypeListe[0].nettoBarnetillegg.compareTo(BigDecimal.valueOf(1105.00))) },
        )
    }

    @Test
    @DisplayName("Netto barnetillegg - eksempel 2 Skal ikke beregne for motsatt part ")
    fun testNettoBarnetillegg_Eksempel02() {
        filnavn = "src/test/resources/testfiler/nettobarnetillegg/netto_barnetillegg_eksempel2.json"
        val rolleBP = Grunnlagstype.PERSON_BIDRAGSPLIKTIG
        val resultatBP = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolleBP)

        val rolleBM = Grunnlagstype.PERSON_BIDRAGSMOTTAKER
        val resultatBM = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolleBM)

        assertAll(
            { assertThat(resultatBP).hasSize(1) },
            { assertEquals(0, resultatBP[0].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(1700.00))) },
            { assertEquals(0, resultatBP[0].summertNettoBarnetillegg.compareTo(BigDecimal.valueOf(1105.00))) },

            { assertEquals(0, resultatBP[0].barnetilleggTypeListe[0].barnetilleggType.compareTo(Inntektstype.BARNETILLEGG_PENSJON)) },
            { assertEquals(0, resultatBP[0].barnetilleggTypeListe[0].bruttoBarnetillegg.compareTo(BigDecimal.valueOf(1700.00))) },
            { assertEquals(0, resultatBP[0].barnetilleggTypeListe[0].nettoBarnetillegg.compareTo(BigDecimal.valueOf(1105.00))) },

            { assertThat(resultatBM).hasSize(1) },
            { assertEquals(0, resultatBM[0].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(800.00))) },
            { assertEquals(0, resultatBM[0].summertNettoBarnetillegg.compareTo(BigDecimal.valueOf(560.00))) },

            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[0].barnetilleggType.compareTo(Inntektstype.BARNETILLEGG_PENSJON)) },
            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[0].bruttoBarnetillegg.compareTo(BigDecimal.valueOf(300.00))) },
            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[0].nettoBarnetillegg.compareTo(BigDecimal.valueOf(210.00))) },
            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[1].barnetilleggType.compareTo(Inntektstype.BARNETILLEGG_DAGPENGER)) },
            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[1].bruttoBarnetillegg.compareTo(BigDecimal.valueOf(500.00))) },
            { assertEquals(0, resultatBM[0].barnetilleggTypeListe[1].nettoBarnetillegg.compareTo(BigDecimal.valueOf(350.00))) },
        )
    }

    @Test
    @DisplayName("Netto barnetillegg - eksempel 3 Manglende grunnlag ")
    fun testNettoBarnetillegg_Eksempel03() {
        filnavn = "src/test/resources/testfiler/nettobarnetillegg/netto_barnetillegg_eksempel3.json"

        val rolleBM = Grunnlagstype.PERSON_BIDRAGSMOTTAKER
        val resultatBM = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolleBM)

        assertAll(
            { assertThat(resultatBM).hasSize(0) },

        )
    }

    @Test
    @DisplayName("Netto barnetillegg - eksempel 4 periodisering ")
    fun testNettoBarnetillegg_Eksempel04() {
        filnavn = "src/test/resources/testfiler/nettobarnetillegg/netto_barnetillegg_eksempel4_periodisering.json"

        val rolleBM = Grunnlagstype.PERSON_BIDRAGSMOTTAKER
        val resultat = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolleBM)

        assertAll(
            { assertThat(resultat).hasSize(3) },

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2024-02", "2024-03")) },
            { assertEquals(0, resultat[0].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(300.00))) },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-03", "2024-06")) },
            { assertEquals(0, resultat[1].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(800.00))) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2024-06", "2024-07")) },
            { assertEquals(0, resultat[2].summertBruttoBarnetillegg.compareTo(BigDecimal.valueOf(500.00))) },

        )
    }

    @Test
    @DisplayName("Netto barnetillegg - eksempel 5 - tiltakspenger")
    fun testNettoBarnetillegg_Eksempel05() {
        filnavn = "src/test/resources/testfiler/nettobarnetillegg/netto_barnetillegg_eksempel5.json"
        val rolle = Grunnlagstype.PERSON_BIDRAGSPLIKTIG
        val resultat = utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolle)

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].summertBruttoBarnetillegg).isEqualTo(BigDecimal.valueOf(1700).setScale(2)) },
            { assertThat(resultat[0].summertNettoBarnetillegg).isEqualTo(BigDecimal.valueOf(2105).setScale(2)) },
            { assertThat(resultat[0].barnetilleggTypeListe).hasSize(2) },
            { assertThat(resultat[0].barnetilleggTypeListe[0].barnetilleggType).isEqualTo(Inntektstype.BARNETILLEGG_PENSJON) },
            { assertThat(resultat[0].barnetilleggTypeListe[0].bruttoBarnetillegg).isEqualTo(BigDecimal.valueOf(1700).setScale(2)) },
            { assertThat(resultat[0].barnetilleggTypeListe[0].nettoBarnetillegg).isEqualTo(BigDecimal.valueOf(1105).setScale(2)) },
            { assertThat(resultat[0].barnetilleggTypeListe[1].barnetilleggType).isEqualTo(Inntektstype.BARNETILLEGG_TILTAKSPENGER) },
            { assertThat(resultat[0].barnetilleggTypeListe[1].bruttoBarnetillegg).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(resultat[0].barnetilleggTypeListe[1].nettoBarnetillegg).isEqualTo(BigDecimal.valueOf(1000).setScale(2)) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatNettoBarnetillegg(rolle: Grunnlagstype): List<DelberegningNettoBarnetillegg> {
        val request = lesFilOgByggRequest(filnavn)
        val nettoBarnetilleggResultat = api.beregnNettoBarnetillegg(request, rolle)
        printJson(nettoBarnetilleggResultat)

        val alleReferanser = hentAlleReferanser(nettoBarnetilleggResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(nettoBarnetilleggResultat)

        val nettoBarnetilleggResultatListe = nettoBarnetilleggResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningNettoBarnetillegg>(Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG)
            .map {
                DelberegningNettoBarnetillegg(
                    periode = it.innhold.periode,
                    summertBruttoBarnetillegg = it.innhold.summertBruttoBarnetillegg,
                    summertNettoBarnetillegg = it.innhold.summertNettoBarnetillegg,
                    barnetilleggTypeListe = it.innhold.barnetilleggTypeListe,
                )
            }

        assertAll(
            { assertThat(nettoBarnetilleggResultat).isNotNull },
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return nettoBarnetilleggResultatListe
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
