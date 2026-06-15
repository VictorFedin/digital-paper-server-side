package ru.digitalpaper.server.dto.request.document

import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.document.holder.DocumentStatus

data class ChangeDocumentStatusRequest(
    @field:NotNull(message = "Необходимо указать новый статус документа")
    val status: DocumentStatus?
)
