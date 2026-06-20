package ru.digitalpaper.server.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.digitalpaper.server.dto.request.organization.OrganizationUserListFilter
import ru.digitalpaper.server.model.organization.UserOrganization

interface UserOrganizationCustomRepo {

    fun getUsersByOrganizationId(
        filter: OrganizationUserListFilter,
        pageable: Pageable
    ): Page<UserOrganization>
}
