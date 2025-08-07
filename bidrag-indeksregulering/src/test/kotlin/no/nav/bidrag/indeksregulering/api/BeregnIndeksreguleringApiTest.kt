package no.nav.bidrag.indeksregulering.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.indeksregulering.bo.BeregnIndeksreguleringGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningIndeksregulering
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
    @DisplayName("Test indeksregulering")
    fun testIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksregulering()

        assertAll(

            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-07"), null)) },
            { assertThat(resultat[0].beløp.verdi.compareTo(BigDecimal.valueOf(1230.00))).isEqualTo(0) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatIndeksregulering(): List<SluttberegningIndeksregulering> {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = api.beregnIndeksregulering(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningIndeksregulering>(Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING)
            .map {
                SluttberegningIndeksregulering(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                    originaltBeløp = it.innhold.originaltBeløp,
                    nesteIndeksreguleringsår = it.innhold.nesteIndeksreguleringsår,
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

    private fun lesFilOgByggRequest(filnavn: String): BeregnIndeksreguleringGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnIndeksreguleringGrunnlag::class.java)
    }

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
