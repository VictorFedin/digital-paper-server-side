package ru.digitalpaper.server.dto.request.document

import org.springframework.data.domain.Sort
import ru.digitalpaper.server.model.document.holder.DocumentType

data class DocumentListRequest(
    val page: Int = 1,
    val size: Int = 10,
    val sortField: String = "createdAt",
    val sortDirection: Sort.Direction = Sort.Direction.DESC,
    val type: DocumentType? = null,
    val search: String? = null,
)
