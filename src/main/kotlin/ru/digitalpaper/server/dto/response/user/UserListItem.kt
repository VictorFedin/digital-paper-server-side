package ru.digitalpaper.server.dto.response.user

import ru.digitalpaper.server.dto.response.Response
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class UserListItem(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val birthday: LocalDate? = null,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) : Response()
