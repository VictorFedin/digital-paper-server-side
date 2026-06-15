package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.DocumentType
import java.util.UUID

@Schema(description = "Метаданные нового документа")
data class CreateDocumentRequest(
    @field:Schema(description = "Название документа", example = "Договор поставки №42")
    val name: String,

    @field:Schema(description = "Категория документа", example = "JURIDICAL")
    val type: DocumentType,

    @field:Schema(
        description = "Идентификатор папки для размещения документа",
        example = "550e8400-e29b-41d4-a716-446655440000",
        format = "uuid",
        nullable = true
    )
    val folderId: UUID? = null
)
