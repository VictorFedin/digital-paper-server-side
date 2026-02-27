package ru.digitalpaper.server.exception

import org.springframework.http.HttpStatus

data class InternalErrorException(
    override val message: String
) : CustomException(
    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
    message = message
)