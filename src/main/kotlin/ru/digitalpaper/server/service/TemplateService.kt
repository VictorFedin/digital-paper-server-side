package ru.digitalpaper.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.response.document.TemplateFieldResponse
import ru.digitalpaper.server.dto.response.document.TemplateResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.exception.NotFoundException
import ru.digitalpaper.server.model.document.DocumentTemplate
import ru.digitalpaper.server.model.document.DocumentTemplateField
import ru.digitalpaper.server.repository.TemplateRepo
import ru.digitalpaper.server.type.StorageObjectType
import java.util.UUID

@Service
class TemplateService(
    private val organizationService: OrganizationService,
    private val storageService: StorageService,
    private val templateRepo: TemplateRepo,
    private val docxTemplateParser: DocxTemplateParser
) {

    @Transactional(readOnly = true)
    fun getTemplateDetails(
        payload: UserPayload,
        templateId: UUID,
    ): TemplateResponse {
        val relation = organizationService.getRelationByUserId(payload.id)

        val template = templateRepo.findByIdAndOrganizationId(templateId, relation.organization.id)
            ?: throw NotFoundException("Шаблон не найден")

        return template.toResponse()
    }

    @Transactional
    fun upload(
        payload: UserPayload,
        file: MultipartFile,
        name: String,
    ): TemplateResponse {
        val relation = organizationService.getRelationByUserId(payload.id)

        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            throw BadRequestException("Название шаблона не может быть пустым")
        }

        val parsedFields = docxTemplateParser.parse(file)

        val storedFileInfo = storageService.upload(
            file = file,
            type = StorageObjectType.TEMPLATE,
            ownerId = relation.organization.id.toString()
        )

        val template = DocumentTemplate(
            name = name,
            path = storedFileInfo.objectKey,
            organization = relation.organization,
            author = relation.user
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

    fun DocumentTemplate.toResponse(): TemplateResponse =
        TemplateResponse(
            id = id,
            name = name,
            organizationId = organization.id,
            createdBy = author.id,
            fields = fields.map { it.toResponse() },
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
}

