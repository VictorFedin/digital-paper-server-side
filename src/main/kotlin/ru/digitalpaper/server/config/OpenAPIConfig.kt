package ru.digitalpaper.server.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Value($$"${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var keycloakAddress: String

    @Bean
    fun customOpenAPI(): OpenAPI? =
        OpenAPI()
            .info(
                Info()
                    .title("DigitalPaper API")
                    .version("v1.0.0")
                    .description("Серверный API для проекта DigitalPaper API")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "oauth2",
                        SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                OAuthFlows()
                                    .implicit(
                                        OAuthFlow()
                                            .authorizationUrl("$keycloakAddress/protocol/openid-connect/auth")
                                    )
                            )
                    )
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList("oauth2")
            )
}