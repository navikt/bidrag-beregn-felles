package no.nav.bidrag.beregn.core.exception

import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientResponseException

class SjablonConsumerException(exception: RestClientResponseException) : RuntimeException(exception) {
    val statusCode: HttpStatus

    init {
        statusCode = HttpStatus.valueOf(exception.rawStatusCode)
    }
}
