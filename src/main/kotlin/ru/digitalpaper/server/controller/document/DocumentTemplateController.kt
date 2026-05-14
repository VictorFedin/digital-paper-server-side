package ru.digitalpaper.server.controller.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.controller.base.CommonApiResponses
import ru.digitalpaper.server.dto.response.document.TemplateResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.TemplateService
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/templates"])
@Validated
@CommonApiResponses
class DocumentTemplateController(
    private val templateService: TemplateService
) {

    @Operation(
        summary = "Получить шаблон документа",
        description = "Возвращает шаблон документа по уникальному идентификатору"
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
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable("id") templateId: UUID
        ): TemplateResponse {
        return templateService.getTemplateDetails(payload, templateId)
    }


    @Operation(
        summary = "Загрузить шаблон документа",
        description = "Возращает шаблон документа"
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
        @AuthenticationPrincipal payload: UserPayload,
        @RequestPart("file") file: MultipartFile,
        @RequestParam("name") name: String,
    ): TemplateResponse {
        return templateService.upload(payload, file, name)
    }
}
