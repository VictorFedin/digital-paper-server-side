package ru.digitalpaper.server.dto.request.auth

data class LoginRequest(
    val username: String,
    val password: String
)