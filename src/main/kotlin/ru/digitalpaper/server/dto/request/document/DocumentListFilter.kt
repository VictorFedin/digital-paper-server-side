package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.DocumentType
import java.util.UUID

@Schema(hidden = true)
data class DocumentListFilter(
    val organizationId: UUID? = null,
    val type: DocumentType? = null,
    val search: String? = null,
    val deleted: Boolean? = null
)
