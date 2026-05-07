package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.digitalpaper.server.dto.request.document.DocumentListFilter
import ru.digitalpaper.server.model.document.Document

interface DocumentCustomRepo {

    fun getDocumentsPagedList(
        filter: DocumentListFilter,
        pageable: Pageable
    ): Page<Document>
}
