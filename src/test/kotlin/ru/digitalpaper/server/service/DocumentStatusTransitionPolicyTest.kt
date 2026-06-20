package ru.digitalpaper.server.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.digitalpaper.server.exception.BadRequestException
import ru.digitalpaper.server.model.document.holder.DocumentStatus
import kotlin.test.assertEquals

class DocumentStatusTransitionPolicyTest {

    private val policy = DocumentStatusTransitionPolicy()

    @Test
    fun `returns available transitions for every status`() {
        assertEquals(
            setOf(DocumentStatus.CREATED, DocumentStatus.CANCELLED, DocumentStatus.EXPIRED),
            policy.availableFrom(DocumentStatus.DRAFT)
        )
        assertEquals(
            setOf(
                DocumentStatus.IN_PROGRESS,
                DocumentStatus.PENDING_REVIEW,
                DocumentStatus.CANCELLED,
                DocumentStatus.EXPIRED
            ),
            policy.availableFrom(DocumentStatus.CREATED)
        )
        assertEquals(
            setOf(DocumentStatus.PENDING_REVIEW, DocumentStatus.CANCELLED, DocumentStatus.EXPIRED),
            policy.availableFrom(DocumentStatus.IN_PROGRESS)
        )
        assertEquals(
            setOf(
                DocumentStatus.APPROVED,
                DocumentStatus.CHANGES_REQUESTED,
                DocumentStatus.REJECTED,
                DocumentStatus.EXPIRED
            ),
            policy.availableFrom(DocumentStatus.PENDING_REVIEW)
        )
        assertEquals(
            setOf(
                DocumentStatus.IN_PROGRESS,
                DocumentStatus.PENDING_REVIEW,
                DocumentStatus.CANCELLED,
                DocumentStatus.EXPIRED
            ),
            policy.availableFrom(DocumentStatus.CHANGES_REQUESTED)
        )
        assertEquals(
            setOf(DocumentStatus.SIGNED, DocumentStatus.DONE, DocumentStatus.EXPIRED),
            policy.availableFrom(DocumentStatus.APPROVED)
        )
        assertEquals(
            setOf(DocumentStatus.DONE),
            policy.availableFrom(DocumentStatus.SIGNED)
        )
        assertEquals(
            setOf(
                DocumentStatus.IN_PROGRESS,
                DocumentStatus.PENDING_REVIEW,
                DocumentStatus.CANCELLED,
                DocumentStatus.EXPIRED
            ),
            policy.availableFrom(DocumentStatus.REJECTED)
        )
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.DONE))
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.CANCELLED))
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.EXPIRED))
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.DELETED))
    }

    @Test
    fun `accepts allowed transition`() {
        policy.validate(
            currentStatus = DocumentStatus.CREATED,
            targetStatus = DocumentStatus.PENDING_REVIEW
        )
    }

    @Test
    fun `rejects skipped transition`() {
        assertThrows<BadRequestException> {
            policy.validate(
                currentStatus = DocumentStatus.CREATED,
                targetStatus = DocumentStatus.DONE
            )
        }
    }

    @Test
    fun `rejects transition from terminal status`() {
        assertThrows<BadRequestException> {
            policy.validate(
                currentStatus = DocumentStatus.DONE,
                targetStatus = DocumentStatus.IN_PROGRESS
            )
        }
    }

    @Test
    fun `rejects workflow transition to deleted status`() {
        assertThrows<BadRequestException> {
            policy.validate(
                currentStatus = DocumentStatus.IN_PROGRESS,
                targetStatus = DocumentStatus.DELETED
            )
        }
    }
}
