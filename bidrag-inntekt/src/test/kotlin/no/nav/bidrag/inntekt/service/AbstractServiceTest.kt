package no.nav.bidrag.inntekt.service

import no.nav.bidrag.commons.service.KodeverkProvider
import no.nav.bidrag.inntekt.testdata.StubUtils
import no.nav.bidrag.inntekt.testdata.StubUtils.Companion.kodeverkUrl
import no.nav.bidrag.inntekt.testdata.StubUtils.Companion.wireMockServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractServiceTest {
    @BeforeEach
    fun initKodeverk() {
        wireMockServer.start()
        KodeverkProvider.initialiser(kodeverkUrl)
        StubUtils.stubKodeverkSkattegrunnlag()
        StubUtils.stubKodeverkLønnsbeskrivelse()
        StubUtils.stubKodeverkYtelsesbeskrivelser()
        StubUtils.stubKodeverkPensjonsbeskrivelser()
        StubUtils.stubKodeverkNaeringsinntektsbeskrivelser()
    }

    @AfterEach
    fun clearMocks() {
        wireMockServer.stop()
    }
}
