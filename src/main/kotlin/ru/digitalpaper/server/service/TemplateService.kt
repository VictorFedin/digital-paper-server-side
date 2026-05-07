package ru.digitalpaper.server.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.dto.response.document.TemplateResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.document.DocumentTemplate
import ru.digitalpaper.server.model.document.DocumentTemplateField
import ru.digitalpaper.server.repository.TemplateFieldRepo
import ru.digitalpaper.server.repository.TemplateRepo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@Service
class TemplateService(
    private val organizationService: OrganizationService,
    private val storageService: StorageService,
    private val templateRepo: TemplateRepo,
    private val docxTemplateParser: DocxTemplateParser,
    private val templateFieldRepo: TemplateFieldRepo
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
    }

    @Transactional
    fun upload(
        payload: UserPayload,
        file: MultipartFile,
        name: String,
        rs: RequestSatellites
    ): Response {
        logger.info(
            ServerLogUtil.info(
                "TemplateService.upload",
                rs.traceId,
                "Enter"
            )
        )

        val relation = organizationService.getRelationByUserId(payload.id, rs)

        val parsedFields = docxTemplateParser.parse(file)

        val storedFileInfo = storageService.upload(
            file,
            StorageObjectType.TEMPLATE,
            relation.organization.id.toString(),
            rs
        )

        val template = DocumentTemplate(
            name = name,
            path = storedFileInfo.objectKey,
            organization = relation.organization,
            author = relation.user
        )

        val result = templateRepo.save(template)


        val fields = parsedFields.mapIndexed { index, parsed ->
            println("index: ${index}")
            DocumentTemplateField(
                template = result,
                key = parsed.key,
                label = parsed.label,
                type = parsed.type,
                required = parsed.required,
                sortOrder = index
            )
        }

        templateFieldRepo.saveAll(fields)

        return result.toResponse()
    }

    fun DocumentTemplate.toResponse(): TemplateResponse =
        TemplateResponse(
            id = id,
            name = name,
            organizationId = organization.id,
            createdBy = author.id,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}

