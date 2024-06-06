package no.nav.bidrag.beregn.saertilskudd.rest.consumer

import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil
import no.nav.bidrag.beregn.saertilskudd.rest.exception.SjablonConsumerException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
@DisplayName("SjablonConsumerTest")
internal class SjablonConsumerTest {
    @InjectMocks
    private val sjablonConsumer: SjablonConsumer? = null

    @Mock
    private val restTemplateMock: RestTemplate? = null

    @Test
    @DisplayName("Skal hente liste av Sjablontall når respons fra tjenesten er OK")
    fun skalHenteListeAvSjablontallNaarResponsFraTjenestenErOk() {
        Mockito.`when`(
            restTemplateMock?.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Sjablontall>>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.dummySjablonSjablontallListe(), HttpStatus.OK))

        val sjablonResponse = sjablonConsumer?.hentSjablonSjablontall()
        assertAll(
            Executable { assertThat(sjablonResponse).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).hasSameSizeAs(TestUtil.dummySjablonSjablontallListe()) },
            Executable {
                assertThat(sjablonResponse?.responseEntity?.body?.get(0)?.typeSjablon)
                    .isEqualTo(TestUtil.dummySjablonSjablontallListe()[0].typeSjablon)
            },
        )
    }

    @Test
    @DisplayName("Skal kaste SjablonConsumerException når respons fra tjenesten ikke er OK for Sjablontall")
    fun skalKasteRestClientExceptionNaarResponsFraTjenestenIkkeErOkForSjablontall() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Sjablontall>>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThatExceptionOfType(SjablonConsumerException::class.java).isThrownBy { sjablonConsumer?.hentSjablonSjablontall() }
    }

    @Test
    @DisplayName("Skal hente liste av Samvaersfradrag-sjabloner når respons fra tjenesten er OK")
    fun skalHenteListeAvSamvaersfradragSjablonerNaarResponsFraTjenestenErOk() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Samvaersfradrag>>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.dummySjablonSamvaersfradragListe(), HttpStatus.OK))
        val sjablonResponse = sjablonConsumer?.hentSjablonSamvaersfradrag()
        assertAll(
            Executable { assertThat(sjablonResponse).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).hasSameSizeAs(TestUtil.dummySjablonSamvaersfradragListe()) },
            Executable {
                assertThat(sjablonResponse?.responseEntity?.body?.get(0)?.belopFradrag)
                    .isEqualTo(TestUtil.dummySjablonSamvaersfradragListe()[0].belopFradrag)
            },
        )
    }

    @Test
    @DisplayName("Skal kaste SjablonConsumerException når respons fra tjenesten ikke er OK for Samvaersfradrag")
    fun skalKasteRestClientExceptionNaarResponsFraTjenestenIkkeErOkForSamvaersfradrag() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Samvaersfradrag>>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThatExceptionOfType(SjablonConsumerException::class.java).isThrownBy { sjablonConsumer?.hentSjablonSamvaersfradrag() }
    }

    @Test
    @DisplayName("Skal hente liste av Bidragsevne-sjabloner når respons fra tjenesten er OK")
    fun skalHenteListeAvBidragsevneSjablonerNaarResponsFraTjenestenErOk() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Bidragsevne>>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.dummySjablonBidragsevneListe(), HttpStatus.OK))
        val sjablonResponse = sjablonConsumer?.hentSjablonBidragsevne()
        assertAll(
            Executable { assertThat(sjablonResponse).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).hasSameSizeAs(TestUtil.dummySjablonBidragsevneListe()) },
            Executable {
                assertThat(sjablonResponse?.responseEntity?.body?.get(0)?.belopBoutgift)
                    .isEqualTo(TestUtil.dummySjablonBidragsevneListe()[0].belopBoutgift)
            },
        )
    }

    @Test
    @DisplayName("Skal kaste SjablonConsumerException når respons fra tjenesten ikke er OK for Bidragsevne")
    fun skalKasteRestClientExceptionNaarResponsFraTjenestenIkkeErOkForBidragsevne() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<Bidragsevne>>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThatExceptionOfType(SjablonConsumerException::class.java).isThrownBy { sjablonConsumer?.hentSjablonBidragsevne() }
    }

    @Test
    @DisplayName("Skal hente liste av TrinnvisSkattesats-sjabloner når respons fra tjenesten er OK")
    fun skalHenteListeAvTrinnvisSkattesatsSjablonerNaarResponsFraTjenestenErOk() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<TrinnvisSkattesats>>>(),
            ),
        )
            .thenReturn(ResponseEntity(TestUtil.dummySjablonTrinnvisSkattesatsListe(), HttpStatus.OK))
        val sjablonResponse = sjablonConsumer?.hentSjablonTrinnvisSkattesats()
        assertAll(
            Executable { assertThat(sjablonResponse).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).isNotNull() },
            Executable { assertThat(sjablonResponse?.responseEntity?.body).hasSameSizeAs(TestUtil.dummySjablonTrinnvisSkattesatsListe()) },
            Executable {
                assertThat(sjablonResponse?.responseEntity?.body?.get(0)?.inntektgrense)
                    .isEqualTo(TestUtil.dummySjablonTrinnvisSkattesatsListe()[0].inntektgrense)
            },
        )
    }

    @Test
    @DisplayName("Skal kaste SjablonConsumerException når respons fra tjenesten ikke er OK for TrinnvisSkattesats")
    fun skalKasteRestClientExceptionNaarResponsFraTjenestenIkkeErOkForTrinnvisSkattesats() {
        Mockito.`when`(
            restTemplateMock!!.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                any<ParameterizedTypeReference<List<TrinnvisSkattesats>>>(),
            ),
        )
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        assertThatExceptionOfType(SjablonConsumerException::class.java).isThrownBy { sjablonConsumer?.hentSjablonTrinnvisSkattesats() }
    }
}
