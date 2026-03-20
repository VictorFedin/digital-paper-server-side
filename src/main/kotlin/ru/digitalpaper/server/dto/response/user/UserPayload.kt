package ru.digitalpaper.server.dto.response.user

import java.util.*

data class UserPayload(
    val id: UUID,
    val sub: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val verified: Boolean,
    val roles: List<String>
)
