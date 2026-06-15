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
            setOf(DocumentStatus.IN_PROGRESS),
            policy.availableFrom(DocumentStatus.CREATED)
        )
        assertEquals(
            setOf(DocumentStatus.PENDING_REVIEW),
            policy.availableFrom(DocumentStatus.IN_PROGRESS)
        )
        assertEquals(
            setOf(DocumentStatus.DONE, DocumentStatus.REJECTED),
            policy.availableFrom(DocumentStatus.PENDING_REVIEW)
        )
        assertEquals(
            setOf(DocumentStatus.IN_PROGRESS),
            policy.availableFrom(DocumentStatus.REJECTED)
        )
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.DONE))
        assertEquals(emptySet(), policy.availableFrom(DocumentStatus.DELETED))
    }

    @Test
    fun `accepts allowed transition`() {
        policy.validate(
            currentStatus = DocumentStatus.PENDING_REVIEW,
            targetStatus = DocumentStatus.DONE
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
