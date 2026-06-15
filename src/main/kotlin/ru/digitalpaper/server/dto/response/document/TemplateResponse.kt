package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Шаблон документа и его редактируемые поля")
data class TemplateResponse(
    @field:Schema(description = "Идентификатор шаблона", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название шаблона", example = "Трудовой договор")
    val name: String,

    @field:Schema(description = "Идентификатор организации-владельца", format = "uuid", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    val organizationId: UUID,

    @field:Schema(description = "Идентификатор автора шаблона", format = "uuid", example = "6ba7b811-9dad-11d1-80b4-00c04fd430c8")
    val createdBy: UUID,

    @field:Schema(description = "Редактируемые поля, найденные в DOCX-шаблоне")
    val fields: List<TemplateFieldResponse>,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)
