package no.nav.bidrag.beregn.forskudd.testdata

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall

class SjablonApiStub {
    fun settOppSjablonStub() {
        settOppSjablonSjablontallStub()
    }

    private fun settOppSjablonSjablontallStub() {
        mockkObject(SjablonProvider.Companion)
        val sjablonliste =
            " [  " +
                "{\"typeSjablon\": \"0005\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 1670," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0005\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 1880," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0006\"," +
                "\"datoFom\": \"2003-01-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 10000," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0013\"," +
                "\"datoFom\": \"2003-01-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 320," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0013\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 330," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0033\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 297500," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0033\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 346200," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0034\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 468500," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0034\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 532400," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0035\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 360800," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0035\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 420600," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0036\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 69100," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0036\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 74600," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0038\"," +
                "\"datoFom\": \"2020-07-01\"," +
                "\"datoTom\": \"2023-06-30\"," +
                "\"verdi\": 1250," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:13:16.999\"}," +

                "{\"typeSjablon\": \"0038\"," +
                "\"datoFom\": \"2023-07-01\"," +
                "\"datoTom\": \"9999-12-31\"," +
                "\"verdi\": 1410," +
                "\"brukerid\": \"A100364 \"," +
                "\"tidspktEndret\": \"2020-05-17T14:15:49.233\"}]"

        every {
            SjablonProvider.hentSjablontall()
        } returns ObjectMapper().findAndRegisterModules().readValue(sjablonliste, object : TypeReference<List<Sjablontall>>() {})
    }
}
