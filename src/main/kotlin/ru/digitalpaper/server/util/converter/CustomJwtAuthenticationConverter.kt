package ru.digitalpaper.server.util.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.service.UserIdentityService

@Component
class CustomJwtAuthenticationConverter(
    private val userIdentityService: UserIdentityService
) : Converter<Jwt, UsernamePasswordAuthenticationToken> {

    override fun convert(jwt: Jwt): UsernamePasswordAuthenticationToken {
        /* Required */
        val sub = jwt.getClaimAsString("sub")
            ?: throw IllegalStateException("sub claim is null")
        val email = jwt.getClaimAsString("email")
            ?: throw IllegalStateException("email claim is null")

        jwt.claims.forEach {
            println("${it.key}: ${it.value}")
        }

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

        val user = userIdentityService.getOrCreateUser(
            sub = sub,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
        )

        val userPayload = UserPayload(
            id = user.id,
            sub = sub,
            firstName = firstName,
            lastName = lastName,
            email = email,
            verified = verified,
            roles = roles
        )

        return UsernamePasswordAuthenticationToken(userPayload, jwt, roles.map { SimpleGrantedAuthority("ROLE_$it") })
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val realmAccess = jwt.getClaim("realm_access") as? Map<*, *> ?: return emptyList()
        return (realmAccess["roles"] as? Collection<*>)?.mapNotNull { it?.toString() } ?: emptyList()
    }

}