package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentType
import java.util.*

@Repository
interface DocumentRepo : JpaRepository<Document, UUID> {

    @Query(
        value = """
            SELECT d
            FROM Document d
            WHERE d.organization.id = :organizationId
            AND (:type IS NULL OR d.type = :type)
        """
    )
    fun getDocumentsByOrganizationId(
        organizationId: UUID,
        type: DocumentType? = null,
        pageable: Pageable
    ): Page<Document>

    @Query(
        value = """
            SELECT d
            FROM Document d
            WHERE d.id = :id
            AND d.organization.id = :organizationId
        """
    )
    fun getDocumentByIdAndOrganizationId(id: UUID, organizationId: UUID): Document?
}