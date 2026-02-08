package no.nav.amt.pdfgen

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.amt.lib.models.journalforing.pdf.InnholdPdfDto
import no.nav.amt.pdfgen.TestUtils.renderSection
import org.jsoup.nodes.Document

class InnholdTest :
    DescribeSpec({

        describe("Innhold template") {
            it("Digitalt jobbsøkerkurs - Kun ledetekst") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = emptyList(),
                    fritekstBeskrivelse = "",
                    ledetekst = "Dette er ledeteksten"
                )
                val doc = renderInnhold(innhold)
                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()

            }

            it("Tiltak som alltid er individuelle - Innholdsliste og ledetekst") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = listOf("Innholdselement 1", "Innholdselement 2"),
                    fritekstBeskrivelse = "",
                    ledetekst = "Dette er ledeteksten"
                )
                val doc = renderInnhold(innhold)

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()
                doc.getElementById("fritekst").shouldBeNull()

                val innholdselementer = doc
                    .getElementById("innholdselementer")
                    ?.getElementsByClass("fritekst")
                    ?.asList()
                innholdselementer.shouldNotBeNull()
                innholdselementer.map { it.text() } shouldBe innhold.valgteInnholdselementer

            }

            it("Tiltak som bare har innholdselement annet og fritekst") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = emptyList(),
                    fritekstBeskrivelse = "dette er en fritekst",
                    ledetekst = "Dette er ledeteksten"
                )
                val doc = renderInnhold(innhold)

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()
                doc.getElementById("fritekst")?.text()  shouldBe innhold.fritekstBeskrivelse.shouldNotBeNull()

            }


        }
    }) {
    companion object {
        private fun renderInnhold(payload: InnholdPdfDto): Document = renderSection("innhold", payload)

    }
}
