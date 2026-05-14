package ru.digitalpaper.server.service

import org.springframework.stereotype.Service
import ru.digitalpaper.server.dto.response.department.DepartmentResponse
import ru.digitalpaper.server.model.department.Department

@Service
class DepartmentService {

    fun Department.toResponse(): DepartmentResponse =
        DepartmentResponse(
            id = id,
            name = name,
            description = description,
            phoneNumber = phoneNumber,
            email = email,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
