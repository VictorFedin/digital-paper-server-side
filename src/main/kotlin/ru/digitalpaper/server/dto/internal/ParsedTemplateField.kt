package ru.digitalpaper.server.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.TemplateFieldType

@Schema(hidden = true)
data class ParsedTemplateField(
    val key: String,
    val label: String,
    val type: TemplateFieldType,
    val required: Boolean
)
