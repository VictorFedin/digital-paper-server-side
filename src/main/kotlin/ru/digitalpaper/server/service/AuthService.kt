package ru.digitalpaper.server.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import ru.digitalpaper.server.config.properties.KeycloakClientProperties
import ru.digitalpaper.server.dto.request.auth.LoginRequest
import ru.digitalpaper.server.dto.request.auth.LogoutRequest
import ru.digitalpaper.server.dto.request.auth.RefreshTokenRequest
import ru.digitalpaper.server.dto.response.auth.TokenResponse

@Service
class AuthService(private val keycloakClientProperties: KeycloakClientProperties) {

    private val restClient = RestClient.builder().build()

    fun login(request: LoginRequest): TokenResponse {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", keycloakClientProperties.clientId)
            keycloakClientProperties.clientSecret?.takeIf { it.isNotBlank() }?.let {
                add("client_secret", it)
            }
            add("username", request.username)
            add("password", request.password)
        }

        val response = restClient.post()
            .uri(keycloakClientProperties.tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body<TokenResponse>()
            ?: throw IllegalStateException("Empty Keycloak token response")

        return response
    }

    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("client_id", keycloakClientProperties.clientId)
            keycloakClientProperties.clientSecret?.takeIf { it.isNotBlank() }?.let {
                add("client_secret", it)
            }
            add("refresh_token", request.refreshToken)
        }

        val response = restClient.post()
            .uri(keycloakClientProperties.tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body<TokenResponse>()
            ?: throw IllegalStateException("Empty Keycloak refresh response")

        return response
    }

    fun logout(request: LogoutRequest) {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", keycloakClientProperties.clientId)
            keycloakClientProperties.clientSecret?.takeIf { it.isNotBlank() }?.let {
                add("client_secret", it)
            }
            add("refresh_token", request.refreshToken)
        }

        restClient.post()
            .uri(keycloakClientProperties.logoutUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .toBodilessEntity()
    }

}