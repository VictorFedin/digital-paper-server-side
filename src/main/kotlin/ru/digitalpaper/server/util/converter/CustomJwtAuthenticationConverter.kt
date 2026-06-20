package ru.digitalpaper.server.util.converter

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import ru.digitalpaper.server.config.properties.KeycloakClientProperties
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.InvitationService
import ru.digitalpaper.server.service.UserIdentityService

@Component
class CustomJwtAuthenticationConverter(
    private val userIdentityService: UserIdentityService,
    private val invitationService: InvitationService,
    private val keycloakClientProperties: KeycloakClientProperties
) : Converter<Jwt, UsernamePasswordAuthenticationToken> {

    override fun convert(jwt: Jwt): UsernamePasswordAuthenticationToken {
        /* Required */
        val sub = jwt.getClaimAsString("sub")
            ?: throw IllegalStateException("sub claim is null")
        val email = jwt.getClaimAsString("email")
            ?: throw IllegalStateException("email claim is null")

        /* Fallback handlers */
        val firstName = jwt.getClaimAsString("given_name")
            ?: ""
        val lastName = jwt.getClaimAsString("family_name")
            ?: ""
        val middleName = jwt.getClaimAsString("middle_name")
            ?: ""
        val verified = jwt.getClaimAsBoolean("email_verified")
            ?: false

        val roles = extractRoles(jwt)

        val isAdmin: Boolean = roles.any {
            it.equals("ADMIN", ignoreCase = true) ||
                it.equals("DIGITAL-PAPER-SUPER-ADMIN", ignoreCase = true)
        }

        val user = userIdentityService.getOrCreateUser(
            sub = sub,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
        )

        invitationService.acceptInvitation(user)

        val userPayload = UserPayload(
            id = user.id,
            sub = sub,
            firstName = firstName,
            lastName = lastName,
            email = email,
            verified = verified,
            roles = roles,
            isAdmin = isAdmin
        )

        return UsernamePasswordAuthenticationToken(userPayload, jwt, roles.map { SimpleGrantedAuthority("ROLE_$it") })
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val realmRoles = extractRealmRoles(jwt)
        val clientRoles = extractClientRoles(jwt)

        return (realmRoles + clientRoles)
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun extractRealmRoles(jwt: Jwt): List<String> {
        val realmAccess = jwt.getClaim("realm_access") as? Map<*, *> ?: return emptyList()
        return extractRoleNames(realmAccess)
    }

    private fun extractClientRoles(jwt: Jwt): List<String> {
        val resourceAccess = jwt.getClaim("resource_access") as? Map<*, *> ?: return emptyList()
        val clientAccess = resourceAccess[keycloakClientProperties.clientId] as? Map<*, *> ?: return emptyList()
        return extractRoleNames(clientAccess)
    }

    private fun extractRoleNames(access: Map<*, *>): List<String> {
        return (access["roles"] as? Collection<*>)
            ?.mapNotNull { it?.toString() }
            ?: emptyList()
    }

}
