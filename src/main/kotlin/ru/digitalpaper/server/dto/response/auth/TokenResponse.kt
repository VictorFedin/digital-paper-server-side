package ru.digitalpaper.server.dto.response.auth

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Токены и параметры пользовательской сессии, полученные от Keycloak")
data class TokenResponse(
    @JsonProperty("access_token")
    @field:Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @JsonProperty("expires_in")
    @field:Schema(description = "Срок действия access token в секундах", example = "300")
    val expiresIn: Long,

    @JsonProperty("refresh_expires_in")
    @field:Schema(description = "Срок действия refresh token в секундах", example = "1800", nullable = true)
    val refreshExpiresIn: Long?,

    @JsonProperty("refresh_token")
    @field:Schema(description = "Токен для обновления сессии", example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
    val refreshToken: String?,

    @JsonProperty("token_type")
    @field:Schema(description = "Тип токена авторизации", example = "Bearer")
    val tokenType: String,

    @JsonProperty("id_token")
    @field:Schema(description = "OpenID Connect ID token", nullable = true)
    val idToken: String?,

    @JsonProperty("not_before_policy")
    @field:Schema(description = "Политика Keycloak not-before", example = "0", nullable = true)
    val notBeforePolicy: Int?,

    @JsonProperty("session_state")
    @field:Schema(description = "Идентификатор сессии Keycloak", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
    val sessionState: String?,

    @JsonProperty("scope")
    @field:Schema(description = "Выданные OAuth2 scopes", example = "openid profile email", nullable = true)
    val scope: String?
)
