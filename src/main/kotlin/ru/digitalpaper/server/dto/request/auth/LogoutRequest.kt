package ru.digitalpaper.server.dto.request.auth

data class LogoutRequest(
    val refreshToken: String
)