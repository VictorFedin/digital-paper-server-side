package ru.digitalpaper.server.util.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import ru.digitalpaper.server.dto.response.user.UserPayload

class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val sub = jwt.getClaimAsString("sub")
        val firstName = jwt.getClaimAsString("given_name")
        val lastName = jwt.getClaimAsString("family_name")
        val email = jwt.getClaimAsString("email")
        val verified = jwt.getClaimAsBoolean("email_verified")
        val roles = extractRoles(jwt)

        val userPayload = UserPayload(
            sub = sub,
            firstName = firstName,
            lastName = lastName,
            email = email,
            verified = verified,
            roles = roles
        )

        return UsernamePasswordAuthenticationToken(userPayload, "N/A", roles.map { SimpleGrantedAuthority("ROLE_$it") })
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val realmAccess = jwt.getClaim("realm_access") as? Map<*, *> ?: return emptyList()
        return (realmAccess["roles"] as? Collection<*>)?.mapNotNull { it?.toString() } ?: emptyList()
    }

}