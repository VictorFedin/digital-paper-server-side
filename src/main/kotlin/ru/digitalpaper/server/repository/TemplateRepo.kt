package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.DocumentTemplate
import java.util.*

@Repository
interface TemplateRepo : JpaRepository<DocumentTemplate, UUID>{
    @Query(
        value = """
            SELECT dt
            FROM DocumentTemplate dt
            WHERE dt.id = :templateId
            AND dt.organization.id = :organizationId
        """
    )
    fun findByIdAndOrganizationId(templateId: UUID, organizationId: UUID): DocumentTemplate?
}
