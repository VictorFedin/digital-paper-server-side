package ru.digitalpaper.server.dto.response.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Long,

    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Long?,

    @JsonProperty("refresh_token")
    val refreshToken: String?,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("id_token")
    val idToken: String?,

    @JsonProperty("not_before_policy")
    val notBeforePolicy: Int?,

    @JsonProperty("session_state")
    val sessionState: String?,

    @JsonProperty("scope")
    val scope: String?
)
