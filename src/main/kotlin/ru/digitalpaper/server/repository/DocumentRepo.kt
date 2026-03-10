package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.Document
import java.util.UUID

@Repository
interface DocumentRepo : JpaRepository<Document, UUID> {

    @Query(
        value = """
            SELECT d
            FROM Document d
        """
    )
    fun getDocuments(pageable: Pageable): Page<Document>
}