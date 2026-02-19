package ru.digitalpaper.server.model.base

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.ZonedDateTime
import java.util.UUID

@MappedSuperclass
abstract class UniqueEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: ZonedDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: ZonedDateTime

    @PrePersist
    fun onCreate() {
        val now = ZonedDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = ZonedDateTime.now()
    }
}
