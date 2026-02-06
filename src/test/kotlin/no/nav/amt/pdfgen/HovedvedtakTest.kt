package no.nav.amt.pdfgen

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import no.nav.amt.lib.models.journalforing.pdf.HovedvedtakPdfDto
import no.nav.amt.lib.models.journalforing.pdf.InnholdPdfDto
import no.nav.amt.pdfgen.TestUtils.fixedDate
import no.nav.amt.pdfgen.TestUtils.render
import org.jsoup.nodes.Document

class HovedvedtakTest :
    DescribeSpec({

        describe("Hovedvedtak PDF") {
            it("VARIG_TILRETTELAGT_ARBEID_SKJERMET") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = emptyList(),
                    fritekstBeskrivelse = "Dette er en beskrivelse av annet for VTA",
                    ledetekst = "Dette er ledeteksten"
                )
                val doc = renderHovedvedtak(baseDto(
                    Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET,
                    innhold))
                doc.text() shouldContain "sidetittel: VARIG_TILRETTELAGT_ARBEID_SKJERMET"
                doc.text() shouldContain "avgjør om du tilbys plass. Ved tilbud om plass vil du bli ansatt."

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst")?.text() shouldBe innhold.ledetekst.shouldNotBeNull()


            }

            it("GRUPPE_ARBEIDSMARKEDSOPPLAERING") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = emptyList(),
                    fritekstBeskrivelse = "Dette er en beskrivelse av annet for VTA",
                    ledetekst = ""
                )
                val doc = renderHovedvedtak(baseDto(
                    Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING,
                    innhold
                ))
                doc.text() shouldContain "sidetittel: GRUPPE_ARBEIDSMARKEDSOPPLAERING"
                doc.text() shouldContain "Du er meldt på arbeidsmarkedstiltaket"

                doc.text() shouldContain innhold.fritekstBeskrivelse.shouldNotBeNull()
                doc.getElementById("ledetekst").shouldBeNull()

                doc.text() shouldNotContain "Bakgrunnsinfo"
                doc.text() shouldNotContain "Deltakelsesmengde"


            }

            it("AVKLARING") {
                val innhold = InnholdPdfDto(
                    valgteInnholdselementer = listOf(
                        "Innhold Pt 1",
                        "Innhold Pt 2"
                    ),
                    fritekstBeskrivelse = "",
                    ledetekst = "Avklaring ledetekst"
                )
                val doc = renderHovedvedtak(baseDto(
                    Tiltakskode.AVKLARING,
                    innhold
                ))
                doc.text() shouldContain "sidetittel: AVKLARING"
                doc.text() shouldContain "Du er meldt på arbeidsmarkedstiltaket"

                doc.getElementById("fritekst").shouldBeNull()
                doc.getElementById("ledetekst").shouldNotBeNull()
                val innholdselementer = doc
                    .getElementById("innholdselementer")
                    ?.getElementsByClass("fritekst")
                    ?.asList()
                innholdselementer.shouldNotBeNull()
                innholdselementer.map { it.text() } shouldBe innhold.valgteInnholdselementer

                doc.text() shouldContain "Bakgrunnsinfo"
                doc.text() shouldContain "Deltakelsesmengde"


            }

            describe("Klagerett") {
                it("viser klagerett på tiltakstyper som alltid har klagerett") {
                    val doc = renderHovedvedtak(baseDto(Tiltakskode.JOBBKLUBB))
                    doc.text() shouldContain "Du har rett til å klage"
                    doc.text() shouldNotContain "Unntak fra klageretten"

                }

                it("viser klagerett på amo tiltak med løpende oppstart") {
                    val doc = renderHovedvedtak(baseDto(Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING))
                    doc.text() shouldContain "Du har rett til å klage"
                    doc.text() shouldNotContain "Unntak fra klageretten"

                }
            }
        }
    }) {
    companion object {
        private fun renderHovedvedtak(payload: HovedvedtakPdfDto): Document = render("hovedvedtak", payload)

        private fun baseDeltaker(innholdPdfDto: InnholdPdfDto? = null) =
            HovedvedtakPdfDto.DeltakerDto(
                fornavn = "Ola",
                mellomnavn = null,
                etternavn = "Nordmann",
                personident = "12345678910",
                innhold = innholdPdfDto,
                bakgrunnsinformasjon = " String",
                deltakelsesmengdeTekst = " String",
                adresseDelesMedArrangor = false,
            )

        private fun baseDeltakerliste(
            tiltakskode: Tiltakskode = Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING,
        ) = HovedvedtakPdfDto.DeltakerlisteDto(
            navn = "Tiltaksliste",
            ledetekst = "Dette er ledeteksten",
            arrangor = HovedvedtakPdfDto.ArrangorDto("Arrangør AS"),
            forskriftskapittel = 42,
            tiltakskode = tiltakskode,
            oppmoteSted = "Her og der",
        )

        private fun baseAvsender() =
            HovedvedtakPdfDto.AvsenderDto(
                navn = "Nav Saksbehandler",
                enhet = "Nav Oslo",
            )

        private fun baseDto(tiltakskode: Tiltakskode, innholdPdfDto: InnholdPdfDto? = null) = HovedvedtakPdfDto(
            deltaker = baseDeltaker(innholdPdfDto),
            deltakerliste = baseDeltakerliste(tiltakskode),
            avsender = baseAvsender(),
            vedtaksdato = fixedDate,
            sidetittel = "sidetittel: " + tiltakskode.name,
            ingressnavn = "Arbeidsforberedende trening",
            opprettetDato = fixedDate.minusMonths(1),
        )
    }
}
