package ru.digitalpaper.server.dto.response.user

import ru.digitalpaper.server.dto.response.Response
import java.time.ZonedDateTime
import java.util.UUID

data class UserProfileResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) : Response()