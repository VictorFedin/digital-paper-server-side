package ru.digitalpaper.server.service

import org.springframework.stereotype.Component
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.model.document.holder.DocumentStatus

@Component
class DocumentStatusTransitionPolicy {

    private val transitions = mapOf(
        DocumentStatus.CREATED to setOf(DocumentStatus.IN_PROGRESS),
        DocumentStatus.IN_PROGRESS to setOf(DocumentStatus.PENDING_REVIEW),
        DocumentStatus.PENDING_REVIEW to setOf(
            DocumentStatus.DONE,
            DocumentStatus.REJECTED
        ),
        DocumentStatus.REJECTED to setOf(DocumentStatus.IN_PROGRESS),
        DocumentStatus.DONE to emptySet(),
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
