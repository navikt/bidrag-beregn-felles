package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.BeregnIndeksreguleringPrivatAvtaleApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtalePeriode
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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnIndeksregulerPrivatAvtaleApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnIndeksreguleringPrivatAvtaleApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnIndeksreguleringPrivatAvtaleApi()
    }

    @Test
    @DisplayName("Privat avtale - uten indeksregulering")
    fun testIndeksreguleringPrivatAvtaleUtenIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_uten_indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale()

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2024-09")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(100.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-11")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(150.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-11"), null)) },
            { assertThat(resultat[2].beløp.compareTo(BigDecimal.valueOf(210.00))).isEqualTo(0) },
            { assertThat(resultat[2].indeksreguleringFaktor).isNull() },
          )
    }

    @Test
    @DisplayName("Privat avtale - med indeksregulering")
    fun testIndeksreguleringPrivatAvtaleMedIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_med_indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale()

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2022-01", "2023-07")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(1000.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(1070.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(7.00))).isEqualTo(0) },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(resultat[2].beløp.compareTo(BigDecimal.valueOf(1120.00))).isEqualTo(0) },
            { assertThat(resultat[2].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(4.70))).isEqualTo(0) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale(): List<DelberegningPrivatAvtalePeriode> {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = api.beregnIndeksreguleringPrivatAvtale(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtalePeriode>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE)
            .map {
                DelberegningPrivatAvtalePeriode(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
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
