package ru.digitalpaper.server.controller.organization

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import ru.digitalpaper.server.dto.internal.PagedRequest
import ru.digitalpaper.server.dto.request.organization.AddOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.AddUserToOrganizationRequest
import ru.digitalpaper.server.dto.request.organization.UpdateOrganizationRequest
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.dto.response.common.MessageResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationResponse
import ru.digitalpaper.server.dto.response.organization.OrganizationsPagedListResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UsersListResponse
import ru.digitalpaper.server.dto.response.user.UsersPagedListResponse
import ru.digitalpaper.server.service.OrganizationService
import java.util.*

@RestController
@RequestMapping(value = ["/api/v1/organizations"])
@Validated
class OrganizationController(
    private val organizationService: OrganizationService
) {

    @Operation(
        summary = "Получить детали организации",
        description = "Возвращает детали организации по уникальному идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping(value = ["/{id}"])
    fun getOrganizationDetails(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
    ): OrganizationResponse {
        return organizationService.getOrganizationDetails(id, payload)
    }

    @Operation(
        summary = "Получить список организаций пользователя",
        description = "Возвращает список организаций текущего пользователя"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationsPagedListResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping(value = ["/my"])
    fun getMyOrganizationsList(
        @AuthenticationPrincipal payload: UserPayload,
        @RequestParam(required = false) page: Int = 1,
        @RequestParam(required = false) size: Int = 10,
        @RequestParam(required = false) sortField: String = "createdAt",
        @RequestParam(required = false) sortDirection: String = "DESC",
    ): OrganizationsPagedListResponse {
        val request = PagedRequest(
            page = page,
            size = size,
            sortField = sortField,
            sortDirection = sortDirection
        )

        return organizationService.getMyOrganizationsList(
            payload,
            request
        )
    }

    @Operation(
        summary = "Получить список всех организаций",
        description = "Возвращает список организаций в системе"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationsPagedListResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping(value = [""])
    fun getOrganizationsList(
        @RequestParam(required = false) page: Int = 1,
        @RequestParam(required = false) size: Int = 10,
        @RequestParam(required = false) sortField: String = "createdBy",
        @RequestParam(required = false) sortDirection: String = "DESC",
    ): OrganizationsPagedListResponse {
        val request = PagedRequest(
            page = page,
            size = size,
            sortField = sortField,
            sortDirection = sortDirection
        )
        return organizationService.getOrganizationsList(
            request
        )
    }

    @Operation(
        summary = "Создать новую организацию",
        description = "Возвращает детали организации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping(value = [""])
    fun addOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @Valid @RequestBody addOrganizationRequest: AddOrganizationRequest,
    ): OrganizationResponse {
        return organizationService.addOrganization(addOrganizationRequest, payload)
    }

    @Operation(
        summary = "Обновить детали организации",
        description = "Возвращает детали организации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrganizationResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PutMapping(value = ["/{id}"])
    fun updateOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        @Valid @RequestBody updateOrganizationRequest: UpdateOrganizationRequest,
    ): OrganizationResponse {
        return organizationService.updateOrganization(id, updateOrganizationRequest, payload)
    }

    @Operation(
        summary = "Удалить организацию",
        description = "Возращает результат удаления организации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping(value = ["/{id}/delete"])
    fun deleteOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID
    ): MessageResponse {
        return organizationService.deleteOrganization(id, payload)
    }

    @Operation(
        summary = "Восстановить организацию",
        description = "Возращает результат восстановления организации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PatchMapping(value = ["/{id}/restore"])
    fun restoreOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID
    ): MessageResponse {
        return organizationService.restoreOrganization(id, payload)
    }

    @Operation(
        summary = "Добавить пользователя к организации",
        description = "Возвращает результат добавления пользователя"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MessageResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping(value = ["/{id}/users/add"])
    fun addUserToOrganization(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        @Valid @RequestBody addUserToOrganizationRequest: AddUserToOrganizationRequest,
    ): MessageResponse {
        return organizationService.addUserToOrganization(payload, id, addUserToOrganizationRequest)
    }

    @Operation(
        summary = "Получить список сотрудников организации",
        description = "Возвращает список пользователей организации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UsersPagedListResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping(value = ["/{id}/users"])
    fun getOrganizationUsers(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable id: UUID,
        @RequestParam page: Int = 1,
        @RequestParam size: Int = 10,
        @RequestParam sortField: String = "createdAt",
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @RequestParam search: String? = null,
    ): UsersPagedListResponse {
        return organizationService.getOrganizationUsers(id, payload, page, size, sortField, sortDirection, search)
    }

    @Operation(
        summary = "Получить список дней рождений в этом месяце",
        description = "Возвращает список пользователей организации, у которых в этом месяце день рождения"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Операция успешна",
                responseCode = "200",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UsersListResponse::class)
                )]
            ),
            ApiResponse(
                description = "Ошибка сервера",
                responseCode = "500",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping(value = ["/{id}/users/birthday"])
    fun getOrganizationUsersBirthdays(
        @AuthenticationPrincipal payload: UserPayload,
        @PathVariable("id") organizationId: UUID,
        @RequestParam month: Int,
    ): UsersListResponse {
        return organizationService.getOrganizationUsersBirthdays(payload, organizationId, month)
    }
}
