package no.nav.bidrag.indeksregulering

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.service.VedtakService
import no.nav.bidrag.beregn.core.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.core.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.core.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.service.IndeksreguleringOrkestrator
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningIndeksregulering
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.junit.jupiter.api.Assertions
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
import java.time.Year
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
class IndeksreguleringOrkestratorTest {
    private lateinit var filnavn: String

    private lateinit var indeksreguleringOrkestrator: IndeksreguleringOrkestrator

    @Mock
    lateinit var beregningVedtakConsumer: BeregningVedtakConsumer

    @Mock
    lateinit var beregningBeløpshistorikkConsumer: BeregningBeløpshistorikkConsumer

    @Mock
    lateinit var identUtils: IdentUtils

//    @Mock
    private lateinit var vedtakService: VedtakService

    @Mock
    private lateinit var api: BeregnIndeksreguleringApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        vedtakService = VedtakService(
            vedtakConsumer = beregningVedtakConsumer,
            stønadConsumer = beregningBeløpshistorikkConsumer,
            vedtakFilter = Vedtaksfiltrering(),
            identUtils = identUtils,
        )
        indeksreguleringOrkestrator = IndeksreguleringOrkestrator(
            vedtakService = vedtakService,
            beregnIndeksreguleringApi = BeregnIndeksreguleringApi(),
        )
    }

    @Test
    @DisplayName("Test indeksreguleringOrkestrator")
    fun testIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksreguleringorkestrator.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksregulering()

        Assertions.assertAll(

            { org.assertj.core.api.Assertions.assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2025-07"), null)) },
            { org.assertj.core.api.Assertions.assertThat(resultat[0].beløp.verdi.compareTo(BigDecimal.valueOf(1230.00))).isEqualTo(0) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatIndeksregulering(): List<SluttberegningIndeksregulering> {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = indeksreguleringOrkestrator.utførIndeksreguleringBarnebidrag(
            request.indeksreguleresForÅr,
            request.stønadsid,
            request.grunnlagListe,
        )
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

        Assertions.assertAll(
            { org.assertj.core.api.Assertions.assertThat(resultat).isNotNull },
            { org.assertj.core.api.Assertions.assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return resultatListe
    }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

    private fun lesFilOgByggRequest(filnavn: String): IndeksreguleringOrkestratorGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            Assertions.fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, IndeksreguleringOrkestratorGrunnlag::class.java)
    }

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}

data class IndeksreguleringOrkestratorGrunnlag(
    val indeksreguleresForÅr: Year,
    val stønadsid: Stønadsid,
    val grunnlagListe: List<GrunnlagDto> = emptyList(),
)
