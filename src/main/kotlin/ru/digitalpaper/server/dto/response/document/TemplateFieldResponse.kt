package ru.digitalpaper.server.dto.response.document

import io.swagger.v3.oas.annotations.media.Schema
import ru.digitalpaper.server.model.document.holder.TemplateFieldType
import java.util.*

@Schema(description = "Редактируемое поле шаблона документа")
data class TemplateFieldResponse(
    @field:Schema(description = "Идентификатор поля", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Машинный ключ поля", example = "employee_name")
    val key: String,

    @field:Schema(description = "Название поля для пользователя", example = "ФИО сотрудника")
    val label: String,

    @field:Schema(description = "Тип значения поля", example = "TEXT")
    val type: TemplateFieldType,

    @field:Schema(description = "Признак обязательного заполнения", example = "false")
    val required: Boolean,

    @field:Schema(description = "Порядок отображения поля", example = "0")
    val sortOrder: Int
)
