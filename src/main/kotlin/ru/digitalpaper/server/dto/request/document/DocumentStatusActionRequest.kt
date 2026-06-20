package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос на выполнение действия над статусом документа")
data class DocumentStatusActionRequest(
    @field:Schema(
        description = "Причина или комментарий к действию",
        example = "Необходимо заполнить пункт 3.2",
        nullable = true
    )
    val reason: String? = null
)
