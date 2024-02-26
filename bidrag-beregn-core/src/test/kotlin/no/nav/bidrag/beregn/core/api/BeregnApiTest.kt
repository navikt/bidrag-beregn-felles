package no.nav.bidrag.beregn.core.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.core.TestUtil
import no.nav.bidrag.beregn.core.inntekt.service.BeregnInntektService
import no.nav.bidrag.beregn.core.testdata.SjablonApiStub
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnValgteInntekterGrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
internal class BeregnApiTest {
    private lateinit var filnavn: String

    @Mock
    private lateinit var beregnInntektService: BeregnInntektService

    @BeforeEach
    fun initMock() {
        SjablonApiStub().settOppSjablonStub()
        beregnInntektService = BeregnInntektService()
    }

    @Test
    fun skalBeregneInntekt() {
        filnavn = "src/test/resources/testfiler/summer_inntekter_request_eksempel1.json"

        val request = lesFilOgByggRequest(filnavn)

        // Kall rest-API for beregning og periodisering av inntekt
        val inntektResultat = beregnInntektService.beregn(request)

        TestUtil.printJson(inntektResultat)

        assertAll(
            { assertThat(inntektResultat).isNotNull },
            { assertThat(inntektResultat.inntektPerBarnListe.size).isEqualTo(3) },

            { assertThat(inntektResultat.inntektPerBarnListe[0].inntektGjelderBarnIdent).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe.size).isEqualTo(5) },

            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-02")) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].totalinntekt).isEqualTo(BigDecimal.valueOf(275000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(260000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[0].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-02", "2023-04")) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].totalinntekt).isEqualTo(BigDecimal.valueOf(345000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(330000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[1].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-05")) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].totalinntekt).isEqualTo(BigDecimal.valueOf(245000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[2].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-05", "2023-06")) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].totalinntekt).isEqualTo(BigDecimal.valueOf(245000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[3].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-07")) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].totalinntekt).isEqualTo(BigDecimal.valueOf(265000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[0].summertInntektListe[4].småbarnstillegg).isEqualTo(BigDecimal.valueOf(20000)) },

            { assertThat(inntektResultat.inntektPerBarnListe[1].inntektGjelderBarnIdent.toString()).contains("1") },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe.size).isEqualTo(6) },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-02")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].totalinntekt).isEqualTo(BigDecimal.valueOf(285000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].kontantstøtte).isEqualTo(BigDecimal.valueOf(10000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(260000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[0].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-02", "2023-03")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].totalinntekt).isEqualTo(BigDecimal.valueOf(355000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].kontantstøtte).isEqualTo(BigDecimal.valueOf(10000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(330000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[1].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-03", "2023-04")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].totalinntekt).isEqualTo(BigDecimal.valueOf(354000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(330000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].barnetillegg).isEqualTo(BigDecimal.valueOf(9000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[2].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-05")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].totalinntekt).isEqualTo(BigDecimal.valueOf(245000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[3].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-05", "2023-06")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].totalinntekt).isEqualTo(BigDecimal.valueOf(245000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[4].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-07")) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].totalinntekt).isEqualTo(BigDecimal.valueOf(265000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[1].summertInntektListe[5].småbarnstillegg).isEqualTo(BigDecimal.valueOf(20000)) },

            { assertThat(inntektResultat.inntektPerBarnListe[2].inntektGjelderBarnIdent.toString()).contains("2") },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe.size).isEqualTo(5) },

            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-02")) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].totalinntekt).isEqualTo(BigDecimal.valueOf(275000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(260000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[0].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-02", "2023-04")) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].totalinntekt).isEqualTo(BigDecimal.valueOf(351000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(330000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].barnetillegg).isEqualTo(BigDecimal.valueOf(6000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[1].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].periode).isEqualTo(ÅrMånedsperiode("2023-04", "2023-05")) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].totalinntekt).isEqualTo(BigDecimal.valueOf(251000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].barnetillegg).isEqualTo(BigDecimal.valueOf(6000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[2].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-05", "2023-06")) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].totalinntekt).isEqualTo(BigDecimal.valueOf(251000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].barnetillegg).isEqualTo(BigDecimal.valueOf(6000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[3].småbarnstillegg).isNull() },

            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2023-07")) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].totalinntekt).isEqualTo(BigDecimal.valueOf(265000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].kontantstøtte).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(230000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].barnetillegg).isNull() },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].utvidetBarnetrygd).isEqualTo(BigDecimal.valueOf(15000)) },
            { assertThat(inntektResultat.inntektPerBarnListe[2].summertInntektListe[4].småbarnstillegg).isEqualTo(BigDecimal.valueOf(20000)) },
        )
    }

    private fun lesFilOgByggRequest(filnavn: String?): BeregnValgteInntekterGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn!!))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnValgteInntekterGrunnlag::class.java)
    }
}
