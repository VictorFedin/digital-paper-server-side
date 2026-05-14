package ru.digitalpaper.server.dto.response.common

data class ErrorResponse(
    val code: Int,
    val message: String,
    val errors: Map<String, String>
)
