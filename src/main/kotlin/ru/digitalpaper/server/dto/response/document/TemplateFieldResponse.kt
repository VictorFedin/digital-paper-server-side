package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.model.document.holder.TemplateFieldType
import java.util.*

data class TemplateFieldResponse(
    val id: UUID,
    val key: String,
    val label: String,
    val type: TemplateFieldType,
    val required: Boolean,
    val sortOrder: Int
)
