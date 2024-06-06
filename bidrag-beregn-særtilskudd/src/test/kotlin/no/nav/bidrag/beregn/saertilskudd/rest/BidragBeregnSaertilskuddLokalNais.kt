package no.nav.bidrag.beregn.saertilskudd.rest

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableMockOAuth2Server
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragBeregnSaertilskudd::class])])
@Profile("lokal-nais")
class BidragBeregnSaertilskuddLokalNais

fun main(args: Array<String>) {
    val app = SpringApplication(BidragBeregnSaertilskuddLokalNais::class.java)
    app.setAdditionalProfiles("lokal-nais", "lokal-nais-secrets")
    app.run(*args)
}
