package ru.digitalpaper.server.dto.response.department

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.UUID

@Schema(description = "Данные подразделения организации")
data class DepartmentResponse(
    @field:Schema(description = "Идентификатор подразделения", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название подразделения", example = "Юридический отдел")
    val name: String,

    @field:Schema(description = "Описание подразделения", example = "Правовое сопровождение организации", nullable = true)
    val description: String?,

    @field:Schema(description = "Контактный телефон подразделения", example = "+7 999 123-45-67", nullable = true)
    val phoneNumber: String?,

    @field:Schema(description = "Email подразделения", example = "legal@example.com", format = "email", nullable = true)
    val email: String?,

    @field:Schema(description = "Категория подразделения", example = "LEGAL")
    val category: String,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)
