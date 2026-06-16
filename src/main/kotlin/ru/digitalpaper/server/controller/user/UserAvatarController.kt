package ru.digitalpaper.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.InputStreamResource
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.digitalpaper.server.config.decorator.Public
import ru.digitalpaper.server.service.UserService
import java.util.UUID
import java.util.concurrent.TimeUnit

@Public
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Пользователь", description = "Профиль и аватар пользователя")
class UserAvatarController(
    private val userService: UserService
) {

    @Operation(
        summary = "Получить аватар пользователя",
        description = "Возвращает аватар пользователя через backend"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Содержимое изображения",
        content = [Content(
            mediaType = "image/*",
            schema = Schema(type = "string", format = "binary")
        )]
    )
    @GetMapping("/{id}/avatar")
    fun getUserAvatar(
        @Parameter(description = "Идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID
    ): ResponseEntity<InputStreamResource> {
        val file = userService.getUserAvatar(id)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.contentType))
            .contentLength(file.contentLength ?: 0)
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
            .body(file.resource)
    }
}
