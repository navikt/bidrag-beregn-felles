package no.nav.bidrag.beregn.særtilskudd

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.util.StdDateFormat
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.beregn.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.BPsAndelSaertilskuddCore
import no.nav.bidrag.beregn.saertilskudd.SaertilskuddCore
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.SjablonConsumer
import no.nav.bidrag.beregn.samvaersfradrag.SamvaersfradragCore
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.commons.web.UserMdcFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RootUriTemplateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.client.RestTemplate

const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-beregn-saertilskudd-rest", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
)
@Import(CorrelationIdFilter::class, UserMdcFilter::class, DefaultCorsFilter::class)
class BeregnSaertilskuddConfig {
    @Bean
    @Scope("prototype")
    fun restTemplate(): HttpHeaderRestTemplate {
        val httpHeaderRestTemplate = HttpHeaderRestTemplate()
        httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER) { CorrelationId.fetchCorrelationIdForThread() }
        return httpHeaderRestTemplate
    }

    @Bean
    fun bidragsevneCore(): BidragsevneCore {
        return BidragsevneCore.getInstance()
    }

    @Bean
    fun bPsAndelSaertilskuddCoreCore(): BPsAndelSaertilskuddCore {
        return BPsAndelSaertilskuddCore.getInstance()
    }

    @Bean
    fun saertilskuddCore(): SaertilskuddCore {
        return SaertilskuddCore.getInstance()
    }

    @Bean
    fun samvaersfradragCore(): SamvaersfradragCore {
        return SamvaersfradragCore.getInstance()
    }

    @Bean
    fun sjablonConsumer(@Value("\${BIDRAGSJABLON_URL}") sjablonBaseUrl: String, restTemplate: RestTemplate): SjablonConsumer {
        restTemplate.uriTemplateHandler = RootUriTemplateHandler(sjablonBaseUrl)
        return SjablonConsumer(restTemplate)
    }

    @Bean
    fun exceptionLogger(): ExceptionLogger {
        return ExceptionLogger(BidragBeregnSaertilskudd::class.java.simpleName)
    }

    @Bean
    fun correlationIdFilter(): CorrelationIdFilter {
        return CorrelationIdFilter()
    }

    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        return Jackson2ObjectMapperBuilder()
            .dateFormat(StdDateFormat())
            .failOnUnknownProperties(false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
