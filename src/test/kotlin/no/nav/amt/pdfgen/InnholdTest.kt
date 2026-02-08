package no.nav.amt.pdfgen

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.amt.lib.models.journalforing.pdf.InnholdPdfDto
import no.nav.amt.pdfgen.util.DeltakerWrapper
import no.nav.amt.pdfgen.util.DtoBuilders.hovedvedtakDeltaker
import no.nav.amt.pdfgen.util.RenderUtils.renderSection
import org.jsoup.nodes.Document

class InnholdTest :
    DescribeSpec({

        describe("Innhold template") {
            it("Digitalt jobbsøkerkurs - Kun ledetekst") {
                val innhold =
                    InnholdPdfDto(
                        valgteInnholdselementer = emptyList(),
                        ledetekst = "Dette er ledeteksten",
                        fritekstBeskrivelse = "Dette er friteksten",
                    )

                val doc = renderInnhold(innhold)

                val paraList = doc.getElementsByTag("p")
                paraList.size shouldBe 2

                val innholdElement = paraList.first().shouldNotBeNull()
                innholdElement.text() shouldBe innhold.ledetekst

                val fritekstElement = paraList.last().shouldNotBeNull()
                fritekstElement.text() shouldBe innhold.fritekstBeskrivelse
            }

            it("Tiltak som alltid er individuelle - Innholdsliste og ledetekst") {
                val innhold =
                    InnholdPdfDto(
                        valgteInnholdselementer = listOf("Innholdselement 1", "Innholdselement 2"),
                        fritekstBeskrivelse = "",
                        ledetekst = "Dette er ledeteksten",
                    )
                val doc = renderInnhold(innhold)

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()
                doc.getElementById("fritekst").shouldBeNull()

                val innholdselementer =
                    doc
                        .getElementById("innholdselementer")
                        ?.getElementsByClass("fritekst")
                        ?.asList()
                innholdselementer.shouldNotBeNull()
                innholdselementer.map { it.text() } shouldBe innhold.valgteInnholdselementer
            }

            it("Tiltak som bare har innholdselement annet og fritekst") {
                val innhold =
                    InnholdPdfDto(
                        valgteInnholdselementer = emptyList(),
                        fritekstBeskrivelse = "dette er en fritekst",
                        ledetekst = "Dette er ledeteksten",
                    )
                val doc = renderInnhold(innhold)

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()
                doc.getElementById("fritekst")?.text() shouldBe innhold.fritekstBeskrivelse.shouldNotBeNull()
            }
        }
    }) {
    companion object {
        private fun renderInnhold(innholdDto: InnholdPdfDto): Document {
            val deltaker = hovedvedtakDeltaker(innhold = innholdDto)
            return renderSection("innhold", DeltakerWrapper(deltaker))
        }
    }
}
