package com.ashakhov.app.jbproducts.exception

import com.devskiller.friendly_id.FriendlyId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException

class ProductNotFoundException(message: String?) : ResponseStatusException(HttpStatus.NOT_FOUND, message ?: "product is not found")
class RemoteServerException(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR, message: String) : ResponseStatusException(status, message)

@ControllerAdvice
class ProductNotFoundExceptionHandler {

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleError(exception: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                code = HttpStatus.NOT_FOUND.reasonPhrase,
                message = exception.message),
            HttpStatus.NOT_FOUND
        )
    }
}

@ControllerAdvice
class RemoteServerExceptionHandler {

    @ExceptionHandler(RemoteServerException::class)
    fun handleError(exception: RemoteServerException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                code = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = exception.message),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

data class ErrorResponse(
    val id: String = FriendlyId.createFriendlyId(),
    val status: Int,
    val code: String,
    val message: String
)