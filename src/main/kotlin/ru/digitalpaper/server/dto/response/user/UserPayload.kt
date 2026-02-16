package ru.digitalpaper.server.dto.response.user

data class UserPayload(
    val sub: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val verified: Boolean,
    val roles: List<String>
)
