package no.nav.bidrag.inntekt.testdata

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import no.nav.bidrag.commons.service.KodeverkKoderBetydningerResponse
import no.nav.bidrag.commons.service.KodeverkProvider
import no.nav.bidrag.commons.service.finnVisningsnavn
import no.nav.bidrag.commons.service.finnVisningsnavnLønnsbeskrivelse
import no.nav.bidrag.commons.service.finnVisningsnavnSkattegrunnlag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class StubUtils {
    companion object {
        val wiremockPort = 1233
        val wireMockServer = WireMockServer(wiremockPort)
        val kodeverkUrl = "http://localhost:$wiremockPort/kodeverk"

        private fun createGenericResponse() = WireMock.aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .withStatus(HttpStatus.OK.value())

        fun dummystubKodeverkProvider() {
            mockkObject(KodeverkProvider)
            mockkStatic(::finnVisningsnavnSkattegrunnlag)
            mockkStatic(::finnVisningsnavnLønnsbeskrivelse)
            mockkStatic(::finnVisningsnavn)
            every { finnVisningsnavn(any()) } returns "Visningsnavn"
            every { finnVisningsnavnLønnsbeskrivelse(any()) } returns "Visningsnavn lønnsbeskrivelse"
            every { finnVisningsnavnSkattegrunnlag(any()) } returns "Visningsnavn skattegrunnlag"
        }

        fun stubKodeverkLønnsbeskrivelse(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/Loennsbeskrivelse.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_loennsbeskrivelser.json")
                    },
                ),
            )
        }

        fun stubKodeverkSkattegrunnlag(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/Summert.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_summert_skattegrunnlag.json")
                    },
                ),
            )
        }

        fun stubKodeverkYtelsesbeskrivelser(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/YtelseFraOffentligeBeskrivelse.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_ytelserbeskrivelser.json")
                    },
                ),
            )
        }

        fun stubKodeverkPensjonsbeskrivelser(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/PensjonEllerTrygdeBeskrivelse.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_ytelserbeskrivelser.json")
                    },
                ),
            )
        }

        fun stubKodeverkNaeringsinntektsbeskrivelser(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/Naeringsinntektsbeskrivelse.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_naeringsinntektsbeskrivelse.json")
                    },
                ),
            )
        }

        fun stubKodeverkSpesifisertSkattegrunnlag(response: KodeverkKoderBetydningerResponse? = null, status: HttpStatus = HttpStatus.OK) {
            wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching(".*/kodeverk/SpesifisertSummertSkattegrunnlag.*")).willReturn(
                    if (response != null) {
                        createGenericResponse().withStatus(status.value()).withBody(
                            ObjectMapper().findAndRegisterModules().writeValueAsString(response),
                        )
                    } else {
                        createGenericResponse()
                            .withBodyFile("respons_kodeverk_summert_skattegrunnlag.json")
                    },
                ),
            )
        }
    }
}
