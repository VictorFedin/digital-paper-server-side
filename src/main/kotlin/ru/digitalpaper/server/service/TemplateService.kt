package ru.digitalpaper.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.dto.request.document.CreateDocumentFromTemplateRequest
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.TemplateFieldResponse
import ru.digitalpaper.server.dto.response.document.TemplateResponse
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.DocumentTemplate
import ru.digitalpaper.server.model.document.DocumentTemplateField
import ru.digitalpaper.server.repository.DocumentRepo
import ru.digitalpaper.server.repository.TemplateRepo
import ru.digitalpaper.server.type.StorageObjectType
import java.util.UUID

@Service
class TemplateService(
    private val storageService: StorageService,
    private val documentRepo: DocumentRepo,
    private val templateRepo: TemplateRepo,
    private val docxTemplateParser: DocxTemplateParser,
    private val docxTemplateRenderer: DocxTemplateRenderer
) {

    companion object {
        private const val DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    @Transactional(readOnly = true)
    fun getAllTemplates(): List<TemplateResponse> {
        return templateRepo.findAllSharedTemplates()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTemplateDetails(
        context: OrganizationContext,
        templateId: UUID,
    ): TemplateResponse {
        val template = templateRepo.findByIdAndOrganizationId(templateId, context.organization.id)
            ?: throw NotFoundException("Шаблон не найден")

        return template.toResponse()
    }

    @Transactional
    fun upload(
        context: OrganizationContext,
        file: MultipartFile,
        name: String,
    ): TemplateResponse {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            throw BadRequestException("Название шаблона не может быть пустым")
        }

        val parsedFields = docxTemplateParser.parse(file)

        val storedFileInfo = storageService.upload(
            file = file,
            type = StorageObjectType.TEMPLATE,
            ownerId = context.organization.id.toString()
        )

        val template = DocumentTemplate(
            name = normalizedName,
            path = storedFileInfo.objectKey,
            organization = context.organization,
            author = context.user
        )

        val result = templateRepo.save(template)

        val fields = parsedFields.mapIndexed { index, parsed ->
            DocumentTemplateField(
                template = result,
                key = parsed.key,
                label = parsed.label,
                type = parsed.type,
                required = parsed.required,
                sortOrder = index
            )
        }

        template.fields.addAll(fields)
        templateRepo.save(template)

        return result.toResponse()
    }

    @Transactional
    fun createDocument(
        context: OrganizationContext,
        templateId: UUID,
        request: CreateDocumentFromTemplateRequest
    ): DocumentResponse {
        val template = templateRepo.findByIdAndOrganizationId(templateId, context.organization.id)
            ?: throw NotFoundException("Шаблон не найден")
        val documentName = request.name.trim()
        if (documentName.isBlank()) {
            throw BadRequestException("Название документа не может быть пустым")
        }

        val type = request.type
            ?: throw BadRequestException("Необходимо указать тип документа")

        validateTemplateValues(template, request.fields)

        val templateObject = storageService.download(
            objectKey = template.path,
            type = StorageObjectType.TEMPLATE
        )
        val renderedDocx = templateObject.inputStream.use { input ->
            docxTemplateRenderer.render(input, request.fields)
        }

        val storedDocument = storageService.uploadBytes(
            bytes = renderedDocx,
            filename = ensureDocxExtension(documentName),
            contentType = DOCX_CONTENT_TYPE,
            type = StorageObjectType.DOCUMENT,
            ownerId = context.organization.id.toString()
        )

        val document = Document(
            name = documentName,
            path = storedDocument.objectKey,
            type = type,
            contentType = storedDocument.contentType,
            createdBy = context.user,
            responsibleUser = context.user,
            organization = context.organization,
            template = template
        )

        return documentRepo.save(document).toDocumentResponse()
    }

    fun DocumentTemplate.toResponse(): TemplateResponse =
        TemplateResponse(
            id = id,
            name = name,
            organizationId = organization.id,
            createdBy = author.id,
            fields = fields.sortedBy { it.sortOrder }.map { it.toResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    fun DocumentTemplateField.toResponse(): TemplateFieldResponse =
        TemplateFieldResponse(
            id = id,
            key = key,
            label = label,
            type = type,
            required = required,
            sortOrder = sortOrder
        )

    private fun validateTemplateValues(
        template: DocumentTemplate,
        values: Map<String, String>
    ) {
        val fieldsByKey = template.fields.associateBy { it.key }
        val unknownKeys = values.keys - fieldsByKey.keys
        if (unknownKeys.isNotEmpty()) {
            throw BadRequestException("Переданы неизвестные поля шаблона: ${unknownKeys.joinToString(", ")}")
        }

        val missingRequired = template.fields
            .filter { it.required }
            .map { it.key }
            .filter { values[it].isNullOrBlank() }

        if (missingRequired.isNotEmpty()) {
            throw BadRequestException("Не заполнены обязательные поля шаблона: ${missingRequired.joinToString(", ")}")
        }
    }

    private fun ensureDocxExtension(name: String): String {
        return if (name.endsWith(".docx", ignoreCase = true)) {
            name
        } else {
            "$name.docx"
        }
    }

    private fun Document.toDocumentResponse(): DocumentResponse =
        DocumentResponse(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            type = type,
            status = status,
            statusReason = statusReason,
            contentType = contentType,
            responsible = responsibleUser.getShortName(),
            createdBy = createdBy.getShortName()
        )
}

