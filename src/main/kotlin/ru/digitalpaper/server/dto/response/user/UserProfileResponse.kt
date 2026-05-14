package ru.digitalpaper.server.dto.response.user

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class UserProfileResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val birthday: LocalDate? = null,
    val avatar: AvatarResponse? = null,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
