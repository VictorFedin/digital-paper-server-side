package ru.digitalpaper.server.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak.client")
data class KeycloakClientProperties(
    val serverUrl: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String? = null
) {
    val tokenUrl: String
        get() = "$serverUrl/realms/$realm/protocol/openid-connect/token"

    val logoutUrl: String
        get() = "$serverUrl/realms/$realm/protocol/openid-connect/logout"
}