package ru.digitalpaper.server.service

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertContains

class DocxTemplateRendererTest {

    private val renderer = DocxTemplateRenderer()

    @Test
    fun `renders content controls and legacy placeholders`() {
        val template = createTemplateDocument()

        val rendered = renderer.render(
            template = ByteArrayInputStream(template),
            values = mapOf(
                "organization_name" to "ООО Digital Paper",
                "date" to "2026-06-17"
            )
        )

        val text = XWPFDocument(ByteArrayInputStream(rendered)).use { document ->
            document.paragraphs.joinToString("\n") { it.text }
        }

        assertContains(text, "ООО Digital Paper")
        assertContains(text, "2026-06-17")
    }

    private fun createTemplateDocument(): ByteArray {
        return ByteArrayOutputStream().use { output ->
            XWPFDocument().use { document ->
                val contentControlParagraph = document.createParagraph()
                val control = contentControlParagraph.ctp.addNewSdt()
                control.addNewSdtPr().apply {
                    addNewTag().`val` = "organization_name"
                    addNewAlias().`val` = "Имя организации"
                }
                control.addNewSdtContent().addNewR().addNewT().stringValue = "Организация"

                document.createParagraph()
                    .createRun()
                    .setText("Дата: \${date}")

                document.write(output)
            }

            output.toByteArray()
        }
    }
}
