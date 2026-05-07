package ru.digitalpaper.server.dto.internal

import ru.digitalpaper.server.model.document.holder.TemplateFieldType

data class ParsedTemplateField(
    val key: String,
    val label: String,
    val type: TemplateFieldType,
    val required: Boolean
)
