package ru.digitalpaper.server.exception

import org.springframework.http.HttpStatus

data class ForbiddenException(
    override val message: String,
) : CustomException(
    code = HttpStatus.FORBIDDEN.value(),
    message = message
)
