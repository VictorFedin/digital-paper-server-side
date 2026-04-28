package ru.digitalpaper.server.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import ru.digitalpaper.server.config.properties.KeycloakClientProperties
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil

@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val keycloakClientProperties: KeycloakClientProperties
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
    }

    fun sendInvitation(
        email: String,
        organizationName: String,
        rs: RequestSatellites
    ) {
        logger.info(
            ServerLogUtil.info(
                "NotificationService.sendInvitation",
                rs.traceId,
                "Enter"
            )
        )

        val registrationUrl = UriComponentsBuilder
            .fromUriString("${keycloakClientProperties.serverUrl}/realms/${keycloakClientProperties.realm}/protocol/openid-connect/registrations")
            .queryParam("client_id", keycloakClientProperties.clientId)
            .queryParam("response_type", "code")
            .queryParam("scope", "openid email profile")
            .queryParam("redirect_uri", "http://localhost:3000")
            .queryParam("login_hint", email)
            .build()
            .encode()
            .toUriString()

        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(email)
        helper.setSubject("Приглашение в организацию $organizationName")
        helper.setText(buildHtml(organizationName, registrationUrl), true)

        mailSender.send(message)
    }

    private fun buildHtml(orgName: String, link: String): String {
        return """
            <html>
              <body>
                <h2>Вас пригласили в организацию "$orgName"</h2>
                <p>Нажмите кнопку ниже, чтобы зарегистрироваться:</p>
                <a href="$link" 
                   style="padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;">
                   Принять приглашение
                </a>
                <p>Если вы не ожидали это письмо — просто проигнорируйте его.</p>
              </body>
            </html>
        """.trimIndent()
    }
}