package ru.digitalpaper.server.model.user.holder

enum class UserRole {
    EMPLOYEE,
    OWNER,
    ADMIN,
    LAWYER;

    fun canManageOrganization(): Boolean =
        when (this) {
            OWNER, ADMIN -> true
            EMPLOYEE, LAWYER -> false
        }
}