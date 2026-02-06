package no.nav.amt.pdfgen

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import no.nav.pdfgen.core.pdf.createHtml
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter

object TestUtils {
    private const val TEMPLATE_DIR = "amt"

    val fixedDate: LocalDate = LocalDate.of(Year.now().value, 2, 15)

    fun LocalDate.toNorwegianShortDate(): String = this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    fun String.assertSectionText(
        pattern: String,
        value: Any?,
    ) {
        if (value != null) {
            if (value is LocalDate) {
                this shouldContain "$pattern ${value.toNorwegianShortDate()}"
            } else {
                this shouldContain "$pattern $value"
            }
        } else {
            this shouldNotContain pattern
        }
    }

    fun render(
        templateName: String,
        payload: Any,
    ): Document =
        Jsoup.parse(
            createHtml(
                template = templateName,
                directoryName = TEMPLATE_DIR,
                jsonPayload = objectMapper.valueToTree<JsonNode>(payload),
            ).shouldNotBeNull(),
        )

    fun Document.sectionText(): String = select("section").joinToString(" ") { it.text() }

    val objectMapper =
        jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
}
