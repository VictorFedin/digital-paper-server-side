package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.config.decorator.CurrentOrganization
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.controller.base.CommonApiResponses
import ru.digitalpaper.server.dto.request.document.CreateDocumentFromTemplateRequest
import ru.digitalpaper.server.dto.response.document.DocumentResponse
import ru.digitalpaper.server.dto.response.document.TemplateResponse
import ru.digitalpaper.server.service.TemplateService
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/templates"])
@Validated
@CommonApiResponses
@Tag(name = "Шаблоны документов", description = "Загрузка DOCX-шаблонов и получение найденных редактируемых полей")
class DocumentTemplateController(
    private val templateService: TemplateService
) {

    @Operation(
        summary = "Получить шаблон документа",
        description = "Возвращает шаблон текущей организации и список найденных Content Controls или legacy-полей"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Операция успешна",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = TemplateResponse::class)
        )]
    )
    @GetMapping(value = ["/{id}"])
    fun getTemplateById(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор шаблона", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable("id") templateId: UUID
    ): TemplateResponse {
        return templateService.getTemplateDetails(context, templateId)
    }


    @Operation(
        summary = "Загрузить шаблон документа",
        description = "Загружает DOCX-шаблон, извлекает Content Controls и поля формата \${field_name}, затем сохраняет метаданные"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Операция успешна",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = TemplateResponse::class)
        )],
    )
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadTemplate(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(
            description = "DOCX-файл шаблона размером до 50 МБ",
            required = true,
            schema = Schema(type = "string", format = "binary")
        )
        @RequestPart("file") file: MultipartFile,
        @Parameter(description = "Название шаблона", example = "Трудовой договор", required = true)
        @RequestParam("name") name: String,
    ): TemplateResponse {
        return templateService.upload(context, file, name)
    }

    @Operation(
        summary = "Создать документ из шаблона",
        description = "Генерирует DOCX на основе шаблона и переданных значений полей, сохраняет файл и создаёт документ"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Документ создан",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = DocumentResponse::class)
        )]
    )
    @PostMapping(value = ["/{id}/documents"])
    fun createDocumentFromTemplate(
        @CurrentOrganization context: OrganizationContext,
        @Parameter(description = "Идентификатор шаблона", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable("id") templateId: UUID,
        @Valid @RequestBody request: CreateDocumentFromTemplateRequest
    ): DocumentResponse {
        return templateService.createDocument(context, templateId, request)
    }
}
