package no.nav.bidrag.beregn.saertilskudd.rest

import com.nimbusds.jose.JOSEObjectType
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddLocal.Companion.LOCAL_PROFILE
import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest.Companion.TEST_PROFILE
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

private val LOGGER = LoggerFactory.getLogger(BidragBeregnSaertilskuddTestConfig::class.java)

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-beregn-saertilskudd", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@Profile(TEST_PROFILE, LOCAL_PROFILE)
class BidragBeregnSaertilskuddTestConfig {
    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate): HttpHeaderTestRestTemplate {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val iss = mockOAuth2Server.issuerUrl(ISSUER)
        val newIssuer = iss.newBuilder().host("localhost").build()
        val token =
            mockOAuth2Server.issueToken(
                issuerId = ISSUER,
                clientId = "aud-localhost",
                tokenCallback =
                DefaultOAuth2TokenCallback(
                    issuerId = ISSUER,
                    subject = "aud-localhost",
                    typeHeader = JOSEObjectType.JWT.type,
                    audience = listOf("aud-localhost"),
                    claims = mapOf("iss" to newIssuer.toString()),
                    expiry = 3600,
                ),
            )
        return "Bearer " + token.serialize()
    }
}
