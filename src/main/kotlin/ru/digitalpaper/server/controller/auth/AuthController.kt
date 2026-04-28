package ru.digitalpaper.server.controller.auth

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.digitalpaper.server.config.decorator.Public
import ru.digitalpaper.server.dto.request.auth.LoginRequest
import ru.digitalpaper.server.dto.request.auth.LogoutRequest
import ru.digitalpaper.server.dto.request.auth.RefreshTokenRequest
import ru.digitalpaper.server.dto.response.auth.TokenResponse
import ru.digitalpaper.server.service.AuthService

@RestController
@RequestMapping(value = ["/api/v1/auth"])
class AuthController(private val authService: AuthService) {

    @Public
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        return authService.login(request)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): TokenResponse {
        return authService.refresh(request)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody request: LogoutRequest) {
        authService.logout(request)
    }
}