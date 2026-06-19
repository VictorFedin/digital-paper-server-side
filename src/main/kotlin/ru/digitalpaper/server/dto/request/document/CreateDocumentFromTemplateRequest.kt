package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.document.holder.DocumentType

@Schema(description = "Запрос на создание DOCX-документа из шаблона")
data class CreateDocumentFromTemplateRequest(
    @field:NotBlank(message = "Название документа не может быть пустым")
    @field:Schema(description = "Название создаваемого документа", example = "Приказ о назначении ответственного")
    val name: String,

    @field:NotNull(message = "Необходимо указать тип документа")
    @field:Schema(description = "Категория создаваемого документа", example = "ADMINISTRATIVE")
    val type: DocumentType?,

    @field:Schema(
        description = "Значения полей шаблона по ключу поля",
        example = """{"organization_name":"ООО Digital Paper","date":"2026-06-17","city":"Москва","title":"О назначении ответственного"}"""
    )
    val fields: Map<String, String> = emptyMap()
)
