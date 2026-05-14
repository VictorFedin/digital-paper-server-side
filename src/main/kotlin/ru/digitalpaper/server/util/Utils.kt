package ru.digitalpaper.server.util

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Order
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import ru.digitalpaper.server.exception.BadRequestException

object Utils {

    fun safePage(
        page: Int
    ): Int {
        return (page - 1).coerceAtLeast(0)
    }

    fun safeSize(
        size: Int
    ): Int {
        return when {
            size <= 0 -> 10
            size > 100 -> 100
            else -> size
        }
    }

    fun safeDirection(
        direction: String
    ): Sort.Direction {
        return if (direction.contains("ASC", true))
            Sort.Direction.ASC
        else Sort.Direction.DESC
    }

    fun <T> applySorting(
        cb: CriteriaBuilder,
        query: CriteriaQuery<T>,
        pageable: Pageable,
        defaultOrder: Order,
        sortMapping: Map<String, Expression<out Comparable<*>>>,
    ) {
        if (pageable.sort.isUnsorted) {
            query.orderBy(defaultOrder)
            return
        }

        val orders = pageable.sort.map { sortOrder ->
            val expression = sortMapping[sortOrder.property]
                ?: throw BadRequestException("Unsupported sort field: ${sortOrder.property}")

            if (sortOrder.isAscending) {
                cb.asc(expression)
            } else {
                cb.desc(expression)
            }
        }.toList()

        query.orderBy(orders)
    }

}
