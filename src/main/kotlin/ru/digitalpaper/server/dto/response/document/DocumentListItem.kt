package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Краткая информация о документе в списке")
data class DocumentListItem(
    @field:Schema(description = "Идентификатор документа", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название документа", example = "Договор поставки №42")
    val name: String,

    @field:Schema(description = "Идентификатор автора документа", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val createdById: UUID,

    @field:Schema(description = "ФИО ответственного сотрудника", example = "Иванов И.И.")
    val responsible: String,

    @field:Schema(description = "Текущий статус документа", example = "IN_PROGRESS")
    val status: DocumentStatus,

    @field:Schema(description = "MIME-тип файла", example = "application/pdf")
    val contentType: String,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)
