package ru.digitalpaper.server.dto.response.user

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(hidden = true)
data class UserPayload(
    val id: UUID,
    val sub: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val verified: Boolean,
    val roles: List<String>,
    val isAdmin: Boolean = roles.any { it.equals("ADMIN", ignoreCase = true) }
)
