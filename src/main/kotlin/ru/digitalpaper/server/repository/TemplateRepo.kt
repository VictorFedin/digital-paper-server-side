package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.DocumentTemplate
import java.util.*

@Repository
interface TemplateRepo : JpaRepository<DocumentTemplate, UUID> {
    @Query(
        value = """
            SELECT DISTINCT dt
            FROM DocumentTemplate dt
            JOIN FETCH dt.organization
            JOIN FETCH dt.author
            LEFT JOIN FETCH dt.fields
            ORDER BY dt.createdAt DESC
        """
    )
    fun findAllSharedTemplates(): List<DocumentTemplate>

    @Query(
        value = """
            SELECT dt
            FROM DocumentTemplate dt
            WHERE dt.id = :templateId
        """
    )
    fun findByIdAndOrganizationId(templateId: UUID, organizationId: UUID): DocumentTemplate?
}
