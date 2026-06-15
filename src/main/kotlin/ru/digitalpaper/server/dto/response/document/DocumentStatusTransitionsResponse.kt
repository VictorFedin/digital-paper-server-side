package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.DocumentStatus

@Schema(description = "Доступные переходы документа по бизнес-процессу")
data class DocumentStatusTransitionsResponse(
    @field:Schema(description = "Текущий статус документа", example = "PENDING_REVIEW")
    val currentStatus: DocumentStatus,

    @field:Schema(
        description = "Статусы, в которые документ разрешено перевести",
        example = "[\"DONE\", \"REJECTED\"]"
    )
    val availableStatuses: Set<DocumentStatus>
)
