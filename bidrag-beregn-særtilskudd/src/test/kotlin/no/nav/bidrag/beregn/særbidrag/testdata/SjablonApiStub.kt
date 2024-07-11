package no.nav.bidrag.beregn.særbidrag.testdata

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats

class SjablonApiStub {
    fun settOppSjablonStub() {
        mockkObject(SjablonProvider.Companion)
        settOppSjablonSjablontallStub()
        settOppSjablonBidragsevneStub()
        settOppSjablonTrinnvisSkattesatsStub()
    }

    private fun settOppSjablonSjablontallStub() {
        val sjablonliste =
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

                "{\"typeSjablon\": \"0006\"," +
                "\"datoFom\": \"2003-01-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 10000," +
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
                "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]"

        every {
            SjablonProvider.hentSjablontall()
        } returns ObjectMapper().findAndRegisterModules().readValue(sjablonliste, object : TypeReference<List<Sjablontall>>() {})
    }

    private fun settOppSjablonBidragsevneStub() {
        val sjablonliste =
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
                "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]"

        every {
            SjablonProvider.hentSjablonBidragsevne()
        } returns ObjectMapper().findAndRegisterModules().readValue(sjablonliste, object : TypeReference<List<Bidragsevne>>() {})
    }

    private fun settOppSjablonTrinnvisSkattesatsStub() {
        val sjablonliste =
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
                "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]"

        every {
            SjablonProvider.hentSjablonTrinnvisSkattesats()
        } returns ObjectMapper().findAndRegisterModules().readValue(sjablonliste, object : TypeReference<List<TrinnvisSkattesats>>() {})
    }
}
