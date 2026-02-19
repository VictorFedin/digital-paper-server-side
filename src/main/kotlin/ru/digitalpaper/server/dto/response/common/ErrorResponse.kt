package ru.digitalpaper.server.dto.response.common

import ru.digitalpaper.server.dto.response.Response

data class ErrorResponse(
    val code: Int,
    val message: String,
    val reason: String,
    val errors: Map<String, String>
) : Response()
