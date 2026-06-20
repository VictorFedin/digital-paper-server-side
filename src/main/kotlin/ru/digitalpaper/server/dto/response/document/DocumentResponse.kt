package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.document.holder.DocumentType
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Подробная информация о документе")
data class DocumentResponse(
    @field:Schema(description = "Идентификатор документа", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Название документа", example = "Договор поставки №42")
    val name: String,

    @field:Schema(description = "Категория документа", example = "JURIDICAL")
    val type: DocumentType,

    @field:Schema(description = "Текущий статус документа", example = "PENDING_REVIEW")
    val status: DocumentStatus,

    @field:Schema(
        description = "Последняя причина или комментарий к изменению статуса",
        example = "Необходимо заполнить пункт 3.2",
        nullable = true
    )
    val statusReason: String? = null,

    @field:Schema(description = "MIME-тип файла", example = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    val contentType: String,

    @field:Schema(description = "ФИО ответственного сотрудника", example = "Иванов И.И.")
    val responsible: String,

    @field:Schema(description = "ФИО автора документа", example = "Петров П.П.")
    val createdBy: String,

    @field:Schema(description = "Дата и время создания", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime,

    @field:Schema(description = "Дата и время последнего изменения", format = "date-time", example = "2026-06-15T13:30:00+03:00")
    val updatedAt: ZonedDateTime
)
