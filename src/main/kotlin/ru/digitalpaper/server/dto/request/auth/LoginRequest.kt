package ru.digitalpaper.server.dto.request.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Данные для входа пользователя")
data class LoginRequest(
    @field:Schema(description = "Логин или email пользователя", example = "user@example.com")
    val username: String,

    @field:Schema(
        description = "Пароль пользователя",
        example = "StrongPassword123!",
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    val password: String
)
