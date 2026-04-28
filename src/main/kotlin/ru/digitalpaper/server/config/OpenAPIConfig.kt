package ru.digitalpaper.server.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.*
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    companion object {
        private const val SECURITY_SCHEME_NAME = "keycloakOAuth2"
    }

    @Value($$"${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuerUri: String

    @Bean
    fun customOpenAPI(): OpenAPI? =
        OpenAPI()
            .info(apiInfo())
            .servers(apiServers())
            .components(
                Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        keycloakOAuth2Scheme()
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList(
                    SECURITY_SCHEME_NAME,
                    listOf("openid", "profile", "email")
                )
            )

    private fun apiInfo(): Info {
        return Info()
            .title("DigitalPaper API")
            .version("v1.0.0")
            .description(
                """
                Серверный API системы DigitalPaper.

                Основные возможности:
                - авторизация пользователей;
                - работа с документами;
                - работа с номенклатурой дел;
                - загрузка и скачивание файлов;
                - управление статусами документов.
                """.trimIndent()
            )
            .contact(
                Contact()
                    .name("DigitalPaper Team")
            )
            .license(
                License()
                    .name("Private")
            )
    }

    private fun apiServers(): List<Server> {
        return listOf(
            Server()
                .url("http://localhost:8083")
                .description("Local environment")
        )
    }

    private fun keycloakOAuth2Scheme(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .description("Авторизация через Keycloak")
            .flows(
                OAuthFlows()
                    .authorizationCode(
                        OAuthFlow()
                            .authorizationUrl("$issuerUri/protocol/openid-connect/auth")
                            .tokenUrl("$issuerUri/protocol/openid-connect/token")
                            .scopes(
                                Scopes()
                                    .addString("openid", "OpenID Connect")
                                    .addString("profile", "User profile")
                                    .addString("email", "User email")
                            )
                    )
            )
    }
}