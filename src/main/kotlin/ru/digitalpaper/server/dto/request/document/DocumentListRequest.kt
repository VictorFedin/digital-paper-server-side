package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Sort
import ru.digitalpaper.server.model.document.holder.DocumentType

@Schema(description = "Параметры получения страницы документов")
data class DocumentListRequest(
    @field:Schema(description = "Номер страницы, начиная с 1", example = "1", minimum = "1")
    val page: Int = 1,

    @field:Schema(description = "Количество документов на странице", example = "10", minimum = "1")
    val size: Int = 10,

    @field:Schema(
        description = "Поле сортировки",
        example = "createdAt",
        allowableValues = ["id", "name", "type", "status", "createdAt", "updatedAt"]
    )
    val sortField: String = "createdAt",

    @field:Schema(description = "Направление сортировки", example = "DESC")
    val sortDirection: Sort.Direction = Sort.Direction.DESC,

    @field:Schema(description = "Фильтр по категории документа", example = "FINANCIAL", nullable = true)
    val type: DocumentType? = null,

    @field:Schema(description = "Поисковая строка по названию документа", example = "договор", nullable = true)
    val search: String? = null,
)
