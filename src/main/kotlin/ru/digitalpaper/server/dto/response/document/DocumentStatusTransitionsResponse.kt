package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.model.document.holder.DocumentStatus

data class DocumentStatusTransitionsResponse(
    val currentStatus: DocumentStatus,
    val availableStatuses: Set<DocumentStatus>
)
