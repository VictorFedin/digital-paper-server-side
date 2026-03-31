package ru.digitalpaper.server.dto.request.document

import ru.digitalpaper.server.model.document.holder.DocumentType
import java.util.UUID

data class CreateDocumentRequest(
    val name: String,
    val description: String? = null,
    val type: DocumentType,
    val folderId: UUID? = null
)
