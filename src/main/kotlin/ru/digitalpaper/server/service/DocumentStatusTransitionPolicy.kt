package ru.digitalpaper.server.service

import org.springframework.stereotype.Component
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.model.document.holder.DocumentStatus

@Component
class DocumentStatusTransitionPolicy {

    private val transitions = mapOf(
        DocumentStatus.DRAFT to setOf(
            DocumentStatus.CREATED,
            DocumentStatus.CANCELLED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.CREATED to setOf(
            DocumentStatus.IN_PROGRESS,
            DocumentStatus.PENDING_REVIEW,
            DocumentStatus.CANCELLED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.IN_PROGRESS to setOf(
            DocumentStatus.PENDING_REVIEW,
            DocumentStatus.CANCELLED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.PENDING_REVIEW to setOf(
            DocumentStatus.APPROVED,
            DocumentStatus.CHANGES_REQUESTED,
            DocumentStatus.REJECTED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.CHANGES_REQUESTED to setOf(
            DocumentStatus.IN_PROGRESS,
            DocumentStatus.PENDING_REVIEW,
            DocumentStatus.CANCELLED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.APPROVED to setOf(
            DocumentStatus.SIGNED,
            DocumentStatus.DONE,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.SIGNED to setOf(DocumentStatus.DONE),
        DocumentStatus.REJECTED to setOf(
            DocumentStatus.IN_PROGRESS,
            DocumentStatus.PENDING_REVIEW,
            DocumentStatus.CANCELLED,
            DocumentStatus.EXPIRED
        ),
        DocumentStatus.DONE to emptySet(),
        DocumentStatus.CANCELLED to emptySet(),
        DocumentStatus.EXPIRED to emptySet(),
        DocumentStatus.DELETED to emptySet()
    )

    fun availableFrom(status: DocumentStatus): Set<DocumentStatus> =
        transitions.getValue(status)

    fun validate(currentStatus: DocumentStatus, targetStatus: DocumentStatus) {
        if (targetStatus !in availableFrom(currentStatus)) {
            throw BadRequestException(
                "Переход документа из статуса '$currentStatus' в '$targetStatus' недопустим"
            )
        }
    }
}
