package ru.digitalpaper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.digitalpaper.server.dto.request.document.DocumentListFilter
import ru.digitalpaper.server.model.document.Document
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.document.holder.DocumentType
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.repository.DocumentCustomRepo
import ru.digitalpaper.server.util.Utils
import java.time.ZonedDateTime
import java.util.*

@Repository
class DocumentCustomRepoImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : DocumentCustomRepo {

    override fun getDocumentsPagedList(
        filter: DocumentListFilter,
        pageable: Pageable
    ): Page<Document> {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Document::class.java)
        val root = query.from(Document::class.java)

        val predicates = buildPredicates(
            cb = cb,
            root = root,
            filter = filter,
        )

        query.where(*predicates.toTypedArray())

        applySorting(cb, query, root, pageable)

        val resultQuery = entityManager.createQuery(query)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)

        val documents = resultQuery.resultList

        val total = countDocuments(filter)

        return PageImpl(documents, pageable, total)
    }

    private fun buildPredicates(
        cb: CriteriaBuilder,
        root: Root<Document>,
        filter: DocumentListFilter
    ): MutableList<Predicate> {
        val predicates = mutableListOf<Predicate>()

        if (filter.organizationId != null) {
            predicates.add(
                cb.equal(root.get<Organization>("organization").get<UUID>("id"), filter.organizationId)
            )
        }


        if (filter.type != null) {
            predicates.add(
                cb.equal(root.get<DocumentType>("type"), filter.type)
            )
        }

        if (filter.deleted != null) {
            if (filter.deleted) {
                predicates.add(
                    cb.equal(root.get<DocumentStatus>("status"), DocumentStatus.DELETED)
                )
            } else {
                predicates.add(
                    cb.notEqual(root.get<DocumentStatus>("status"), DocumentStatus.DELETED)
                )
            }
        }

        if (filter.search != null) {

            val normalizedSearch = filter.search
                .trim()
                .lowercase()
                .takeIf { it.isNotBlank() }

            if (normalizedSearch != null) {
                val pattern = "%$normalizedSearch%"

                val responsibleUser = root.join<Document, User>("responsibleUser")

                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(responsibleUser.get("email")), pattern),
                        cb.like(cb.lower(responsibleUser.get("firstName")), pattern),
                        cb.like(cb.lower(responsibleUser.get("lastName")), pattern),
                        cb.like(cb.lower(responsibleUser.get("middleName")), pattern),
                    )
                )
            }
        }

        return predicates
    }

    private fun countDocuments(
        filter: DocumentListFilter,
    ): Long {
        val cb = entityManager.criteriaBuilder

        val query = cb.createQuery(Long::class.java)
        val root = query.from(Document::class.java)

        val predicates = buildPredicates(
            cb = cb,
            root = root,
            filter = filter
        )

        query.select(cb.count(root))
        query.where(*predicates.toTypedArray())

        return entityManager.createQuery(query).singleResult
    }

    private fun applySorting(
        cb: CriteriaBuilder,
        query: CriteriaQuery<Document>,
        root: Root<Document>,
        pageable: Pageable
    ) {
        Utils.applySorting(
            cb = cb,
            query = query,
            pageable = pageable,
            defaultOrder = cb.desc(root.get<ZonedDateTime>("createdAt")),
            sortMapping = mapOf(
                "name" to root.get<String>("name"),
                "type" to root.get<DocumentType>("type"),
                "status" to root.get<DocumentStatus>("status"),
                "updatedAt" to root.get<ZonedDateTime>("updatedAt"),
                "createdAt" to root.get<ZonedDateTime>("createdAt"),
            )
        )
    }

}
