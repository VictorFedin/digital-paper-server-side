package ru.digitalpaper.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.model.document.DocumentTemplate
import java.util.*

@Repository
interface TemplateRepo : JpaRepository<DocumentTemplate, UUID>
