package no.nav.bidrag.beregn.saertilskudd.rest

import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest.Companion.TEST_PROFILE
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BidragBeregnSaertilskuddTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragBeregnSaertilskudd")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class BidragBeregnSaertilskuddApplicationTest {
    @Test
    fun contextLoads() {
    }
}
