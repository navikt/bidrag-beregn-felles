package no.nav.bidrag.indeksregulering.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningIndeksreguleringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
internal class BeregnIndeksreguleringApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnIndeksreguleringApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnIndeksreguleringApi()
    }

    @Test
    @Disabled
    @DisplayName("Test indeksregulering")
    fun testIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksregulering/indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksregulering()

        assertAll(
            { assertThat(resultat).hasSize(4) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2021-01", "2021-07")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(1000.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2021-07", "2022-07")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(1200.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2022-07", "2023-07")) },
            { assertThat(resultat[2].beløp.compareTo(BigDecimal.valueOf(1240.00))).isEqualTo(0) },
            { assertThat(resultat[2].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0320))).isEqualTo(0) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(resultat[3].beløp.compareTo(BigDecimal.valueOf(1330.00))).isEqualTo(0) },
            { assertThat(resultat[3].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0700))).isEqualTo(0) },

            { assertThat(resultat[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(resultat[4].beløp.compareTo(BigDecimal.valueOf(1390.00))).isEqualTo(0) },
            { assertThat(resultat[4].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0470))).isEqualTo(0) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatIndeksregulering(): List<DelberegningIndeksreguleringPeriode> {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = api.beregnIndeksregulering(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningIndeksreguleringPeriode>(Grunnlagstype.DELBEREGNING_INDEKSREGULERING_PERIODE)
            .map {
                DelberegningIndeksreguleringPeriode(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                    valutakode = it.innhold.valutakode,
                    indeksreguleringFaktor = it.innhold.indeksreguleringFaktor,
                )
            }

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return resultatListe
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
