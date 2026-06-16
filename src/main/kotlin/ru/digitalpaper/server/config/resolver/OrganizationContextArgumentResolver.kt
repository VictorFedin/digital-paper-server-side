package ru.digitalpaper.server.config.resolver

import org.springframework.core.MethodParameter
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import ru.digitalpaper.server.config.decorator.CurrentOrganization
import ru.digitalpaper.server.context.OrganizationContext
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.exception.ForbiddenException
import ru.digitalpaper.server.service.OrganizationContextService

@Component
class OrganizationContextArgumentResolver(
    private val organizationContextService: OrganizationContextService
) : HandlerMethodArgumentResolver {

    companion object {
        const val HEADER_NAME = "X-Organization-Id"
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentOrganization::class.java) &&
            parameter.parameterType == OrganizationContext::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): OrganizationContext {
        val authentication = webRequest.userPrincipal as? Authentication
        val payload = authentication?.principal as? UserPayload
            ?: throw ForbiddenException("Пользователь не авторизован")

        return organizationContextService.resolve(
            userId = payload.id,
            organizationHeader = webRequest.getHeader(HEADER_NAME)
        )
    }
}
