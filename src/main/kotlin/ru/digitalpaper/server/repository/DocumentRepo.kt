package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.Document
import java.util.*

@Repository
interface DocumentRepo : JpaRepository<Document, UUID> {

    @Query(
        value = """
            SELECT d
            FROM Document d
            WHERE d.organization.id = :organizationId
        """
    )
    fun getDocumentsByOrganizationId(
        organizationId: UUID,
        pageable: Pageable
    ): Page<Document>
}