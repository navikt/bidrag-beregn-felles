package no.nav.bidrag.beregn.særtilskudd.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlerAdvice {
    @ExceptionHandler
    fun handleUgyldigInputException(exception: UgyldigInputException): ResponseEntity<*> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header("Error", errorMsg(exception))
            .build<Any>()
    }

    @ExceptionHandler
    fun handleSjablonConsumerException(exception: SjablonConsumerException): ResponseEntity<*> {
        return ResponseEntity
            .status(exception.statusCode)
            .header("Error", errorMsg(exception))
            .build<Any>()
    }

    private fun errorMsg(runtimeException: RuntimeException): String {
        return String.format("%s: %s", runtimeException.javaClass.getSimpleName(), runtimeException.message)
    }
}
