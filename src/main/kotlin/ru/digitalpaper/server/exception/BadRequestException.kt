package ru.digitalpaper.server.exception

import org.springframework.http.HttpStatus

data class BadRequestException(
    override val message: String
) : CustomException(
    code = HttpStatus.BAD_REQUEST.value(),
    message = message
)
