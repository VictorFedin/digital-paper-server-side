package ru.digitalpaper.server.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import ru.digitalpaper.server.config.properties.KeycloakClientProperties

@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val keycloakClientProperties: KeycloakClientProperties
) {

    fun sendInvitation(
        email: String,
        organizationName: String,
    ) {
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
