package ru.digitalpaper.server.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.internal.StoredObjectInfo
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.model.organization.Organization
import ru.digitalpaper.server.model.organization.UserOrganization
import ru.digitalpaper.server.model.user.User
import ru.digitalpaper.server.model.user.holder.Avatar
import ru.digitalpaper.server.model.user.holder.UserRole
import ru.digitalpaper.server.repository.OrganizationRepo
import ru.digitalpaper.server.repository.UserOrganizationRepo
import ru.digitalpaper.server.repository.UserRepo
import ru.digitalpaper.server.type.StorageObjectType
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrganizationAvatarServiceTest {

    private val organizationRepo = mock(OrganizationRepo::class.java)
    private val userRepo = mock(UserRepo::class.java)
    private val userOrganizationRepo = mock(UserOrganizationRepo::class.java)
    private val invitationService = mock(InvitationService::class.java)
    private val userService = mock(UserService::class.java)
    private val storageService = mock(StorageService::class.java)

    private val service = OrganizationService(
        organizationRepo = organizationRepo,
        userRepo = userRepo,
        userOrganizationRepo = userOrganizationRepo,
        invitationService = invitationService,
        userService = userService,
        storageService = storageService
    )

    @AfterEach
    fun clearRequestContext() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `owner can replace organization avatar`() {
        val user = User(email = "owner@example.com").apply {
            id = UUID.randomUUID()
        }
        val organization = Organization(name = "Organization").apply {
            id = UUID.randomUUID()
        }
        val oldAvatar = Avatar(
            id = UUID.randomUUID(),
            bucket = "public",
            objectKey = "organization_images/old.png",
            fileName = "old.png",
            fileSize = 100,
            contentType = "image/png",
            createdAt = ZonedDateTime.now()
        )
        organization.avatar = oldAvatar

        val membership = UserOrganization(
            user = user,
            organization = organization,
            role = UserRole.OWNER
        )
        val payload = UserPayload(
            id = user.id,
            sub = "subject",
            firstName = "Owner",
            lastName = "User",
            email = user.email,
            verified = true,
            roles = emptyList()
        )
        val file = mock(MultipartFile::class.java)
        val newObjectKey = "organization_images/${organization.id}/new.png"

        `when`(userService.getCurrentUser(payload)).thenReturn(user)
        `when`(userOrganizationRepo.findMembership(user.id, organization.id))
            .thenReturn(membership)
        `when`(file.originalFilename).thenReturn("new.png")
        `when`(
            storageService.upload(
                file,
                StorageObjectType.ORGANIZATION_IMAGE,
                organization.id.toString()
            )
        ).thenReturn(
            StoredObjectInfo(
                bucket = "public",
                objectKey = newObjectKey,
                originalFileName = "new.png",
                contentType = "image/png",
                size = 200,
                etag = null,
                versionId = null
            )
        )

        RequestContextHolder.setRequestAttributes(
            ServletRequestAttributes(
                MockHttpServletRequest().apply {
                    scheme = "http"
                    serverName = "localhost"
                    serverPort = 8080
                }
            )
        )

        val avatarUrl = service.saveOrganizationAvatar(
            id = organization.id,
            payload = payload,
            file = file
        )

        assertEquals(newObjectKey, organization.avatar?.objectKey)
        assertTrue(avatarUrl.startsWith("http://localhost:8080/api/v1/organizations/${organization.id}/avatar?v="))
        verify(storageService).delete(
            oldAvatar.objectKey,
            StorageObjectType.ORGANIZATION_IMAGE
        )
    }
}
