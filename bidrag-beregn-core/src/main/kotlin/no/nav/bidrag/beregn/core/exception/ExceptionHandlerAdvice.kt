package no.nav.bidrag.beregn.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlerAdvice {
    @ExceptionHandler
    fun handleUgyldigInputException(exception: UgyldigInputException): ResponseEntity<*> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .header("Error", errorMsg(exception))
        .build<Any>()

    @ExceptionHandler
    fun handleSjablonConsumerException(exception: SjablonConsumerException): ResponseEntity<*> = ResponseEntity
        .status(exception.statusCode)
        .header("Error", errorMsg(exception))
        .build<Any>()

    @ExceptionHandler(BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException::class)
    fun handleBegrensetRevurderingException(
        exception: BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException,
    ): ResponseEntity<Map<String, Any>> = ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
        .body(
            mapOf(
                "melding" to exception.melding,
                "data" to exception.data,
            ),
        )

    private fun errorMsg(runtimeException: RuntimeException): String =
        String.format("%s: %s", runtimeException.javaClass.getSimpleName(), runtimeException.message)
}
