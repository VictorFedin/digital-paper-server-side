package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.Document
import java.util.*

@Repository
interface DocumentRepo : JpaRepository<Document, UUID>, DocumentCustomRepo {

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
