package ru.digitalpaper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.dto.request.organization.OrganizationUserListFilter
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.repository.UserOrganizationCustomRepo
import ru.digitalpaper.server.util.Utils
import java.time.ZonedDateTime
import java.util.*

@Repository
class UserOrganizationCustomRepoImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : UserOrganizationCustomRepo {

    override fun getUsersByOrganizationId(
        filter: OrganizationUserListFilter,
        pageable: Pageable
    ): Page<UserOrganization> {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(UserOrganization::class.java)
        val root = query.from(UserOrganization::class.java)
        val userJoin = root.join<UserOrganization, User>("user")

        val predicates = buildPredicates(
            cb = cb,
            root = root,
            userJoin = userJoin,
            filter = filter
        )

        query.select(root)
        query.where(*predicates.toTypedArray())

        applySorting(cb, query, userJoin, pageable)

        val memberships = entityManager.createQuery(query)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)
            .resultList

        val total = countUsers(filter)

        return PageImpl(memberships, pageable, total)
    }

    private fun buildPredicates(
        cb: CriteriaBuilder,
        root: Root<UserOrganization>,
        userJoin: Join<UserOrganization, User>,
        filter: OrganizationUserListFilter
    ): MutableList<Predicate> {
        val predicates = mutableListOf<Predicate>()

        if (filter.organizationId != null) {
            predicates.add(
                cb.equal(
                    root.get<Organization>("organization").get<UUID>("id"),
                    filter.organizationId
                )
            )
        }

        if (filter.search != null) {

            val searchWords = filter.search
                .trim()
                .lowercase()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }

            if (searchWords.isNotEmpty()) {
                val wordPredicates = searchWords.map { word ->
                    val pattern = "%$word%"

                    cb.or(
                        cb.like(cb.lower(userJoin.get("firstName")), pattern),
                        cb.like(cb.lower(userJoin.get("lastName")), pattern),
                        cb.like(cb.lower(userJoin.get("middleName")), pattern),
                    )
                }

                predicates.add(cb.and(*wordPredicates.toTypedArray()))
            }
        }

        return predicates
    }

    private fun countUsers(filter: OrganizationUserListFilter): Long {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Long::class.java)
        val root = query.from(UserOrganization::class.java)
        val userJoin = root.join<UserOrganization, User>("user")

        val predicates = buildPredicates(
            cb = cb,
            root = root,
            userJoin = userJoin,
            filter = filter
        )

        query.select(cb.count(root))
        query.where(*predicates.toTypedArray())

        return entityManager.createQuery(query).singleResult
    }

    private fun applySorting(
        cb: CriteriaBuilder,
        query: CriteriaQuery<UserOrganization>,
        userJoin: Join<UserOrganization, User>,
        pageable: Pageable
    ) {
        Utils.applySorting(
            cb = cb,
            query = query,
            pageable = pageable,
            defaultOrder = cb.asc(userJoin.get<String>("email")),
            sortMapping = mapOf(
                "email" to userJoin.get<String>("email"),
                "firstName" to userJoin.get<String>("firstName"),
                "lastName" to userJoin.get<String>("lastName"),
                "middleName" to userJoin.get<String>("middleName"),
                "createdAt" to userJoin.get<ZonedDateTime>("createdAt"),
            )
        )
    }
}
