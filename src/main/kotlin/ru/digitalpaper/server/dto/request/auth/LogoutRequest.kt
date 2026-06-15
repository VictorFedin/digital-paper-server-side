package ru.digitalpaper.server.dto.request.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Данные для завершения пользовательской сессии")
data class LogoutRequest(
    @field:Schema(
        description = "Refresh token активной сессии",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    val refreshToken: String
)
