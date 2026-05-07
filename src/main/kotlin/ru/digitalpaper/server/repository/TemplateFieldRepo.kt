package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.DocumentTemplateField
import java.util.*

@Repository
interface TemplateFieldRepo : JpaRepository<DocumentTemplateField, UUID>
