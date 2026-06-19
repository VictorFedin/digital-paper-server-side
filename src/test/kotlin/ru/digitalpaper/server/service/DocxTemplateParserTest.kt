package ru.digitalpaper.server.service

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import ru.digitalpaper.server.model.document.holder.TemplateFieldType
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class DocxTemplateParserTest {

    private val parser = DocxTemplateParser()

    @Test
    fun `parses legacy placeholders`() {
        val file = createDocument {
            createParagraph().createRun().setText("Employee: \${employee_name}")
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("employee_name", fields.single().key)
        assertEquals("Employee Name", fields.single().label)
    }

    @Test
    fun `parses content control using tag and title`() {
        val file = createDocument {
            val paragraph = createParagraph()
            val control = paragraph.ctp.addNewSdt()
            control.addNewSdtPr().apply {
                addNewTag().`val` = "employee_name"
                addNewAlias().`val` = "ФИО сотрудника"
            }
            control.addNewSdtContent().addNewR().addNewT().stringValue = "Введите ФИО"
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("employee_name", fields.single().key)
        assertEquals("ФИО сотрудника", fields.single().label)
    }

    @Test
    fun `content control takes precedence over legacy placeholder with same key`() {
        val file = createDocument {
            val paragraph = createParagraph()
            val control = paragraph.ctp.addNewSdt()
            control.addNewSdtPr().apply {
                addNewTag().`val` = "employee_name"
                addNewAlias().`val` = "ФИО сотрудника"
            }
            control.addNewSdtContent().addNewR().addNewT().stringValue = "\${employee_name}"
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("employee_name", fields.single().key)
        assertEquals("ФИО сотрудника", fields.single().label)
    }

    @Test
    fun `uses normalized title when content control has no tag`() {
        val file = createDocument {
            val paragraph = createParagraph()
            val control = paragraph.ctp.addNewSdt()
            control.addNewSdtPr().addNewAlias().`val` = "ФИО сотрудника"
            control.addNewSdtContent().addNewR().addNewT().stringValue = "Введите ФИО"
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("фио_сотрудника", fields.single().key)
        assertEquals("ФИО сотрудника", fields.single().label)
    }

    @Test
    fun `parses date content control by key`() {
        val file = createDocument {
            val paragraph = createParagraph()
            val control = paragraph.ctp.addNewSdt()
            control.addNewSdtPr().apply {
                addNewTag().`val` = "date"
                addNewAlias().`val` = "Дата"
            }
            control.addNewSdtContent().addNewR().addNewT().stringValue = "16.06.2026"
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("date", fields.single().key)
        assertEquals(TemplateFieldType.DATE, fields.single().type)
    }

    @Test
    fun `parses date content control by russian title`() {
        val file = createDocument {
            val paragraph = createParagraph()
            val control = paragraph.ctp.addNewSdt()
            control.addNewSdtPr().addNewAlias().`val` = "Дата приказа"
            control.addNewSdtContent().addNewR().addNewT().stringValue = "16.06.2026"
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("дата_приказа", fields.single().key)
        assertEquals(TemplateFieldType.DATE, fields.single().type)
    }

    @Test
    fun `parses legacy date placeholder`() {
        val file = createDocument {
            createParagraph().createRun().setText("Date: \${date}")
        }

        val fields = parser.parse(file)

        assertEquals(1, fields.size)
        assertEquals("date", fields.single().key)
        assertEquals(TemplateFieldType.DATE, fields.single().type)
    }

    private fun createDocument(configure: XWPFDocument.() -> Unit): MockMultipartFile {
        val bytes = ByteArrayOutputStream().use { output ->
            XWPFDocument().use { document ->
                document.configure()
                document.write(output)
            }
            output.toByteArray()
        }

        return MockMultipartFile(
            "file",
            "template.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            bytes
        )
    }
}
