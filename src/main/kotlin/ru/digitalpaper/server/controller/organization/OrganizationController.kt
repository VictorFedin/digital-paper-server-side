package ru.digitalpaper.server.controller.organization

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import ru.digitalpaper.server.controller.base.CommonApiResponses
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
@CommonApiResponses
@Tag(name = "Организации", description = "Организации, сотрудники, роли и приглашения")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Номер страницы, начиная с 1", example = "1")
        @RequestParam(required = false) page: Int = 1,
        @Parameter(description = "Количество элементов на странице", example = "10")
        @RequestParam(required = false) size: Int = 10,
        @Parameter(
            description = "Поле сортировки",
            example = "createdAt",
            schema = Schema(allowableValues = ["id", "name", "email", "status", "createdAt", "updatedAt"])
        )
        @RequestParam(required = false) sortField: String = "createdAt",
        @Parameter(description = "Направление сортировки", example = "DESC", schema = Schema(allowableValues = ["ASC", "DESC"]))
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
        description = "Возвращает страницу организаций, доступных в системе"
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
        @Parameter(description = "Номер страницы, начиная с 1", example = "1")
        @RequestParam(required = false) page: Int = 1,
        @Parameter(description = "Количество элементов на странице", example = "10")
        @RequestParam(required = false) size: Int = 10,
        @Parameter(
            description = "Поле сортировки",
            example = "createdAt",
            schema = Schema(allowableValues = ["id", "name", "email", "status", "createdAt", "updatedAt"])
        )
        @RequestParam(required = false) sortField: String = "createdBy",
        @Parameter(description = "Направление сортировки", example = "DESC", schema = Schema(allowableValues = ["ASC", "DESC"]))
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
        @Parameter(hidden = true)
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID,
        @Parameter(description = "Номер страницы, начиная с 1", example = "1")
        @RequestParam page: Int = 1,
        @Parameter(description = "Количество сотрудников на странице", example = "10")
        @RequestParam size: Int = 10,
        @Parameter(
            description = "Поле сортировки",
            example = "createdAt",
            schema = Schema(allowableValues = ["id", "email", "name", "createdAt", "updatedAt"])
        )
        @RequestParam sortField: String = "createdAt",
        @Parameter(description = "Направление сортировки", example = "DESC")
        @RequestParam sortDirection: Sort.Direction = Sort.Direction.DESC,
        @Parameter(description = "Поиск по email, имени, фамилии или отчеству", example = "Иванов")
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
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable("id") organizationId: UUID,
        @Parameter(description = "Номер месяца от 1 до 12", example = "6", schema = Schema(minimum = "1", maximum = "12"))
        @RequestParam month: Int,
    ): UsersListResponse {
        return organizationService.getOrganizationUsersBirthdays(payload, organizationId, month)
    }
}
