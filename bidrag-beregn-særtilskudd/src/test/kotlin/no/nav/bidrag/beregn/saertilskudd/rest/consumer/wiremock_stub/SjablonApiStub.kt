package no.nav.bidrag.beregn.saertilskudd.rest.consumer.wiremockstub

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.springframework.cloud.contract.spec.internal.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class SjablonApiStub {
    fun settOppSjablonStub() {
        settOppSjablonSjablontallStub()
        settOppSjablonSamvaersfradragStub()
        settOppSjablonBidragsevneStub()
        settOppSjablonTrinnvisSkattesatsStub()
    }

    private fun aClosedJsonResponse(): ResponseDefinitionBuilder {
        return WireMock.aResponse()
            .withHeader(HttpHeaders.CONNECTION, "close")
            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    }

    private fun settOppSjablonSjablontallStub() {
        val url = "/bidrag-sjablon/sjablontall/all"
        val sjablonliste =
            listOf(
                " [  " +
                    "{\"typeSjablon\": \"0001\"," +
                    "\"datoFom\": \"2019-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 1054," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0003\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 2825," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0004\"," +
                    "\"datoFom\": \"2013-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 0," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0005\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 1670," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0015\"," +
                    "\"datoFom\": \"2019-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 25.05," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0017\"," +
                    "\"datoFom\": \"2014-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 8.2," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0019\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 3841," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0021\"," +
                    "\"datoFom\": \"2018-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 5313," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0022\"," +
                    "\"datoFom\": \"2018-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 2187," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0023\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 87450," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0025\"," +
                    "\"datoFom\": \"2018-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 31," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0027\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 51300," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0028\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 51300," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0030\"," +
                    "\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 93273," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0031\"," +
                    "\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 93273," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0039\"," +
                    "\"datoFom\": \"2019-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 12977," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0040\"," +
                    "\"datoFom\": \"2019-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 22," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"typeSjablon\": \"0041\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"verdi\": 1354," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]",
            )
        stubFor(
            get(urlEqualTo(url))
                .willReturn(
                    aClosedJsonResponse()
                        .withStatus(HttpStatus.OK)
                        .withBody(
                            sjablonliste.joinToString(),
                        ),
                ),
        )
    }

    private fun settOppSjablonSamvaersfradragStub() {
        val url = "/bidrag-sjablon/samvaersfradrag/all"
        val sjablonliste =
            listOf(
                " [  " +
                    "{\"samvaersklasse\": \"00\"," +
                    "\"alderTom\": 99," +
                    "\"datoFom\": \"2013-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 1," +
                    "\"antNetterTom\": 1," +
                    "\"belopFradrag\": 0," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"01\"," +
                    "\"alderTom\": 5," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 3," +
                    "\"antNetterTom\": 3," +
                    "\"belopFradrag\": 256," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"01\"," +
                    "\"alderTom\": 10," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 3," +
                    "\"antNetterTom\": 3," +
                    "\"belopFradrag\": 353," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"01\"," +
                    "\"alderTom\": 14," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 3," +
                    "\"antNetterTom\": 3," +
                    "\"belopFradrag\": 457," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"01\"," +
                    "\"alderTom\": 18," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 3," +
                    "\"antNetterTom\": 3," +
                    "\"belopFradrag\": 528," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"01\"," +
                    "\"alderTom\": 99," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 3," +
                    "\"antNetterTom\": 3," +
                    "\"belopFradrag\": 528," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"02\"," +
                    "\"alderTom\": 5," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 8," +
                    "\"belopFradrag\": 849," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"02\"," +
                    "\"alderTom\": 10," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 8," +
                    "\"belopFradrag\": 1167," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"02\"," +
                    "\"alderTom\": 14," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 8," +
                    "\"belopFradrag\": 1513," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"02\"," +
                    "\"alderTom\": 18," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 8," +
                    "\"belopFradrag\": 1749," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"02\"," +
                    "\"alderTom\": 99," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 8," +
                    "\"belopFradrag\": 1749," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"03\"," +
                    "\"alderTom\": 5," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 13," +
                    "\"belopFradrag\": 2272," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"03\"," +
                    "\"alderTom\": 10," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 13," +
                    "\"belopFradrag\": 2716," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"03\"," +
                    "\"alderTom\": 14," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 13," +
                    "\"belopFradrag\": 3199," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"03\"," +
                    "\"alderTom\": 18," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 13," +
                    "\"belopFradrag\": 3528," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"03\"," +
                    "\"alderTom\": 99," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 13," +
                    "\"belopFradrag\": 3528," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"04\"," +
                    "\"alderTom\": 5," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 15," +
                    "\"belopFradrag\": 2852," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"04\"," +
                    "\"alderTom\": 10," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 15," +
                    "\"belopFradrag\": 3410," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"04\"," +
                    "\"alderTom\": 14," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 15," +
                    "\"belopFradrag\": 4016," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"04\"," +
                    "\"alderTom\": 18," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 15," +
                    "\"belopFradrag\": 4429," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"samvaersklasse\": \"04\"," +
                    "\"alderTom\": 99," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"antDagerTom\": 0," +
                    "\"antNetterTom\": 15," +
                    "\"belopFradrag\": 4429," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]",
            )
        stubFor(
            get(urlEqualTo(url))
                .willReturn(
                    aClosedJsonResponse()
                        .withStatus(HttpStatus.OK)
                        .withBody(
                            sjablonliste.joinToString(),
                        ),
                ),
        )
    }

    private fun settOppSjablonBidragsevneStub() {
        val url = "/bidrag-sjablon/bidragsevner/all"
        val sjablonliste =
            listOf(
                " [  " +
                    "{\"bostatus\": \"EN\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"belopBoutgift\": 9764," +
                    "\"belopUnderhold\": 9818," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"bostatus\": \"GS\"," +
                    "\"datoFom\": \"2020-07-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"belopBoutgift\": 5981," +
                    "\"belopUnderhold\": 8313," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]",
            )
        stubFor(
            get(urlEqualTo(url))
                .willReturn(
                    aClosedJsonResponse()
                        .withStatus(HttpStatus.OK)
                        .withBody(
                            sjablonliste.joinToString(),
                        ),
                ),
        )
    }

    private fun settOppSjablonTrinnvisSkattesatsStub() {
        val url = "/bidrag-sjablon/trinnvisskattesats/all"
        val sjablonliste =
            listOf(
                " [  " +
                    "{\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"inntektgrense\": 180800," +
                    "\"sats\": 1.9," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"inntektgrense\": 254500," +
                    "\"sats\": 4.2," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"inntektgrense\": 617500," +
                    "\"sats\": 13.2," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +
                    "{\"datoFom\": \"2020-01-01\"," +
                    "\"datoTom\": \"9999-12-31\"," +
                    "\"inntektgrense\": 964800," +
                    "\"sats\": 16.2," +
                    "\"brukerid\": \"A100364 \"," +
                    "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]",
            )
        stubFor(
            get(urlEqualTo(url))
                .willReturn(
                    aClosedJsonResponse()
                        .withStatus(HttpStatus.OK)
                        .withBody(
                            sjablonliste.joinToString(),
                        ),
                ),
        )
    }
}
