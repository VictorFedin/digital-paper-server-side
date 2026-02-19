package ru.digitalpaper.server.exception

import org.springframework.http.HttpStatus

data class NotFoundException(
    override val message: String
) : CustomException(
    code = HttpStatus.NOT_FOUND.value(),
    message = message
)