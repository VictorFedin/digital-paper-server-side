package ru.digitalpaper.server.dto.response.user

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Профиль текущего пользователя")
data class UserProfileResponse(
    @field:Schema(description = "Идентификатор пользователя", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Email пользователя", example = "user@example.com", format = "email")
    val email: String,

    @field:Schema(description = "Имя", example = "Иван")
    val firstName: String,

    @field:Schema(description = "Фамилия", example = "Иванов")
    val lastName: String,

    @field:Schema(description = "Отчество", example = "Иванович")
    val middleName: String,

    @field:Schema(description = "Дата рождения", example = "1990-05-20", format = "date", nullable = true)
    val birthday: LocalDate? = null,

    @field:Schema(description = "Аватар пользователя", nullable = true)
    val avatar: AvatarResponse? = null,

    @field:Schema(description = "Дата и время создания профиля", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)
